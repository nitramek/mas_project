package cz.nitramek

import cz.nitramek.agent.AGENT_PACKAGE_NAME
import cz.nitramek.messaging.message.*
import cz.nitramek.messaging.network.UDPSender
import cz.nitramek.utils.NetworkUtils
import java.net.InetSocketAddress

object Sender {

    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1"}}
    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}}

    @JvmStatic
    fun main(args: Array<String>) {
        val sender = UDPSender()
        val mockSource = MessageHeader(InetSocketAddress("127.0.0.1", 11112))
        val converter = MessagesConverter()
        sender.start()
        val testAgentAddress = InetSocketAddress(NetworkUtils.localAddres(), 11111)
        val dup = Duplicate(MessageHeader(testAgentAddress), testAgentAddress)
        val sendDup = Send(mockSource, testAgentAddress, converter.objToStr(dup))
        val storeSomething = Store(mockSource, "potato")
        val halt = Halt(mockSource)
        val execute = Execute(MessageHeader(testAgentAddress), "java -jar $AGENT_PACKAGE_NAME")
        val store = Store(MessageHeader(testAgentAddress, "potato"), "Hello")

        val otherAgentAddress = InetSocketAddress("192.168.0.2", 11112)
        val duplicateToMyself = Duplicate(mockSource, otherAgentAddress)
        val sendDupToOther = Send(mockSource, testAgentAddress, converter.objToStr(duplicateToMyself))
        val sendSendToMyself = Send(mockSource, otherAgentAddress, converter.objToStr(sendDupToOther))
//        println(converter.objToStr(sendSendToMyself))
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
        sender.sendPacket(converter.objToJson(sendSendToMyself).toString(), testAgentAddress)
//        sender.sendPacket(converter.objToJson(storeSomething).toString(), testAgentAddress)
        sender.shutdown()
    }
}