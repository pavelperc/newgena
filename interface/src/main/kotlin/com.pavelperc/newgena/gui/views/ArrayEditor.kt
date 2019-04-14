package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.delayHack
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.Stylesheet.Companion.cell


class ArrayEditor(
        initialObjects: List<String>,
        title: String = "Array Editor",
        val onSuccess: (List<String>) -> Unit = {}
) : Fragment(title) {
    
    init {
//        println("Created with: $initialObjects")
    }
    
    val objects: ObservableList<String> = initialObjects.toMutableList().observable()
    
    
    fun EventTarget.header() {
        vbox {
            addClass(Styles.addItemRoot)
            hbox {
                label("Add: ") {
                    tooltip("Press enter inside a text field to add.") {
                        delayHack(100)
                    }
                }
                textfield {
                    promptText = "Click enter to add."
                    action {
                        val text = textProperty().value
                
                        if (text.isNotEmpty()) {
                            objects.add(text)
                            selectAll()
                        }
                    }
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
            hbox {
                alignment = javafx.geometry.Pos.CENTER_LEFT
                addClass(com.pavelperc.newgena.gui.app.Styles.sortingPanel)
                label("Sort: ")
                button("Values↓") {
                    alignment = javafx.geometry.Pos.BOTTOM_CENTER
                    action {
                        objects.sortBy { it }
                    }
                }
                button("Values↑") {
                    action {
                        objects.sortByDescending { it }
                    }
                }
            }
        }
    }
    
    override val root = vbox {
        header()
    
        style {
            padding = box(1.em, 1.em, 0.em, 1.em)
        }
        
        listview(objects) {
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    objects.remove(selectedItem)
                }
            }
            
            isEditable = true
            cellFormat { value ->
                // -------- ONE CELL --------
                graphic = hbox {
                    addClass(Styles.itemRoot)
                    upDownPanel(objects, item) {
                        removeWhen(editingProperty())
                    }
                    
                    label(itemProperty()) {
                        setId(Styles.contentLabel)
                        
                        hgrow = Priority.ALWAYS
                        useMaxSize = true
                        removeWhen { editingProperty() }
                    }
                    textfield(itemProperty()) {
                        hgrow = Priority.ALWAYS
                        removeWhen { editingProperty().not() }
                        whenVisible { requestFocus() }
                        action { commitEdit(item) }
                    }
                    button(graphic = Styles.closeIcon()) {
                        addClass(Styles.deleteButton)
//                        removeWhen { parent.hoverProperty().not().or(editingProperty()) }
                        removeWhen { editingProperty() }
                        action { objects.remove(item) }
                    }
                }
            }
            
        }
    }
}
