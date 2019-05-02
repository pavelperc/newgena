package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.QuiteIntConverter
import com.pavelperc.newgena.gui.customfields.longField
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
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
            prefWidth = 550.0
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
                                if (newActivity.isNullOrEmpty())
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
                    longField(model.executionTimeSeconds, nextValidator={ value ->
                        if (value < 0L) error("Should not be negative.") else null
                    }) {
                        action { 
                            commit()
                        }
                    }
                    
                    longField(model.maxTimeDeviationSeconds, nextValidator =  { value ->
                        if (value < 0L) error("Should not be negative.") else null
                    }) {
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
        
        header()
        
        tableview(objects) {
            isEditable = true
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            column("Activity",NoiseEvent::activity) {
//                makeEditable(activityConverter)
                
                cellFormat {
                    val model = NoiseEventModel(rowItem)
                    graphic = vbox {
                        textfield(model.activity, activityConverter) {
                            removeWhen(editingProperty().not())
                            validator { newActivity ->
                                if (newActivity.isNullOrEmpty())
                                    error("Should not be empty.")
                                else null
                            }
                            // Call cell.commitEdit() only if validation passes
                            action {
                                if(model.commit()) {
//                                    cancelEdit()
                                    commitEdit(model.activity.value)
                                }
                            }
    
                            // jump on value when editing starts.
                            whenVisible { requestFocus() }
                        }
                        label(model.activity) {
//                            removeWhen(editingProperty())
                        }
                    }
                    onEditCancel = EventHandler { 
                        model.rollback()
                    }
                }
            }
            column("executionTimeSeconds", NoiseEvent::executionTimeSeconds) {
                makeEditable()
            }
            column("maxTimeDeviationSeconds", NoiseEvent::maxTimeDeviationSeconds) {
                makeEditable()
            }
            column("Delete", NoiseEvent::activity) {
                cellFormat {
                    graphic = button {
                        style {
                            backgroundColor += Color.TRANSPARENT
                        }
                        graphic = Styles.closeIcon()
                        action {
                            objects.remove(rowItem)
                        }
                    }
                }
            }
        }
    }
}