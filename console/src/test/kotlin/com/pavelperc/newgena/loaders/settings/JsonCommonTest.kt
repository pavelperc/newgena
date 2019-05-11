package com.pavelperc.newgena.loaders.settings

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.junit.Test


class JsonCommonTest {
    
    @Serializable
    class Sample {
        @Required
        val settingsInfo = SettingsInfo("petrinet", "0.1")
        @Required
        var a: String? = "hello"
        @Required
        var b = 20
        var c: String = "empty"
        
        init {
            check(b >= 0) { " b should not be negative, but it equals $b." }
        }
        
        override fun toString() = "Sample(a=$a, b=$b, c=$c)"
    }
    
    
    val `migrate 0_0 0_1` = Migrator("""
        0.0 -> 0.1:
        Added field c with default value "EMPTY".
        b is now Integer. (default is 0)
    """.trimIndent()) { oldSettings ->
        oldSettings.replace { map ->
            map.setVersion("0.1")
            
            map["c"] = JsonPrimitive("EMPTY")
            map["b"] = JsonPrimitive(map["b"]?.contentOrNull?.toIntOrNull() ?: 0)
        }
    }
    
    fun getPetrinetMigratorsTest(settingsJE: JsonElement): List<Migrator> {
        
        val info = getSettingsInfo(settingsJE)
        
        if (info.type != "petrinet") {
            throw IllegalArgumentException("Settings type should be `petrinet`.")
        }
        
        val versionsToMigrators = listOf(
                "0.0" to `migrate 0_0 0_1`,
                "0.1" to Migrator.empty
        )
        val versions = versionsToMigrators.map { it.first }
        
        if (info.version !in versions) {
            throw IllegalArgumentException("Unknown settings version: ${info.version}." +
                    "Possible versions are $versions.")
        }
        
        return versionsToMigrators
                .dropWhile { it.first != info.version }
                .map { it.second }
                .dropLast(1)
    }
    
    
    @UnstableDefault
    @ImplicitReflectionSerializer
    @Test
    fun deserialize() {
        
        val sampleStr = """
            {
            // hello
                "settingsInfo" : { "version": "0.0", "type" : "petrinet" }
                "a": "a",
                "b": "2" // bye
            } // okey
            // good
        """.trimIndent()
        
        val noComments = removeComments(sampleStr)
        
        println("no comments:")
        println(noComments)
        
        val jo = MyJson.parseJson(noComments).jsonObject
        println("jo:")
        println(jo)
        
        val migrations = getPetrinetMigratorsTest(jo)
        
        val migrated = if (migrations.isEmpty()) {
            jo
        } else {
            println("Applying migrations:")
            migrations.fold(jo) { old, migrator -> println(migrator.description); migrator(old) }
        }
        println("Migrated:")
        println(migrated)
        
        val sample = MyJson.fromJson(Sample.serializer(), migrated)
        println(sample)
        println(MyJson.stringify(sample))
        
    }
}