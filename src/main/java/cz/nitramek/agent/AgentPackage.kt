package cz.nitramek.agent

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


object AgentPackage {

    val parts: List<String>


    init {
        val jarLocation = Agent::class.java.protectionDomain.codeSource.location
        parts = fileToBase64Parts(Paths.get(jarLocation.toURI()))
    }

    fun fileToBase64Parts(path: Path): List<String> {
        val bos = ByteArrayOutputStream()
        Files.copy(path, bos)
        val bytes = bos.toByteArray()
        val encodedString = Base64.getEncoder().encodeToString(bytes)
        val partCount = encodedString.length / PART_MAX_SIZE + 1
        return (1..partCount).map { i ->
            val startIndex = (i - 1) * PART_MAX_SIZE
            val endIndex = minOf(i * PART_MAX_SIZE, encodedString.length)
            encodedString.substring(startIndex, endIndex)
        }.toList()
    }
}