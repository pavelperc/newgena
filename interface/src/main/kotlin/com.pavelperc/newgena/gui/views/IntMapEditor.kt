package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.QuiteIntConverter
import com.pavelperc.newgena.gui.customfields.delayHack
import com.pavelperc.newgena.gui.customfields.intSpinner
import com.pavelperc.newgena.gui.customfields.simpleIntField
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import tornadofx.*
import javax.swing.text.Style


private typealias SP = SimpleStringProperty
private typealias IP = SimpleIntegerProperty
private typealias SIPair = Pair<String, Int>

/** Allows to edit a map<String, Int> */
class IntMapEditor(
        initialObjects: Map<String, Int>,
        title: String = "Map Editor",
        val intValueRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
        val onSuccess: (Map<String, Int>) -> Unit = {}
) : Fragment(title) {
    class MutablePair(string: String, int: Int) {
        val stringProp = SimpleStringProperty(string)
        val intProp = SimpleIntegerProperty(int) as Property<Int>
        
        operator fun component1() = stringProp
        operator fun component2() = intProp
        
        fun toPair() = stringProp.value to intProp.value
    }

//    val errorCounter = SimpleIntegerProperty(0)
    
    
    val objects = initialObjects.entries.map { (k, v) -> MutablePair(k, v) }.observable()
    
    override val root = vbox {
        addClass(Styles.addItemRoot)
        
        header()
        
        listview(objects) {
            //            fitToWidth(this@vbox)
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    objects.remove(selectedItem)
                }
            }
            
            cellFormat { (stringProp, intProp) ->
                // -------- ONE CELL --------
                graphic = hbox {
                    addClass(Styles.intMapEditorItem)
                    textfield(stringProp) {
                        hgrow = Priority.ALWAYS
                    }
                    
                    intSpinner(intProp, intValueRange) {
                        maxWidth = 100.0
                    }
                    
                    button(graphic = Styles.closeIcon()) {
                        addClass(Styles.intMapDeleteButton)
                        
                        isFocusTraversable = false
                        action { objects.remove(item) }
                    }
                }
            }
            
        }
    }
    
    fun EventTarget.header() {
        hbox {
            label("Add: ") {
                tooltip("Press enter inside a text field to add.") {
                    delayHack(100)
                }
            }
            var canAdd = true
            val lastNumber = SimpleIntegerProperty(1) as Property<Int>
            val lastText = SimpleStringProperty("")
            // counts errors in lastNumber textfield
            val localErrorCounter = SimpleIntegerProperty(0)
            
            fun commit(): Boolean {
                if (lastText.value.isNotEmpty() && localErrorCounter.value == 0) {
                    objects.add(MutablePair(lastText.value, lastNumber.value))
                    lastNumber.value = 1
                    return true
                }
                return false
            }
            
            textfield(lastText) {
                promptText = "Click enter to add."
                hgrow = Priority.ALWAYS
                action {
                    if (commit()) {
                        selectAll()
                    }
                }
            }
            intSpinner(lastNumber, intValueRange) {
                maxWidth = 100.0
                editor.action {
                    commit()
                }
            }
            
            button("save") {
                shortcut("Ctrl+S")
                tooltip("Ctrl+S")
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
    }
}