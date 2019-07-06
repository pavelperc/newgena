package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.NoiseEventsEditor
import javafx.beans.property.IntegerProperty
import javafx.event.EventTarget
import org.processmining.models.time_driven_behavior.GranularityTypes
import tornadofx.*


fun EventTarget.noisePanel(controller: SettingsUIController) {
    val settings = controller.settingsModel
    val noise = controller.noiseModel
    val timeNoise = controller.timeNoiseModel
    
    checkboxField(settings.isUsingNoise) {
        action {
            if (isSelected)
                settings.isUsingStaticPriorities.value = false
        }
    }
    
    foldingFieldSet("Noise", settings.isUsingNoise) {
        intField(noise.noiseLevel, validRange = 1..100, fieldOp = {
            slider(1..100, noise.noiseLevel.value) {
                blockIncrement = 1.0
                valueProperty().bindBidirectional(noise.noiseLevel as IntegerProperty)
            }
        }) {
            minWidth = 50.0
            maxWidth = 50.0
        }
        
        checkboxField(noise.isSkippingTransitions)
        checkboxField(noise.isUsingExternalTransitions)
        checkboxField(noise.isUsingInternalTransitions)
        arrayField(
                noise.internalTransitionIds,
                predefinedValuesToHints = { controller.transitionIdsWithHints },
                valuesName = "id",
                hintName = "label"
        )
    
    
        docField("artificialNoiseEvents") {
            
            readOnlyTextField(noise.artificialNoiseEvents, { newList ->
                newList.let { if (it.isEmpty()) "Empty." else it.joinToString("; ") }
            })
            
            button("Edit") {
                action {
                    NoiseEventsEditor(noise.artificialNoiseEvents.value) { events ->
                        noise.artificialNoiseEvents.value = events.toMutableList()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
        
        
        foldingFieldSet("timeDrivenNoise", settings.isUsingTime) {
            checkboxField(timeNoise.isUsingTimestampNoise)
            checkboxField(timeNoise.isUsingLifecycleNoise)
            checkboxField(timeNoise.isUsingTimeGranularity)
            intField(timeNoise.maxTimestampDeviationSeconds)
    
            docField("granularityType") {
                combobox(timeNoise.granularityType, GranularityTypes.values().toList())
            }
        }
    }
}