package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod


/**
 * Fixing boolean properties.
 * All boolean properties now should start from is.
 */
private class MyPropertyNamingStrategy() : PropertyNamingStrategy() {
    override fun nameForGetterMethod(config: MapperConfig<*>, method: AnnotatedMethod, defaultName: String): String {
//            print("getter method: ${method.name}")
        return if ((method.rawReturnType == Boolean::class.java || method.rawReturnType == Boolean::class.javaPrimitiveType)
                && method.name.startsWith("is")) {
            method.name
        } else super.nameForGetterMethod(config, method, defaultName) // .also { println(" -> $it") }
    }
    
    override fun nameForSetterMethod(config: MapperConfig<*>, method: AnnotatedMethod, defaultName: String): String {
//            print("setter method: ${method.name}")
        return if (method.parameterCount == 1
                && method.name.startsWith("set")
                && (method.getRawParameterType(0) == Boolean::class.java
                        || method.getRawParameterType(0) == Boolean::class.javaPrimitiveType)) {
            ("is" + method.name.substring(3)) // .also { println(" -> $it") }
        } else super.nameForSetterMethod(config, method, defaultName) // .also { println(" -> $it") }
    }
}


/** Jackson converter setup. */
val mapper: ObjectMapper = ObjectMapper().also { mapper ->
    // loading java8 time support
    mapper.findAndRegisterModules()
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    
    // allow comments
    mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
    
    // sort alphabetically
//    mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    
    // fixing boolean properties
    mapper.propertyNamingStrategy = MyPropertyNamingStrategy()
}

fun JsonSettings.Companion.fromJson(json: String): JsonSettings {
    return mapper.readValue<JsonSettings>(json, JsonSettings::class.java)
}

inline fun <reified T> fromJson(json: String): T {
    return mapper.readValue<T>(json, T::class.java)
}

fun Any.toJson(): String {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

//        val gson = GsonBuilder().setPrettyPrinting().create()

//    val sb = StringBuilder(Klaxon().toJsonString(this))
//    val jsonString = (Parser.default().parse(sb) as JsonObject).toJsonString(true)
//    return jsonString
}