package cz.nitramek.messaging.message

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import cz.nitramek.messaging.message.Message.MessageType.*
import java.net.InetSocketAddress

class MessagesConverter {

    private val jsonParser = JsonParser()

    fun strToObj(source: InetSocketAddress, json: String): Message {
        val jsonObject = jsonParser.parse(json).asJsonObject
        val type = jsonObject["type"].asString
        when (type) {
            STORE.name -> return Store(source, jsonObject["value"].asString)
            RESULT.name -> return Result(source, jsonObject["status"].asString, jsonObject["value"].asString, jsonObject["message"].toString())
            SEND.name -> {
                val recipient = InetSocketAddress(jsonObject["ip"].asString, jsonObject["port"].asInt)
                val message = jsonObject["message"].toString()
                return Send(source, recipient, message)
            }
            ACK.name -> return Ack(source, jsonObject["message"].toString())
            AGENTS.name -> return Agents(source)
            ADD_AGENTS.name -> {
                val agentsArray = jsonObject["agents"].asJsonArray
                val agents = agentsArray.map { it.asJsonObject }.map { InetSocketAddress(it["ip"].asString, it["port"].asInt) }
                return AddAgents(source, agents)
            }
            else -> return UnknownMessage(source, jsonObject.toString())
        }


    }

    fun objToStr(message: Message): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", message.type)

        when (message) {
            is Store -> {
                jsonObject.addProperty("value", message.value)
            }
            is Result -> {
                jsonObject.addProperty("status", message.status)
                jsonObject.add("message", jsonParser.parse(message.message))
                jsonObject.addProperty("result", message.result)
            }
            is Send -> {
                jsonObject.addProperty("ip", message.recipient.hostString)
                jsonObject.addProperty("port", message.recipient.port)
            }
            is Ack -> {
                jsonObject.add("message", jsonParser.parse(message.message))
            }
            is AddAgents -> {
                val agents: JsonArray = message.addresses.map {
                    val agentAddress = JsonObject()
                    agentAddress.addProperty("ip", it.hostString)
                    agentAddress.addProperty("port", it.port)
                    agentAddress
                }.fold(JsonArray(), { arr, address -> arr.add(address); arr; })
                jsonObject.add("agents", agents)
            }
            is UnknownMessage -> {
                return message.message
            }
        }
        return jsonObject.toString()
    }
}