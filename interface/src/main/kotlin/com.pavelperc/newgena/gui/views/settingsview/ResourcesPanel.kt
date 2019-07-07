package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.model.triggerValidator
import com.pavelperc.newgena.gui.views.ResourceGroupsEditor
import com.pavelperc.newgena.gui.views.ResourceMappingEditor
import com.pavelperc.newgena.utils.common.plusAssign
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import tornadofx.*
import java.lang.StringBuilder

fun EventTarget.resourcesPanel(controller: SettingsUIController) {
    val time = controller.timeModel
    checkboxField(time.isUsingResources)
    
    foldingFieldSet("Resources", time.isUsingResources) {
        hgrow = Priority.ALWAYS
        
        checkboxField(time.isUsingComplexResourceSettings)
        checkboxField(time.isUsingSynchronizationOnResources)
        
        arrayField(time.simplifiedResources, valuesName = "name")
        
        docField("resourceGroups") {
            readOnlyTextField(time.resourceGroups, { newList ->
                newList.flatMap {
                    it.roles.flatMap {
                        it.resources.map { it.name }
                    }
                }.let { if (it.isEmpty()) "Empty." else it.joinToString("; ") }
            })
            
            button("Edit") {
                action {
                    ResourceGroupsEditor(time.resourceGroups.value) { groups ->
                        time.resourceGroups.value = groups.toMutableList()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
        
        docField("transitionIdsToResources") {
            time.resourceGroups.onChange {
                time.transitionIdsToResources.triggerValidator()
            }
            time.simplifiedResources.onChange {
                time.transitionIdsToResources.triggerValidator()
            }
            
            statusLabel(time.transitionIdsToResources) { newValue ->
                val transitions = controller.transitionIdsWithHints.keys
                val noise = controller.noiseModel.artificialNoiseEvents.value.map { it.activity.toString() }.toSet()
                
                val transitionsAndNoise = transitions + noise
                
                if (transitions.isEmpty()) {
                    return@statusLabel Status.unknown
                }
                
                // check ids:
                val enteredIds = newValue.keys
                val unknownIds = (enteredIds - transitionsAndNoise).size
                if (unknownIds > 0) {
                    return@statusLabel Status("Found $unknownIds unknown ids.", true)
                }
                
                // check resources:
                
                val simpleRes = time.simplifiedResources.value.toSet()
                val complexRes = time.resourceGroups.value.flatMap { it.roles.flatMap { it.resources.map { it.name } } }.toSet()
                val roles = time.resourceGroups.value.flatMap { it.roles.map { it.name } }.toSet()
                val groups = time.resourceGroups.value.map { it.name }.toSet()
                
                val enteredSimpleRes = newValue.values.flatMap { it.simplifiedResourceNames }.toSet()
                val enteredComplRes = newValue.values.flatMap { it.complexResourceNames }.toSet()
                val enteredRoles = newValue.values.flatMap { it.resourceRoles }.toSet()
                val enteredGroups = newValue.values.flatMap { it.resourceGroups }.toSet()
                
                val unknownSimpleRes = (enteredSimpleRes - simpleRes).size
                val unknownComplexRes = (enteredComplRes - complexRes).size
                val unknownRoles = (enteredRoles - roles).size
                val unknownGroups = (enteredGroups - groups).size
                
                val sumUnknownRes = unknownSimpleRes + unknownComplexRes + unknownRoles + unknownGroups
                if (sumUnknownRes > 0) {
                    val text = StringBuilder("Found unknown: ")
                    if (unknownSimpleRes > 0) text += "$unknownSimpleRes simpleRes, "
                    if (unknownComplexRes > 0) text += "$unknownComplexRes complexRes, "
                    if (unknownRoles > 0) text += "$unknownRoles roles, "
                    if (unknownGroups > 0) text += "$unknownGroups groups, "
                    return@statusLabel Status(text.dropLast(2).toString(), true)
                }
                Status.correct
            }
            
            button("Edit") {
                action {
                    val transitionsWithHintsAndArtificialEvents = controller.transitionIdsWithHints +
                            controller.noiseModel.artificialNoiseEvents.value
                                    .map { it.activity.toString() to "" }
                                    .toMap()
                    
                    ResourceMappingEditor(
                            time.transitionIdsToResources.value,
                            transitionsWithHintsAndArtificialEvents,
                            time.simplifiedResources.value,
                            time.resourceGroups.value
                    ) { newMapping ->
                        time.transitionIdsToResources.value = newMapping.toMutableMap()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
    }
}