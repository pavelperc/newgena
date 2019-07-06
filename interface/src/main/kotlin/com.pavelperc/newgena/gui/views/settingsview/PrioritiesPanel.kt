package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
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
    
    foldingFieldSet("Static priorities", settings.isUsingStaticPriorities) {
        intMapField(
                staticPriorities.transitionIdsToPriorities,
                predefinedValuesToHints = { controller.transitionIdsWithHints },
                hintName = "label",
                fillDefaultButton = true,
                valuesName = "ids"
        ) xx@{ map ->
            if (controller.petrinet == null) {
                return@xx null
            }
    
            val input: Set<String> = map.keys
            val unknown = input - input.intersect(controller.transitionIdsWithHints.keys)
            when {
                unknown.isNotEmpty() -> warning("Not found transitions: $unknown")
                map.size < controller.transitionIdsWithHints.size -> warning("Not all priorities defined!!")
                else -> null
            }
        }
    }
}