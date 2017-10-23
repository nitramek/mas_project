package cz.nitramek.agent

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val SENDER_THREAD_COUNT = 2
const val RECEIVER_THREAD_COUNT = 6
const val PART_MAX_SIZE = 1028
const val AGENT_JAR_NAME = "agent_nitramek.jar"
const val MAX_RETRIES = 10
const val RESEND_DELAY = 1000L

val RECIEVED_PACKAGES_DIR = Paths.get("receivedParts")


fun ensureDirectoryExistency(dir: Path) {
    if (!Files.exists(dir)) {
        Files.createDirectory(dir)
    }
}