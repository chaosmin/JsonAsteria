package com.romani.jsonparser

import com.jayway.jsonpath.TypeRef

/**
 * @author romani
 * @since 2019-06-26 13:14
 */
data class Schema<T>(
        val name: String,
        val type: TypeRef<T>,
        val readPath: String,
        val writePath: String,
        val optional: Boolean,
        val doc: String
) {
    private var value: T? = null
    private var default: T? = null
    val valueOrDefault: T? by lazy {
        when {
            value != null -> value
            default != null -> default
            optional -> null
            else -> throw RuntimeException("illegal value")
        }
    }

    fun setValue(values: Pair<T?, T?>): Schema<T> {
        this.value = values.first
        this.default = values.second
        return this
    }

    companion object {
        inline fun <reified Type> buildSchema(name: String, optional: Boolean, readPath: String, writePath: String, doc: String): Schema<Type> {
            return Schema(
                    name = name,
                    optional = optional,
                    readPath = readPath,
                    writePath = writePath,
                    type = object : TypeRef<Type>() {
                        override fun toString(): String = this.type.typeName
                    },
                    doc = doc
            )
        }
    }
}