package cz.nitramek

import cz.nitramek.messaging.message.Execute
import cz.nitramek.messaging.message.MessageHeader
import cz.nitramek.messaging.message.MessagesConverter
import cz.nitramek.messaging.network.UDPSender
import java.net.InetSocketAddress

object Sender {

    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1"}}
    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}}

    @JvmStatic
    fun main(args: Array<String>) {
        val sender = UDPSender()
        sender.start()
        val msghalt = """{"type":"HALT", "sourceIp": "", "sourcePort": 0}"""
        val msg = """{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}"""
        val testAgentAddress = InetSocketAddress("192.168.47.1", 53156)
//        sender.sendPacket(msg, testAgentAddress)
//        Thread.sleep(TimeUnit.SECONDS.toMillis(10))
        val converter = MessagesConverter()
        val execute = Execute(MessageHeader(InetSocketAddress("127.0.0.1", 11111)), "java -jar agent_nitramek.jar")

        sender.sendPacket(converter.objToStr(execute), testAgentAddress)


//        sender.sendPacket(msghalt, testAgentAddress)
        sender.shutdown()
    }
}