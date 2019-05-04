package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.actionedAutoCompletion
import com.pavelperc.newgena.gui.customfields.notEmpty
import com.pavelperc.newgena.loaders.settings.JsonResources
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*


class ResourceMappingEditor(
        initialObjects: Map<String, JsonResources.ResourceMapping>,
        private val predefinedTransitionsToHints: Map<String, String> = emptyMap(),
        predefinedSimpleResources: List<String>,
        predefinedResourceGroups: List<JsonResources.Group>,
        val onSuccess: (Map<String, JsonResources.ResourceMapping>) -> Unit = {}
) : Fragment("ResourceMappingEditor") {
    
    private val predefinedHintsToTransitions = predefinedTransitionsToHints
            .filter { (_, v) -> v.isNotEmpty() }
            .map { (k, v) -> v to k } // inverse
            .toMap()
    
    private val predefinedResources = predefinedSimpleResources +
            predefinedResourceGroups.flatMap { group ->
                group.roles.flatMap { role ->
                    role.resources.map { resource ->
                        "${resource.name}:${role.name}:${group.name}"
                    }
                }
            }
    
    
    public data class TransitionResourcesTuple(
            var transitionId: String = "",
            val resources: ObservableList<String> = observableList()
    ) {
        
        fun toResourceMapping(): Pair<String, JsonResources.ResourceMapping> {
            val simplified = mutableListOf<String>()
            val full = mutableListOf<JsonResources.ResourceMapping.FullResourceName>()
            
            val regex = Regex("""(.+):(.*):(.*)""")
            resources.forEach { resourceName ->
                val matchResult = regex.matchEntire(resourceName)
                if (matchResult != null) {
                    val (_, name, role, group) = matchResult.groups.map { it!!.value }
                    full += JsonResources.ResourceMapping.FullResourceName(group, role, name)
                } else {
                    simplified += resourceName
                }
            }
            
            return transitionId to JsonResources.ResourceMapping(full, simplified)
        }
        
        constructor(transitionId: String, resources: List<String>) : this(transitionId, resources.observable())
    }
    
    public inner class TransitionResourcesModel(initial: TransitionResourcesTuple) : ItemViewModel<TransitionResourcesTuple>(initial) {
        val transitionId = bind(TransitionResourcesTuple::transitionId)
        val hint = SimpleStringProperty(predefinedTransitionsToHints[transitionId.value] ?: "")
        
        val resourceList = bind(TransitionResourcesTuple::resources)
        
        
        init {
            // setup a hint
            transitionId.onChange { newString ->
                // reset the hint if the value is unknown
                hint.value = predefinedTransitionsToHints[newString] ?: ""
            }
            hint.onChange { hint ->
                // always update the value
                if (!hint.isNullOrEmpty()) {
                    transitionId.value = predefinedHintsToTransitions[hint] ?: ""
                }
            }
        }
    }
    
    private val objects = initialObjects
            .map { (transitionId, mapping) ->
                TransitionResourcesTuple(transitionId,
                        mapping.simplifiedResourceNames +
                                mapping.fullResourceNames.map { (group, role, name) -> "$name:$role:$group" }
                )
            }
            .observable()
    
    private val showLabel = SimpleBooleanProperty(predefinedTransitionsToHints.size > 0)
    
    // --- TRANSITION HEADER ---
    fun EventTarget.headerTransition() {
        vbox {
            //            prefWidth = 550.0
//            addClass(Styles.addItemRoot)
            style {
                padding = box(5.px)
            }
            
            form {
                val model = TransitionResourcesModel(TransitionResourcesTuple())
                
                fun commit(): Boolean {
                    if (model.commit()) {
                        objects.add(model.item)
                        // create a copy
                        model.item = TransitionResourcesTuple(model.item.transitionId)
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("transitionId") {
                        textfield(model.transitionId) {
                            notEmpty { newString ->
                                if (objects.any { it.transitionId == newString })
                                    error("Duplicate.")
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
                    // hint (label)
                    // hint synchronization is hidden in viewModel
                    field("Search by label:") {
                        removeWhen { showLabel.not() }
                        
                        textfield(model.hint) {
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
            }
            vbox {
                hbox {
                alignment = Pos.CENTER_LEFT
                spacing = 5.0
                    
                    button("save") {
                        addEventFilter(KeyEvent.KEY_PRESSED) {
                            if (it.code == KeyCode.ENTER) {
                                fire()
                            }
                        }
                        action {
                            onSuccess(objects.map {
                                it.toResourceMapping()
                            }.toMap())
                            close()
                        }
                    }
                    if (predefinedTransitionsToHints.size > 0) {
                        button("fill transitions") {
                            action {
                                objects.setAll(predefinedTransitionsToHints.keys
                                        .map { TransitionResourcesTuple(it) })
                            }
                        }
                    }
                }
                if (predefinedTransitionsToHints.size > 0) {
                    checkbox("Show label", showLabel)
                }
            }
        }
    }
    
    val selectedTransition = SimpleObjectProperty<TransitionResourcesTuple>(null)
    
    fun EventTarget.headerResources() {
        hbox {
            //            prefWidth = 550.0
//            addClass(Styles.addItemRoot)
            
            form {
                val resourceName = SimpleStringProperty("")
                
                fun commit(): Boolean {
                    if (resourceName.value != "" && selectedTransition.value != null) {
                        // create a copy
                        selectedTransition.value!!.resources.add(resourceName.value)
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("resource full name:") {
                        textfield(resourceName) {
                            enableWhen { selectedTransition.isNotNull }
                            
                            promptText = "Click enter to add."
                            action {
                                if (commit()) {
                                    selectAll()
                                }
                            }
                            
                            actionedAutoCompletion(predefinedResources)
                        }
                    }
                }
            }
        }
    }
    
    // -------- TRANSITIONS LIST --------
    private fun EventTarget.transitionsList() {
        listview(objects) {
            
            bindSelected(selectedTransition)
            
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    objects.remove(selectedItem)
                }
            }
            isEditable = true
            
            // ---ONE CELL---
            cellFormat {
                addEventFilter(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.ESCAPE) {
                        cancelEdit()
                    }
                }
                
                val model = TransitionResourcesModel(item)
                
                graphic = hbox {
                    alignment = Pos.CENTER_LEFT
                    
                    upDownPanel(objects, item) {
                        removeWhen(editingProperty())
                    }
                    
                    
                    fun commit() {
                        if (model.commit()) {
                            commitEdit(model.item!!)
                        }
                    }
                    setOnEditCancel {
                        model.rollback()
                    }
                    
                    
                    // look:
                    hbox {
                        alignment = Pos.CENTER
                        hgrow = Priority.ALWAYS
                        removeWhen { editingProperty() }
                        style {
                            padding = box(0.px, 10.px, 0.px, 5.px)
                        }
                        
                        label(model.transitionId) {
                            setId(Styles.contentLabel)
                            hgrow = Priority.ALWAYS
                            useMaxSize = true
                        }
                        label(model.hint) {
                            paddingLeft = 5
                            removeWhen { showLabel.not() }
                        }
                    }
                    
                    // edit
                    hbox {
                        alignment = Pos.CENTER
                        hgrow = Priority.ALWAYS
                        removeWhen { editingProperty().not() }
                        
                        // value
                        val tfValue = textfield(model.transitionId) {
                            hgrow = Priority.ALWAYS
                            useMaxWidth = true
                            prefWidth = 100.0
                            
                            action { commitEdit(item) }
                            promptText = "Edit value."
                            
                            actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
                        }
                        
                        whenVisible { tfValue.requestFocus() }
                        
                        // hint
                        textfield(model.hint) {
                            prefWidth = 100.0
                            useMaxWidth = true
                            hgrow = Priority.ALWAYS
                            promptText = "Search by label"
    
                            removeWhen { showLabel.not() }
                            
                            action { commitEdit(item) }
                            actionedAutoCompletion(predefinedHintsToTransitions.keys.toList())
                        }
                    }
                    
                    // delete
                    button(graphic = Styles.closeIcon()) {
                        addClass(Styles.deleteButton)
//                        removeWhen { parent.hoverProperty().not().or(editingProperty()) }
                        removeWhen { editingProperty() }
                        action { objects.removeAt(index) }
                    }
                }
            }
        }
    }
    
    // -------- RESOURCES LIST --------
    private fun EventTarget.resourcesList() {
        
        listview<String>() {
            
            selectedTransition.onChange { newTransition ->
                if (newTransition == null) {
                    items = observableList()
                } else {
                    items = newTransition.resources
                }
            }
            
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    items.remove(selectedItem)
                }
            }
            isEditable = true
            
            // ---ONE CELL---
            cellFormat {
                addEventFilter(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.ESCAPE) {
                        cancelEdit()
                    }
                }
                
                val valueProp = SimpleStringProperty(item)
                
                graphic = hbox {
                    alignment = Pos.CENTER_LEFT
                    
                    upDownPanel(items, item) {
                        removeWhen(editingProperty())
                    }
                    
                    setOnEditCancel {
                        valueProp.value = item
                    }
                    
                    // look:
                    label(itemProperty()) {
                        alignment = Pos.CENTER
                        hgrow = Priority.ALWAYS
                        removeWhen { editingProperty() }
                        style {
                            padding = box(0.px, 10.px, 0.px, 5.px)
                        }
                        
                        hgrow = Priority.ALWAYS
                        useMaxSize = true
                    }
                    
                    
                    // edit
                    textfield(valueProp) {
                        removeWhen { editingProperty().not() }
                        hgrow = Priority.ALWAYS
                        alignment = Pos.CENTER
                        useMaxWidth = true
                        
                        action { commitEdit(valueProp.value) }
                        promptText = "Edit value."
                        
                        actionedAutoCompletion(predefinedResources)
                        whenVisible { requestFocus() }
                    }
                    
                    // delete
                    button(graphic = Styles.closeIcon()) {
                        addClass(Styles.deleteButton)
//                        removeWhen { parent.hoverProperty().not().or(editingProperty()) }
                        removeWhen { editingProperty() }
                        action { items.removeAt(index) }
                    }
                }
            }
        }
    }
    
    
    override val root = hbox {
        vbox {
            headerTransition()
            transitionsList()
        }
        
        vbox {
            headerResources()
            resourcesList()
        }
    }
}