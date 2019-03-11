package com.pavelperc.newgena.gui.customfields

import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Tooltip
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import javafx.util.converter.IntegerStringConverter
import tornadofx.Field
import tornadofx.checkbox
import tornadofx.field
import tornadofx.textfield
import kotlin.reflect.KMutableProperty

fun <T : String?> EventTarget.myStringField(
        prop: KMutableProperty<T>
) = myTextField(prop, DefaultStringConverter())

fun EventTarget.myIntField(
        prop: KMutableProperty<Int>
) = myTextField(prop, IntegerStringConverter())


typealias Checker<T> = (newValue: T) -> Unit
typealias OnSuccess<T> = (newValue: T) -> Unit

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

