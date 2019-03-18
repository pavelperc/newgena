package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.sun.glass.ui.CommonDialogs
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.util.StringConverter
import javafx.util.converter.NumberStringConverter
import tornadofx.*
import tornadofx.Form
import java.io.File
import java.nio.file.Paths
import kotlin.reflect.full.memberProperties

class QuitIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String) = if (string.isInt()) string.toInt() else 0
}

fun EventTarget.intfield(
        property: Property<Int>,
        op: TextField.() -> Unit = {}
) = textfield(property, QuitIntConverter(), op)


class SettingsView : View("Settings") {
    
    val settings = SettingsModel(JsonSettings())
    
    override val root = Form()
    
    init {
        with(root) {
            fieldset {
                field("outputFolder") {
                    textfield(settings.outputFolder) {
                        required()
                    }
                    
                    button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                        action {
                            val cwd = File(System.getProperty("user.dir"))
                            println(cwd.absolutePath)
                            
                            val directoryChooser = DirectoryChooser()
                            directoryChooser.initialDirectory = cwd
                            
                            var path = directoryChooser.showDialog(null)?.path
                            if (path != null) {
                                if (path.startsWith(cwd.path))
                                    path = path.substringAfter(cwd.path + "\\")
                                
                                settings.outputFolder.value = path
                            }
                        }
                    }
                }
                field("maxNumberOfSteps", Orientation.VERTICAL) {
                    textfield(settings.maxNumberOfSteps, QuitIntConverter()) {
                        required()
                    }
                    slider(0..100, settings.maxNumberOfSteps.value) {
                        blockIncrement = 1.0
                        
                        valueProperty().onChange { num -> settings.maxNumberOfSteps.value = num.toInt() }
                        settings.maxNumberOfSteps.onChange { num -> valueProperty().value = num?.toDouble() ?: 0.0 }
                    }
                }
                
                button("Save and print") {
                    enableWhen(settings.valid)
                    action {
                        settings.commit()
                        println(settings.item)
                    }
                }
            }
        }
    }
    
}
