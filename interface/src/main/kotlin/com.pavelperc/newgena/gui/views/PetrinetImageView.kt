package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.PetrinetDrawer
import com.pavelperc.newgena.gui.customfields.ImageViewer
import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.gui.customfields.toggleCheckbox
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
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
    
    var tmpImageFile: File? = null
    
    
    private val drawArcIds = SimpleBooleanProperty(false)
    private val drawTransitionIds = SimpleBooleanProperty(false)
    private val drawPlaсeIds = SimpleBooleanProperty(true)
    private val drawTransitionNames = SimpleBooleanProperty(true)
    private val drawLegend = SimpleBooleanProperty(true)
    private val drawLabel = SimpleBooleanProperty(false)
    private val drawVertical = SimpleBooleanProperty(false)
    private val drawFinalMarking = SimpleBooleanProperty(true)
    
    
    override fun onBeforeShow() {
        super.onBeforeShow()
        root.setPrefSize(1000.0, 600.0)
    }
    
    /** [isFirstDraw] is true when we do not update an already drawn image. */
    fun draw(updateInhResetArcs: Boolean = false, updateZoom: Boolean = false) {
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
                    graphLabelStr = if (drawLabel.value) petrinet.label else "",
                    drawArcIds = drawArcIds.value,
                    drawLegend = drawLegend.value,
                    drawTransitionIds = drawTransitionIds.value,
                    drawPlaceIds = drawPlaсeIds.value,
                    drawTransitionNames = drawTransitionNames.value,
                    drawVertical = drawVertical.value,
                    drawFinalMarking = drawFinalMarking.value
            ).makeGraph()
        }
        
        if (graph != null) {
            val imageFile = createTempFile(System.currentTimeMillis().toString(), ".svg")
            graph.toGraphviz().render(Format.SVG).toFile(imageFile)

//            tmpImageFile?.delete()
            tmpImageFile = imageFile

//            if (drawVertical.value) {
//                imageViewer.drawFile(imageFile, resetSize = true)
//            } else {
//                imageViewer.drawFile(imageFile, adjustSize = true)
//            }
            imageViewer.drawFile(
                    imageFile,
                    resetSize = updateZoom && drawVertical.value,
                    adjustSize = updateZoom && !drawVertical.value
            )
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
            
            fun drawCheckbox(text: String, prop: SimpleBooleanProperty, updateZoom: Boolean = false) {
                toggleCheckbox(text, prop) {
                    action { draw(updateZoom = updateZoom) }
                }
            }
            
            drawCheckbox("Draw vertical", drawVertical, true)
            label("Show:")
            drawCheckbox("Arc ids", drawArcIds)
            drawCheckbox("Place ids", drawPlaсeIds)
            drawCheckbox("Transition ids", drawTransitionIds)
            drawCheckbox("Transition names", drawTransitionNames)
            drawCheckbox("Legend", drawLegend)
            drawCheckbox("FinalMarking", drawFinalMarking)
            drawCheckbox("Label", drawLabel)
        }
        
        imageViewer.apply {
            attachTo(this@vbox)
        }
        
    }
}
