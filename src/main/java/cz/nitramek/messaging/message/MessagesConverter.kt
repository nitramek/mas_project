package cz.nitramek.messaging.message

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import cz.nitramek.messaging.UDPCommunicator
import cz.nitramek.messaging.message.Message.MessageType.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class MessagesConverter {

    private val jsonParser = JsonParser()
    private val log = LoggerFactory.getLogger(UDPCommunicator::class.java)!!


    fun strToObj(json: String): Message {
        try {
            val obj = jsonParser.parse(json).asJsonObject
            val type = obj["type"].asString
            val header = MessageHeader(InetSocketAddress(obj["sourceIp"].asString, obj["sourcePort"].asInt))
            when (type) {
                STORE.name -> return Store(header, obj["value"].asString)
                RESULT.name -> return Result(header, obj["status"].asString, obj["result"].asString, obj["message"].toString())
                SEND.name -> {
                    val recipient = InetSocketAddress(obj["ip"].asString, obj["port"].asInt)
                    val message = obj["message"].toString()
                    return Send(header, recipient, message)
                }
                ACK.name -> return Ack(header, obj["message"].toString())
                AGENTS.name -> return Agents(header)
                ADD_AGENTS.name -> {
                    val agentsArray = obj["agents"].asJsonArray
                    val agents = agentsArray.map { it.asJsonObject }.map { InetSocketAddress(it["ip"].asString, it["port"].asInt) }
                    return AddAgents(header, agents)
                }
                else -> return UnknownMessage(header, type, obj.toString())
            }
        } catch (exception: JsonParseException) {
            log.error(exception.message, exception.printStackTrace())
            throw exception
        }


    }

    fun objToJson(message: Message): JsonObject {

        val obj = JsonObject()
        obj.addProperty("type", message.type)
        obj.addProperty("sourceIp", message.header.source.address.hostAddress)
        obj.addProperty("sourcePort", message.header.source.port)

        when (message) {
            is Store -> {
                obj.addProperty("value", message.value)
            }
            is Result -> {
                obj.addProperty("status", message.status)
                obj.addProperty("message", message.message)
                obj.addProperty("result", message.result)
            }
            is Send -> {
                obj.addProperty("ip", message.recipient.hostString)
                obj.addProperty("port", message.recipient.port)
            }
            is Ack -> {
                obj.add("message", jsonParser.parse(message.message))
            }
            is AddAgents -> {
                val agents: JsonArray = message.addresses.map {
                    JsonObject().apply {
                        addProperty("ip", it.hostString)
                        addProperty("port", it.port)
                    }
                }.fold(JsonArray(), JsonArray::insert)
                obj.add("agents", agents)
            }
            is UnknownMessage -> {
                return jsonParser.parse(message.message).asJsonObject
            }
        }
        return obj
    }

    fun objToStr(message: Message): String {
        return objToJson(message).toString()
    }
}


