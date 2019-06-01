package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.checkboxField
import com.pavelperc.newgena.gui.customfields.foldingFieldSet
import com.pavelperc.newgena.gui.customfields.intField
import com.pavelperc.newgena.gui.customfields.intMapField
import javafx.event.EventTarget
import tornadofx.*


fun EventTarget.prioritiesPanel(controller: SettingsUIController) {
    val settings = controller.settingsModel
    val staticPriorities = controller.staticPrioritiesModel
    
    checkboxField(settings.isUsingStaticPriorities) {
        action {
            if (isSelected) {
                settings.isUsingTime.value = false
                settings.isUsingNoise.value = false
            }
        }
    }
    
    val validatePriorities: ValidationContext.(map: Map<String, Int>) -> ValidationMessage? = xx@{ map ->
        if (map.values.any { it !in 1..staticPriorities.maxPriority.value })
            return@xx error("All priorities should be in 1...maxPriority")
        
        if (controller.petrinet == null) {
            return@xx null
        }
        
        val input: Set<String> = map.keys
        val unknown = input - input.intersect(controller.transitionIdsWithHints.keys)
        if (unknown.isNotEmpty()) {
            warning("Not found transitions: $unknown")
            
        } else if (map.size < controller.transitionIdsWithHints.size) {
            return@xx warning("Not enough priorities defined!!")
        } else {
            null
        }
    }
    
    foldingFieldSet("Static priorities", settings.isUsingStaticPriorities) {
        intField(staticPriorities.maxPriority, nonNegative = true) {
            textProperty().onChange {
                staticPriorities.validate() // run both field to validate
            }
        }
        intMapField(
                staticPriorities.transitionIdsToPriorities,
                predefinedValuesToHints = { controller.transitionIdsWithHints },
                hintName = "label",
                fillDefaultButton = true,
                valuesName = "ids"
        ) { map ->
            validatePriorities(map)
        }
    }
}