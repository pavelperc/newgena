package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.gui.controller.SettingsUIController
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Renderer
import guru.nidi.graphviz.toGraphviz
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.*
import javafx.scene.web.WebView
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import java.io.File
import javax.swing.text.html.ImageView

class PetrinetImageView : Fragment("Petrinet Viewer.") {
    override val root = BorderPane()
    
    val controller by inject<SettingsUIController>()
    
    private val renderedImageProp = SimpleObjectProperty<Renderer>(null)
    private var renderedImage by renderedImageProp
    
    
    init {
        title = controller.petrinet?.label ?: "No petrinet loaded."
        
        val graph = controller.petrinet?.toGraphviz(
                controller.markings.first
        )
        
        with(root) {
            if (graph != null) {
                val renderedImage = graph.toGraphviz().render(Format.SVG)
                val bufferedImage = renderedImage.toImage()
                val img = SwingFXUtils.toFXImage(bufferedImage, null)
                
                center = imageview(img) {
                    isPreserveRatio = true
                    fitWidthProperty().bind(root.widthProperty())
                    fitHeightProperty().bind(root.heightProperty())
                }
                bottom = button("save to file") {
                    
                    action {
                        val fileStr = controller.petrinetSetupModel.petrinetFile.value?.replace(".pnml", ".svg")
                        if (fileStr != null) {
                            renderedImage.toFile(File(fileStr))
                            alert(Alert.AlertType.INFORMATION, "Saved to $fileStr")
                        }
                    }
                }
                
            } else {
                label("No Petrinet loaded.") {
                    style {
                        fontSize = 10.em
                    }
                }
            }
            
            
        }
        
    }
    
}
