package com.chaosmin.asteria

/**
 * ParserFactory to create Parser
 *
 * @author romani
 * @since 2019-07-19 10:14
 */
object ParserFactory {
    /**
     * default Parser will be return.
     */
    fun build(): Parser {
        return DefaultParser()
    }
}