package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.loaders.settings.JsonTimeDescription
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.StringConverter
import javafx.util.converter.DefaultStringConverter
import tornadofx.*


/**  */
class TransitionDelaysEditor(
        initialObjects: Map<String, JsonTimeDescription.DelayWithDeviation>,
        private val predefinedTransitionsToHints: Map<String, String> = emptyMap(),
        val onSuccess: (Map<String, JsonTimeDescription.DelayWithDeviation>) -> Unit = {}
) : Fragment("NoiseEventsEditor") {
    
//    private val predefinedHintsToTransitions = predefinedTransitionsToHints
//            .filter { (_, v) -> v.isNotEmpty() }
//            .map { (k, v) -> v to k }
//            .toMap()
    
    private data class TransitionDelayTuple(
            var transitonId: String = "",
            var delay: Long = 5L,
            var deviation: Long = 1L
    ) {
        constructor(transitonId: String, delayWithDeviation: JsonTimeDescription.DelayWithDeviation)
                : this(transitonId, delayWithDeviation.delay, delayWithDeviation.deviation)
        
        fun toPair() = transitonId to JsonTimeDescription.DelayWithDeviation(delay, deviation)
    }
    
    private inner class TransitionDelayModel(initial: TransitionDelayTuple) : ItemViewModel<TransitionDelayTuple>(initial) {
        val transitionId = bind(TransitionDelayTuple::transitonId)
//        val hintProp = SimpleStringProperty(predefinedHintsToTransitions[transitionId.value] ?: "")
        
        val delay = bind(TransitionDelayTuple::delay)
        val deviation = bind(TransitionDelayTuple::deviation)
    }
    
    // without a copy the noise in onsuccess is considered unchanged!!! and we have some bad links.
    private val objects = initialObjects.map { (tr, delay) -> TransitionDelayTuple(tr, delay) }.observable()
    
    // --- HEADER ---
    fun EventTarget.header() {
        hbox {
            prefWidth = 550.0
            addClass(Styles.addItemRoot)
            
            
            
            form {
                val model = TransitionDelayModel(TransitionDelayTuple())
                
                fun commit(): Boolean {
                    if (model.commit()) {
                        // create a copy
                        objects.add(model.item.copy())
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("transitionId") {
                        textfield(model.transitionId) {
                            validator { newString ->
                                if (newString.isNullOrEmpty())
                                    error("Should not be empty.")
                                else null
                            }
                            promptText = "Click enter to add."
                            action {
                                if (commit()) {
                                    selectAll()
                                }
                            }
                            actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
                        }
                    }
                    
                    longField(model.delay, nextValidator = { value ->
                        if (value < 0L) error("Should not be negative.") else null
                    }) {
                        action {
                            commit()
                        }
                    }
                    
                    longField(model.deviation, nextValidator = { value ->
                        if (value < 0L) error("Should not be negative.") else null
                    }) {
                        action {
                            commit()
                        }
                    }
                }
                button("Add") {
                    enableWhen(model.valid)
                    action {
                        commit()
                    }
                }
            }
            
            vbox {
                button("save") {
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code == KeyCode.ENTER) {
                            fire()
                        }
                    }
                    action {
                        onSuccess(objects.map { it.toPair() }.toMap())
                        close()
                    }
                }
                if (predefinedTransitionsToHints.size > 0) {
                    button("fill with default") {
                        action {
                            objects.setAll(predefinedTransitionsToHints.keys.map { TransitionDelayTuple(it) })
                        }
                    }
                }
                
            }
        }
    }
    
    
    override val root = vbox {
        
        header()
        
        tableview(objects) {
            isEditable = true
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            validatedColumn(TransitionDelayTuple::transitonId, DefaultStringConverter()) {
                actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
            }
            
            validatedLongColumn(TransitionDelayTuple::delay, nextValidator = { newLong ->
                if (newLong < 0L) error("Should not be negative.") else null
            })
            
            validatedLongColumn(TransitionDelayTuple::deviation, nextValidator = { newLong ->
                if (newLong < 0L) error("Should not be negative.") else null
            })
            
            column("Delete", TransitionDelayTuple::transitonId) {
                cellFormat {
                    graphic = button {
                        style {
                            backgroundColor += Color.TRANSPARENT
                        }
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