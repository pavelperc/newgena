package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.actionedAutoCompletion
import com.pavelperc.newgena.gui.customfields.delayHack
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.controlsfx.bindAutoCompletion


class ArrayEditor(
        initialObjects: List<String>,
        title: String = "Array Editor",
        val predefinedValuesToHints: Map<String, String?> = mutableMapOf(),
        val hintName: String = "hint",
        val onSuccess: (List<String>) -> Unit = {}
) : Fragment(title) {
    
    private val predefinedHintsToValues = predefinedValuesToHints
            .filter { (_, v) -> v != null && v.isNotEmpty() }
            .map { (k, v) -> v!! to k }
            .toMap()
    
    init {
//        println("Created with: $initialObjects")
    }
    
    val objects: ObservableList<String> = initialObjects.toMutableList().observable()
    
    val showHint = SimpleBooleanProperty(predefinedValuesToHints.size > 0)
    
    // ---HEADER:---
    fun EventTarget.header() {
        vbox {
            addClass(Styles.addItemRoot)
            hbox {
                label("Add: ") {
                    tooltip("Press enter inside a text field to add.") {
                        delayHack(100)
                    }
                }
                
                val valueProp = SimpleStringProperty("")
                val hintProp = SimpleStringProperty("")
                
                // value
                val tfValue = textfield(valueProp) {
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    prefWidth = 150.0
                    
                    promptText = "Click enter to add."
                    action {
                        println("ACTION!")
                        val text = valueProp.value
                        if (text.isNotEmpty()) {
                            objects.add(text)
                            selectAll()
                        }
                    }
                    
                    actionedAutoCompletion(predefinedValuesToHints.keys.toList())
                }
                
                valueProp.onChange { text ->
                    if (text in predefinedValuesToHints) {
                        hintProp.value = predefinedValuesToHints[text] ?: ""
                    }
                }
                hintProp.onChange { hint ->
                    if (hint in predefinedHintsToValues) {
                        valueProp.value = predefinedHintsToValues[hint]
                    }
                }
                
                // hint
                textfield(hintProp) {
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    prefWidth = 150.0
                    
                    removeWhen { showHint.not() }
                    promptText = "Search by $hintName"
                    action {
                        val text = valueProp.value
                        if (text.isNotEmpty()) {
                            objects.add(text)
                            selectAll()
                            tfValue.selectAll()
                        }
                    }
                    
                    actionedAutoCompletion(predefinedHintsToValues.keys.toList())
                }
                
                
                button("save") {
                    shortcut("Ctrl+S")
                    tooltip("Ctrl+S")
                    addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) {
                        if (it.code == javafx.scene.input.KeyCode.ENTER) {
//                        if (it.target is Button && !it.isControlDown)
//                            return@addEventFilter
                            fire()
                        }
                    }
                    action {
                        onSuccess(objects)
                        close()
                    }
                }
            }
            
            if (predefinedValuesToHints.size > 0) {
                checkbox("Show $hintName", showHint)
            }
            
            // Sorting:
            hbox {
                alignment = Pos.CENTER_LEFT
                addClass(com.pavelperc.newgena.gui.app.Styles.sortingPanel)
                label("Sort: ")
                button("Values↓") {
                    //                    alignment = Pos.BOTTOM_CENTER
                    action {
                        objects.sortBy { it }
                    }
                }
                button("Values↑") {
                    action {
                        objects.sortByDescending { it }
                    }
                }
                button("${hintName}↓") {
                    removeWhen { showHint.not() }
                    action {
                        objects.sortBy { predefinedValuesToHints[it] ?: "" }
                    }
                }
                button("${hintName}↑") {
                    removeWhen { showHint.not() }
                    action {
                        objects.sortByDescending { predefinedValuesToHints[it] ?: "" }
                    }
                }
            }
        }
    }
    
    override val root = vbox {
        header()
        style {
            //            backgroundColor += Color.RED
            padding = box(1.em)
        }
        hgrow = Priority.ALWAYS
        
        listview(objects) {
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    objects.remove(selectedItem)
                }
            }
            
            isEditable = true
            cellFormat {
                oneCell()
            }
            
            useMaxSize = true
            vgrow = Priority.ALWAYS
        }
    }
    
    // -------- ONE CELL --------
    private fun ListCell<String>.oneCell() {
        graphic = hbox {
            addClass(Styles.itemRoot)
            upDownPanel(objects, item) {
                removeWhen(editingProperty())
            }
            
            // setup a hint
            val hintProp = SimpleStringProperty(predefinedValuesToHints[item] ?: "")
            itemProperty().onChange { value ->
                if (value in predefinedValuesToHints) {
                    hintProp.value = predefinedValuesToHints[value] ?: ""
                }
            }
            hintProp.onChange { hint ->
                if (hint in predefinedHintsToValues) {
                    itemProperty().value = predefinedHintsToValues[hint]
                }
            }
            
            // look:
            hbox {
                alignment = Pos.CENTER
                hgrow = Priority.ALWAYS
                removeWhen { editingProperty() }
                style {
                    padding = box(0.px, 10.px, 0.px, 5.px)
                }
                
                label(itemProperty()) {
                    setId(Styles.contentLabel)
                    hgrow = Priority.ALWAYS
                    useMaxSize = true
                }
                label(hintProp) {
                    paddingLeft = 5
                    
                    removeWhen { showHint.not() }
//                    useMaxSize = true
//                    hgrow = Priority.ALWAYS
                    setId(Styles.contentLabel)
                }
            }
            
            // edit
            valueHintEditor(this@oneCell, hintProp)
            
            // delete
            button(graphic = Styles.closeIcon()) {
                addClass(Styles.deleteButton)
//                        removeWhen { parent.hoverProperty().not().or(editingProperty()) }
                removeWhen { editingProperty() }
                action { objects.remove(item) }
            }
        }
    }
    
    fun EventTarget.valueHintEditor(cell: ListCell<String>, hintProp: StringProperty) {
        hbox {
            alignment = Pos.CENTER
            hgrow = Priority.ALWAYS
            removeWhen { cell.editingProperty().not() }
            
            // value
            val tfValue = textfield(cell.itemProperty()) {
                hgrow = Priority.ALWAYS
                useMaxWidth = true
                prefWidth = 100.0
                
                action { cell.commitEdit(cell.item) }
                promptText = "Edit value."
                
                actionedAutoCompletion(predefinedValuesToHints.keys.toList())
                
                
            }
            
            whenVisible { tfValue.requestFocus() }
            
            // hint
            textfield(hintProp) {
                prefWidth = 100.0
                useMaxWidth = true
                hgrow = Priority.ALWAYS
                promptText = "Search by $hintName"
                
                removeWhen { showHint.not() }
                
                action { cell.commitEdit(cell.item) }
                actionedAutoCompletion(predefinedHintsToValues.keys.toList())
            }
        }
    }
}
