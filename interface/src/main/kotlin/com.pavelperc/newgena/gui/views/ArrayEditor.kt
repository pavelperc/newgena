package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*


class ArrayEditor(
        initialObjects: List<String> = listOf("A", "B", "C"),
        onSuccess: (List<String>) -> Unit = {}
) : Fragment("Array Editor") {
    
    init {
//        println("Created with: $initialObjects")
    }
    
    val objects: ObservableList<String> = initialObjects.toMutableList().observable()
    
    
    override val root = vbox {
        hbox {
            addClass(Styles.addItemRoot)
            label("Add: ")
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
                addEventFilter(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.ENTER) {
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
        listview(objects) {
            isEditable = true
            cellFormat { value ->
                // -------- ONE CELL --------
                graphic = hbox {
                    addClass(Styles.itemRoot)
                    
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
                        removeWhen { parent.hoverProperty().not().or(editingProperty()) }
                        action { objects.remove(item) }
                    }
                }
            }
            
        }
    }
}
