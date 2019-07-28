package com.chaosmin.asteria

import com.chaosmin.toolkit.JsonUtil
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

/**
 * @author romani
 * @since 2019-07-19 11:11
 */
class DefaultParserTest {
    @Test
    fun execute() {
        val classLoader = DefaultParserTest::class.java.classLoader
        val originFile = classLoader.getResource("origin.json")?.path
        val schemaFile = classLoader.getResource("schema.json")?.path
        if (originFile.isNullOrBlank() || schemaFile.isNullOrBlank()) {
            println("originFile or schemaFile is null.")
        } else {
            val origin = File(originFile).readText(Charset.forName("UTF-8"))
            val schema = File(schemaFile).readText(Charset.forName("UTF-8"))
            val result = ParserFactory.build().execute(origin, schema)
            println(JsonUtil.encode(result, true))
        }
    }

    @Test
    fun executeCarrier() {
        val classLoader = DefaultParserTest::class.java.classLoader
        val originFile = classLoader.getResource("origin_carrier.json")?.path
        val schemaFile = classLoader.getResource("schema_carrier.json")?.path
        if (originFile.isNullOrBlank() || schemaFile.isNullOrBlank()) {
            println("originFile or schemaFile is null.")
        } else {
            val origin = File(originFile).readText(Charset.forName("UTF-8"))
            val schema = File(schemaFile).readText(Charset.forName("UTF-8"))
            val result = ParserFactory.build().execute(origin, schema)
            println(JsonUtil.encode(result, true))
        }
    }
}