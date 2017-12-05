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
import java.util.concurrent.ConcurrentHashMap


//TODO přidávání agentů od kterých příjde jakákoliv message do známých adres a odebírání pokud na ně nedojde zpráva
class Agent(val onStopListener: (() -> Unit), val loggerAddress: InetSocketAddress? = null) {

    /**
     * Utilities
     */
    private val log = LoggerFactory.getLogger(this::class.java)!!

    private val gson = Gson()
    private val converter = MessagesConverter()

    private val communicator: Communicator = UDPCommunicator()

    /**
     * State
     */
    val bindedAddress = communicator.respondAdress()
    var isRunning: Boolean = false
        private set
    private val receivedParts = ConcurrentHashMap<InetSocketAddress, PartedPackage>()
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

    open class LoggerMessageHandler(val agent: Agent) : MessageHandler() {
        private val storeLog = LoggerFactory.getLogger("storeLogger")!!

        override fun handle(unknownMessage: UnknownMessage) {
            val type = unknownMessage.type
            if (agent.isLogger() && type == "KILLALL") {
                agent.killAllAgents()
            }
        }

        override fun handle(store: Store) {
            storeLog.info(store.value)
        }
    }


    private val messageHandler = object : LoggerMessageHandler(this) {

        override fun newAgentFound(address: InetSocketAddress) {
            val duplicateRequest = Duplicate(localHeader, address)
            val sendMeDuplicate = Send(localHeader, bindedAddress, converter.objToStr(duplicateRequest))
//            communicator.sendMessage(sendMeDuplicate, address, true)
        }


        override fun handle(duplicate: Duplicate) {
            sendMyself(duplicate.recipient)
        }

        override fun handle(packageReceived: PackageReceived) {
            log.info("Look, he got a package! he: ${packageReceived.header}")
            val executeMsg = Execute(localHeader, "java -jar $AGENT_JAR_NAME ${loggerAddress?.address?.canonicalHostName} ${loggerAddress?.port}")
//            communicator.sendMessage(executeMsg, packageReceived.header.source, true)
        }

        override fun handle(aPackage: Package) {
            val source = aPackage.header.source
            val fileName = aPackage.fileName
            val partedPackage = receivedParts.getOrPut(source, { PartedPackage(aPackage.partsCount, fileName) })
            if (!partedPackage.isCompleted().get()) {
                val packageCompleted = partedPackage.addPart(aPackage.order, aPackage.data)
                if (packageCompleted) {
                    log.info("Unpacking package \n")
                    partedPackage.notUnpacked.set(false)
                    repository.savePackage(addressAsRepoName(source), partedPackage)
                    val myHeader = MessageHeader(communicator.respondAdress())
                    val resultMsg = PackageReceived(myHeader)
                    communicator.sendMessage(resultMsg, source, true)
                }
//                val haltMsg = Halt(myHeader)
//                communicator.sendMessage(haltMsg, source, false)
            }


        }

        override fun handle(execute: Execute) {
            val repoName = addressAsRepoName(execute.header.source)
            val repoPath = repository.repositoryPath(repoName)
            log.info("Executing {} from {} in {}", execute.command, execute.header.source, repoPath)
            var cmd = execute.command
            if (cmd.contains("exe")) {
                cmd = "cmd /c start $cmd"
            }
            Runtime.getRuntime().exec(cmd, arrayOf(), repoPath.toAbsolutePath().toFile())
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

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")
            val arrayOfAgents = communicator.addressBook.map {
                JsonObject().apply {
                    addProperty("ip", it.key.address.hostAddress)
                    addProperty("port", it.key.port)
                }
            }.fold(JsonArray(), JsonArray::insert)


            val resultMsg = Result(
                    MessageHeader(communicator.respondAdress()),
                    "sucess", gson.toJson(arrayOfAgents),
                    agents.original)
            communicator.sendMessage(resultMsg, agents.header.source, true)

        }

        override fun handle(result: Result) {
            log.info("Received value {} - {}", result.status, result.value)
            if (result.message.isNotBlank()) {
                val obj = converter.strToObj(result.message)
                if (obj is Agents) {
                    val agentsArray = converter.strToJsonO(result.value).asJsonArray
                    agentsArray.map { it.asJsonObject }.map {
                        val ip = it["ip"].asString
                        val port = it["port"].asInt
                        InetSocketAddress(ip, port)
                    }.forEach {
                        communicator.addNewAgentAddress(it)
                    }

                }
            }
        }

        override fun handle(send: Send) {
            log.debug("Sending")

            val messageSendNoLocalParams = send.message
            val messageToSend = converter.addHeaderParams(messageSendNoLocalParams, localHeader)
            communicator.sendMessage(messageToSend, send.recipient, true)
        }
    }

    fun killAllAgents() {
        log.info("I will prevail thus logger am I! Killing all pesky agents")
        val halt = Halt(localHeader)
        communicator.addressBook.forEach {
            communicator.sendMessage(halt, it.key, true)
        }
    }

    fun isLogger() = loggerAddress == null

    init {
        if (!isLogger()) {
            communicator.addMessageHandler(messageHandler)
        } else {
            communicator.addMessageHandler(LoggerMessageHandler(this))
        }
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
            onStopListener()
        }
    }


}
