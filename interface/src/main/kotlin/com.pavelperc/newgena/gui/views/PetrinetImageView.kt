package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.PetrinetDrawer
import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.ImageViewer
import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.gui.customfields.toggleCheckbox
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ToggleButton
import javafx.scene.paint.Color
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
    
    var tmpImageFile: File? = null
    
    
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
            val imageFile = createTempFile(System.currentTimeMillis().toString(), ".svg")
            graph.toGraphviz().render(Format.SVG).toFile(imageFile)

//            tmpImageFile?.delete()
            tmpImageFile = imageFile
            
            if (drawVerticalProp.value) {
                imageViewer.drawFile(imageFile, resetSize = true)
            } else {
                imageViewer.drawFile(imageFile, adjustSize = true)
            }
        }
    }
    
    val imageViewer = ImageViewer(this)
    
    
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
                        notification("Can not update image:", e.message ?: "", duration = 4000)
//                            imgProp.value = null
                    }
                }
            }
            
            hgap = 5.0
            vgap = 5.0
            paddingAll = 5.0
//            paddingBottom = 5.0
//            paddingTop = 5.0
//            paddingVertical = 5.0
            
            toggleCheckbox("Draw vertical", drawVerticalProp) {
                action { draw() }
            }
            label("  Show: ")
            toggleCheckbox("Arc ids", drawArcIdsProp) {
                action { draw() }
            }
            toggleCheckbox("Transition ids", drawTransitionIdsProp) {
                action { draw() }
            }
            toggleCheckbox("Transition names", drawTransitionNamesProp) {
                action { draw() }
            }
            toggleCheckbox("Legend", drawLegendProp) {
                action { draw() }
            }
            toggleCheckbox("FinalMarking", drawFinalMarkingProp) {
                action { draw() }
            }
            toggleCheckbox("Label", drawLabelProp) {
                action { draw() }
            }
        }
        
        imageViewer.apply {
            attachTo(this@vbox)
        }
        
    }
}
