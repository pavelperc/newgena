package com.pavelperc.newgena.gui.views.settingsEditors

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonResources
import impl.org.controlsfx.autocompletion.SuggestionProvider
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.util.converter.DefaultStringConverter
import org.controlsfx.control.textfield.TextFields
import tornadofx.*


class ResourceGroupsEditor(
        initialObjects: List<JsonResources.Group>,
        val onSuccess: (List<JsonResources.Group>) -> Unit = {}
) : Fragment("ResourceGroupsEditor") {
    
    /** Resource, role and group. */
    class ResourceTuple(
            var resourceName: String = "",
            var minDelayBetweenActionsMillis: Long = 0L,
            var maxDelayBetweenActionsMillis: Long = 0L,
            var roleName: String = "",
            var groupName: String = ""
    
    ) {
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
    
    /** [inTable] means, that this model is used in table. */
    inner class ResourceTupleModel(initial: ResourceTuple, inTable: Boolean = true) : ItemViewModel<ResourceTuple>(initial) {
        val resourceName = bind(ResourceTuple::resourceName)
        val minDelayBetweenActionsMillis = bind(ResourceTuple::minDelayBetweenActionsMillis)
        val maxDelayBetweenActionsMillis = bind(ResourceTuple::maxDelayBetweenActionsMillis)
        val roleName = bind(ResourceTuple::roleName)
        val groupName = bind(ResourceTuple::groupName)
        
        init {
            if (inTable) {
                roleName.onChange { 
                    updateRoleSuggestions()
                }
                groupName.onChange { 
                    updateGroupSuggestions()
                }
                
            }
            
            minDelayBetweenActionsMillis.onChange { minDelay ->
                if (minDelay!! > maxDelayBetweenActionsMillis.value) {
                    maxDelayBetweenActionsMillis.value = minDelay
                }
            }
            maxDelayBetweenActionsMillis.onChange { maxDelay ->
                if (maxDelay!! < minDelayBetweenActionsMillis.value) {
                    minDelayBetweenActionsMillis.value = maxDelay
                }
            }
        }
    }
    
    private val objects = initialObjects
            .flatMap { group ->
                group.roles.flatMap { role ->
                    role.resources.map { resource ->
                        ResourceTupleModel(ResourceTuple(resource, role, group))
                    }
                }
            }
            .observable()
    
    val roleSuggestions = SuggestionProvider.create(mutableListOf<String>())
    val groupSuggestions = SuggestionProvider.create(mutableListOf<String>())
    
    fun updateRoleSuggestions() {
        roleSuggestions.clearSuggestions()
        roleSuggestions.addPossibleSuggestions(objects.map { it.roleName.value }.toSet())
    }
    
    fun updateGroupSuggestions() {
        groupSuggestions.clearSuggestions()
        groupSuggestions.addPossibleSuggestions(objects.map { it.groupName.value }.toSet())
    }
    
    
    
    init {
        updateRoleSuggestions()
        updateGroupSuggestions()
        objects.onChange {
            updateRoleSuggestions()
            updateGroupSuggestions()
            // also update on role, group changes
        }
    }
    
    
    // --- HEADER ---
    fun EventTarget.header() {
        hbox {
            addClass(Styles.addItemRoot)
            
            form {
                val model = ResourceTupleModel(ResourceTuple(), inTable = false)
                fun commit(): Boolean {
                    if (model.commit()) {
                        // create a copy
                        objects.add(ResourceTupleModel(model.item.copy()))
                        return true
                    }
                    return false
                }
                
                fieldset {
                    field("name") {
                        textfield(model.resourceName) {
                            notEmpty()
                            action { if (commit()) selectAll() }
                        }
                    }
                    field("role") {
                        textfield(model.roleName) {
                            // autocompletion
                            TextFields.bindAutoCompletion(this, roleSuggestions).apply {
                                onAutoCompleted = EventHandler {
                                    // if selected role, then autoselect the group
                                    val suggestedGroup = objects.asSequence()
                                            .firstOrNull { it.roleName.value == model.roleName.value }
                                            ?.groupName?.value
                                    if (suggestedGroup != null) {
                                        model.groupName.value = suggestedGroup
                                    }
                                }
                            }
                            
                            action { if (commit()) selectAll() }
                            notEmpty()
                        }
                    }
                    
                    field("group") {
                        textfield(model.groupName) {
                            TextFields.bindAutoCompletion(this, groupSuggestions)
                            action { if (commit()) selectAll() }
                            notEmpty()
                        }
                    }
                    
                    longSpinnerField(model.minDelayBetweenActionsMillis, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
                        validator { newValue ->
                            if (newValue ?: 0 > model.maxDelayBetweenActionsMillis.value)
                                error("Min delay should not be greater than max delay.")
                            else null
                        }
                    }
                    
                    longSpinnerField(model.maxDelayBetweenActionsMillis, 0..Long.MAX_VALUE) {
                        editor.action { commit() }
                        validator { newValue ->
                            if (newValue ?: 0 < model.minDelayBetweenActionsMillis.value)
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
                    shortcut("Ctrl+S")
                    tooltip("Ctrl+S")
                    
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code == KeyCode.ENTER) {
                            fire()
                        }
                    }
                    action {
                        // collect all rows in groups
                        objects
                                .map { it.commit(force = true); it.item }
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
            
            
            validatedColumn(ResourceTupleModel::resourceName, DefaultStringConverter())
            validatedColumn(ResourceTupleModel::roleName, DefaultStringConverter()) {
                TextFields.bindAutoCompletion(this, roleSuggestions)
            }
            validatedColumn(ResourceTupleModel::groupName, DefaultStringConverter()) {
                TextFields.bindAutoCompletion(this, groupSuggestions)
            }
            
            
            this.validatedLongColumn(
                    ResourceTupleModel::minDelayBetweenActionsMillis,
                    columnName = "minDelay",
                    nonNegative = true)
            
            this.validatedLongColumn(
                    ResourceTupleModel::maxDelayBetweenActionsMillis,
                    columnName = "maxDelay",
                    nonNegative = true)
            
            makeDeleteColumn()
        }
    }
}