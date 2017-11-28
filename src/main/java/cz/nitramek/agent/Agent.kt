package cz.nitramek.agent

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths

//TODO přidávání agentů od kterých příjde jakákoliv message do známých adres a odebírání pokud na ně nedojde zpráva
class Agent(val loggerAddress: InetSocketAddress? = null) {

    /**
     * Utilities
     */
    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val storeLog = LoggerFactory.getLogger("storeLogger")!!
    private val gson = Gson()
    private val converter = MessagesConverter()

    private val communicator: Communicator = UDPCommunicator()

    /**
     * State
     */
    val bindedAddress = communicator.respondAdress()
    var isRunning: Boolean = false
        private set
    private val receivedParts = mutableMapOf<InetSocketAddress, MutableMap<String, PartedPackage>>()
    private val repository = FileRepository(bindedAddress, executablePath())

    private val localHeader = MessageHeader(bindedAddress)

    init {
        println(bindedAddress)
        if (loggerAddress != null) {
            communicator.sendMessage(Store(localHeader,
                    "START ${bindedAddress.address.hostAddress} ${bindedAddress.port} $THIS_AGENT_TAG"),
                    loggerAddress, true)
        }
        val configFile = Paths.get(CONFIG_FILE_NAME)
        if (Files.exists(configFile)) {
            val configFileLine = Files.readAllLines(configFile)[0]
            val parts = configFileLine.split(":")
            val pappaAddress = InetSocketAddress(parts[0], parts[1].toInt())
            communicator.sendMessage(Agents(localHeader), pappaAddress, true)
        }
    }

    private val messageHandler = object : MessageHandler() {

        override fun newAgentFound(address: InetSocketAddress) {

        }


        override fun handle(duplicate: Duplicate) {
            sendMyself(duplicate.recipient)
        }

        override fun handle(packageReceived: PackageReceived) {
            val executeMsg = Execute(localHeader, "java -jar $AGENT_JAR_NAME")
            communicator.sendMessage(executeMsg, packageReceived.header.source, true)
        }

        override fun handle(aPackage: Package) {
            val source = aPackage.header.source
            val packages = receivedParts.getOrPut(source, { mutableMapOf() })
            val fileName = aPackage.fileName
            val partedPackage = packages.getOrPut(fileName, { PartedPackage(aPackage.partsCount, fileName) })
            partedPackage.addPart(aPackage.order, aPackage.data)
            if (partedPackage.isCompleted()) {
                receivedParts.remove(source)
                repository.savePackage(addressAsRepoName(source), partedPackage)
                val myHeader = MessageHeader(communicator.respondAdress())
                val resultMsg = PackageReceived(myHeader)
                communicator.sendMessage(resultMsg, source, true)
//                val haltMsg = Halt(myHeader)
//                communicator.sendMessage(haltMsg, source, false)

            }
        }

        override fun handle(execute: Execute) {
            val repoName = addressAsRepoName(execute.header.source)
            val repoPath = repository.repositoryPath(repoName)
            log.info("Executing {} from {} in {}", execute.command, execute.header.source, repoPath)
            Runtime.getRuntime().exec(execute.command, arrayOf(), repoPath.toAbsolutePath().toFile())
        }

        override fun handle(halt: Halt) {
            log.info("Halting :/ - address of the bastard {}", halt.header.source)
            if (loggerAddress != null) {
                val halterIp = halt.header.source.address.hostAddress
                val halterPort = halt.header.source.port
                communicator.sendMessage(
                        Store(localHeader,
                                "END   ${bindedAddress.address.hostAddress}:${bindedAddress.port} $THIS_AGENT_TAG by $halterIp:$halterPort ${halt.header.tag}"),
                        loggerAddress, true)
            }
            this@Agent.stop()
        }

        override fun handle(unknownMessage: UnknownMessage) {
            val type = unknownMessage.type
            if (amIAgentLogger() && type == "KILLALL") {
                log.info("I will prevail thus logger am I! Killing all pesky agents")
                val halt = Halt(localHeader)
                communicator.addressBook.forEach {
                    communicator.sendMessage(halt, it, true)
                }
            }
        }

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")
            val arrayOfAgents = communicator.addressBook.map {
                JsonObject().apply {
                    addProperty("ip", it.address.hostAddress)
                    addProperty("port", it.port)
                }
            }.fold(JsonArray(), JsonArray::insert)


            val resultMsg = Result(
                    MessageHeader(communicator.respondAdress()),
                    "sucess", gson.toJson(arrayOfAgents),
                    converter.objToStr(agents))
            communicator.sendMessage(resultMsg, agents.header.source, true)

        }

        override fun handle(result: Result) {
            log.info("Received value {} - {}", result.status, result.value)
        }

        override fun handle(send: Send) {
            log.debug("Sending")

            val messageSendNoLocalParams = send.message
            val messageToSend = converter.addHeaderParams(messageSendNoLocalParams, localHeader)
            communicator.sendMessage(messageToSend, send.recipient, true)
        }

        override fun handle(store: Store) {
            log.debug("Storing {}", store.value)
            storeLog.info(store.value)
        }
    }

    private fun amIAgentLogger() = loggerAddress == null

    init {
        communicator.addMessageHandler(messageHandler)
    }

    fun sendMyself(recipient: InetSocketAddress) {
        log.debug("Sending myself to the other side")
        repository.agentInParts.forEachIndexed { index, part ->
            communicator.sendMessage(
                    Package(
                            localHeader,
                            part,
                            index,
                            AGENT_PACKAGE_NAME,
                            repository.agentInParts.size
                    ), recipient, true)
        }
    }


    fun localMessage(message: String) {
        val msg = MessagesConverter().strToObj(message)
        msg.handle(messageHandler)
    }

    fun addressAsRepoName(address: InetSocketAddress) = "${address.hostString}_${address.port}"


    fun start() {
        communicator.start()
        this.isRunning = true
    }

    fun stop() {
        if (this.isRunning) {
            communicator.stop()
            log.debug("Agent stopped")
            this.isRunning = false
        }
    }


}
