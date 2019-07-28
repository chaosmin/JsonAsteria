package com.chaosmin.asteria

import com.jayway.jsonpath.TypeRef

/**
 * Definition of one field in JSON format
 *
 * @author romani
 * @since 2019-06-26 13:14
 */
data class Schema<T>(
        // name of this field
        val name: String,
        // type of this field based on JsonPath TypeRef
        val type: TypeRef<T>,
        // jsonPath in origin JSON to read
        val readPath: String,
        // jsonPath to write
        val writePath: String,
        // this field can be null or not
        val optional: Boolean,
        // doc of field
        val doc: String
) {
    // value of this field which read from origin JSON
    private var value: T? = null
    // default value of this field which defined in schema
    private var default: T? = null
    // final value of this field
    val valueOrDefault: T? by lazy {
        when {
            value != null -> value
            default != null -> default
            optional -> null
            else -> throw RuntimeException("illegal value")
        }
    }

    // set value and default value of this field
    // value and default value cannot be obtained in first time, so it needs post processing
    fun values(value: T?, default: T?): Schema<T> {
        this.value = value
        this.default = default
        return this
    }

    companion object {
        // use this static function to create a field schema based on it's JavaType
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