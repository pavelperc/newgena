package com.pavelperc.newgena.loaders.settings

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeInt
import com.pavelperc.newgena.utils.propertyinitializers.NonNegativeLong
import org.junit.Test
import kotlin.system.measureTimeMillis


class JsonCommonTest {
    
    class Sample {
        var a: String? = "hello"
        var b by NonNegativeLong(20)
        
        
        var c: String = "empty"
        
        @JsonCreator
        constructor(a: String?, b: Long, c: String) {
            this.a = a
            this.b = b
            this.c = c
        }
        
        override fun toString() = "Sample(a=$a, b=$b, c=$c)"
    }
    
    data class Data(
            val a: String = "a",
            val b: String = "b",
            val c: String? = "c"
    ) {
        val d: String? = "d"
        
        override fun toString() = "Data(a='$a', b='$b', c=$c, d=$d)"
    
    }
    
    
    @Test
    fun deserialize() {
        
        val str = """
            {
                "a": null,
                "b": 11,
                "c": "c"
            }
        """.trimIndent()
        
        measureTimeMillis {
            val sample = fromJson<Sample>(str)
            println(sample)
        }.also { println("time1: $it ms") }
        
        measureTimeMillis {
            val sample = fromJson<Sample>(str)
            println(sample)
        }.also { println("time2: $it ms") }
        
        val data = fromJson<Data>("""
            {
                "a": "1",
                "c": "3"
            }
        """.trimIndent())
        println(data)
        
        
        
    }
}