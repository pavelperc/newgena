package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.MyApp
import com.pavelperc.newgena.gui.customfields.Validator
import com.pavelperc.newgena.gui.customfields.arrayField
import com.pavelperc.newgena.gui.customfields.confirmed
import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.petrinet.petrinetExtensions.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.petrinet.petrinetExtensions.markInhResetArcsByIds
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.stage.Stage
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import tornadofx.*


class ArcsEditorView(
        /** Can be changed. Should be a copy. */
        val petrinet: ResetInhibitorNet,
        val onUpdatePetrinet: (petrinet: ResetInhibitorNet) -> Unit = {}
) : View("ArcsEditor") {
    companion object {
        // it is used just for working validators in arrayField. We don't have backing item here.
        class Model(petrinet: ResetInhibitorNet) : ViewModel() {
            val inhibitorArcIds = bind(forceObjectProperty = true) {
                SimpleObjectProperty(null, "inhibitorArcIds",
                        petrinet.edges.filter { it is InhibitorArc }.map { it.pnmlId }.toMutableList())
            }
            // forceObject to wrap MutableList correctly, (See viewModels class)
            val resetArcIds = bind(forceObjectProperty = true) {
                SimpleObjectProperty(null, "resetArcIds", petrinet.edges.filter { it is ResetArc }.map { it.pnmlId }.toMutableList())
            }
        }
    }
    
    val inputEdgeIdsWithHints = petrinet.transitions
            .flatMap { petrinet.getInEdges(it) }
            .map { it.pnmlId to "${it.source.pnmlId}->${it.target.pnmlId}" }
            .toMap()
    
    // wrapper for storing arc ids
    val model = Model(petrinet)
    
    override val root = Form().apply {
        minWidth = MyApp.WINDOW_WIDTH
        fieldset {
            val validateEdges: Validator<List<String>> = { list ->
                val input = list.toSet()
                val unknown = input - input.intersect(inputEdgeIdsWithHints.keys)
                if (unknown.isNotEmpty()) {
                    warning("Not found input edges: $unknown")
                } else null
            }
            
            arrayField(
                    model.inhibitorArcIds,
                    listValidator = validateEdges,
                    predefinedValuesToHints = { inputEdgeIdsWithHints },
                    hintName = "hint",
                    valuesName = "id"
            )
            arrayField(
                    model.resetArcIds,
                    listValidator = validateEdges,
                    predefinedValuesToHints = { inputEdgeIdsWithHints },
                    hintName = "hint",
                    valuesName = "id"
            )
            
            hbox {
                button("Done") {
                    shortcut("Ctrl+S")
                    tooltip("Ctrl+S")
                    
                    action {
                        try {
                            petrinet.deleteAllInhibitorResetArcs()
                            petrinet.markInhResetArcsByIds(model.inhibitorArcIds.value, model.resetArcIds.value)
                            
                            onUpdatePetrinet(petrinet)
                            close()
                        } catch (e: Exception) {
                            error("Saving error", e.message ?: "")
                        }
                    }
                }
            }
        }
    }
    
    fun setOnCloseAction(myStage: Stage?) {
        myStage?.onCloseRequest = EventHandler { event ->
            if (!confirmed("Cancel editing?", "Your changes will be lost.")) {
                event.consume() // cancels closing
            }
        }
    }
}