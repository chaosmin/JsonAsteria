package com.romani.jsonparser

/**
 * @author romani
 * @since 2019-07-19 10:14
 */
object ParserFactory {
    fun build(): Parser {
        return DefaultParser()
    }
}