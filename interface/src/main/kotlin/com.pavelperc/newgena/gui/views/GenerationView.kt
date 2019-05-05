package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.exportXml
import com.pavelperc.newgena.utils.xlogutils.toList
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Parent
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import tornadofx.*

class GenerationView() : View("My View") {
    
    val generationKit: PetrinetGenerators.GenerationKit<*> by param()
    val outputFolder: String by param()
    
    
    val logArrayObservable = observableList<XTrace>()
    
    var eventLogArrayProp = SimpleObjectProperty<EventLogArray?>(null)
    var eventLogArray by eventLogArrayProp
    
    val pb = ProgressBar()
    
    
    override val root = vbox {
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
        button("show petrinet") {
            action {
                find<PetrinetImageView>().also { it.draw() }.openWindow(owner = this@GenerationView.currentStage)
            }
        }
        
        listview(logArrayObservable) {
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            cellFormat {
                text = item.eventNames().toString()
                
                onDoubleClick {
                    OneTraceFragment(item).openWindow()
                }
            }
        }
    }
    
    override fun onDock() {
        super.onDock()
        logArrayObservable.clear()
        eventLogArray = null
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
//            val forList = logArray.eventNames().map { event ->
//                event.toString()
//            }
            
            logArrayObservable.setAll(logArray.toList().flatten())
        }
    }
}


class OneTraceFragment(
        trace: XTrace,
        time: Boolean = true,
        lifecycle: Boolean = true,
        resources: Boolean = true,
        complexResources: Boolean = true
) : Fragment(
        trace.attributes["concept:name"]?.toString() ?: "One Trace"
) {
    
    
    override val root = vbox {
        tableview(trace.observable()) {
            columnResizePolicy = SmartResize.POLICY
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            selectionModel = null
            
            fun attrColumn(name: String, attr: String) {
                column<XEvent, String>(name, valueProvider = { cellDataFeatures ->
                    val value = cellDataFeatures.value
                    SimpleStringProperty(value.attributes[attr].toString())
                }).apply {
                    isSortable = false
                }
            }
            
            attrColumn("Name", "concept:name")
            attrColumn("Timestamp", "time:timestamp")
            attrColumn("Lifecycle", "lifecycle:transition")
            attrColumn("Resource", "org:resource")
            attrColumn("Role", "org:role")
            attrColumn("Group", "org:group")
            
        }
    }
}



