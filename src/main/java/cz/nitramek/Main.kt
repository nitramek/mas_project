package cz.nitramek

import javafx.application.Application


object Main {

    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1"}}
    //{"type":"SEND","ip":"192.168.47.1","port":53156,"sourcePort":1000,"sourceIp":"192.168.47.1",message:{"type":"ADD_AGENTS","sourcePort":53156,"sourceIp":"192.168.47.1","agents":[{"ip":"192.168.47.1",port:53156}]}}
    @JvmStatic
    fun main(args: Array<String>) {
        val windowClass = if (args.size >= 2) AgentWindow::class.java else AgentLoggerGui::class.java
        Application.launch(windowClass, *args)


    }

}