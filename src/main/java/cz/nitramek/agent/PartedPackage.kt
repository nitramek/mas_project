package cz.nitramek.agent

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

class PartedPackage(private val partCount: Int, val name: String) {
    private val log = LoggerFactory.getLogger(PartedPackage::class.java)
    private val parts = AtomicReferenceArray<String>(partCount)


    private var receivedParts: AtomicInteger = AtomicInteger(0)

    private var completed = AtomicBoolean(false)

    var notUnpacked = AtomicBoolean(true)


    fun isCompleted() = completed

    fun addPart(order: Int, data: String): Boolean {
        val savedPart = parts.getAndSet(order, data)
        if (!completed.get()) {
            if (savedPart.isNullOrBlank()) {
                val currentPartsCount = receivedParts.incrementAndGet()
                val nowCompleted = currentPartsCount == partCount
                completed.compareAndSet(false, nowCompleted)
                log.info("$order\t$partCount\t$currentPartsCount - $this")
                return nowCompleted
            }
            return false
        }
        return false

    }

    fun partsAsBytes(): ByteArray {
        try {
            if (!isCompleted().get()) {
                throw PartsNotCompletedException("Got $receivedParts/$partCount")
            }
            val dataString = completeParts()
            val decoder = Base64.getDecoder()
            return decoder.decode(dataString)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
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