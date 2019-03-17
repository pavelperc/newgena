package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import javafx.collections.ObservableList
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
                action {
                    promptText = "Click enter to add."
                    
                    val text = textProperty().value
                    objects.add(text)
                    selectAll()
                }
            }
            button("save") {
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
