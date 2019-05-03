//package com.pavelperc.newgena.gui.views
//
//import com.pavelperc.newgena.gui.app.Styles
//import com.pavelperc.newgena.gui.customfields.*
//import com.pavelperc.newgena.loaders.settings.JsonResources
//import com.pavelperc.newgena.loaders.settings.JsonTimeDescription
//import javafx.beans.property.SimpleIntegerProperty
//import javafx.beans.property.SimpleLongProperty
//import javafx.beans.property.SimpleStringProperty
//import javafx.event.EventTarget
//import javafx.scene.input.KeyCode
//import javafx.scene.input.KeyEvent
//import javafx.scene.layout.Priority
//import javafx.scene.paint.Color
//import javafx.util.converter.DefaultStringConverter
//import tornadofx.*
//
//
///**  */
//class ComplexResourcesEditor(
//        initialObjects: List<JsonResources.Group>,
//        val onSuccess: (Map<String, JsonTimeDescription.DelayWithDeviation>) -> Unit = {}
//) : Fragment("ComplexResourcesEditor") {
//    
//    /** Resource, role and group. */
//    public class ResourceTuple(
//            resourceName: String = "",
//            willBeFreed: Long = 0L,
//            minDelayBetweenActionsMillis: Long = 0L,
//            maxDelayBetweenActionsMillis: Long = 0L,
//            roleName: String = "",
//            groupName: String = ""
//    
//    ) {
//        val resourceNameProp = SimpleStringProperty(resourceName)
//        var resourceName by resourceNameProp
//    
//        val willBeFreedProp = SimpleLongProperty(willBeFreed)
//        var willBeFreed by willBeFreedProp
//    
//        val minDelayBetweenActionsMillisProp = SimpleLongProperty(minDelayBetweenActionsMillis)
//        var minDelayBetweenActionsMillis by minDelayBetweenActionsMillisProp
//        
//        val maxDelayBetweenActionsMillisProp = SimpleLongProperty(maxDelayBetweenActionsMillis)
//        var maxDelayBetweenActionsMillis by maxDelayBetweenActionsMillisProp
//        
//        val roleNameProp = SimpleStringProperty(roleName)
//        var roleName by roleNameProp
//        
//        val groupNameProp = SimpleStringProperty(groupName)
//        var groupName by groupNameProp
//        
//        
//        val fullName: String
//            get() = "$groupName:$roleName:$resourceName"
//        
//        
//        constructor(resource: JsonResources.Resource,
//                    role: JsonResources.Role,
//                    group: JsonResources.Group)
//                : this(
//                resource.name,
//                resource.willBeFreed,
//                resource.minDelayBetweenActionsMillis,
//                resource.maxDelayBetweenActionsMillis,
//                role.name,
//                group.name)
//        
//        fun copy() = ResourceTuple(
//                resourceName,
//                willBeFreed,
//                minDelayBetweenActionsMillis,
//                maxDelayBetweenActionsMillis,
//                roleName,
//                groupName
//        )
//        
//        fun toResource() = JsonResources.Resource(resourceName, willBeFreed, minDelayBetweenActionsMillis, maxDelayBetweenActionsMillis)
//    }
//    
//    
//    inner class ResourceTupleModel(initial: ResourceTuple) : ItemViewModel<ResourceTuple>(initial) {
//        val resourceNameProp = bind(ResourceTuple::resourceNameProp)
//        val willBeFreedProp = bind(ResourceTuple::willBeFreedProp)
//        val minDelayBetweenActionsMillisProp = bind(ResourceTuple::minDelayBetweenActionsMillisProp)
//        val maxDelayBetweenActionsMillisProp = bind(ResourceTuple::maxDelayBetweenActionsMillisProp)
//        val roleNameProp = bind(ResourceTuple::roleNameProp)
//        val groupNameProp = bind(ResourceTuple::groupNameProp)
//    }
//    
//    private val objects = initialObjects
//            .flatMap { group ->
//                group.roles.flatMap { role ->
//                    role.resources.map { resource ->
//                        ResourceTuple(resource, role, group)
//                    }
//                }
//            }
//            .observable()
//    
//    init {
//        objects.associateBy { it.fullName }
//    }
//    
//    
//    // --- HEADER ---
//    fun EventTarget.header() {
//        hbox {
//            prefWidth = 550.0
//            addClass(Styles.addItemRoot)
//            
//            form {
//                val model = ResourceTupleModel(ResourceTuple())
//                fun commit(): Boolean {
//                    if (model.commit()) {
//                        // create a copy
//                        objects.add(model.item.copy())
//                        return true
//                    }
//                    return false
//                }
//                
//                fieldset {
//                    field("name") {
//                        textfield(item.resourceNameProp) {
//                            promptText = "Click enter to add."
//                            action {
//                                if (commit()) {
//                                    selectAll()
//                                }
//                            }
//                            actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
//                        }
//                    }
//                    // hint (label)
//                    // hint synchronization is hidden in viewModel
//                    if (showLabel) {
//                        field("Search by label:") {
//                            textfield(model.hintProp) {
//                                //                        removeWhen { showHint.not() }
//                                promptText = "Search by label"
//                                action {
//                                    if (commit()) {
//                                        selectAll()
//                                    }
//                                }
//                                
//                                actionedAutoCompletion(predefinedHintsToTransitions.keys.toList())
//                            }
//                        }
//                    }
//                    longField(model.delay, nextValidator = { value ->
//                        if (value < 0L) error("Should not be negative.") else null
//                    }) {
//                        action {
//                            commit()
//                        }
//                    }
//                    
//                    longField(model.deviation, nextValidator = { value ->
//                        if (value < 0L) error("Should not be negative.") else null
//                    }) {
//                        action {
//                            commit()
//                        }
//                    }
//                }
//                button("Add") {
//                    enableWhen(model.valid)
//                    action {
//                        commit()
//                    }
//                }
//            }
//            
//            vbox {
//                button("save") {
//                    addEventFilter(KeyEvent.KEY_PRESSED) {
//                        if (it.code == KeyCode.ENTER) {
//                            fire()
//                        }
//                    }
//                    action {
//                        onSuccess(objects.map { it.toPair() }.toMap())
//                        close()
//                    }
//                }
//                if (showLabel) {
//                    button("fill with default") {
//                        action {
//                            objects.setAll(predefinedTransitionsToHints.keys.map { TransitionDelayTuple(it) })
//                        }
//                    }
//                }
//                
//            }
//        }
//    }
//    
//    
//    override val root = vbox {
//        
//        header()
//        
//        tableview(objects) {
//            isEditable = true
//            useMaxSize = true
//            vgrow = Priority.ALWAYS
//            
//            validatedColumn(TransitionDelayTuple::transitionId, DefaultStringConverter(), allowDuplicates = false) {
//                actionedAutoCompletion(predefinedTransitionsToHints.keys.toList())
//            }
//            
//            if (showLabel) {
//                column("Label", TransitionDelayTuple::hintProp)
//            }
//            
//            validatedLongColumn(TransitionDelayTuple::delay, nextValidator = { newLong ->
//                if (newLong < 0L) error("Should not be negative.") else null
//            })
//            
//            validatedLongColumn(TransitionDelayTuple::deviation, nextValidator = { newLong ->
//                if (newLong < 0L) error("Should not be negative.") else null
//            })
//            
//            column("Delete", TransitionDelayTuple::transitionId) {
//                cellFormat {
//                    graphic = button {
//                        style {
//                            backgroundColor += Color.TRANSPARENT
//                        }
//                        graphic = Styles.closeIcon()
//                        action {
//                            objects.remove(rowItem)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}