package com.pavelperc.newgena.gui.customfields

import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.util.StringConverter
import tornadofx.*
import kotlin.reflect.KMutableProperty

typealias Checker<T> = (newValue: T) -> Unit
typealias OnSuccess<T> = (newValue: T) -> Unit
typealias Converter<A, B> = (A) -> B

interface Validatable {
    val isDirty: Boolean
}

/**
 * [P] is property type. [F] is field type.
 */
abstract class MyPropertyWrapper<P : Any?, F : Any?>(
        val prop: KMutableProperty<out P>,
        var onSuccess: OnSuccess<P> = { },
        var checker: Checker<P> = { }
) : Validatable {
    
    
    /** Null converters as well. */
    abstract fun convertToProp(newValue: F): P
    
    abstract fun onFixed()
    abstract fun onBroken(error: String)
    
    var lastError: String? = null
        protected set
    
    override val isDirty: Boolean
        get() = lastError != null
    
    /**
     * Sets [newValue] to [prop] with all checkers.
     * [newValue] is new data from ui.
     */
    open fun setToProp(newValue: F) {
        try {
            val converted = convertToProp(newValue)
            checker(converted)
            
            prop.setter.call(converted)
            
            // we fixed an error
            lastError?.also { le ->
                lastError = null
                
                println("${prop.name}: fixed error: $le")
                onFixed()
            }
            onSuccess(converted)
        } catch (e: Exception) {
            lastError = e.message ?: e.cause?.message ?: e.cause?.cause?.message ?: "unknown error"
            
            if (e is NumberFormatException)
                lastError = "Bad Number Format: $lastError"
            
            println("${prop.name}: error: $lastError")
//                    e.printStackTrace()
            onBroken(lastError ?: "??")
        }
    }
}

abstract class MyPropertyLabeled<P, F>(
        prop: KMutableProperty<out P>
) : MyPropertyWrapper<P, F>(prop) {
    val labelField: Field
    
    init {
        var label = prop.name
        if (prop.returnType.isMarkedNullable)
            label += "?"
        
        labelField = Field(label)
    }
}

