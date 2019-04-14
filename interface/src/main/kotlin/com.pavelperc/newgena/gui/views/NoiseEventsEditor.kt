package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.simpleLongField
import javafx.beans.property.Property
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.processmining.models.time_driven_behavior.NoiseEvent
import tornadofx.*


/**  */
class NoiseEventsEditor(
        initialObjects: List<NoiseEvent>,
        val onSuccess: (List<NoiseEvent>) -> Unit = {}
) : Fragment("NoiseEventsEditor") {
    
    
    class ObservableNoiseEvent(noiseEvent: NoiseEvent) {
        val activity = SimpleStringProperty(noiseEvent.activity.toString())
        val executionTimeSeconds = SimpleLongProperty(noiseEvent.executionTimeSeconds) as Property<Long>
        val maxTimeDeviationSeconds = SimpleLongProperty(noiseEvent.maxTimeDeviationSeconds) as Property<Long>
        
        fun toNoiseEvent() = NoiseEvent(activity.value, executionTimeSeconds.value, maxTimeDeviationSeconds.value)
    }
    
    private val objects = initialObjects.map { ObservableNoiseEvent(it) }.observable()
    
    fun EventTarget.header() {
        hbox {
            addClass(Styles.addItemRoot)
            val newEvent = ObservableNoiseEvent(NoiseEvent(""))
            val validationContext = ValidationContext()
            lateinit var tf: TextField
            form {
                fieldset {
                    field("Activity") {
                        tf = textfield(newEvent.activity) {
                            validationContext.addValidator(this, ValidationTrigger.OnChange()) { text ->
                                if (text.isNullOrEmpty()) error("Should not be empty") else null
                            }
                        }
                    }
                    field("executionTimeSeconds") {
                        simpleLongField(newEvent.executionTimeSeconds, validationContext) { value ->
                            if (value < 0L) error("Should not be negative.") else null
                        }
                    }
                    field("maxTimeDeviationSeconds") {
                        simpleLongField(newEvent.maxTimeDeviationSeconds, validationContext) { value ->
                            if (value < 0L) error("Should not be negative.") else null
                        }
                    }
                }
                button("Add") {
                    enableWhen(validationContext.valid)
                    action {
                        // create a copy
                        objects.add(ObservableNoiseEvent(newEvent.toNoiseEvent()))
                        tf.selectAll()
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
                    onSuccess(objects.map { it.toNoiseEvent() })
                    close()
                }
            }
        }
    }
    
    
    override val root = vbox {
        
        header()
        
        tableview(objects) {
            isEditable = true
            
            column("Activity", ObservableNoiseEvent::activity) {
                makeEditable()
            }
            column("executionTimeSeconds", ObservableNoiseEvent::executionTimeSeconds) {
                makeEditable()
            }
            column("executionTimeSeconds", ObservableNoiseEvent::maxTimeDeviationSeconds) {
                makeEditable()
            }
            column("Delete", ObservableNoiseEvent::maxTimeDeviationSeconds) {
                cellFormat {
                    addClass(Styles.deleteButton)
                    graphic = button {
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