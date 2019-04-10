package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.views.ArrayEditor
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import javafx.util.Duration
import javafx.util.StringConverter
import org.controlsfx.control.Notifications
import tornadofx.*


class QuiteIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String) = if (string.isInt()) string.toInt() else 0
}

typealias Validator<T> = ValidationContext.(T) -> ValidationMessage?

fun TextInputControl.validInt(
        nextValidator: Validator<Int> = { null }
) {
    this.validator { value ->
        when {
            value == null -> error("Null")
            !value.isInt() -> error("Not an Int")
            else -> nextValidator(value.toInt())
        }
    }
    
}

fun TextInputControl.validUint(
        nextValidator: Validator<Int> = { null }
) {
    validInt { value ->
        when {
            value < 0 -> error("Should be positive")
            else -> nextValidator(value)
        }
    }
}

fun TextInputControl.validRangeInt(
        intRange: IntRange,
        nextValidator: Validator<Int> = { null }
) {
    validInt { value ->
        when {
            value !in intRange -> error("Should be in $intRange")
            else -> nextValidator(value)
        }
    }
}


fun Form.scrollablefieldset(op: EventTarget.() -> Unit) {
    scrollpane {
        form {
            fieldset {
                //        prefHeightProperty().bind(scrollPane.heightProperty()) 
//        prefWidthProperty().bind(scrollPane.widthProperty())
                op()
            }
        }
    }
}

/** Int validators for textfield are not included!! add them in [op] lambda. */
fun EventTarget.intField(
        property: Property<Int>,
//        sliderRange: IntRange? = null,
        fieldOp: Field.() -> Unit = {},
        op: TextField.() -> Unit = {}
) = field(property.name, Orientation.HORIZONTAL) {
    textfield(property, QuiteIntConverter(), op)
//    slider(1..100, property.value) {
//        blockIncrement = 1.0
//        valueProperty().bindBidirectional(property as IntegerProperty)
//    }
    fieldOp()
}

fun EventTarget.checkboxField(property: Property<Boolean>, op: CheckBox.() -> Unit = {}) =
        field(property.name) {
            checkbox(property = property, op = op)
        }

fun EventTarget.arrayField(listProp: Property<ObservableList<String>>, op: TextField.() -> Unit = {}) =
        field(listProp.name) {
            val textProp = SimpleStringProperty(listProp.value.joinToString("; "))
            
            // bind bidirectional listProp and textProp:
            listProp.onChange { list ->
                textProp.value = list?.joinToString("; ") ?: ""
            }
            
            textProp.onChange { value ->
                val splitted = (value ?: "")
                        .trim('[', ']', '{', '}', ';', ',')
                        .split(';', ',')
                        .map { it.trimIndent() }
                        .filter { it.isNotEmpty() }
                        .toMutableList()
                // don't replace the whole list!
                listProp.value.setAll(splitted)
//                println("New listProp: ${listProp.value.toList()}")
            }
            textfield(textProp, op)
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                isFocusTraversable = false
                action {
                    // make a copy of the list, because we can cancel editing.
                    val arrayEditor = ArrayEditor(listProp.value.toList(), onSuccess = { changedObjects ->
                        // set to textProp, textProp sets to list
                        textProp.value = changedObjects.joinToString("; ")
                    })
                    
                    arrayEditor.openModal()
                }
            }
        }

//fun <A, B> Property<A>.bindWithConverter(other: Property<B>, toOther: (me: A) -> B, fromOther: (he: B) -> A) {
//    // recursion????
//    this.onChange { changed ->
//        other.value = toOther(changed!!)
//    }
//    other.onChange { changed ->
//        this.value = fromOther(changed!!)
//    }
//}


fun UIComponent.notification(
        title: String = "",
        text: String = "",
        duration: Int = 1500,
        op: Notifications.() -> Unit = {}
) {
    val builder = Notifications.create()
    builder.owner(this.root)
    builder.title(title)
    builder.text(text)
    builder.position(Pos.TOP_CENTER)
    builder.hideAfter(Duration(duration.toDouble()))
    
    builder.op()
    builder.show()
}


fun confirmed(
        header: String,
        content: String = "",
        confirmButton: ButtonType = ButtonType.OK,
        cancelButton: ButtonType = ButtonType.CANCEL,
        owner: Window? = null,
        title: String? = null
): Boolean {
    alert(Alert.AlertType.CONFIRMATION, header, content, confirmButton, cancelButton, owner = owner, title = title) {
        return it == confirmButton
    }
    return false // not clicked at all
}

inline fun confirmIf(
        condition: Boolean,
        header: String,
        content: String = "",
        confirmButton: ButtonType = ButtonType.OK,
        cancelButton: ButtonType = ButtonType.CANCEL,
        owner: Window? = null,
        title: String? = null,
        actionFn: () -> Unit
) {
    if (condition) {
        if (confirmed(header, content, confirmButton, cancelButton, owner, title)) {
            actionFn()
        }
    } else {
        actionFn()
    }
}


