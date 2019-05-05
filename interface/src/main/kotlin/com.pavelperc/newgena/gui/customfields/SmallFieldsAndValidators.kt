package com.pavelperc.newgena.gui.customfields

import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

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
                !value.isLong() -> error("Not a Long.")
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
        fieldOp: Field.() -> Unit = {},
        nextValidator: Validator<Int> = { null },
        nonNegative: Boolean = false,
        positive: Boolean = false,
        validRange: IntRange? = null,
        op: TextField.() -> Unit = {}
) = field(property.name, Orientation.HORIZONTAL) {
    textfield(property, QuiteIntConverter()) {
        validInt { value ->
            when {
                nonNegative && value < 0 -> error("Should not be negative.")
                positive && value <= 0 -> error("Should be positive.")
                validRange != null && value !in validRange -> error("Should be in $validRange.")
                else -> nextValidator(value)
            }
        }
        op()
    }
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