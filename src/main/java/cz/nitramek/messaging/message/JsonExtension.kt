package cz.nitramek.messaging.message

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonArray.insert(element: JsonObject): JsonArray {
    add(element)
    return this
}

