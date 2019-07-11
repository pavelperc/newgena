package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.PetrinetDrawer
import com.pavelperc.newgena.gui.customfields.ImageViewer
import com.pavelperc.newgena.gui.customfields.notification
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*
import java.io.File

interface PetrinetDrawProvider {
    val petrinet: ResetInhibitorNet?
    
    fun requestPetrinetUpdate()
    
    val markings: Pair<Marking, Marking>
    
    /** File path, ended with .pnml extension. */
    val pnmlLocationForDrawing: String?
}

class PetrinetImageView : View("Petrinet Viewer.") {
    //class PetrinetImageView : Fragment("Petrinet Viewer.") {
    
    val petrinetDrawProvider by param<PetrinetDrawProvider>()
    
    val tmpImageFileProp = SimpleObjectProperty<File>(null)
    var tmpImageFile by tmpImageFileProp
    
    
    private val drawArcIdsProp = SimpleBooleanProperty(false)
    private val drawTransitionIdsProp = SimpleBooleanProperty(false)
    private val drawTransitionNamesProp = SimpleBooleanProperty(true)
    private val drawLegendProp = SimpleBooleanProperty(true)
    private val drawLabelProp = SimpleBooleanProperty(false)
    private val drawVerticalProp = SimpleBooleanProperty(false)
    private val drawFinalMarkingProp = SimpleBooleanProperty(true)
    
    
    override fun onBeforeShow() {
        super.onBeforeShow()
        root.setPrefSize(1000.0, 600.0)
    }
    
    /** [isFirstDraw] is true when we do not update an already drawn image. */
    fun draw(updateInhResetArcs: Boolean = false) {
        if (updateInhResetArcs) {
            petrinetDrawProvider.requestPetrinetUpdate()
        }
        
        // update window title.
        title = petrinetDrawProvider.petrinet?.label ?: "No petrinet loaded."
        
        val (initialMarking, finalMarking) = petrinetDrawProvider.markings
        
        val graph = petrinetDrawProvider.petrinet?.let { petrinet ->
            PetrinetDrawer(
                    petrinet,
                    initialMarking = initialMarking,
                    finalMarking = finalMarking,
                    graphLabelStr = if (drawLabelProp.value) petrinet.label else "",
                    drawArcIds = drawArcIdsProp.value,
                    drawLegend = drawLegendProp.value,
                    drawTransitionIds = drawTransitionIdsProp.value,
                    drawTransitionNames = drawTransitionNamesProp.value,
                    drawVertical = drawVerticalProp.value,
                    drawFinalMarking = drawFinalMarkingProp.value
            ).makeGraph()
        }
        
        if (graph != null) {
            tmpImageFile = createTempFile(System.currentTimeMillis().toString(), ".svg")
            graph.toGraphviz().render(Format.SVG).toFile(tmpImageFile)
            
        } else {
            
        }
    }
    
    override val root = vbox {
        flowpane {
            alignment = Pos.BOTTOM_LEFT
            button("Save image to file") {
                action {
                    val fileStr = petrinetDrawProvider.pnmlLocationForDrawing?.removeSuffix(".pnml")?.plus(".svg")
                    if (fileStr != null) {
                        confirm("Save image to $fileStr?") {
                            
                            if (tmpImageFile != null) {
                                val newImageFile = File(fileStr)
                                tmpImageFile!!.copyTo(newImageFile, overwrite = true)
                                notification("Saved to $fileStr")
                            } else {
                                error("No Rendered image found.")
                            }
                            
                        }
                    }
                }
            }
            button("Update") {
                shortcut("Ctrl+R")
                tooltip("Ctrl+R")
                action {
                    try {
                        draw(true)
                    } catch (e: Exception) {
                        notification("Can not update image:", e.message ?: "", duration = 3000)
//                            imgProp.value = null
                    }
                }
                
            }

//                addClass(Styles.graphvizButtonsPanel)
            spacing = 3.0
            checkbox("Draw vertical", drawVerticalProp) {
                action { draw() }
            }
            label("  Show: ")
            checkbox("Arc ids", drawArcIdsProp) {
                action { draw() }
            }
            checkbox("Transition ids", drawTransitionIdsProp) {
                action { draw() }
            }
            checkbox("Transition names", drawTransitionNamesProp) {
                action { draw() }
            }
            checkbox("Legend", drawLegendProp) {
                action { draw() }
            }
            checkbox("FinalMarking", drawFinalMarkingProp) {
                action { draw() }
            }
            checkbox("Label", drawLabelProp) {
                action { draw() }
            }
        }
        
        ImageViewer(tmpImageFileProp, this@PetrinetImageView).apply {
            attachTo(this@vbox)
        }
        
    }
}
