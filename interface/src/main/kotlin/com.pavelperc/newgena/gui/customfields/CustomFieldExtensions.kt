package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.views.ArrayEditor
import com.pavelperc.newgena.gui.views.IntMapEditor
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.stage.Window
import javafx.util.Duration
import javafx.util.StringConverter
import org.controlsfx.control.Notifications
import tornadofx.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.controlsfx.control.textfield.TextFields


class QuiteIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String?) = string?.toIntOrNull() ?: 0
}

class QuiteLongConverter : StringConverter<Long>() {
    override fun toString(obj: Long?) = obj.toString()
    override fun fromString(string: String?) = string?.toLongOrNull() ?: 0
}



typealias Validator<T> = ValidationContext.(T) -> ValidationMessage?

fun TextInputControl.validInt(
        nextValidator: Validator<Int> = { null }
) {
    this.validator { value ->
        when {
            value == null -> error("Null.")
            !value.isInt() -> error("Not an Int.")
            else -> nextValidator(value.toInt())
        }
    }
    
}

fun TextInputControl.validUint(
        nextValidator: Validator<Int> = { null }
) {
    validInt { value ->
        when {
            value < 0 -> error("Should not be negative.")
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


fun Pane.scrollablefieldset(op: EventTarget.() -> Unit) {
    scrollpane {
        val scrollPane = this
        form {
            alignment = Pos.CENTER
            fieldset {
                alignment = Pos.CENTER
                //        prefHeightProperty().bind(scrollPane.heightProperty()) 
//        prefWidthProperty().bind(scrollPane.prefViewportWidthProperty())
//        prefWidthProperty().bind(scrollPane.minViewportWidthProperty())
                scrollPane.isFitToWidth = true
                op()
            }
        }
    }
}


/** Just a Long textField outside a fieldset, which is not bound to ItemViewModel. */
fun EventTarget.simpleLongField(
        longProp: Property<Long>,
        validationContext: ValidationContext = ValidationContext(),
        nextValidator: Validator<Long> = { null }
) {
    textfield(longProp, QuiteLongConverter()) {
        validationContext.addValidator(this, ValidationTrigger.OnChange()) { value ->
            when {
                value == null -> error("Null")
                !value.isLong() -> error("Not a Long")
                else -> nextValidator(value.toLong())
            }
        }
    }
}

fun EventTarget.intSpinner(
        intProp: Property<Int>,
        intValueRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
        style: String = Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL,
        op: Spinner<Int>.() -> Unit = {}
) {
    spinner(SpinnerValueFactory.IntegerSpinnerValueFactory(intValueRange.first, intValueRange.last, intProp.value)) {
        valueFactory.valueProperty().bindBidirectional(intProp)
        styleClass.add(style)
        isEditable = true
        editor.textProperty().onChange { text ->
            if (text?.isInt() == true) {
                intProp.value = text.toInt()
            }
        }
        op()
    }
}

fun EventTarget.intSpinnerField(
        intProp: Property<Int>,
        intValueRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
        style: String = Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL,
        op: Spinner<Int>.() -> Unit = {}
) = field(intProp.name) {
    intSpinner(intProp, intValueRange, style, op)
}


/** A field for fieldset for int property. Field name is taken from property.
 * Int validators for textfield are not included!! add them in [op] lambda. */
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

/** A field for fieldset for long property. Field name is taken from property.
 * Add int validators in [nextValidator]  */
fun EventTarget.longField(
        property: Property<Long>,
//        sliderRange: IntRange? = null,
        fieldOp: Field.() -> Unit = {},
        op: TextField.() -> Unit = {},
        nextValidator: Validator<Long> = { null }
) = field(property.name, Orientation.HORIZONTAL) {
    textfield(property, QuiteLongConverter()) {
        validator { newString ->
            when {
                newString.isNullOrEmpty() -> error("Should not be empty.")
                !newString.isLong() -> error("Not a Long.")
                else -> nextValidator(newString.toLong())
            }
        }
        op()
    }
    fieldOp()
}


fun EventTarget.checkboxField(property: Property<Boolean>, op: CheckBox.() -> Unit = {}) =
        field(property.name) {
            checkbox(property = property, op = op)
        }


fun EventTarget.arrayField(
        listProp: Property<MutableList<String>>,
        predefinedValuesToHints: () -> Map<String, String?> = { emptyMap() },
        hintName: String = "hint",
        listValidator: Validator<List<String>> = { null }
) =
        field(listProp.name) {
            
            val viewModel = listProp.viewModel
            // textProp is now bound to ViewModel, so we can add a validator.
            val textProp = SimpleStringProperty(viewModel, "${listProp.name}_text", listProp.value.joinToString("; "))
            
            
            // bind bidirectional listProp and textProp:
            listProp.onChange { list ->
                textProp.value = list?.joinToString("; ") ?: ""
            }
            
            textProp.onChange { newString ->
                val splitted = (newString ?: "")
                        .trim('[', ']', '{', '}', ';', ',')
                        .split(';', ',')
                        .map { it.trimIndent() }
                        .filter { it.isNotEmpty() }
                        .toMutableList()
                // replace the whole list!
                listProp.value = splitted
            }
            
            textfield(textProp) {
                
                // it's like onChange, but better!
                validator { newString ->
                    //                    println("Validating ${textProp.name}: \"$newString\"")
                    
                    val splitted = (newString ?: "")
                            .trim('[', ']', '{', '}', ';', ',')
                            .split(';', ',')
                            .map { it.trimIndent() }
                            .filter { it.isNotEmpty() }
                            .toMutableList()
                    
                    // end with external validator.
                    listValidator(splitted)
                }
            }
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                isFocusTraversable = false
                action {
                    // make a copy of the list, because we can cancel editing.
                    val arrayEditor = ArrayEditor(
                            listProp.value.toList(),
                            listProp.name + " editor",
                            predefinedValuesToHints(),
                            hintName
                    ) { changedObjects ->
                        // set to textProp, textProp sets to list
                        textProp.value = changedObjects.joinToString("; ")
                    }

//                    arrayEditor.openWindow(resizable = false)
                    arrayEditor.openWindow(escapeClosesWindow = false)
                }
            }
        }

/** Fires onAction after completion. */
fun <T> TextField.actionedAutoCompletion(suggestions: List<T>) {
    TextFields.bindAutoCompletion(this, suggestions).apply {
        onAutoCompleted = EventHandler {
            this@actionedAutoCompletion.fireEvent(ActionEvent())
        }
    }
}


val positiveRange = 1..Int.MAX_VALUE
val nonNegativeRange = 0..Int.MAX_VALUE

fun EventTarget.intMapField(
        mapProp: Property<MutableMap<String, Int>>,
        intValueRange: IntRange = positiveRange,
        predefinedValuesToHints: () -> Map<String, String?> = { emptyMap() },
        hintName: String = "hint",
        mapValidator: ValidationContext.(Map<String, Int>) -> ValidationMessage? = { null }
) =
        field(mapProp.name) {
            
            fun Map<String, Int>.makeString() = this.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            
            fun splitString(string: String) = string
                    .trim('[', ']', '{', '}', ';', ',', ' ')
                    .split(';', ',')
                    .filter { it.isNotBlank() }
                    .map {
                        val kv = it.split(":")
                        val k = kv.first().trim(' ')
                        val v = kv.getOrNull(1)?.trim(' ')?.toIntOrNull()
                                ?: throw IllegalArgumentException("Value $it doesn't match pattern \"string:int\" .")
                        k to v
                    }.toMap()
            
            val viewModel = mapProp.viewModel
            // textProp is now bound to ViewModel, so we can add a validator.
            val textProp = SimpleStringProperty(viewModel, "${mapProp.name}_text", mapProp.value.makeString())
            
            
            // bind bidirectional mapProp and textProp:
            mapProp.onChange { map ->
                textProp.value = map?.makeString() ?: ""
            }
            
            textProp.onChange { newString ->
                try {
                    val splitted = splitString(newString ?: "")
                    // replace the whole map!
                    mapProp.value = splitted.toMutableMap()
                    
                } catch (e: IllegalArgumentException) {
                }
            }
            
            textfield(textProp) {
                // it's like onChange, but better!
                validator { newString ->
                    val splitted = try {
                        splitString(newString ?: "")
                    } catch (e: IllegalArgumentException) {
                        // split error
                        return@validator error(e.message)
                    }
                    
                    // end with external validator.
                    mapValidator(splitted)
                }
            }
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                isFocusTraversable = false
                action {
                    val intMapEditor = IntMapEditor(
                            mapProp.value,
                            mapProp.name + " editor",
                            intValueRange,
                            predefinedValuesToHints(),
                            hintName
                    ) { changedObjects ->
                        // set to textProp, textProp sets to mapProp
                        textProp.value = changedObjects.makeString()
                    }
                    
                    intMapEditor.openWindow(escapeClosesWindow = false)
                }
            }
        }

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

fun Tooltip.delayHack(delayInMillis: Int) {
    try {
        val fieldBehavior = this.javaClass.getDeclaredField("BEHAVIOR")
        fieldBehavior.isAccessible = true
        val objBehavior = fieldBehavior.get(this)
        
        val fieldTimer = objBehavior.javaClass.getDeclaredField("activationTimer")
        fieldTimer.isAccessible = true
        val objTimer = fieldTimer.get(objBehavior) as Timeline
        
        objTimer.getKeyFrames().clear()
        objTimer.getKeyFrames().add(KeyFrame(Duration(delayInMillis.toDouble())))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
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


