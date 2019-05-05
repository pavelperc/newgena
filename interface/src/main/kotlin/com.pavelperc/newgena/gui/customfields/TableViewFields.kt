package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.app.Styles
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.util.Callback
import javafx.util.StringConverter
import tornadofx.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * Custom column builder.
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
        op: TextField.() -> Unit = {},
        /** just an optimization for KProp. */
        rawItemGetter: ((row: S) -> T)? = { row -> itemGetter(row).value }
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
                return tableView.items
                        .map { raw -> itemGetter(raw).value }
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
        op,
        { row -> itemProp.call(row) } // just an optimization for itemGetter
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
//        prefWidth = width
        fixedWidth(80.0)
        
        
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
                
                setOnEditStart {
                    fire()
                }
            }
        }
        this@makeDeleteColumn.columns += this
    }
}