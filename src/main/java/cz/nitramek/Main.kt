package cz.nitramek

import javafx.application.Application


object Main {

    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1"}}
    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}}
    @JvmStatic
    fun main(args: Array<String>) {
        Application.launch(AgentWindow::class.java, *args)
//        val cmd = "cmd /c start Agent.exe"
//        val f = File("D:\\drive\\vsb\\Ing\\3.semestr\\MAS\\project\\Agents\\receivedParts\\192.168.43.130_22222\\ ")
//        Runtime.getRuntime().exec(cmd, arrayOf(), f)
//        print("potato")

    }

    @Throws(java.io.IOException::class)
    fun execCmd(cmd: String): String {
        val s = java.util.Scanner(Runtime.getRuntime().exec(cmd).inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}