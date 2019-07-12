package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.settingsEditors.TransitionDelaysEditor
import javafx.event.EventTarget
import javafx.util.StringConverter
import tornadofx.*
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit


fun EventTarget.timePanel(controller: SettingsUIController) {
    val settings = controller.settingsModel
    val time = controller.timeModel
    
    
    checkboxField(settings.isUsingTime) {
        action {
            if (isSelected)
                settings.isUsingStaticPriorities.value = false
        }
    }
    
    foldingFieldSet("Time and Resources", settings.isUsingTime) {
        docField("generationStart") {
            val timeConverter = object : StringConverter<Instant>() {
                
                override fun toString(obj: Instant) = obj.toString()
                override fun fromString(string: String) =
                        try {
                            Instant.parse(string)
                        } catch (e: DateTimeParseException) {
                            Instant.now()
                        }
            }
            
            textfield(time.generationStart, timeConverter) {
                validator(ValidationTrigger.OnBlur) { newString ->
                    try {
                        Instant.parse(newString)
                        null
                    } catch (e: DateTimeParseException) {
                        error("Bad time, format example: 2007-12-03T10:15:30.00Z")
                    }
                }
                action {
                    time.validationContext.validate(time.generationStart)
                }
            }
            button("Now") {
                action {
                    time.generationStart.value = Instant.now()
                }
            }
            button("Today") {
                action {
                    time.generationStart.value = Instant.now().truncatedTo(ChronoUnit.DAYS)
                }
            }
        }
        
        checkboxField(time.isUsingLifecycle)
        checkboxField(time.isSeparatingStartAndFinish)
        
        intField(time.minimumIntervalBetweenActions, nonNegative = true)
        intField(time.maximumIntervalBetweenActions, nonNegative = true)
        
        docField("transitionIdsToDelays") {
            statusLabel(time.transitionIdsToDelays) { newValue ->
                val delayIds = newValue.keys
                val petrinetIds = controller.transitionIdsWithHints.keys
                
                val unknownIds = delayIds - petrinetIds
                val unsetIds = petrinetIds - delayIds
                
                when {
                    petrinetIds.isEmpty() -> Status.unknown
                    unknownIds.isNotEmpty() -> Status("Found ${unknownIds.size} unknown ids.", true)
                    unsetIds.isNotEmpty() -> Status("Found ${unsetIds.size} unset ids.", true)
                    else -> Status.correct
                }
            }
            
            button("Edit") {
                action {
                    TransitionDelaysEditor(time.transitionIdsToDelays.value,
                            controller.transitionIdsWithHints) { newMap ->
                        time.transitionIdsToDelays.value = newMap.toMutableMap()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
        
        // ---RESOURCES---
        resourcesPanel(controller)
    }
}

data class Status(val text: String, val isWarning: Boolean = false) {
    companion object {
        val unknown = Status("Unknown: Petrinet is not loaded or empty.")
        val correct = Status("Correct.")
    }
}