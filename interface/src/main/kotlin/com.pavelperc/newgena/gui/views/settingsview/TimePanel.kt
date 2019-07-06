package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.TransitionDelaysEditor
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
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
        field("generationStart") {
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
        
        field("transitionIdsToDelays") {
            val Status = object {
                val incorrect = "Ids doesn't match with model transitions."
                val correct = "Correct."
                val unknown = "Unknown: Petrinet is not loaded or empty"
                val empty = "Empty."
            }
            
            fun getStatus(): String {
                val delayIds = time.transitionIdsToDelays.value.keys
                val petrinetIds = controller.transitionIdsWithHints.keys
                
                return when {
                    petrinetIds.isEmpty() -> Status.unknown
                    delayIds.isEmpty() -> Status.empty
                    petrinetIds != delayIds -> Status.incorrect
                    else -> Status.correct
                }
            }
            
            val statusLabel = textfield(getStatus()) {
                hgrow = Priority.ALWAYS
                isEditable = false
                style {
                    backgroundColor += Color.TRANSPARENT
                }
            }
            time.transitionIdsToDelays.addValidator(statusLabel) {
                getStatus().let { status ->
                    statusLabel.text = status
                    when (status) {
                        Status.correct, Status.unknown -> null
                        else -> warning(status)
                    }
                }
            }
            controller.petrinetController.petrinetProp.onChange {
                statusLabel.text = getStatus()
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