package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonTimeDescription
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.util.converter.DefaultStringConverter
import tornadofx.*


class TransitionDelaysEditor(
        initialObjects: Map<String, JsonTimeDescription.DelayWithDeviation>,
        private val predefinedTransitionsToHints: Map<String, String> = emptyMap(),
        val onSuccess: (Map<String, JsonTimeDescription.DelayWithDeviation>) -> Unit = {}
) : Fragment("TransitionDelaysEditor") {
    
    private val predefinedHintsToTransitions = predefinedTransitionsToHints
            .filter { (_, v) -> v.isNotEmpty() }
            .map { (k, v) -> v to k }
            .toMap()
    
    public data class TransitionDelayTuple(
            var transitionId: String = "",
            var delay: Long = 0L,
            var deviation: Long = 0L
    ) {
        constructor(transitionId: String, delayWithDeviation: JsonTimeDescription.DelayWithDeviation)
                : this(transitionId, delayWithDeviation.delay, delayWithDeviation.deviation)
        
        fun toPair() = transitionId to JsonTimeDescription.DelayWithDeviation(delay, deviation)
    }
    
    public inner class TransitionDelayModel(initial: TransitionDelayTuple) : ItemViewModel<TransitionDelayTuple>(initial) {
        val transitionId = bind(TransitionDelayTuple::transitionId)
        val hintProp = SimpleStringProperty(predefinedTransitionsToHints[transitionId.value] ?: "")
        
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
                if (!hint.isNullOrEmpty()) {
                    transitionId.value = predefinedHintsToTransitions[hint] ?: ""
                }
            }
        }
    }
    
    private val objects = initialObjects.map { (tr, delay) -> TransitionDelayModel(TransitionDelayTuple(tr, delay)) }.observable()
    
    private val showLabel = predefinedTransitionsToHints.size > 0
    
    // --- HEADER ---
    fun EventTarget.header() {
        hbox {
            addClass(Styles.addItemRoot)
            
            form {
                val model = TransitionDelayModel(TransitionDelayTuple())
                
                fun commit(): Boolean {
                    if (model.commit()) {
                        // create a copy
                        objects.add(TransitionDelayModel(model.item.copy()))
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
                                    objects.any { it.transitionId.value == newString } -> error("Duplicate.")
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
                    
                    longSpinnerField(model.delay, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
                    }
                    longSpinnerField(model.deviation, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
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
                    shortcut("Ctrl+S")
                    tooltip("Ctrl+S")
                    
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code == KeyCode.ENTER) {
                            fire()
                        }
                    }
                    action {
                        onSuccess(objects.map { it.commit(); it.item.toPair() }.toMap())
                        close()
                    }
                }
                if (showLabel) {
                    button("update to default") {
                        action {
                            val defaultIds = predefinedTransitionsToHints.keys
                            val oldIds = objects.map { it.transitionId.value }.toSet()
                            objects.removeIf { it.transitionId.value !in defaultIds }
                            objects.addAll(defaultIds
                                    .filter { it !in oldIds }
                                    .map { TransitionDelayTuple(it) }
                                    .map { TransitionDelayModel(it) })
                            objects.sortBy { it.transitionId.value }
                        }
                    }
                }
                
            }
        }
    }
    
    override fun onDock() {
        if (!showLabel) {
            notification(text = "Try to load the petrinet model to use hints.")
        }
    }
    
    override val root = vbox {
        prefWidth = 500.0
        header()
        
        tableview(objects) {
            isEditable = true
            useMaxSize = true
            vgrow = Priority.ALWAYS
            regainFocusAfterEdit()
            selectionModel.isCellSelectionEnabled = true
            columnResizePolicy = SmartResize.POLICY
    
            validatedColumn(TransitionDelayModel::transitionId, DefaultStringConverter(), allowDuplicates = false) {
                actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
            }
            
            if (showLabel) {
                validatedColumn(TransitionDelayModel::hintProp, DefaultStringConverter(), "Label",
                        validator = { newString, _ ->
                            if (newString !in predefinedHintsToTransitions)
                                error("Unknown hint.")
                            else null
                        }) {
                    actionedAutoCompletion(predefinedHintsToTransitions.keys.toList())
                }
            }
    
            validatedLongColumn(TransitionDelayModel::delay, nonNegative = true)
    
            validatedLongColumn(TransitionDelayModel::deviation, nonNegative = true)
            
            makeDeleteColumn()
        }
    }
}