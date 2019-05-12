package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.exportXml
import com.pavelperc.newgena.utils.xlogutils.toList
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import tornadofx.*

class GenerationView() : View("My View") {
    
    val generationKit: PetrinetGenerators.GenerationKit<*> by param()
    val outputFolder: String by param()
    
    
    /** Log index and trace. */
    val logArrayObservable = observableList<Pair<Int, XTrace>>()
    
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
                val (logIndex, trace) = item
                
                text = trace.eventNames().toString()
                
                onDoubleClick {
                    val traceName = trace.attributes["concept:name"]?.toString() ?: "Some Trace"
                    val name = "Log ${logIndex + 1}, $traceName"
                    find<OneTraceFragment>(mapOf(
                            "trace" to trace,
                            "name" to name
                    )).openWindow()
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
            
            logArrayObservable.setAll(logArray.toList().mapIndexed { i, log ->
                log.map { trace -> i to trace }
            }.flatten())
        }
    }
}


class OneTraceFragment : Fragment() {
    
    val observableTrace: ObservableList<XEvent> = observableList()
    
    override fun onDock() {
        super.onDock()
        
        title = params["name"]?.toString() ?: "Some trace"
        
        observableTrace.setAll(params["trace"] as? XTrace ?: mutableListOf<XEvent>())
    }
    
    val showSimpleTime = SimpleBooleanProperty(false)
    
    
    override val root = vbox {
        
        checkbox("Show simple time.", showSimpleTime)
        
        tableview(observableTrace) {
            showSimpleTime.onChange {
                this@tableview.refresh()
            }
            
            columnResizePolicy = SmartResize.POLICY
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            selectionModel = null
            
            fun attrColumn(name: String, attr: String, attrConverter: (String) -> String = { it }) {
                column<XEvent, String>(name, valueProvider = { cellDataFeatures ->
                    val value = cellDataFeatures.value
                    val attrStr = value.attributes[attr].toString()
                    SimpleStringProperty(attrConverter(attrStr))
                }).apply {
                    isSortable = false
                }
            }
            
            attrColumn("Name", "concept:name")
            
            val timeRegex = Regex("""\d\d:\d\d:\d\d""")
            
            attrColumn("Timestamp", "time:timestamp") { timestamp ->
                if (showSimpleTime.value) {
                    timeRegex.find(timestamp)?.value ?: timestamp
                } else {
                    timestamp
                }
            }
            attrColumn("Lifecycle", "lifecycle:transition")
            attrColumn("Resource", "org:resource")
            attrColumn("Role", "org:role")
            attrColumn("Group", "org:group")
            
        }
    }
}



