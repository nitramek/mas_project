package cz.nitramek

import cz.nitramek.agent.*
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.IntStream
import kotlin.test.assertTrue

class FileRepositoryTest {


    @Test
    fun testSavePackage() {
        val mockExecutable = Paths.get(AGENT_JAR_NAME)
        val rootRepoPath = Paths.get(RECIEVED_PACKAGES_DIR)
        val testRepoPath = Paths.get(RECIEVED_PACKAGES_DIR, "test")
        val jarInRepo = Paths.get(RECIEVED_PACKAGES_DIR, "test", AGENT_JAR_NAME)
        val configFileInRepo = Paths.get(RECIEVED_PACKAGES_DIR, "test", CONFIG_FILE_NAME)
        sequenceOf(mockExecutable, configFileInRepo, jarInRepo, testRepoPath, rootRepoPath)
                .forEach { Files.deleteIfExists(it) }
        Files.createFile(mockExecutable)

        val fileRepository = FileRepository(InetSocketAddress(InetAddress.getLoopbackAddress(), 80), mockExecutable)
        val agentInParts = fileRepository.agentInParts
        val pp = PartedPackage(agentInParts.size, "agentZip")
        IntStream.range(0, agentInParts.size)
                .parallel()
                .forEach { pp.addPart(it, agentInParts[it]) }





        fileRepository.savePackage("test", pp)

        assertTrue(Files.exists(rootRepoPath), "Directory $RECIEVED_PACKAGES_DIR wasnt created rootRepo ")
        assertTrue(Files.exists(testRepoPath), "$RECIEVED_PACKAGES_DIR/test wasnt created testRepo")
        assertTrue(Files.exists(jarInRepo), "File $RECIEVED_PACKAGES_DIR/test/$AGENT_JAR_NAME wasnt created  wasnt created ")
        assertTrue(Files.exists(configFileInRepo), "File $RECIEVED_PACKAGES_DIR/test/$CONFIG_FILE_NAME wasnt created  wasnt created ")

        sequenceOf(mockExecutable, configFileInRepo, jarInRepo, testRepoPath, rootRepoPath)
                .forEach { Files.deleteIfExists(it) }

    }
}