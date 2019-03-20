package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.gui.controller.SettingsUIController
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.toGraphviz
import javafx.embed.swing.SwingFXUtils
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.*
import javafx.scene.web.WebView
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import javax.swing.text.html.ImageView

class PetrinetImageView : Fragment("Petrinet Viewer.") {
    override val root = StackPane()
    
    val controller by inject<SettingsUIController>()
    
    init {
        title = controller.petrinet!!.label
        
        val graph = controller.petrinet!!.toGraphviz(controller.jsonSettingsController.initialMarking)
        val bufferedImage = graph.toGraphviz().render(Format.SVG).toImage()
        val img = SwingFXUtils.toFXImage(bufferedImage, null)
        
        with(root) {
            imageview(img) {
                isPreserveRatio = true
                fitWidthProperty().bind(root.widthProperty())
                fitHeightProperty().bind(root.heightProperty())
            }
        }
        
    }
    
}
