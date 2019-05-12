package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.loaders.settings.JsonResources
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.util.converter.DefaultStringConverter
import tornadofx.*
import tornadofx.controlsfx.bindAutoCompletion


class ResourceGroupsEditor(
        initialObjects: List<JsonResources.Group>,
        val onSuccess: (List<JsonResources.Group>) -> Unit = {}
) : Fragment("ResourceGroupsEditor") {
    
    /** Resource, role and group. */
    public class ResourceTuple(
            var resourceName: String = "",
            var minDelayBetweenActionsMillis: Long = 0L,
            var maxDelayBetweenActionsMillis: Long = 0L,
            var roleName: String = "",
            var groupName: String = ""
    
    ) {
        val fullName: String
            get() = "$groupName:$roleName:$resourceName"
        
        
        constructor(resource: JsonResources.Resource,
                    role: JsonResources.Role,
                    group: JsonResources.Group)
                : this(
                resource.name,
                resource.minDelayBetweenActionsMillis,
                resource.maxDelayBetweenActionsMillis,
                role.name,
                group.name)
        
        fun copy() = ResourceTuple(
                resourceName,
                minDelayBetweenActionsMillis,
                maxDelayBetweenActionsMillis,
                roleName,
                groupName
        )
        
        fun toResource() = JsonResources.Resource(resourceName, minDelayBetweenActionsMillis, maxDelayBetweenActionsMillis)
    }
    
    
    inner class ResourceTupleModel(initial: ResourceTuple) : ItemViewModel<ResourceTuple>(initial) {
        val resourceNameProp = bind(ResourceTuple::resourceName)
        val minDelayBetweenActionsMillisProp = bind(ResourceTuple::minDelayBetweenActionsMillis)
        val maxDelayBetweenActionsMillisProp = bind(ResourceTuple::maxDelayBetweenActionsMillis)
        val roleNameProp = bind(ResourceTuple::roleName)
        val groupNameProp = bind(ResourceTuple::groupName)
    }
    
    private val objects = initialObjects
            .flatMap { group ->
                group.roles.flatMap { role ->
                    role.resources.map { resource ->
                        ResourceTuple(resource, role, group)
                    }
                }
            }
            .observable()
    
    fun getGroups() = objects.map { it.groupName }.toSet()
    fun getRoles() = objects.map { it.roleName }.toSet()
    
    
    // --- HEADER ---
    fun EventTarget.header() {
        hbox {
            addClass(Styles.addItemRoot)
            
            form {
                val model = ResourceTupleModel(ResourceTuple())
                fun commit(): Boolean {
                    if (model.commit()) {
                        // create a copy
                        objects.add(model.item.copy())
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("name") {
                        textfield(model.resourceNameProp) {
                            notEmpty()
                            action { if (commit()) selectAll() }
                        }
                    }
                    field("role") {
                        textfield(model.roleNameProp) {
                            bindAutoCompletion { getRoles() }
                            action { if (commit()) selectAll() }
                            notEmpty()
                        }
                    }
                    // if selected role, then autoselect the group
                    model.resourceNameProp.onChange { newRole ->
                        val suggestedGroup = objects.firstOrNull { it.roleName == newRole }?.roleName
                        if (suggestedGroup != null) {
                            model.groupNameProp.value = suggestedGroup
                        }
                    }
                    
                    field("group") {
                        textfield(model.groupNameProp) {
                            bindAutoCompletion { getGroups() }
                            action { if (commit()) selectAll() }
                            notEmpty()
                        }
                    }
                    
                    longSpinnerField(model.minDelayBetweenActionsMillisProp, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
                        validator { newValue ->
                            if (newValue ?: 0 > model.maxDelayBetweenActionsMillisProp.value)
                                error("Min delay should not be greater than max delay.")
                            else null
                        }
                    }
                    
                    longSpinnerField(model.maxDelayBetweenActionsMillisProp, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
                        validator { newValue ->
                            if (newValue ?: 0 < model.minDelayBetweenActionsMillisProp.value)
                                error("Max delay should not be less than min delay.")
                            else null
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
                        // collect all rows in groups
                        objects
                                .groupBy { it.groupName }
                                .mapValues {
                                    // group roles
                                    it.value.groupBy { it.roleName }
                                }
                                .map { (groupName, inGroup) ->
                                    JsonResources.Group(
                                            groupName,
                                            inGroup.map { (roleName, inRole) ->
                                                JsonResources.Role(
                                                        roleName,
                                                        inRole.map { it.toResource() }.toMutableList()
                                                )
                                            }.toMutableList()
                                    )
                                }
                                .also { onSuccess(it) }
                        close()
                    }
                }
            }
        }
    }
    
    
    override val root = vbox {
        prefWidth = 600.0
        header()
        
        tableview(objects) {
            isEditable = true
            useMaxSize = true
            vgrow = Priority.ALWAYS
            regainFocusAfterEdit()
            selectionModel.isCellSelectionEnabled = true
            columnResizePolicy = SmartResize.POLICY
            
            
            validatedColumn(ResourceTuple::resourceName, DefaultStringConverter())
            validatedColumn(ResourceTuple::roleName, DefaultStringConverter()) {
                bindAutoCompletion { getRoles() }
            }
            validatedColumn(ResourceTuple::groupName, DefaultStringConverter()) {
                bindAutoCompletion { getGroups() }
            }
            
            
            validatedLongColumn(
                    ResourceTuple::minDelayBetweenActionsMillis,
                    columnName = "minDelay",
                    nonNegative = true,
                    nextValidator = { newValue, rowItem ->
                        if (newValue > rowItem.maxDelayBetweenActionsMillis)
                            error("Min delay should not be greater than max delay.")
                        else null
                    }
            )
            validatedLongColumn(
                    ResourceTuple::maxDelayBetweenActionsMillis,
                    columnName = "maxDelay",
                    nonNegative = true,
                    nextValidator = { newValue, rowItem ->
                        if (newValue < rowItem.minDelayBetweenActionsMillis)
                            error("Max delay should not be less than min delay.")
                        else null
                    }
            )
            
            makeDeleteColumn()
        }
    }
}