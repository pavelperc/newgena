package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.notification
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import tornadofx.*
import java.io.File

class PetrinetImageView : View("Petrinet Viewer.") {
    override val root = BorderPane()
    
    val controller by inject<SettingsUIController>()
    
    private val renderedImageProp = SimpleObjectProperty<Renderer>(null)
    private var renderedImage by renderedImageProp
    
    val imgProp = SimpleObjectProperty<Image>(null)
    
    var drawArcIdsProp = SimpleBooleanProperty(true)
    
    fun draw() {
        controller.updateInhResetArcsFromModel()
        
        // update window title.
        title = controller.petrinet?.label ?: "No petrinet loaded."
        
        val (initialMarking, finalMarking) = controller.markings
        
        val graph = controller.petrinet?.toGraphviz(
                initialMarking = initialMarking,
                graphLabel = "",
                finalMarking = finalMarking,
                drawArcIds = drawArcIdsProp.value
        )
        
        if (graph != null) {
            val renderedImage = graph.toGraphviz().render(Format.SVG)
            val bufferedImage = renderedImage.toImage()
            val img = SwingFXUtils.toFXImage(bufferedImage, null)
            
            imgProp.value = img
        } else {
            imgProp.value = null
        }
    }
    
    init {
//        imgProp.value = Image("file:examples/petrinet/cycle1/cycle1.png")
//        draw()
        with(root) {
            top = hbox {
                alignment = Pos.BOTTOM_LEFT
                button("Save image to file") {
                    action {
                        val fileStr = controller.petrinetSetupModel.petrinetFile.value?.replace(".pnml", ".svg")
                        if (fileStr != null) {
                            confirm("Save image to $fileStr?") {
                                renderedImage.toFile(File(fileStr))
                                notification("Saved to $fileStr")
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
                            imgProp.value = null
                        }
                    }
                    
                }
                checkbox("Show arc ids", drawArcIdsProp) {
                    action {
                        draw()
                    }
                }
            }
            
            center = pane {
                val pane1 = this
                stackpane {
                    alignment = Pos.CENTER
                    fitToSize(pane1)
                    
                    imageview(imgProp) {
                        isPreserveRatio = true
                        fitWidthProperty().bind(pane1.widthProperty())
                        fitHeightProperty().bind(pane1.heightProperty())
                        
                    }
                }
            }
            
        }
    }
    
}
