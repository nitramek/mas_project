package cz.nitramek.agent

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

class PartedPackage(private val partCount: Int, val name: String) {
    private val log = LoggerFactory.getLogger(PartedPackage::class.java)
    private val parts = AtomicReferenceArray<String>(partCount)

    private var receivedParts: AtomicInteger = AtomicInteger(0)


    fun isCompleted() = synchronized(this) { receivedParts.get() == partCount }

    fun addPart(order: Int, data: String) {
        val savedPart = parts.getAndSet(order, data)
        if (savedPart.isNullOrBlank()) {
            val currentPartsCount = receivedParts.getAndIncrement()
            log.info("$order\t$partCount\t$currentPartsCount - $this")
        }

    }

    fun partsAsBytes(): ByteArray {
        if (!isCompleted()) {
            throw PartsNotCompletedException("Got $receivedParts/$partCount")
        }
        val dataString = completeParts()
        val decoder = Base64.getDecoder()
        return decoder.decode(dataString)
    }

    fun completeParts(): String {
        val builder = StringBuilder()
        for (i in 0 until parts.length()) {
            builder.append(parts[i])
        }
        return builder.toString()
    }

    class PartsNotCompletedException(override val message: String) : Exception()
}