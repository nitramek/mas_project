package cz.nitramek


import cz.nitramek.agent.Agent


object Main {


    @JvmStatic
    fun main(args: Array<String>) {
        val agent = Agent()
        agent.start()
        while (true) {
            try {
                val line = readLine()
                if (line != null && line.isNotBlank()) {
                    if (line == "EXIT") {
                        agent.stop()
                        return
                    }
                    agent.localMessage(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

    }
}