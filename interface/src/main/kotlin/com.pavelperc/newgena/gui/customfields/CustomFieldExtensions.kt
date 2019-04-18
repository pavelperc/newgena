package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.views.ArrayEditor
import com.pavelperc.newgena.gui.views.IntMapEditor
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
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
import javafx.beans.property.IntegerProperty
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane


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


/** Just a Long textField, which is not bound to ItemViewModel. */
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

fun EventTarget.checkboxField(property: Property<Boolean>, op: CheckBox.() -> Unit = {}) =
        field(property.name) {
            checkbox(property = property, op = op)
        }

fun EventTarget.arrayField(listProp: Property<MutableList<String>>, op: TextField.() -> Unit = {}) =
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
                // replace the whole list!
                listProp.value = splitted
            }
            textfield(textProp, op)
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                isFocusTraversable = false
                action {
                    // make a copy of the list, because we can cancel editing.
                    val arrayEditor = ArrayEditor(listProp.value.toList(), listProp.name + " editor") { changedObjects ->
                        // set to textProp, textProp sets to list
                        textProp.value = changedObjects.joinToString("; ")
                    }
                    
                    arrayEditor.openWindow(resizable = false)
                }
            }
        }


val positiveRange = 1..Int.MAX_VALUE
val nonNegativeRange = 0..Int.MAX_VALUE

fun EventTarget.intMapField(
        mapProp: Property<MutableMap<String, Int>>,
        intValueRange: IntRange = positiveRange,
        op: TextField.() -> Unit = {},
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
            val textProp = SimpleStringProperty(viewModel, null, mapProp.value.makeString())
            
            // this flag helps to prevent loop in validator and prop callback.
            // Which leads sometimes to strange internal exception.
            var changedPropFromValidator = false
            
            // bind bidirectional mapProp and textProp:
            mapProp.onChange { map ->
                if (!changedPropFromValidator) {
                    textProp.value = map?.makeString() ?: ""
                } else {
                    changedPropFromValidator = false // caught.
                }
            }
            
            // we update map property inside a text property validator!!!
            textfield(textProp) {
                
                // it's like onChange, but better!
                validator { newString ->
                    val splitted = try {
                        splitString(newString ?: "")
                    } catch (e: IllegalArgumentException) {
                        return@validator error(e.message)
                    }
                    
                    changedPropFromValidator = true
                    // replace the whole map!
                    mapProp.value = splitted.toMutableMap()
                    
                    // end with another validator.
                    mapValidator(splitted)
                }
                op()
            }
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                isFocusTraversable = false
                action {
                    val intMapEditor = IntMapEditor(mapProp.value, mapProp.name + " editor", intValueRange) { changedObjects ->
                        // set to textProp, textProp sets to mapProp
                        textProp.value = changedObjects.makeString()
                    }
                    
                    intMapEditor.openWindow(resizable = false)
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


