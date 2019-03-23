package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.exportXml
import com.pavelperc.newgena.utils.xlogutils.toList
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox
import org.processmining.log.models.EventLogArray
import tornadofx.*

class GenerationView() : View("My View") {
    
    val generationKit: PetrinetGenerators.GenerationKit<*> by param()
    val outputFolder: String by param()
    
    
    val logArrayObservable = observableList<String>()
    
    var eventLogArrayProp = SimpleObjectProperty<EventLogArray?>(null)
    var eventLogArray by eventLogArrayProp
    
    val pb = ProgressBar()
    
    override val root = Form()
    val vbox: VBox
    
    init {
        with(root) {
            vbox = vbox {
                button("back") {
                    action {
                        replaceWith<SettingsView>(ViewTransition.Slide(0.2.seconds).reversed())
                    }
                }
                
                pb.attachTo(this)
                
                button("save to file") {
                    enableWhen(eventLogArrayProp.isNotNull)
                    action {
                        eventLogArray!!.exportXml("${outputFolder}/${generationKit.petrinet.label}.xes")
                        
                        notification("Saved to folder $outputFolder")
                    }
                }
                
                listview(logArrayObservable)
                
            }
        }
    }
    
    override fun onDock() {
        super.onDock()
        
        runAsync {
            PetrinetGenerators.generateFromKit(generationKit) { progress, maxProgress ->
                runLater {
                    pb.progress = progress.toDouble() / maxProgress
                }
            }
        } ui { logArray ->
            pb.progress = 1.0
            logArray.toList().map { it.eventNames() }.joinToString("\n").also { println(it) }
            
            eventLogArray = logArray
            val forList = logArray.eventNames().map { event ->
                event.toString()
            }
            logArrayObservable.setAll(forList)
        }
    }
}