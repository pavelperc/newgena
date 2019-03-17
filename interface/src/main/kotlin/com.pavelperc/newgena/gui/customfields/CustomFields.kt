package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.views.ArrayEditor
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.*
import javafx.stage.StageStyle
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import javafx.util.converter.IntegerStringConverter
import tornadofx.*
import java.util.regex.Pattern
import kotlin.reflect.KMutableProperty


open class MyPropertyTextField<P : Any?>(
        prop: KMutableProperty<out P>,
        val converter: StringConverter<P>,
        customLabel: String? = null
) : MyPropertyLabeled<P, String>(prop, customLabel) {
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
        checkBox.isSelected = prop.call()
        
        checkBox.attachTo(labelField) {
            selectedProperty().addListener { observable, oldValue, newValue ->
                setToProp(newValue)
            }
        }
    }
}

class MyComplexPropertyFieldSet<P>(
        prop: KMutableProperty<P>,
        createFields: EventTarget.(complex: P) -> Unit
) : MyPropertyLabeled<P, Nothing>(prop) {
    val fieldSet = Fieldset()
    
    init {
        fieldSet.createFields(prop.call())
        fieldSet.attachTo(labelField)
    }
    
    override fun convertToProp(newValue: Nothing): P = TODO()
    
    override fun onFixed() {
        
    }
    
    override fun onBroken(error: String) {
        
    }
}

/**
 * [E] is an element.
 */
class ArrayStringConverter<E>(val itemConverter: StringConverter<E>) : StringConverter<List<E>?>() {
    companion object {
        val delimiterPattern = Pattern.compile("""\s*[;,]\s*""")
    }
    
    override fun toString(obj: List<E>?) = obj?.joinToString("; ") { itemConverter.toString(it) } ?: "null"
    
    override fun fromString(string: String): List<E> {
        return string.trim('[', ']', '{', '}')
                .trimIndent()
                .split(delimiterPattern)
                .map { itemConverter.fromString(it) }
    }
}


// todo: improve performance for large fields!!
open class MyArrayField<E>(
        prop: KMutableProperty<out List<E>?>,
        converter: StringConverter<E>,
        customLabel: String? = null
) : MyPropertyWrapper<List<E>?, String>(prop) {
    
    /** Using aggregation instead of extension. Hides textField and synchronization with prop. */
    val myPropertyTextField: MyPropertyTextField<List<E>?>
    
    val labelField: Field
        get() = myPropertyTextField.labelField
    
    
    val observableList = FXCollections.observableArrayList<String>()
    
    init {
        var label = prop.name + "[]"
        if (prop.returnType.isMarkedNullable)
            label += "?"
        
        myPropertyTextField = MyPropertyTextField(prop, ArrayStringConverter<E>(converter), customLabel ?: label)
        
        observableList.setAll(prop.call()?.map { converter.toString(it) }?: mutableListOf())
        
        observableList.addListener { listCangeListener: ListChangeListener.Change<out String> -> 
            myPropertyTextField.textField.text(observableList.toList().joinToString(", "))
        }
        
        val button = Button("|||")
        button.attachTo(labelField) {
            action {
                
//                ArrayEditor(observableList)
//                        .openModal(StageStyle.UTILITY)
                
//                alert(Alert.AlertType.INFORMATION, "Not implemented.")
            }
        }
    }
    
    
    override fun convertToProp(newValue: String) = myPropertyTextField.convertToProp(newValue)
    
    override fun onFixed() = myPropertyTextField.onFixed()
    override fun onBroken(error: String) = myPropertyTextField.onBroken(error)
}




// =================
// final own fields:


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

class MyStringArrayField(
        prop: KMutableProperty<out List<String>>
) : MyArrayField<String>(
        prop,
        DefaultStringConverter()
)

class MyStringArrayFieldNullable(
        prop: KMutableProperty<out List<String>?>
) : MyArrayField<String>(
        prop,
        DefaultStringConverter()
)


/** [E] - Enum */
class MyEnumField<E : Enum<E>>(
        prop: KMutableProperty<E>,
        val stringToEnum: (String) -> E,
        val enumValues: List<String>
) : MyPropertyLabeled<E, String>(prop) {
    
    val comboBox: ComboBox<String>
    
    override fun convertToProp(newValue: String): E {
        return stringToEnum(newValue)
    }
    
    override fun onBroken(error: String) {
        comboBox.style = "-fx-background-color: red;"
        comboBox.tooltip = Tooltip(error)
    }
    
    override fun onFixed() {
        comboBox.style = "-fx-background-color: white;"
        comboBox.tooltip = null
    }
    
    init {
        val list = enumValues.observable()
        val selectedProperty = SimpleStringProperty()
        selectedProperty.set(prop.call().name)
        
        comboBox = ComboBox()
        comboBox.attachTo(labelField) {
            items = list
            bind(selectedProperty)
        }
        
        selectedProperty.onChange { newValue ->
            setToProp(newValue ?: "")
        }
    }
    
    
}