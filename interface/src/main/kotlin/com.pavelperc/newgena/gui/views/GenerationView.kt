package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.MyApp
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.docField
import com.pavelperc.newgena.gui.customfields.notification
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.utils.xlogutils.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.processmining.log.models.EventLogArray
import org.processmining.utils.percentCallBack
import tornadofx.*
import kotlin.concurrent.thread

/** Max number of traces in preview. */
const val MAX_TRACE_VIEW_COUNT = 100
/** Max number of evens in trace in preview. */
const val MAX_EVENT_VIEW_COUNT = 100

class GenerationView : View("Generation View") {
    
    override fun onBeforeShow() {
        // square
        root.setPrefSize(MyApp.WINDOW_WIDTH, MyApp.WINDOW_WIDTH)
    }
    
    
    val generationKit: PetrinetGenerators.GenerationKit by param()
    val controller by inject<SettingsUIController>()
    
    val outputFolder = controller.settingsModel.outputFolder
    
    /** Log index and trace. */
    val logArrayObservable = observableList<Pair<Int, XTrace>>()
    
    var eventLogArrayProp = SimpleObjectProperty<EventLogArray?>(null)
    /** Full eventLogArray for exporting. */
    var eventLogArray by eventLogArrayProp
    
    val progressBar = ProgressBar()
    val progressText = SimpleStringProperty("0 %")
    
    val tooLargeLogs = SimpleBooleanProperty(false)
    
    override fun onUndock() {
        super.onUndock()
        generationThread?.interrupt()
    }
    
    override val root = vbox {
        button("close") {
            shortcut("Ctrl+Z")
            tooltip("Ctrl+Z")
            action {
                close()
            }
        }
        
        hbox {
            progressBar.attachTo(this) {
                prefWidth = 100.0
            }
            label(progressText)
        }
        label("Too large logs. The first $MAX_EVENT_VIEW_COUNT events " +
                "in the first $MAX_TRACE_VIEW_COUNT traces are shown.") {
            removeWhen { tooLargeLogs.not() }
        }
        hbox {
            button("show petrinet") {
                action {
                    val petrinetView = find<PetrinetImageView>(
                            "petrinetDrawProvider" to find<SettingsUIController>() as PetrinetDrawProvider
                    )
                    petrinetView.draw()
                    petrinetView.openWindow(owner = this@GenerationView.currentStage)
                }
            }
            button("save to folder") {
                enableWhen(eventLogArrayProp.isNotNull)
                action {
                    eventLogArray!!.exportXml("${outputFolder.value}/${generationKit.petrinet.label}.xes")
                    
                    notification("Saved to folder ${outputFolder.value}")
                }
            }
            textfield(outputFolder).required()
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                action {
                    controller.requestOutputFolderChooseDialog()
                }
                isFocusTraversable = false
            }
        }
        
        listview(logArrayObservable) {
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            cellFormat {
                val (logIndex, trace) = item
                val trimmedEvents = trace.take(MAX_EVENT_VIEW_COUNT).toMutableList()
                
                text = trimmedEvents.map { it.name }.toString()
                onDoubleClick {
                    val traceName = trace.name ?: "Some Trace"
                    val name = "Log ${logIndex + 1}, $traceName"
                    val traceFragment = OneTraceFragment(name, trimmedEvents)
                    traceFragment.openWindow(owner = this@GenerationView.currentStage)
                }
            }
        }
    }
    
    private var generationThread: Thread? = null
    
    override fun onDock() {
        super.onDock()
        logArrayObservable.clear()
        eventLogArray = null
        tooLargeLogs.value = false
        
        
        
        generationThread = thread(isDaemon = true) {
            
            try {
                val logArray = PetrinetGenerators.generateFromKit(generationKit, percentCallBack { progress, maxProgress ->
                    runLater {
                        progressBar.progress = progress.toDouble() / maxProgress
                        progressText.value = "$progress %"
                    }
                })
                
                // onSuccess
                runLater {
                    progressBar.progress = 1.0
                    progressText.value = "100 %"
                    
                    eventLogArray = logArray
                    
                    
                    with(generationKit.description) {
                        if (numberOfLogs * numberOfTraces > MAX_TRACE_VIEW_COUNT
                                || maxNumberOfSteps > MAX_EVENT_VIEW_COUNT) {
                            
                            tooLargeLogs.set(true)
                        }
                    }
                    
                    logArrayObservable.setAll(logArray.toSeq().mapIndexed { logIndex, log ->
                        log.map { trace -> logIndex to trace }
                    }.flatten().take(MAX_TRACE_VIEW_COUNT).toList())
                }
            } catch (e: InterruptedException) {
                println(e.message)
            }
        }
    }
}


class OneTraceFragment(
        name: String,
        events: MutableList<XEvent>
) : Fragment(name) {
    
    val observableTrace: ObservableList<XEvent> = events.observable()
    val showSimpleTime = SimpleBooleanProperty(true)
    
    override val root = vbox {
        
        checkbox("Show simple time", showSimpleTime)
        
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



