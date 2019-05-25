package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.PetrinetDrawer
import com.pavelperc.newgena.gui.app.MyApp
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.ImageViewer
import com.pavelperc.newgena.gui.customfields.notification
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.nio.file.Files

class PetrinetImageView : View("Petrinet Viewer.") {
    //class PetrinetImageView : Fragment("Petrinet Viewer.") {
    
    val controller by inject<SettingsUIController>()
    
    val tmpImageFileProp = SimpleObjectProperty<File>(null)
    var tmpImageFile by tmpImageFileProp
    
    
    val drawArcIdsProp = SimpleBooleanProperty(true)
    val drawTransitionIdsProp = SimpleBooleanProperty(false)
    val drawTransitionNamesProp = SimpleBooleanProperty(true)
    val drawLegendProp = SimpleBooleanProperty(true)
    val drawLabelProp = SimpleBooleanProperty(false)
    val drawVerticalProp = SimpleBooleanProperty(false)
    
    
    override fun onBeforeShow() {
        super.onBeforeShow()
        root.setPrefSize(1000.0, 600.0)
    }
    
    /** [isFirstDraw] is true when we do not update an already drawn image. */
    fun draw(isFirstDraw: Boolean = false) {
        controller.updateInhResetArcsFromModel()
        
        // update window title.
        title = controller.petrinet?.label ?: "No petrinet loaded."
        
        val (initialMarking, finalMarking) = controller.markings
        
        val graph = controller.petrinet?.let { petrinet ->
            PetrinetDrawer(
                    petrinet,
                    initialMarking = initialMarking,
                    finalMarking = finalMarking,
                    graphLabelStr = if (drawLabelProp.value) petrinet.label else "",
                    drawArcIds = drawArcIdsProp.value,
                    drawLegend = drawLegendProp.value,
                    drawTransitionIds = drawTransitionIdsProp.value,
                    drawTransitionNames = drawTransitionNamesProp.value,
                    drawVertical = drawVerticalProp.value
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
                    val fileStr = controller.petrinetSetupModel.petrinetFile.value?.replace(".pnml", ".svg")
                    if (fileStr != null) {
                        confirm("Save image to $fileStr?") {
                            
                            if (tmpImageFile != null) {
                                tmpImageFile!!.copyTo(File(fileStr))
                                notification("Saved to $fileStr")
                            } else {
                                error("No Rendered image found.")
                            }
                            
                        }
                    }
                }
            }
            button("Update") {
                action {
                    try {
                        draw()
                    } catch (e: Exception) {
                        error("Can not update image:", e.message)
//                            imgProp.value = null
                    }
                }
                
            }
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
            checkbox("Label", drawLabelProp) {
                action { draw() }
            }
        }
        
        ImageViewer(tmpImageFileProp, this@PetrinetImageView).apply {
            attachTo(this@vbox)
        }
        
    }
}
