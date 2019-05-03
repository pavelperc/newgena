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
import javafx.util.converter.DefaultStringConverter
import tornadofx.*


/**  */
class TransitionDelaysEditor(
        initialObjects: Map<String, JsonTimeDescription.DelayWithDeviation>,
        private val predefinedTransitionsToHints: Map<String, String> = emptyMap(),
        val onSuccess: (Map<String, JsonTimeDescription.DelayWithDeviation>) -> Unit = {}
) : Fragment("NoiseEventsEditor") {
    
    private val predefinedHintsToTransitions = predefinedTransitionsToHints
            .filter { (_, v) -> v.isNotEmpty() }
            .map { (k, v) -> v to k }
            .toMap()
    
    public inner class TransitionDelayTuple(
            transitionId: String = "",
            var delay: Long = 5L,
            var deviation: Long = 1L
    ) {
        val transitionIdProp = SimpleStringProperty(transitionId)
        var transitionId by transitionIdProp
        
        val hintProp = SimpleStringProperty(predefinedTransitionsToHints[transitionId] ?: "")
//        var hint by hintProp
        
        init {
            // setup a hint
            transitionIdProp.onChange { newString ->
                // reset the hint if the value is unknown
                hintProp.value = predefinedTransitionsToHints[newString] ?: ""
            }
            hintProp.onChange { hint ->
                // update the value if the hint was found.
                if (hint in predefinedHintsToTransitions) {
                    transitionIdProp.value = predefinedHintsToTransitions[hint]
                }
            }
        }
        
        fun copy() = TransitionDelayTuple(transitionId, delay, deviation)
        
        constructor(transitionId: String, delayWithDeviation: JsonTimeDescription.DelayWithDeviation)
                : this(transitionId, delayWithDeviation.delay, delayWithDeviation.deviation)
        
        fun toPair() = transitionId to JsonTimeDescription.DelayWithDeviation(delay, deviation)
    }
    
    /** ViewModel is used here only for adding, not editing. */
    private inner class TransitionDelayModel(initial: TransitionDelayTuple) : ItemViewModel<TransitionDelayTuple>(initial) {
        val transitionId = bind(TransitionDelayTuple::transitionId)
        val hintProp = SimpleStringProperty(predefinedHintsToTransitions[transitionId.value] ?: "")
        
        val delay = bind(TransitionDelayTuple::delay)
        val deviation = bind(TransitionDelayTuple::deviation)
        
        
        init {
            // setup a hint
            transitionId.onChange { newString ->
                // reset the hint if the value is unknown
                hintProp.value = predefinedTransitionsToHints[newString] ?: ""
            }
            hintProp.onChange { hint ->
                // always update the value
                transitionId.value = predefinedHintsToTransitions[hint] ?: ""
            }
        }
    }
    
    // without a copy the noise in onsuccess is considered unchanged!!! and we have some bad links.
    private val objects = initialObjects.map { (tr, delay) -> TransitionDelayTuple(tr, delay) }.observable()
    
    private val showLabel = predefinedTransitionsToHints.size > 0
    
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
                                when {
                                    newString.isNullOrEmpty() -> error("Should not be empty.")
                                    objects.any { it.transitionId == newString } -> error("Duplicate.")
                                    else -> null
                                }
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
                    // hint (label)
                    // hint synchronization is hidden in viewModel
                    if (showLabel) {
                        field("Search by label:") {
                            textfield(model.hintProp) {
                                //                        removeWhen { showHint.not() }
                                promptText = "Search by label"
                                action {
                                    if (commit()) {
                                        selectAll()
                                    }
                                }
                                
                                actionedAutoCompletion(predefinedHintsToTransitions.keys.toList())
                            }
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
                if (showLabel) {
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
            
            validatedColumn(TransitionDelayTuple::transitionId, DefaultStringConverter(), allowDuplicates = false) {
                actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
            }
            
            if (showLabel) {
                column("Label", TransitionDelayTuple::hintProp)
            }
            
            validatedLongColumn(TransitionDelayTuple::delay, nextValidator = { newLong ->
                if (newLong < 0L) error("Should not be negative.") else null
            })
            
            validatedLongColumn(TransitionDelayTuple::deviation, nextValidator = { newLong ->
                if (newLong < 0L) error("Should not be negative.") else null
            })
            
            column("Delete", TransitionDelayTuple::transitionId) {
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