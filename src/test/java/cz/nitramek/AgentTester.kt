package cz.nitramek

import cz.nitramek.agent.Agent
import cz.nitramek.messaging.network.UDPSender
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertFalse


class AgentTester {

    companion object {

        private val agent = Agent()
        private val sender = UDPSender()

        @BeforeClass
        @JvmStatic
        fun prepare() {

            agent.start()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            agent.stop()
        }
    }


    fun data(): Array<String> {
        return arrayOf(
                """{ "type":"SEND", "ip":"192.168.47.1", "port":53156, "sourcePort":1000, "sourceIp":"192.168.47.1", message:{ "type":"ADD_AGENTS", "sourcePort":53156, "sourceIp":"192.168.47.1", "agents":[{ "ip":"192.168.47.1", port:53156 }] } }""",
                """{ "type":"SEND", "ip":"192.168.47.1", "port":53156, "sourcePort":1000, "sourceIp":"192.168.47.1", message:{ "type":"AGENTS", "sourcePort":53156, "sourceIp":"192.168.47.1" } }"""

        )
    }

    @Test
    fun testHalt() {
        val haltMessage = """{"type":"HALT", "sourceIp": "", "sourcePort": 0}"""
        sendCommands(haltMessage)
        assertFalse(agent.runnning, "Agent is still running")
    }

    @Test
    fun testCommands() {
        data().forEach(this::sendCommands)
    }


    private fun sendCommands(haltCommand: String) {
        System.setIn(haltCommand.byteInputStream())
        agent.localMessage(haltCommand)
        Thread.sleep(300)
    }
}
