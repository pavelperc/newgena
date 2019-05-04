package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.app.Styles
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
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Callback
import org.controlsfx.control.textfield.TextFields
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class QuiteIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String?) = string?.toIntOrNull() ?: 0
}

object QuiteLongConverter : StringConverter<Long>() {
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

/** Validator. The same as [required], but with the next validator. */
fun TextInputControl.notEmpty(
        nextValidator: Validator<String> = { null }
) {
    this.validator { value ->
        when {
            value.isNullOrEmpty() -> error("Should not be empty.")
            else -> nextValidator(value)
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
    textfield(longProp, QuiteLongConverter) {
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

private class LongSpinnerValueFactory(val range: LongRange, val initialValue: Long) : SpinnerValueFactory<Long>() {
    val min = range.first
    val max = range.endInclusive
    
    init {
        converter = QuiteLongConverter
        valueProperty().addListener { o, oldValue, newValue ->
            if (newValue < min) {
                value = min
            } else if (newValue > max) {
                value = max
            }
        }
        value = if (initialValue in range) initialValue else min
    }
    
    override fun decrement(steps: Int) {
        val newIndex = value - steps
        value = if (newIndex >= min) newIndex else min
    }
    
    override fun increment(steps: Int) {
        val newIndex = value + steps
        value = if (newIndex <= max) newIndex else max
    }
}


fun EventTarget.longSpinner(
        prop: Property<Long>,
        valueRange: LongRange = Long.MIN_VALUE..Long.MAX_VALUE,
        style: String = Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL,
        op: Spinner<Long>.() -> Unit = {}
) {
    spinner(LongSpinnerValueFactory(valueRange, prop.value)) {
        valueFactory.valueProperty().bindBidirectional(prop)
        styleClass.add(style)
        isEditable = true
        editor.textProperty().onChange { text ->
            if (text?.isLong() == true) {
                prop.value = text.toLong()
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


fun EventTarget.longSpinnerField(
        prop: Property<Long>,
        valueRange: LongRange = Long.MIN_VALUE..Long.MAX_VALUE,
        style: String = Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL,
        op: Spinner<Long>.() -> Unit = {}
) = field(prop.name) {
    longSpinner(prop, valueRange, style, op)
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
        nonNegative: Boolean = false,
        fieldOp: Field.() -> Unit = {},
        nextValidator: Validator<Long> = { null },
        op: TextField.() -> Unit = {}
) = field(property.name, Orientation.HORIZONTAL) {
    textfield(property, QuiteLongConverter) {
        validator { newString ->
            when {
                newString.isNullOrEmpty() -> error("Should not be empty.")
                !newString.isLong() -> error("Not a Long.")
                nonNegative && newString.toLong() < 0L -> error("Should not be negative.")
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
        fillDefaultButton: Boolean = false,
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
                            hintName,
                            fillDefaultButton
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

/** Read only text field with converter. */
fun <T> EventTarget.readOnlyTextField(prop: Property<T>, converter: (T) -> String, op: TextField.() -> Unit = {}) {
    val tf = textfield(converter(prop.value)) {
        hgrow = Priority.ALWAYS
        isEditable = false
        style {
            backgroundColor += Color.TRANSPARENT
        }
    }
    prop.onChange { newValue ->
        tf.text = newValue?.let { converter(it) } ?: ""
    }
    
}

// --- TABLEVIEW ---

/**
 * This fun requires itemGetter and columnName.
 * It has public overrides with KProp and javafx property as [itemGetter].
 */
private fun <S, T> TableView<S>.validatedColumnItemGetter(
        itemGetter: (row: S) -> Property<T>,
        converter: StringConverter<T>,
        columnName: String,
        required: Boolean = true,
        allowDuplicates: Boolean = true,
        validator: Validator<String> = { null },
        op: TextField.() -> Unit = {}
) {
    // default column and label builders are inline and can not simply infer type T
    // this is copied from column builder:
    val column = TableColumn<S, T>(columnName)
    column.cellValueFactory = Callback { itemGetter(it.value) }
    addColumnInternal(column)
    
    with(column) {
        cellFormat {
            val validationContext = ValidationContext()
            val tempProp = SimpleObjectProperty(item)
            
            fun isDuplicate(newString: String): Boolean {
                val newValue = converter.fromString(newString)
                val objects = tableView.items
                return objects
                        .map { itemGetter(it).value }
                        .any { it == newValue && it != item }
            }
            
            graphic = vbox {
                // edit:
                textfield(tempProp, converter) {
                    removeWhen(editingProperty().not())
                    validationContext.addValidator(this) { newString ->
                        when {
                            newString == null -> error("Null.")
                            required && newString.isEmpty() -> error("Should not be empty.")
                            !allowDuplicates && isDuplicate(newString) -> error("Duplicate.")
                            else -> validator(newString)
                        }
                    }
                    // Call cell.commitEdit() only if validation passes
                    action {
                        if (validationContext.isValid) {
                            commitEdit(tempProp.value)
                        }
                    }
                    
                    // jump on value when editing starts.
                    whenVisible { requestFocus() }
                    op()
                }
                
                // look:
                label(converter.toString(item)) {
                    itemProperty().onChange { newItem ->
                        text = converter.toString(newItem)
                    }
                    removeWhen(editingProperty())
                }
            }
        }
    }
}


fun <S, T> TableView<S>.validatedColumn(
        itemProp: KMutableProperty1<S, T>,
        converter: StringConverter<T>,
        columnName: String = itemProp.name,
        required: Boolean = true,
        allowDuplicates: Boolean = true,
        validator: Validator<String> = { null },
        op: TextField.() -> Unit = {}
) = validatedColumnItemGetter(
        { row -> observable(row, itemProp) },
        converter,
        columnName,
        required,
        allowDuplicates,
        validator,
        op
)


fun <S> TableView<S>.validatedLongColumn(
        itemProp: KMutableProperty1<S, Long>,
        columnName: String = itemProp.name,
        nonNegative: Boolean = false,
        nextValidator: Validator<Long> = { null },
        op: TextField.() -> Unit = {}
) = validatedColumn(itemProp, QuiteLongConverter, columnName, true, true, { newString ->
    when {
        !newString.isLong() -> error("Not a Long.")
        nonNegative && newString.toLong() < 0L -> error("Should not be negative.")
        else -> nextValidator(newString.toLong())
    }
}, op)

fun <S, T> TableView<S>.validatedColumnProp(
        itemProp: KProperty1<S, Property<T>>,
        converter: StringConverter<T>,
        columnName: String = itemProp.name,
        required: Boolean = true,
        allowDuplicates: Boolean = true,
        validator: Validator<String> = { null },
        op: TextField.() -> Unit = {}
) = validatedColumnItemGetter(
        itemProp, // this is substituted as KProperty1<S, Property<T>> !!!!
        converter,
        columnName,
        required,
        allowDuplicates,
        validator,
        op
)


fun <S> TableView<S>.validatedLongColumnProp(
        itemProp: KProperty1<S, Property<Long>>,
        columnName: String = itemProp.name,
        nonNegative: Boolean = false,
        nextValidator: Validator<Long> = { null },
        op: TextField.() -> Unit = {}
) = validatedColumnProp(itemProp, QuiteLongConverter, columnName, true, true, { newString ->
    when {
        !newString.isLong() -> error("Not a Long.")
        nonNegative && newString.toLong() < 0L -> error("Should not be negative.")
        else -> nextValidator(newString.toLong())
    }
}, op)


fun <S> TableView<S>.makeDeleteColumn(): TableColumn<S, Any?> {
    return TableColumn<S, Any?>("Delete").apply {
        isSortable = false
        prefWidth = width
        
        // if we use setCellFactory, it created infinite row of buttons.
        setCellValueFactory { SimpleObjectProperty(Any()) }
        
        cellFormat {
            style {
                alignment = Pos.CENTER
            }
            graphic = button {
                style {
                    backgroundColor += Color.TRANSPARENT
                }
                graphic = Styles.closeIcon()
                action {
                    tableView.items.removeAt(index)
                }
            }
        }
        this@makeDeleteColumn.columns += this
    }
}