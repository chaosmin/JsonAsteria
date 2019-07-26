package com.romani.jsonparser

import com.chaosmin.toolkit.JsonUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider
import com.jayway.jsonpath.spi.mapper.JsonSmartMappingProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author romani
 * @since 2019-07-19 10:19
 */
class DefaultParser : Parser {
    override val logger: Logger = LoggerFactory.getLogger(DefaultParser::class.java)

    override val config: Configuration = Configuration.builder()
            .jsonProvider(JsonSmartJsonProvider())
            .mappingProvider(JsonSmartMappingProvider()).build()

    /**
     * execute parse main function
     *
     * @param origin origin data json to read value from
     * @param schema schema definition to parse origin data
     * @return parse result
     */
    override fun execute(origin: String, schema: String): ObjectNode {
        if (origin.isBlank() || schema.isBlank()) throw RuntimeException("")
        else {
            val readDocument = JsonPath.parse(origin, config)
            val schemas = read("$", JsonUtil.objectMapper.readTree(schema), readDocument)
            val root = JsonUtil.newNode()
            schemas.forEach {
                val paths = it.writePath.split(".")
                val node = JsonUtil.newNode(paths.last(), it.valueOrDefault)
                var currentNode: JsonNode = root
                paths.drop(1).dropLast(1).forEach { path ->
                    currentNode = when {
                        path == "$" -> root
                        node.path(path) is MissingNode -> {
                            (node as ObjectNode).set(path, JsonUtil.newNode())
                            node[path]
                        }
                        else -> node[path]
                    }
                }
                (currentNode as ObjectNode).setAll(node as ObjectNode)
            }
            return root as ObjectNode
        }
    }

    /**
     * analysis schema definition and build jsonNode in recursion
     *
     * @param prefix prefix path when write parse result
     * @param node jsonNode which Jackson read from schema define
     * @param document readContext for get value
     * @return schema list to generate json result
     */
    private fun read(prefix: String, node: JsonNode, document: DocumentContext): List<Schema<*>> {
        return when {
            node.isArray -> node.flatMap { read(prefix, it, document) }
            node.has("fields") -> node["fields"].flatMap { read("$prefix.${node["name"].textValue()}", it, document) }
            else -> {
                val name = node["name"].textValue()
                val optional = if (node.has("optional")) true else node["optional"].booleanValue()
                val readPath = node["jsonPath"].textValue()
                val writePath = "$prefix.$name"
                val isArray = if (node.has("isArray")) node["isArray"].booleanValue() else false
                val type = node["type"].textValue()
                val default = node.path("default")
                val doc = if (node.has("doc")) node["doc"].textValue() else ""
                return listOf(when (type.toLowerCase()) {
                    "string" -> if (isArray) {
                        val values = getVaD<List<String>>(readPath, document, default)
                        val schema = Schema.buildSchema<List<String>>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    } else {
                        val values = getVaD<String>(readPath, document, default)
                        val schema = Schema.buildSchema<String>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    }
                    "int" -> if (isArray) {
                        val values = getVaD<List<Int>>(readPath, document, default)
                        val schema = Schema.buildSchema<List<Int>>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    } else {
                        val values = getVaD<Int>(readPath, document, default)
                        val schema = Schema.buildSchema<Int>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    }
                    else -> if (isArray) {
                        val values = getVaD<List<Any>>(readPath, document, default)
                        val schema = Schema.buildSchema<List<Any>>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    } else {
                        val values = getVaD<Any>(readPath, document, default)
                        val schema = Schema.buildSchema<Any>(name, optional, readPath, writePath, doc)
                        schema.values(values.first, values.second)
                    }
                })
            }
        }
    }

    /**
     * get schema value and default value from data
     *
     * @param path jsonPath to read value
     * @param document readContext for get value
     * @param default default value defined in schema
     * @return first field is schema value, second field is default value
     */
    private inline fun <reified T> getVaD(path: String, document: DocumentContext, default: JsonNode): Pair<T?, T?> {
        val value = document.read(path, T::class.java)
        val defaultValue = JsonUtil.convertValue<T>(default)
        return value to defaultValue
    }
}