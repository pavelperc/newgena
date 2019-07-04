package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.models.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.models.markInhResetArcsByIds
import com.pavelperc.newgena.utils.common.emptyMarking
import org.processmining.models.GenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking

/** Loads the model and creates a generation kit from [jsonSettings]. */
class JsonSettingsController(var jsonSettings: JsonSettings) {
    
    companion object {
        fun createFromFilePath(settingsFilePath: String): JsonSettingsController {
            return JsonSettingsController(JsonSettings.fromFilePath(settingsFilePath))
        }
    }
    
    val petrinet: ResetInhibitorNet
    
    /** Marking, loaded from petrinet model.*/
    private val pnmlMarking: Marking
    
    
    init {
        // load petrinet
        PnmlLoader.loadPetrinetWithOwnParser(jsonSettings.petrinetSetup.petrinetFile).also { result ->
            petrinet = result.first
            pnmlMarking = result.second
        }
    }
    
    
    val initialMarking: Marking
        get() = with(jsonSettings.petrinetSetup.marking) {
            if (isUsingInitialMarkingFromPnml) // what about concurrency?? 
                emptyMarking()
            else
                JsonSettingsBuilder.buildMarkingOnly(this, petrinet).first
        }
    
    val finalMarking: Marking
        get() = JsonSettingsBuilder.buildMarkingOnly(jsonSettings.petrinetSetup.marking, petrinet).second
    
    
    /** Changes the [petrinet]. */
    private fun updateInhResetArcsFromSettings() {
        with(jsonSettings.petrinetSetup) {
            if (inhibitorArcIds.size + resetArcIds.size > 0) {
                petrinet.deleteAllInhibitorResetArcs()
                petrinet.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
            }
        }
    }
    
    /** When we are ready for generation. */
    fun getGenerationKit(): PetrinetGenerators.GenerationKit<GenerationDescription> {
        updateInhResetArcsFromSettings()
        
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        val generationDescription = builder.buildDescription()
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
}