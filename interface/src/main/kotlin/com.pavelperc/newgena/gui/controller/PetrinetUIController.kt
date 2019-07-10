package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.PetrinetSetupModel
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.petrinet.petrinetExtensions.deepCopy
import com.pavelperc.newgena.petrinet.petrinetExtensions.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.petrinet.petrinetExtensions.markInhResetArcsByIds
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.profile
import javafx.beans.property.SimpleObjectProperty
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import tornadofx.*


class PetrinetUIController() {
    
    val petrinetProp = SimpleObjectProperty<ResetInhibitorNet?>(null)
    
    var petrinet by petrinetProp
    private set
    
    /** A petrinet copy with initial arcs. */
    private var petrinetCopy: ResetInhibitorNet? = null
    
    var pnmlMarking = emptyMarking()
        private set
    
    var loadedPetrinetFilePath: String? = null
        private set
    
    fun unloadPetrinet() {
        petrinet = null
        petrinetCopy = null
        pnmlMarking = emptyMarking()
        loadedPetrinetFilePath = null
    }
    
    /** Loading petrinet from petrinetFile in a text field. */
    fun loadPetrinet(petrinetFilePath: String): ResetInhibitorNet {
        profile("Loading petrinet:") {
            petrinet = null
            loadedPetrinetFilePath = null
            PnmlLoader.loadPetrinetWithOwnParser(petrinetFilePath).also { result ->
                petrinet = result.first
                petrinetCopy = result.first.deepCopy()
                
                pnmlMarking = result.second
            }
        }
        loadedPetrinetFilePath = petrinetFilePath
        return petrinet!!
    }
    
    
    fun updateInhResetArcsFromModel(petrinetSetupModel: PetrinetSetupModel) {
        if (petrinetSetupModel.irArcsFromPnml.value) {
            petrinet = petrinetCopy?.deepCopy()
        } else {
            petrinet?.also { petrinet ->
                val resetArcIds = petrinetSetupModel.resetArcIds.value
                val inhibitorArcIds = petrinetSetupModel.inhibitorArcIds.value
            
                // what if we fail after deleting?
                petrinet.deleteAllInhibitorResetArcs()
                petrinet.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
            } ?: IllegalStateException("Petrinet is not loaded.")
        }
    }
}