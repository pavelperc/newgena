package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.actionedAutoCompletion
import com.pavelperc.newgena.gui.customfields.notEmpty
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
import com.pavelperc.newgena.gui.views.ResourceMappingEditor.ResourceType.*
import com.pavelperc.newgena.loaders.settings.JsonResources
import com.pavelperc.newgena.loaders.settings.JsonResources.JsonResourceMapping

class ResourceMappingEditor(
        initialObjects: Map<String, JsonResourceMapping>,
        // also contains artificial noise events!!!
        private val predefinedTransitionsToHints: Map<String, String> = emptyMap(),
        private val predefinedSimpleResources: List<String>,
        predefinedResourceGroups: List<JsonResources.Group>,
        val onSuccess: (Map<String, JsonResourceMapping>) -> Unit = {}
) : Fragment("ResourceMappingEditor") {
    
    
    private val predefinedComplexResNames = predefinedResourceGroups
            .flatMap { it.roles.flatMap { it.resources } }.map { it.name }
    
    private val predefinedGroupNames = predefinedResourceGroups.map { it.name }
    private val predefinedRoleNames = predefinedResourceGroups.flatMap { it.roles }.map { it.name }
    
    
    private val predefinedHintsToTransitions = predefinedTransitionsToHints
            .filter { (_, v) -> v.isNotEmpty() }
            .map { (k, v) -> v to k } // inverse
            .toMap()

//    private val predefinedResources = predefinedSimpleResources +
//            predefinedResourceGroups.flatMap { group ->
//                group.roles.flatMap { role ->
//                    role.resources.map { resource ->
//                        "${resource.name}:${role.name}:${group.name}"
//                    }
//                }
//            }
    
    enum class ResourceType {
        SIMPLE, COMPL, ROLE, GROUP;
        
        fun makeRes(name: String) = UIResource(name, this)
    }
    
    data class UIResource(val name: String, val type: ResourceType)
    
    data class TransitionResourcesTuple(
            var transitionId: String = "",
            val resources: ObservableList<UIResource> = observableList()
    ) {
        fun toResourceMapping(): Pair<String, JsonResourceMapping> {
            fun resOfType(type: ResourceType) = resources
                    .filter { it.type == type }
                    .map { it.name }.toMutableList()
            
            return transitionId to JsonResourceMapping(
                    simplifiedResourceNames = resOfType(SIMPLE),
                    complexResourceNames = resOfType(COMPL),
                    resourceRoles = resOfType(ROLE),
                    resourceGroups = resOfType(GROUP)
            )
        }
        
        companion object {
            private fun JsonResourceMapping.toUIRes() =
                    this.simplifiedResourceNames.map { SIMPLE.makeRes(it) } +
                            this.complexResourceNames.map { COMPL.makeRes(it) } +
                            this.resourceRoles.map { ROLE.makeRes(it) } +
                            this.resourceGroups.map { GROUP.makeRes(it) }
            
            fun fromResourceMapping(transitionId: String, resources: JsonResourceMapping) =
                    TransitionResourcesTuple(transitionId, resources.toUIRes().observable())
        }
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
                TransitionResourcesTuple.fromResourceMapping(transitionId, mapping)
            }
            .observable()
    
    private val showLabel = SimpleBooleanProperty(predefinedTransitionsToHints.size > 0)
    
    // --- TRANSITION HEADER ---
    fun EventTarget.headerTransition() {
        form {
            style {
                padding = box(5.px)
            }
            
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
                field("transitionId/artificialEvent") {
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
    
    val selectedTransition = SimpleObjectProperty<TransitionResourcesTuple>(null)
    
    // right side header
    fun EventTarget.headerResources() {
        hbox {
            //            prefWidth = 550.0
//            addClass(Styles.addItemRoot)
            
            form {
                // props to fill UIResource
                val resName = SimpleStringProperty("")
                val resType = SimpleObjectProperty(SIMPLE)
                
                fun commit(): Boolean {
                    if (resName.value != "" && selectedTransition.value != null) {
                        // create a copy
                        selectedTransition.value!!.resources.add(UIResource(resName.value, resType.value))
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("type") {
                        combobox(resType, ResourceType.values().toList())
                    }
                    val shouldGuessType = SimpleBooleanProperty(true)
                    checkbox("guess type", shouldGuessType)
                    field("name:") {
                        textfield(resName) {
                            enableWhen { selectedTransition.isNotNull }
                            
                            promptText = "Click enter to add."
                            action {
                                if (shouldGuessType.value) {
                                    
                                    // try to guess type!!!
                                    when (resName.value) {
                                        in predefinedSimpleResources -> resType.value = SIMPLE
                                        in predefinedComplexResNames -> resType.value = COMPL
                                        in predefinedRoleNames -> resType.value = ROLE
                                        in predefinedGroupNames -> resType.value = GROUP
                                    }
                                }
                                
                                if (commit()) {
                                    selectAll()
                                }
                            }
                            
                            actionedAutoCompletion {
                                if (shouldGuessType.value) {
                                    predefinedComplexResNames + predefinedGroupNames +
                                            predefinedRoleNames + predefinedSimpleResources
                                } else {
                                    when (resType.value ?: SIMPLE) {
                                        SIMPLE -> predefinedSimpleResources
                                        COMPL -> predefinedComplexResNames
                                        ROLE -> predefinedRoleNames
                                        GROUP -> predefinedGroupNames
                                    }
                                }
                            }
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
            hgrow = Priority.ALWAYS
            
            
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
        
        listview<UIResource>() {
            useMaxSize = true
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
    
            selectedTransition.onChange { newTransition ->
                if (newTransition == null) {
                    items = observableList()
                } else {
                    items = newTransition.resources
                }
            }
            
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
                
                // res name
                val valueProp = SimpleStringProperty(item.name)
                // res type
                val typeProp = SimpleObjectProperty(item.type)
                
                graphic = hbox {
                    alignment = Pos.CENTER_LEFT
                    
                    upDownPanel(items, item) {
                        removeWhen(editingProperty())
                    }
                    
                    setOnEditCancel {
                        
                        // TODO FIX EDIT CANCEL!!!!!!
//                        println("Cancelled: valueProp=${valueProp.value}, item=$item")
                        
                        valueProp.value = item.name
                        typeProp.value = item.type
                        
                    }
                    
                    // look:
                    hbox {
                        alignment = Pos.CENTER
                        hgrow = Priority.ALWAYS
                        useMaxSize = true
                        style {
                            padding = box(0.px, 10.px, 0.px, 5.px)
                            spacing = 5.px
                        }
                        removeWhen { editingProperty() }
                        
                        label(valueProp) {
                            alignment = Pos.CENTER
                            hgrow = Priority.ALWAYS
                            useMaxSize = true
                        }
                        label(typeProp)
                    }
                    
                    
                    // edit
                    hbox {
                        removeWhen { editingProperty().not() }
                        hgrow = Priority.ALWAYS
                        alignment = Pos.CENTER
                        useMaxWidth = true
                        
                        textfield(valueProp) {
                            hgrow = Priority.ALWAYS
//                            alignment = Pos.CENTER
                            useMaxWidth = true
                            
                            action { commitEdit(UIResource(valueProp.value, typeProp.value)) }
                            promptText = "Edit value."
                            
                            // TODO edit resource mapping autocompl.
//                            actionedAutoCompletion(predefinedResources)
                            actionedAutoCompletion {
                                when (typeProp.value ?: SIMPLE) {
                                    SIMPLE -> predefinedSimpleResources
                                    COMPL -> predefinedComplexResNames
                                    ROLE -> predefinedRoleNames
                                    GROUP -> predefinedGroupNames
                                }
                            }
                            
                            whenVisible { requestFocus() }
                        }
                        combobox(typeProp, ResourceType.values().toList())
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
        useMaxSize = true
        hgrow = Priority.ALWAYS
        
        vbox {
            useMaxSize = true
            hgrow = Priority.ALWAYS
            
            headerTransition()
            transitionsList()
        }
        
        vbox {
            useMaxSize = true
            hgrow = Priority.ALWAYS
            
            headerResources()
            resourcesList()
        }
    }
}