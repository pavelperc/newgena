package com.pavelperc.newgena.gui.customfields

import com.pavelperc.newgena.gui.views.ArrayEditor
import com.pavelperc.newgena.gui.views.IntMapEditor
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.*

fun EventTarget.arrayField(
        listProp: Property<MutableList<String>>,
        predefinedValuesToHints: () -> Map<String, String?> = { emptyMap() },
        hintName: String = "hint",
        listValidator: Validator<List<String>> = { null },
        valuesName: String = "values"
) {
    docField(listProp.name) {
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
                        hintName,
                        valuesName
                ) { changedObjects ->
                    // set to textProp, textProp sets to list
                    textProp.value = changedObjects.joinToString("; ")
                }

//                    arrayEditor.openWindow(resizable = false)
                arrayEditor.openWindow(escapeClosesWindow = false)
            }
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
        valuesName: String = "value",
        mapValidator: ValidationContext.(Map<String, Int>) -> ValidationMessage? = { null }
) {
    docField(mapProp.name) {
        
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
                        fillDefaultButton,
                        valuesName
                ) { changedObjects ->
                    // set to textProp, textProp sets to mapProp
                    textProp.value = changedObjects.makeString()
                }
                
                intMapEditor.openWindow(escapeClosesWindow = false)
            }
        }
    }
}