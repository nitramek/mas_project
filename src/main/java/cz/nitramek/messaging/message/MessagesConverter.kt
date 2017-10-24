package cz.nitramek.messaging.message

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
            log.debug("Received length - ${json.length}")
            val obj = jsonParser.parse(json).asJsonObject
            val type = obj["type"].asString

            val header = MessageHeader(InetSocketAddress(obj["sourceIp"].asString, obj["sourcePort"].asInt), obj["tag"].asString)
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
                PACKAGE.name -> {
                    return Package(header, obj["data"].asString, obj["order"].asInt, obj["fileName"].asString, obj["partsCount"].asInt)
                }
                EXECUTE.name -> {
                    return Execute(header, obj["command"].asString)
                }
                HALT.name -> {
                    return Halt(header)
                }
                else -> return UnknownMessage(header, type, obj.toString())
            }
        } catch (exception: JsonParseException) {
            log.error(exception.message, exception.printStackTrace())
            throw exception
        } catch (e: NullPointerException) {
            throw e
        }


    }

    fun objToJson(message: Message): JsonObject {

        val obj = JsonObject()
        obj.addProperty("type", message.type)
        obj.addProperty("sourceIp", message.header.source.address.hostAddress)
        obj.addProperty("sourcePort", message.header.source.port)
        obj.addProperty("tag", message.header.tag)

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
            is Package -> {
                obj.addProperty("data", message.data)
                obj.addProperty("fileName", message.fileName)
                obj.addProperty("order", message.order)
                obj.addProperty("partsCount", message.partsCount)
            }
            is Execute -> {
                obj.addProperty("command", message.command)
            }
            is Halt -> {
                //nothing extra is needed
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


