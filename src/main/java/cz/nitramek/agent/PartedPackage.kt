package cz.nitramek.agent

import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PartedPackage(val partCount: Int, val name: String) {

    val log = LoggerFactory.getLogger(this::class.java)!!
    private val parts = Array(partCount, { "" })

    private var recievedParts: AtomicInteger = AtomicInteger(0)


    fun isCompleted() = recievedParts.get() == partCount

    fun addPart(order: Int, data: String) {
        if (parts[order].isBlank()) {
            recievedParts.incrementAndGet()
        }
        parts[order] = data

    }

    fun saveToFileSystem(): Path {
        if (!isCompleted()) {
            throw PartsNotCompletedException("Got ${recievedParts}/$partCount")
        }
        val dataString = parts.fold(StringBuilder(), StringBuilder::append).toString()
        val decoder = Base64.getDecoder()
        val bytes = decoder.decode(dataString)
        RECIEVED_PACKAGES_DIR.resolve(name)
        val path = Paths.get("receivedParts", name)
        Files.deleteIfExists(path)
        val file = Files.createFile(path)
        Files.newOutputStream(file, StandardOpenOption.TRUNCATE_EXISTING).use {
            val bufferedOutputStream = BufferedOutputStream(it)
            bufferedOutputStream.write(bytes)
            bufferedOutputStream.flush()
        }
        return file
    }

    class PartsNotCompletedException(override val message: String) : Exception()
}