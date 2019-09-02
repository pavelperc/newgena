package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.PetrinetSetupModel
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.petrinet.petrinetExtensions.deepCopy
import com.pavelperc.newgena.petrinet.petrinetExtensions.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.petrinet.petrinetExtensions.markInhResetArcsByIds
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.profile
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import tornadofx.*


class PetrinetUIController() {
    
    /** It is false when the petrinet is not null and was updated. */
    val isPetrinetSaved = SimpleBooleanProperty(true)
    val isPetrinetNotSaved = isPetrinetSaved.not()
    
    val petrinetProp = SimpleObjectProperty<ResetInhibitorNet?>(null)
    
    var petrinet by petrinetProp
    private set
    
    var pnmlMarking = emptyMarking()
        private set
    
    var loadedPetrinetFilePath: String? = null
        private set
    
    fun unloadPetrinet() {
        isPetrinetSaved.set(true)
        petrinet = null
        pnmlMarking = emptyMarking()
        loadedPetrinetFilePath = null
    }
    
    /** Loading petrinet from petrinetFilePath in a text field. */
    fun loadPetrinet(petrinetFilePath: String): ResetInhibitorNet {
        profile("Loading petrinet:") {
            unloadPetrinet()
            // may crash
            PnmlLoader.loadPetrinetWithOwnParser(petrinetFilePath).also { result ->
                petrinet = result.first
                pnmlMarking = result.second
            }
        }
        loadedPetrinetFilePath = petrinetFilePath
        isPetrinetSaved.set(true)
        return petrinet!!
    }
    
    fun loadUpdatedPetrinet(updatedPetrinet: ResetInhibitorNet) {
        isPetrinetSaved.set(false)
        petrinet = updatedPetrinet
    }
}