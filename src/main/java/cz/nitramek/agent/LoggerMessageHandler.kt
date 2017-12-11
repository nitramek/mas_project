package cz.nitramek.agent

import cz.nitramek.messaging.message.*
import org.slf4j.LoggerFactory

open class LoggerMessageHandler(val agent: Agent) : MessageHandler() {
    private val log = LoggerFactory.getLogger(LoggerMessageHandler::class.java)!!
    private val storeLog = LoggerFactory.getLogger("storeLogger")!!

    override fun handle(unknownMessage: UnknownMessage) {
        val type = unknownMessage.type
        if (agent.isLogger() && type == "KILLALL") {
            killAllAgents()
        }
    }

    override fun handle(store: Store) {
        storeLog.info(store.value)
    }

    fun killAllAgents() {
        log.info("I will prevail thus logger am I! Killing all pesky agents")
        val halt = Halt(MessageHeader(agent.localHeader.source, "Logger"))
        agent.communicator.addressBook.forEach {
            agent.communicator.sendMessage(halt, it.key, true)
        }
    }

}