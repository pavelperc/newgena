package com.pavelperc.newgena.gui.customfields

import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import javafx.util.converter.IntegerStringConverter
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

open class MyPropertyTextField<P : Any?>(
        prop: KMutableProperty<out P>,
        val converter: StringConverter<P>
) : MyPropertyLabeled<P, String>(prop) {
    val textField: TextField
    
    override fun convertToProp(newValue: String): P {
        if (newValue == "null" && prop.returnType.isMarkedNullable)
            return null as P
        
        return converter.fromString(newValue)
    }
    
    override fun onBroken(error: String) {
        textField.style = "-fx-background-color: red;"
        textField.tooltip = Tooltip(error)
    }
    
    override fun onFixed() {
        textField.style = "-fx-background-color: white;"
        textField.tooltip = null
    }
    
    init {
        val initial = converter.toString(prop.call())
        textField = TextField(initial)
        var label = prop.name
        if (prop.returnType.isMarkedNullable)
            label += "?"
        
        textField.attachTo(labelField) {
            textProperty().addListener { observable, oldValue, newValue ->
                setToProp(newValue)
            }
        }
    }
}

class MyBooleanField(
        prop: KMutableProperty<Boolean>
) : MyPropertyLabeled<Boolean, Boolean>(prop) {
    val checkBox: CheckBox
    
    override fun convertToProp(newValue: Boolean) = newValue
    
    override fun onBroken(error: String) {
        checkBox.style = "-fx-background-color: red;"
        checkBox.tooltip = Tooltip(error)
    }
    
    override fun onFixed() {
        checkBox.style = "-fx-background-color: white;"
        checkBox.tooltip = null
    }
    
    init {
        checkBox = CheckBox()
        
        checkBox.attachTo(labelField) {
            selectedProperty().addListener { observable, oldValue, newValue ->
                setToProp(newValue)
            }
        }
    }
}

class MyIntField(
        prop: KMutableProperty<out Int>
) : MyPropertyTextField<Int>(
        prop,
        IntegerStringConverter()
)

class MyStringFieldNullable(
        prop: KMutableProperty<String?>
) : MyPropertyTextField<String?>(
        prop,
        DefaultStringConverter()
)

class MyStringField(
        prop: KMutableProperty<String>
) : MyPropertyTextField<String>(
        prop,
        DefaultStringConverter()
)

fun EventTarget.myStringField(
        prop: KMutableProperty<String>,
        op: MyStringField.() -> Unit = {}
): MyStringField {
    val myField = MyStringField(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}


fun EventTarget.myStringFieldNullable(
        prop: KMutableProperty<String?>,
        op: MyStringFieldNullable.() -> Unit = {}
): MyStringFieldNullable {
    val myField = MyStringFieldNullable(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}

fun EventTarget.myIntField(
        prop: KMutableProperty<Int>,
        op: MyIntField.() -> Unit = {}
): MyIntField {
    val myField = MyIntField(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}

fun EventTarget.myBooleanField(
        prop: KMutableProperty<Boolean>,
        op: MyBooleanField.() -> Unit = {}
): MyBooleanField {
    val myField = MyBooleanField(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}



// =================
// how it was before:
// =================
//
//fun <T : String?> EventTarget.myStringField(
//        prop: KMutableProperty<T>
//) = myTextField(prop, DefaultStringConverter())
//
//fun EventTarget.myIntField(
//        prop: KMutableProperty<Int>
//) = myTextField(prop, IntegerStringConverter())
//
//
//fun <T> EventTarget.myTextField(
//        prop: KMutableProperty<out T>,
//        converter: StringConverter<T>,
//        onSuccess: OnSuccess<String> = { },
//        checker: Checker<String> = { }
//): Field {
//    
//    return field(prop.name) {
//        val initial = converter.toString(prop.call())
//        
//        textfield(initial) {
//            val textfield = this
//            var lastError: String? = null
//            
//            textProperty().addListener { observable, oldValue, newValue ->
//                try {
//                    checker(newValue)
//                    // ??????
//                    val nullableValue =
//                            if (newValue == "null" && prop.returnType.isMarkedNullable) null
//                            else newValue
//                    
//                    prop.setter.call(converter.fromString(nullableValue))
//                    
//                    // we fixed an error
//                    lastError?.also { le ->
//                        lastError = null
//                        
//                        println("${prop.name}: fixed error: $le")
//                        
//                        textfield.style = "-fx-background-color: white;"
//                        textfield.tooltip = null
//                    }
//                    
//                    onSuccess(newValue)
//                } catch (e: Exception) {
//                    lastError = e.message ?: e.cause?.message ?: e.cause?.cause?.message ?: "unknown error"
//                    
//                    if (e is NumberFormatException)
//                        lastError = "Bad Number Format: $lastError"
//                    
//                    println("${prop.name}: error: $lastError")
////                    e.printStackTrace()
//                    
//                    textfield.style = "-fx-background-color: red;"
//                    
//                    textfield.tooltip = Tooltip(lastError)
//                }
//            }
//            
//            
//        }
//    }
//}

