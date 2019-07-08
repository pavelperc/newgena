package com.pavelperc.newgena.testutils.launchers

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import org.processmining.log.models.EventLogArray
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet

/** Test version of launch from petrinet, settings and marking */
fun justGenerate(
        petrinet: ResetInhibitorNet,
        settings: JsonSettings
): EventLogArray {
    val builder = JsonSettingsBuilder(petrinet, settings)
    val generationDescription = builder.buildDescription()
    val (initialMarking, finalMarking) = builder.buildMarking()
    
    val generationKit = PetrinetGenerators.GenerationKit(
            petrinet,
            initialMarking = initialMarking,
            finalMarking = finalMarking,
            description = generationDescription
    )
    return PetrinetGenerators.generateFromKit(generationKit)
}