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
        val testAgentAddress = InetSocketAddress("192.168.47.1", 60846)
        val dup = Duplicate(MessageHeader(testAgentAddress), testAgentAddress)
        val sendDup = Send(mockSource, testAgentAddress, converter.objToStr(dup))
        val storeSomething = Store(mockSource, "potato")
        val halt = Halt(mockSource)
        val execute = Execute(MessageHeader(testAgentAddress), "java -jar $AGENT_JAR_NAME")
        val store = Store(MessageHeader(testAgentAddress, "potato"), "Hello")
//        sender.sendPacket(converter.objToStr(addAgents), testAgentAddress)
//        sender.sendPacket(converter.objToStr(execute), testAgentAddress)
//        val killall = JsonObject().apply {
//            addProperty("type", "KILLALL")
//            addProperty("sourceIp", "127.0.0.1")
//            addProperty("sourcePort", 80)
//            addProperty("tag", "potato")
//        }
//        sender.sendPacket(killall.toString(), testAgentAddress)
//        sender.sendPacket(converter.objToStr(store), testAgentAddress)
        sender.sendPacket(converter.objToJson(sendDup).toString(), testAgentAddress)
        sender.shutdown()
    }
}