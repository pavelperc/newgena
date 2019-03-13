package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.PnmlLoader
import com.pavelperc.newgena.models.markInhResetArcsByIds
import org.deckfour.xes.out.XesXmlSerializer
import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import java.io.File

class JsonSettingsController(val jsonSettings: JsonSettings) {
    
    companion object {
        fun createFromFilePath(settingsFilePath: String): JsonSettingsController {
            val jsonSettingsStr = File(settingsFilePath).readText()
            val jsonSettings = JsonSettings.fromJson(jsonSettingsStr)
            
            return JsonSettingsController(jsonSettings)
        }
    }
    
    val petrinet: ResetInhibitorNet
    
    private val pnmlMarking: Marking
    
    init {
        PnmlLoader.loadPetrinet(jsonSettings.petrinetSetup.petrinetFile).also { result ->
            petrinet = result.first
            pnmlMarking = result.second
        }
    }
    
    fun updateInhResetArcsFromSettings() {
        with(jsonSettings.petrinetSetup) {
            petrinet.markInhResetArcsByIds(inhibitorArcIds ?: emptyList(), resetArcIds ?: emptyList())
        }
    }
    
    /** When we are ready for generation. */
    fun getGenerationKit(): PetrinetGenerators.GenerationKit<GenerationDescription> {
        
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        val generationDescription = builder.buildDescription()
    
        var (initialMarking, finalMarking) = builder.buildMarking()
        if (jsonSettings.petrinetSetup.marking.isUsingInitialMarkingFromPnml) {
            initialMarking = pnmlMarking
        }
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
}