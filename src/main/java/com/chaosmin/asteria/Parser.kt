package com.chaosmin.asteria

import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.Configuration
import org.slf4j.Logger

/**
 * @author romani
 * @since 2019-07-19 10:18
 */
interface Parser {
    val logger: Logger
    val config: Configuration

    fun execute(origin: String, schema: String): ObjectNode
}