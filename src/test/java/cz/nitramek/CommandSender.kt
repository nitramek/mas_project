package cz.nitramek

import cz.nitramek.agent.AGENT_JAR_NAME
import cz.nitramek.messaging.message.*
import cz.nitramek.messaging.network.UDPSender
import java.net.InetSocketAddress

object Sender {

    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1"}}
    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}}

    @JvmStatic
    fun main(args: Array<String>) {
        val sender = UDPSender()
        val mockSource = MessageHeader(InetSocketAddress("127.0.0.1", 11111))
        val converter = MessagesConverter()
        sender.start()
        val testAgentAddress = InetSocketAddress("192.168.47.1", 51021)
        val addAgents = AddAgents(mockSource, arrayListOf(testAgentAddress))
        val halt = Halt(mockSource)
        val execute = Execute(MessageHeader(testAgentAddress), "java -jar $AGENT_JAR_NAME")
//        sender.sendPacket(converter.objToStr(addAgents), testAgentAddress)
        sender.sendPacket(converter.objToStr(execute), testAgentAddress)
//        sender.sendPacket(converter.objToStr(halt), testAgentAddress)
        sender.shutdown()
    }
}