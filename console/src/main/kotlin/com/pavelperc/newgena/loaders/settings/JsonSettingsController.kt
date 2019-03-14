package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.models.markInhResetArcsByIds
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
    
    private val pnmlMarking: Marking
    
    val petrinet: ResetInhibitorNet
    
    init {
        PnmlLoader.loadPetrinetWithOwnParser(jsonSettings.petrinetSetup.petrinetFile).also { result ->
            petrinet = result.first
            pnmlMarking = result.second
        }
        // todo: save initial arc state
        
        updateInhResetArcsFromSettings()
    }
    
    fun updateInhResetArcsFromSettings() {
        with(jsonSettings.petrinetSetup) {
            petrinet.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
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