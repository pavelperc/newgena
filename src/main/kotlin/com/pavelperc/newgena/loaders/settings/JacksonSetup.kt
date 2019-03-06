package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod

/** Jackson converter setup. */
val mapper: ObjectMapper = ObjectMapper().also { mapper ->
    // loading java8 time support
    mapper.findAndRegisterModules()
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    
    // sort alphabetically
//    mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    
    // fixing boolean properties
    // all boolean properties now should start from with.
    mapper.propertyNamingStrategy = object : PropertyNamingStrategy() {
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
}

fun JsonSettings.Companion.fromJson(json: String): JsonSettings {
    return mapper.readValue<JsonSettings>(json, JsonSettings::class.java)
}

fun JsonSettings.toJson(): String {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

//        val gson = GsonBuilder().setPrettyPrinting().create()

//    val sb = StringBuilder(Klaxon().toJsonString(this))
//    val jsonString = (Parser.default().parse(sb) as JsonObject).toJsonString(true)
//    return jsonString
}