package cz.nitramek.agent

import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PartedPackage(private val partCount: Int, val name: String) {

    private val parts = Array(partCount, { "" })

    private var receivedParts: AtomicInteger = AtomicInteger(0)


    fun isCompleted() = receivedParts.get() == partCount

    fun addPart(order: Int, data: String) {
        if (parts[order].isBlank()) {
            receivedParts.incrementAndGet()
        }
        parts[order] = data

    }

    fun partsAsBytes(): ByteArray {
        if (!isCompleted()) {
            throw PartsNotCompletedException("Got $receivedParts/$partCount")
        }
        val dataString = parts.fold(StringBuilder(), StringBuilder::append).toString()
        val decoder = Base64.getDecoder()
        return decoder.decode(dataString)

    }

    class PartsNotCompletedException(override val message: String) : Exception()
}