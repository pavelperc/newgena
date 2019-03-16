package com.pavelperc.newgena.gui.customfields

import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import kotlin.reflect.KMutableProperty


typealias Checker<T> = (newValue: T) -> Unit
typealias OnSuccess<T> = (newValue: T) -> Unit
typealias Converter<A, B> = (A) -> B


/**
 * [P] is property type. [F] is field type.
 */
abstract class MyPropertyPane<P : Any?, F : Any?>(
        val prop: KMutableProperty<out P>,
        val onSuccess: OnSuccess<P> = { },
        val checker: Checker<P> = { }
) : Pane() {
    
    
    /** Null converters as well. */
    abstract fun convertToProp(newValue: F): P
    
    abstract fun onFixed()
    abstract fun onBroken(error: String)
    
    var lastError: String? = null
        protected set
    
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


open class MyPropertyTextField<P : Any?>(
        prop: KMutableProperty<out P>,
        onSuccess: OnSuccess<P>,
        checker: Checker<P>,
        val converter: StringConverter<P>
) : MyPropertyPane<P, String>(prop, onSuccess, checker) {
    
    
    val textField: TextField
    val labelField: Field
    
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
        
        labelField = field(prop.name) {
            textField.attachTo(this) {
                textProperty().addListener { observable, oldValue, newValue ->
                    setToProp(newValue)
                }
            }
        }
        
    }
}

class MyBooleanField(
        prop: KMutableProperty<Boolean>,
        onSuccess: OnSuccess<Boolean> = {},
        checker: Checker<Boolean> = {}
) : MyPropertyPane<Boolean, Boolean>(prop, onSuccess, checker) {
    val checkBox: CheckBox
    val labelField: Field
    
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
        
        labelField = field(prop.name) {
            checkBox.attachTo(this) {
                selectedProperty().addListener { observable, oldValue, newValue ->
                    setToProp(newValue)
                }
            }
        }
    }
    
}

class MyIntField(
        prop: KMutableProperty<out Int>,
        onSuccess: OnSuccess<Int> = {},
        checker: Checker<Int> = {}
) : MyPropertyTextField<Int>(
        prop,
        onSuccess,
        checker,
        IntegerStringConverter()
)

class MyStringFieldNullable(
        prop: KMutableProperty<String?>,
        onSuccess: OnSuccess<String?> = {},
        checker: Checker<String?> = {}
) : MyPropertyTextField<String?>(
        prop,
        onSuccess,
        checker,
        DefaultStringConverter()
)

class MyStringField(
        prop: KMutableProperty<String>,
        onSuccess: OnSuccess<String> = {},
        checker: Checker<String> = {}
) : MyPropertyTextField<String>(
        prop,
        onSuccess,
        checker,
        DefaultStringConverter()
)

// =================
// as it was before:
// =================

fun <T : String?> EventTarget.myStringField(
        prop: KMutableProperty<T>
) = myTextField(prop, DefaultStringConverter())

fun EventTarget.myIntField(
        prop: KMutableProperty<Int>
) = myTextField(prop, IntegerStringConverter())


fun <T> EventTarget.myTextField(
        prop: KMutableProperty<out T>,
        converter: StringConverter<T>,
        onSuccess: OnSuccess<String> = { },
        checker: Checker<String> = { }
): Field {
    
    return field(prop.name) {
        val initial = converter.toString(prop.call())
        
        textfield(initial) {
            val textfield = this
            var lastError: String? = null
            
            textProperty().addListener { observable, oldValue, newValue ->
                try {
                    checker(newValue)
                    // ??????
                    val nullableValue =
                            if (newValue == "null" && prop.returnType.isMarkedNullable) null
                            else newValue
                    
                    prop.setter.call(converter.fromString(nullableValue))
                    
                    // we fixed an error
                    lastError?.also { le ->
                        lastError = null
                        
                        println("${prop.name}: fixed error: $le")
                        
                        textfield.style = "-fx-background-color: white;"
                        textfield.tooltip = null
                    }
                    
                    onSuccess(newValue)
                } catch (e: Exception) {
                    lastError = e.message ?: e.cause?.message ?: e.cause?.cause?.message ?: "unknown error"
                    
                    if (e is NumberFormatException)
                        lastError = "Bad Number Format: $lastError"
                    
                    println("${prop.name}: error: $lastError")
//                    e.printStackTrace()
                    
                    textfield.style = "-fx-background-color: red;"
                    
                    textfield.tooltip = Tooltip(lastError)
                }
            }
            
            
        }
    }
}
//
//fun <T, R> handleProperty(
//        control: Control,
//        property: Property<T>,
//        checker: Checker<T>,
//        onSuccess: OnSuccess<Boolean>
//        
//) {
//    var lastError: String? = null
//    property.addListener { observable, oldValue, newValue ->
//        try {
//            checker(newValue)
//            // ??????
//            val nullableValue =
//                    if (newValue == "null" && prop.returnType.isMarkedNullable) null
//                    else newValue
//            
//            prop.setter.call(converter.fromString(nullableValue))
//            
//            // we fixed an error
//            lastError?.also { le ->
//                lastError = null
//                
//                println("${prop.name}: fixed error: $le")
//                
//                textfield.style = "-fx-background-color: white;"
//                textfield.tooltip = null
//            }
//            
//            onSuccess(newValue)
//        } catch (e: Exception) {
//            lastError = e.message ?: e.cause?.message ?: e.cause?.cause?.message ?: "unknown error"
//            
//            if (e is NumberFormatException)
//                lastError = "Bad Number Format: $lastError"
//            
//            println("${prop.name}: error: $lastError")
////                    e.printStackTrace()
//            
//            textfield.style = "-fx-background-color: red;"
//            
//            textfield.tooltip = Tooltip(lastError)
//        }
//    }
//    
//}
//
//
//fun EventTarget.myBoolField(
//        prop: KMutableProperty<Boolean>,
//        onSuccess: OnSuccess<Boolean> = { },
//        checker: Checker<Boolean> = { }
//) = checkbox(prop.name) {
//    
//    val checkBox = this
//    checkBox.isSelected = prop.call()
//    
//    var lastError: String? = null
//    
//    selectedProperty().addListener { observable, oldValue, newValue ->
//        try {
//            checker(newValue)
//            prop.setter.call(newValue)
//            
//            // we fixed an error
//            lastError?.also { le ->
//                lastError = null
//                
//                println("${prop.name}: fixed error: $le")
//                
//                checkBox.style = "-fx-background-color: white;"
//                checkBox.tooltip = null
//            }
//        } catch (e: Exception) {
//            lastError = e.message ?: e.cause?.message ?: e.cause?.cause?.message ?: "unknown error"
//            
//            if (e is NumberFormatException)
//                lastError = "Bad Number Format: $lastError"
//            
//            println("${prop.name}: error: $lastError")
////                    e.printStackTrace()
//            
//            checkBox.style = "-fx-background-color: red;"
//            
//            checkBox.tooltip = Tooltip(lastError)
//        }
//    }
//    
//    
//}
//}

