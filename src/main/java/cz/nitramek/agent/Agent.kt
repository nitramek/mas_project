package cz.nitramek.agent

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import cz.nitramek.messaging.Communicator
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArraySet


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
    private val knownAgentsAdresses: MutableSet<InetSocketAddress> = CopyOnWriteArraySet()
    private val receivedParts = mutableMapOf<InetSocketAddress, MutableMap<String, PartedPackage>>()
    private val repository = FileRepository(bindedAddress, executablePath())

    init {
        println(bindedAddress)
    }

    private val messageHandler = object : MessageHandler() {


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
                val resultMsg = Result(myHeader, "sucess", "FILE_SAVED", "")
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
            this@Agent.stop()
        }

        override fun handle(unknownMessage: UnknownMessage) {

        }

        override fun handle(addAgents: AddAgents) {
            log.debug("Add Agents command - {}", addAgents.addresses)
            knownAgentsAdresses.addAll(addAgents.addresses)
            addAgents.addresses.forEach { sendMyself(it) }
        }

        override fun handle(agents: Agents) {
            log.debug("Sending Agents")
            val arrayOfAgents = knownAgentsAdresses.map {
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
            log.info("Received result {} - {}", result.status, result.result)
        }

        override fun handle(send: Send) {
            log.debug("Sending")

            communicator.sendMessage(send.message, send.recipient, true)
        }

        override fun handle(store: Store) {
            log.debug("Storing {}", store.value)
            storeLog.info(store.value)
        }
    }

    init {
        communicator.addMessageHandler(messageHandler)
    }

    fun sendMyself(recipient: InetSocketAddress) {
        log.debug("Sending myself to the other side")
        repository.agentInParts.forEachIndexed { index, part ->
            communicator.sendMessage(
                    Package(
                            MessageHeader(bindedAddress),
                            part,
                            index,
                            AGENT_JAR_NAME,
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
