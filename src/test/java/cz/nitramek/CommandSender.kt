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
        val mockSource = MessageHeader(InetSocketAddress("127.0.0.1", 11111), tag = "Potato")
        val converter = MessagesConverter()
        sender.start()
        val testAgentAddress = InetSocketAddress(NetworkUtils.localAddres(), 11111)
        val dup = Duplicate(MessageHeader(testAgentAddress), testAgentAddress)
        val sendDup = Send(mockSource, testAgentAddress, converter.objToStr(dup))
        val storeSomething = Store(mockSource, "potato")
        val halt = Halt(mockSource)
        val execute = Execute(MessageHeader(testAgentAddress), "java -jar $AGENT_PACKAGE_NAME")
        val store = Store(MessageHeader(testAgentAddress, "potato"), "Hello")
        val otherAgentAddress = InetSocketAddress("192.168.43.56", 9999) //pavel
//        sender.sendPacket(converter.objToStr(store), )
        sendPackageRequest(mockSource, testAgentAddress, otherAgentAddress, converter, sender)
        sendInitiation(mockSource, sender, converter)
//        sender.sendPacket(converter.objToStr(store), testAgentAddress)
//        sendPackageRequest(mockSource, testAgentAddress, otherAgentAddress, converter, sender)
//        val otherAgentAddress = InetSocketAddress("192.168.43.130", 11111) //pavel
//        val otherAgentAddress = InetSocketAddress("192.168.43.56", 9999) //vojta

        //        println(converter.objToStr(sendSendToMyself))
        //        sender.sendPacket(converter.objToStr(addAgents), from)
        //        sender.sendPacket(converter.objToStr(execute), from)
        //        val killall = JsonObject().apply {
        //            addProperty("type", "KILLALL")
        //            addProperty("sourceIp", "127.0.0.1")
        //            addProperty("sourcePort", 80)
        //            addProperty("tag", "potato")
        //        }
        //        sender.sendPacket(killall.toString(), from)
        //        sender.sendPacket(converter.objToStr(store), from)
        //        sender.sendPacket(converter.objToJson(storeSomething).toString(), from)
        sender.shutdown()
    }

    private fun sendPackageRequest(mockSource: MessageHeader, from: InetSocketAddress, to: InetSocketAddress, converter: MessagesConverter, sender: UDPSender) {
        val duplicateToMyself = Duplicate(mockSource, to)
        val sendDupToOther = Send(mockSource, from, converter.objToStr(duplicateToMyself))
        val sendSendToMyself = Send(mockSource, to, converter.objToStr(sendDupToOther))
        //        println(converter.objToStr(sendSendToMyself))
        //        sender.sendPacket(converter.objToStr(addAgents), from)
        //        sender.sendPacket(converter.objToStr(execute), from)
        //        val killall = JsonObject().apply {
        //            addProperty("type", "KILLALL")
        //            addProperty("sourceIp", "127.0.0.1")
        //            addProperty("sourcePort", 80)
        //            addProperty("tag", "potato")
        //        }
        //        sender.sendPacket(killall.toString(), from)
        //        sender.sendPacket(converter.objToStr(store), from)
        sender.sendPacket(converter.objToJson(sendSendToMyself).toString(), from)
        //        sender.sendPacket(converter.objToJson(storeSomething).toString(), from)
    }

    fun sendInitiation(src: MessageHeader, sender: UDPSender, converter: MessagesConverter) {
        val A = InetSocketAddress("192.168.43.125", 11112)
        val B = InetSocketAddress("192.168.43.130", 11113)
        val C = InetSocketAddress("192.168.43.56", 11114)
        val D = InetSocketAddress("192.168.43.219", 11115)


        val sendtoA = Send(src, A, converter.objToStr(Store(src, "Potato")))
        val sendToB = Send(src, B, converter.objToStr(sendtoA))
        val sendToC = Send(src, C, converter.objToStr(sendToB))
        val sendToD = Send(src, D, converter.objToStr(sendToC))

        println(converter.objToJson(sendToD))

//        sender.sendPacket(converter.objToStr(sendToC), src.source)
//        val sendToAAgain = Send(src, A, converter.objToStr(sendToC))

        //posli C -> posli B -> posli A store

        converter.objToStr(sendToC)

//        val sendToB = Send(src, B, converter.objToStr(sendtoA))
//        val sendToB = Send(src, B, converter.objToStr(sendtoA))
//        val sendtoB = Send(src, InetSocketAddress("192.168.43.130", ))
    }
}