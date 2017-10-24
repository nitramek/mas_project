package cz.nitramek.agent

import cz.nitramek.Main
import java.io.*
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


fun executablePath(): Path {
    val path = Paths.get(Main::class.java.protectionDomain.codeSource.location.toURI())
    return if (path.toFile().absolutePath.endsWith("jar")) {
        path
    } else {
        Paths.get("target", "Agents-1-jar-with-dependencies.jar")
        //not in jar - presumably only debug use so and trying to do something so lets just exepct there is jar in target
    }
}

class FileRepository(private val localAddress: InetSocketAddress, private val executablePath: Path) {

    private val rootRepository = Paths.get(RECIEVED_PACKAGES_DIR)

    fun repositoryPath(repositoryName: String) = rootRepository.resolve(repositoryName)

    val agentInParts: List<String>

    init {
        ensureDirExistance(rootRepository)
        clean()
        ensureDirExistance(rootRepository)
        val zipInMemory = ByteArrayOutputStream(2516582)
        createPackageAsZip(zipInMemory)
        agentInParts = toBase64Part(zipInMemory.toByteArray())

    }


    fun savePackage(repositoryName: String, partedPackage: PartedPackage) {
        val repo = rootRepository.resolve(repositoryName)
        ensureDirExistance(repo)
        val bytes = partedPackage.partsAsBytes()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            generateSequence(zis::getNextEntry)
                    .takeWhile(Objects::nonNull)
                    .forEach { zipEntry -> saveFile(repo, zis, zipEntry) }
        }
    }

    private fun saveFile(repo: Path, zis: ZipInputStream, entry: ZipEntry) {
        val filePath = repo.resolve(entry.name)
        Files.deleteIfExists(filePath)
        Files.copy(zis, filePath)
    }


    private fun ensureDirExistance(dirPath: Path) {
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath)
        }
    }

    private fun toBase64Part(bytes: ByteArray): List<String> {
        val encodedString = Base64.getEncoder().encodeToString(bytes)
        val partCount = encodedString.length / PART_MAX_SIZE + 1
        return (1..partCount).map { i ->
            val startIndex = (i - 1) * PART_MAX_SIZE
            val endIndex = minOf(i * PART_MAX_SIZE, encodedString.length)
            encodedString.substring(startIndex, endIndex)
        }.toList()
    }

    private fun createPackageAsZip(outputStream: OutputStream) {
        ZipOutputStream(outputStream).use { zipStream ->
            zipStream.putNextEntry(ZipEntry(AGENT_JAR_NAME))
            zipStream.write(Files.readAllBytes(executablePath))
            zipStream.putNextEntry(ZipEntry(CONFIG_FILE_NAME))
            val writer = zipStream.bufferedWriter()
            writer.write("${localAddress.hostString}:${localAddress.port}") //config file
            writer.flush()
        }
    }

    fun clean() {
        deleteRecursive(rootRepository.toFile())
    }


    fun deleteRecursive(path: File): Boolean {
        if (!path.exists()) throw FileNotFoundException(path.absolutePath)
        var ret = true
        if (path.isDirectory) {
            for (f in path.listFiles()!!) {
                ret = ret && deleteRecursive(f)
            }
        }
        return ret && path.delete()
    }

}
