package com.pavelperc.newgena.gui.views.settingsEditors

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.*
import javafx.event.EventTarget
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import org.processmining.models.time_driven_behavior.NoiseEvent
import tornadofx.*


/**  */
class NoiseEventsEditor(
        initialObjects: List<NoiseEvent>,
        val onSuccess: (List<NoiseEvent>) -> Unit = {}
) : Fragment("NoiseEventsEditor") {
    
    private val activityConverter = object : StringConverter<Any>() {
        override fun toString(obj: Any?): String = obj.toString()
        override fun fromString(string: String): Any = string
    }
    
    class NoiseEventModel(initial: NoiseEvent) : ItemViewModel<NoiseEvent>(initial) {
        
        val activity = bind(NoiseEvent::activity)
        
        val executionTimeSeconds = bind(NoiseEvent::executionTimeSeconds)
        val maxTimeDeviationSeconds = bind(NoiseEvent::maxTimeDeviationSeconds)
    }
    
    // without a copy the noise in onsuccess is considered unchanged!!! and we have some bad links.
    private val objects = initialObjects.map { it.copy() }.observable()
    
    // --- HEADER ---
    fun EventTarget.header() {
        hbox {
            addClass(Styles.addItemRoot)
            
            
            
            form {
                val model = NoiseEventModel(NoiseEvent(""))
                
                fun commit(): Boolean {
                    if (model.commit()) {
                        // create a copy
                        objects.add(model.item.copy())
                        return true
                    }
                    return false
                }
                
                fieldset {
                    
                    field("Activity") {
                        textfield(model.activity, activityConverter) {
                            validator { newActivity ->
                                if (newActivity == null || newActivity.isEmpty())
                                    error("Should not be empty.")
                                else null
                            }
                            promptText = "Click enter to add."
                            action {
                                if (commit()) {
                                    selectAll()
                                }
                            }
                        }
                    }
                    longField(model.executionTimeSeconds, nonNegative = true) {
                        action {
                            commit()
                        }
                    }
                    
                    longField(model.maxTimeDeviationSeconds, nonNegative = true) {
                        action {
                            commit()
                        }
                    }
                }
                button("Add") {
                    enableWhen(model.valid)
                    action {
                        commit()
                    }
                }
            }
            
            button("save") {
                shortcut("Ctrl+S")
                tooltip("Ctrl+S")
    
                addEventFilter(KeyEvent.KEY_PRESSED) {
                    if (it.code == KeyCode.ENTER) {
//                        if (it.target is Button && !it.isControlDown)
//                            return@addEventFilter
                        fire()
                    }
                }
                action {
                    onSuccess(objects)
                    close()
                }
            }
        }
    }
    
    
    override val root = vbox {
        prefWidth = 600.0
        header()
        
        tableview(objects) {
            isEditable = true
            useMaxSize = true
            vgrow = Priority.ALWAYS
            regainFocusAfterEdit()
            selectionModel.isCellSelectionEnabled = true
            columnResizePolicy = SmartResize.POLICY
    
    
            validatedColumn(NoiseEvent::activity, activityConverter, required = true)
    
            validatedLongColumn(NoiseEvent::executionTimeSeconds, nonNegative = true)
    
            validatedLongColumn(NoiseEvent::maxTimeDeviationSeconds, nonNegative = true)
            
            makeDeleteColumn()
        }
    }
}