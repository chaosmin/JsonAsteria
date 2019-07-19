package com.romani.jsonparser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.module.kotlin.convertValue
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author romani
 * @since 2019-07-01 11:19
 */
object JsonUtil {
    private val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()
        // 允许 Java风格 注释
        configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        // 允许 Yaml风格 注释
        configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true)
        // 允许单引号
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        // 允许末尾逗号
        configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
        // 格式化输出
        configure(SerializationFeature.INDENT_OUTPUT, false)
        dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    }

    fun mapper(): ObjectMapper = this.objectMapper

    inline fun <reified T> convertValue(node: JsonNode): T? {
        return if (node is MissingNode) null
        else mapper().convertValue(node)
    }

    fun encode(obj: Any?, prettyPrinter: Boolean = false): String {
        return if (obj == null) ""
        else try {
            if (!prettyPrinter) mapper().writeValueAsString(obj)
            else mapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun <T> decode(json: String?, valueType: Class<T>): T? {
        if (json.isNullOrBlank()) return null
        return try {
            mapper().readValue(json, valueType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getIfPersent(node: JsonNode, path: String): JsonNode {
        return if (node.path(path) is MissingNode) {
            (node as ObjectNode).set(path, newObjectNode())
            node[path]
        } else node[path]
    }

    private fun <T> basicNode(value: T): JsonNode {
        return when (value) {
            is String -> TextNode(value)
            is Int -> IntNode(value)
            is Long -> LongNode(value)
            is Double -> DoubleNode(value)
            is Boolean -> BooleanNode.valueOf(value)
            else -> POJONode(value)
        }
    }

    fun newObjectNode(): ObjectNode {
        return mapper().createObjectNode()
    }

    fun <T> newObjectNode(key: String, value: List<T>): JsonNode {
        return newObjectNode().set(key, newArrayNode(value))
    }

    fun <T> newObjectNode(key: String, value: T): JsonNode {
        return newObjectNode().set(key, basicNode(value))
    }

    fun <T> newArrayNode(value: List<T>): ArrayNode {
        return mapper().createArrayNode().addAll(value.mapNotNull { basicNode(it) })
    }
}