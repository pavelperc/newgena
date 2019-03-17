package com.pavelperc.newgena.gui.customfields

import javafx.event.EventTarget
import tornadofx.*
import kotlin.reflect.KMutableProperty


// ===================
// extension builders:

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

fun <P> EventTarget.myComplexPropertyFieldSet(
        prop: KMutableProperty<P>,
        createFields: EventTarget.(complex: P) -> Unit
): MyComplexPropertyFieldSet<P> {
    val myField = MyComplexPropertyFieldSet(prop, createFields)
    myField.labelField.attachTo(this)
    return myField
}

fun EventTarget.myStringArrayField(
        prop: KMutableProperty<List<String>>,
        op: MyStringArrayField.() -> Unit = {}
): MyStringArrayField {
    val myField = MyStringArrayField(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}

fun EventTarget.myStringArrayFieldNullable(
        prop: KMutableProperty<out List<String>?>,
        op: MyStringArrayFieldNullable.() -> Unit = {}
): MyStringArrayFieldNullable {
    val myField = MyStringArrayFieldNullable(prop)
    myField.op()
    myField.labelField.attachTo(this)
    return myField
}

inline fun <reified E : Enum<E>> EventTarget.myEnumField(
        prop: KMutableProperty<E>,
        op: MyEnumField<E>.() -> Unit = {}
): MyEnumField<E> {
    val myField = MyEnumField(
            prop,
            { string -> enumValueOf<E>(string) },
            enumValues<E>().map { it.name }
    )
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

