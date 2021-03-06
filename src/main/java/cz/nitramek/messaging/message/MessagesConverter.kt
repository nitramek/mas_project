package cz.nitramek.messaging.message

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import cz.nitramek.messaging.message.Message.MessageType.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class MessagesConverter {

    private val jsonParser = JsonParser()
    private val log = LoggerFactory.getLogger(MessagesConverter::class.java)!!


    fun strToObj(json: String): Message {
        return jObjToObj(strToJsonObj(json), json)
    }

    fun strToJsonObj(json: String) = jsonParser.parse(json).asJsonObject

    fun strToJsonO(json: String) = jsonParser.parse(json)
    fun jObjToObj(obj: JsonObject, json: String): Message {
        try {

            val type = obj["type"].asString.toUpperCase()
            val header = MessageHeader(InetSocketAddress(obj["sourceIp"].asString, obj["sourcePort"].asInt), obj["tag"].asString)
            when (type) {
                STORE.name -> return Store(header, obj["value"].asString)
                RESULT.name -> {
                    return Result(header, obj["status"].asString, obj["value"]?.asString ?: "", obj["message"].toString())
                }
                SEND.name -> {
                    val recipient = InetSocketAddress(obj["ip"].asString, obj["port"].asInt)
                    val message = obj["message"].asString
                    return Send(header, recipient, message)
                }
                ACK.name -> return Ack(header, obj["message"].asString)
                AGENTS.name -> return Agents(header, json)
                PACKAGE.name -> {
                    return Package(header, obj["data"].asString, obj["order"].asInt, obj["fileName"].asString, obj["partsCount"].asInt)
                }
                EXECUTE.name -> {
                    return Execute(header, obj["command"].asString)
                }
                HALT.name -> {
                    return Halt(header)
                }
                DUPLICATE.name -> {
                    return Duplicate(header, InetSocketAddress(obj["sourceIp"].asString, obj["sourcePort"].asInt))
                }
                PACKAGE_RECEIVED.type -> {
                    return PackageReceived(header)
                }
                else -> return UnknownMessage(header, type, obj.toString())
            }
        } catch (exception: Exception) {
            log.error(exception.message, exception)
            throw exception
        }

    }
    fun addHeaderParams(jsonString: String, header: MessageHeader): String {
        val jObject = jsonParser.parse(jsonString).asJsonObject
        jObject.addProperty("sourceIp", header.source.address.hostAddress)
        jObject.addProperty("sourcePort", header.source.port)
        jObject.addProperty("tag", header.tag)
        return jObject.toString()
    }

    fun objToJson(message: Message): JsonObject {
//        log.debug("{}", message)
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
                obj.addProperty("value", message.value)
            }
            is Send -> {
                obj.addProperty("ip", message.recipient.hostString)
                obj.addProperty("port", message.recipient.port)
                obj.addProperty("message", message.message)
            }
            is Ack -> {
                obj.addProperty("message", message.message)
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
            is Duplicate -> {
//                obj.addProperty("sourceIp", message.recipient.hostString)
//                obj.addProperty("port", message.recipient.port)
            }
            is PackageReceived -> {
                //nothing extra
            }
        }
        return obj
    }

    fun objToStr(message: Message): String {
        return objToJson(message).toString()
    }
}


