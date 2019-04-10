package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.QuiteIntConverter
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*


/** Allows to edit a map<String, Int> */
class IntMapEditor2(
        initialObjects: Map<String, Int>,
        title: String = "Map Editor",
        onSuccess: (Map<String, Int>) -> Unit = {}
) : Fragment(title) {
    
    
    class MutablePair(string: String, int: Int) {
        val stringProp = SimpleStringProperty(string)
        val intProp = SimpleIntegerProperty(int)
        
        operator fun component1() = stringProp.value!!
        operator fun component2() = intProp.value!!
        
        fun toPair() = stringProp.value to intProp.value
    }
    
    val objects = initialObjects.entries.map { (k, v) -> MutablePair(k, v) }.observable()
    
    
    override val root = vbox {
        hbox {
            addClass(Styles.addItemRoot)
            label("Add: ")
            textfield {
                promptText = "Click enter to add."
                action {
                    val text = textProperty().value
                    
                    if (text.isNotEmpty()) {
                        objects.add(MutablePair(text, 1))
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
                    onSuccess(objects.map { it.toPair() }.toMap())
                    close()
                }
            }
        }
        
        tableview(objects) {
            isEditable = true
            
            column("Name", MutablePair::stringProp) {
                makeEditable()
            }
            column("Number", MutablePair::intProp) {
                makeEditable()
            }
            column("Delete", MutablePair::intProp) {
                cellFormat {
                    addClass(Styles.deleteButton)
                    graphic = button {
                        graphic = Styles.closeIcon()
                        action { 
                            objects.remove(rowItem)
                        }
                    }
                }
            }
            
            
        }
    }
}