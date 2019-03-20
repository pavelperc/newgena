package com.pavelperc.newgena.loaders.settings

import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.models.markInhResetArcsByIds
import org.processmining.models.GenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import java.io.File
import java.lang.UnsupportedOperationException

class JsonSettingsController(var jsonSettings: JsonSettings) {
    
    companion object {
        fun createFromFilePath(settingsFilePath: String): JsonSettingsController {
            val jsonSettingsStr = File(settingsFilePath).readText()
            val jsonSettings = JsonSettings.fromJson(jsonSettingsStr)
            
            return JsonSettingsController(jsonSettings)
        }
    }
    
    var petrinet: ResetInhibitorNet? = null
    
    /** Marking, loaded from petrinet model.*/
    private var pnmlMarking: Marking? = null
    
    val initialMarking: Marking
        get() =// what about concurrency??
            if (jsonSettings.petrinetSetup.marking.isUsingInitialMarkingFromPnml) {
                pnmlMarking ?: Marking()
            } else {
                petrinet?.let { petrinet ->
                    JsonSettingsBuilder(petrinet, jsonSettings).buildMarking().first
                } ?: Marking()
            }
    
    val finalMarking: Marking
        get() = petrinet?.let { petrinet ->
            JsonSettingsBuilder(petrinet, jsonSettings).buildMarking().second
        } ?: Marking()
    
    
    /** Tries to load petrinet, selected in settings. */
    fun loadPetrinet() {
        PnmlLoader.loadPetrinetWithOwnParser(jsonSettings.petrinetSetup.petrinetFile).also { result ->
            petrinet = result.first
            pnmlMarking = result.second
        }
    }
    
    /** Changes the [petrinet]. */
    private fun updateInhResetArcsFromSettings() {
        with(jsonSettings.petrinetSetup) {
            petrinet?.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
        }
    }
    
    /** When we are ready for generation. */
    fun getGenerationKit(): PetrinetGenerators.GenerationKit<GenerationDescription> {
        val petrinet = petrinet
                ?: throw UnsupportedOperationException("Can not get GenerationKit. Petrinet is not loaded.")
        
        updateInhResetArcsFromSettings()
        
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        val generationDescription = builder.buildDescription()
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
}