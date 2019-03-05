package com.pavelperc.newgena.utils.propertyinitializers

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty


class NonNegativeInt(initialValue: Int) : RangeProp<Int>(
        initialValue,
        0..Int.MAX_VALUE,
        "Value is negative."
)

class NonNegativeLong(initialValue: Long) : RangeProp<Long>(
        initialValue,
        0..Long.MAX_VALUE,
        "Value is negative."
)


class RangeInt(initialValue: Int, intRange: IntRange) : RangeProp<Int>(initialValue, intRange)

class RangeLong(initialValue: Long, longRange: LongRange) : RangeProp<Long>(initialValue, longRange)

/** Throws [IllegalStateException] if the initialValue is not in [range]*/
open class RangeProp<T : Comparable<T>>(
        initialValue: T,
        val range: ClosedRange<T>,
        val message: String = "Value is not in $range."
) : ObservableProperty<T>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
        if (newValue !in range)
            throw IllegalStateException("Property ${property.name} with value $newValue: " + message)
        return true
    }
}


/** Throws [IllegalStateException] if the initialValue is not in [range].*/
open class ExclusiveBoolean(
        val otherProp: KProperty<Boolean>,
        initialValue: Boolean = false
) : ObservableProperty<Boolean>(initialValue) {
    override fun beforeChange(property: KProperty<*>, oldValue: Boolean, newValue: Boolean): Boolean {
        if (newValue && otherProp.getter.call()) {
            throw IllegalStateException("Boolean properties ${property.name} and ${otherProp.name} are exclusive.")
        }
        return true
    }
}

class Temp {
    var a by ExclusiveBoolean(::b)
    var b: Boolean by ExclusiveBoolean(::a)
    
    var c by NonNegativeInt(10)
    
    var d by RangeInt(0, -1..1)
}

fun main(args: Array<String>) {
    fun exc(lambda: () -> Unit) = try {
        lambda()
    } catch (e: Exception) {
        println(e.message)
    }
    
    val temp = Temp()
    
    temp.a = true
    temp.a = false
    temp.b = true
    
    exc { temp.a = true } // Boolean properties a and b are exclusive.
    
    temp.c = 50
    exc { temp.c = -50 } // Property c with value -50: Value is negative.
    exc { temp.d = 2 } // Property d with value 2: Value is not in -1..1.
}



