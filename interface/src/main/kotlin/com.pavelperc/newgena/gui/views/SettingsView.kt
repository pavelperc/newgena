package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.util.StringConverter
import tornadofx.*
import tornadofx.Form
import java.util.regex.Pattern


class QuitIntConverter : StringConverter<Int>() {
    override fun toString(obj: Int?) = obj.toString()
    override fun fromString(string: String) = if (string.isInt()) string.toInt() else 0
}

typealias Validator<T> = ValidationContext.(T) -> ValidationMessage?

fun TextInputControl.validInt(
        nextValidator: Validator<Int> = { null }
) {
    this.validator { value ->
        when {
            value == null -> error("Null")
            !value.isInt() -> error("Not an Int")
            else -> nextValidator(value.toInt())
        }
    }
    
}

fun TextInputControl.validUint(
        nextValidator: Validator<Int> = { null }
) {
    validInt { value ->
        when {
            value < 0 -> error("Should be positive")
            else -> nextValidator(value)
        }
    }
}

fun TextInputControl.validRangeInt(
        intRange: IntRange,
        nextValidator: Validator<Int> = { null }
) {
    validInt { value ->
        when {
            value !in intRange -> error("Should be in $intRange")
            else -> nextValidator(value)
        }
    }
}


fun EventTarget.intField(
        property: Property<Int>,
//        sliderRange: IntRange? = null,
        fieldOp: Field.() -> Unit = {},
        op: TextField.() -> Unit = {}
) = field(property.name, Orientation.HORIZONTAL) {
    textfield(property, QuitIntConverter(), op)
//    if (sliderRange != null) {
//        slider(sliderRange, property.value) {
//            blockIncrement = 1.0
//            valueProperty().bindWithConverter(property, { me -> me.toInt() }, { he -> he.toDouble() })
//        }
//    }
    fieldOp()
}

fun EventTarget.checkboxField(property: Property<Boolean>, op: CheckBox.() -> Unit = {}) =
        field(property.name) {
            checkbox(property = property, op = op)
        }

fun EventTarget.arrayField(property: Property<ObservableList<String>>) =
        field(property.name) {
            val list = property.value
            
            val textProp = SimpleStringProperty(list.joinToString("; "))
            val splitPattern = Pattern.compile("""\s*[;,]\s*""")
            textProp.addListener { observable, oldValue, newValue ->
                val splitted = newValue
                        .trim('[', ']', '{', '}')
                        .trimIndent()
                        .split(splitPattern)
                        .toMutableList()
                list.setAll(splitted)
            }
            
            textfield(textProp)
            
            button(graphic = FontAwesomeIconView(FontAwesomeIcon.EXPAND)) {
                action {
                    val arrayEditor = ArrayEditor(property.value.toList(), onSuccess = { changedObjects ->
                        // set to textProp, textProp sets to list
                        textProp.value = changedObjects.joinToString("; ")
                    })
                    
                    arrayEditor.openModal()
                }
            }
        }

fun <A, B> Property<A>.bindWithConverter(other: Property<B>, toOther: (me: A) -> B, fromOther: (he: B) -> A) {
    // recursion????
    this.onChange { changed ->
        other.value = toOther(changed!!)
    }
    other.onChange { changed ->
        this.value = fromOther(changed!!)
    }
}


class SettingsView : View("Settings") {
    
    private val controller by inject<SettingsUIController>()
    
    private val settings = controller.settingsModel
    private val petrinetSetup = controller.petrinetSetupModel
    private val marking = controller.markingModel
    
    override val root = Form()
    
    init {
        with(root) {
            fieldset {
                field("outputFolder") {
                    textfield(settings.outputFolder).required()
                    
                    button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                        action {
                            controller.requestOutputFolderChooseDialog()
                        }
                        isFocusTraversable = false
                    }
                }
                intField(settings.numberOfLogs) { validUint() }
                intField(settings.numberOfTraces) { validUint() }
                intField(settings.maxNumberOfSteps) { validUint() }
                
                checkboxField(settings.isRemovingEmptyTraces)
                checkboxField(settings.isRemovingUnfinishedTraces)
                
                checkboxField(settings.isUsingNoise)
                
                
                checkboxField(settings.isUsingStaticPriorities) {
                    action {
                        if (isSelected)
                            settings.isUsingTime.value = false
                    }
                }
                checkboxField(settings.isUsingTime) {
                    action {
                        if (isSelected)
                            settings.isUsingStaticPriorities.value = false
                    }
                }
                
                fieldset("petrinetSetup") {
                    field("petrinetFile") {
                        
                        textfield(petrinetSetup.petrinetFile).required()
                        
                        button(graphic = FontAwesomeIconView(FontAwesomeIcon.FILE)) {
                            action {
                                controller.requestPetrinetFileChooseDialog()
                            }
                            isFocusTraversable = false
                        }
                        button("Load model") {
                            toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                            
                            action {
                                // may crash
                                controller.loadPetrinet()
                                println("loaded petrinet")
//                                alert(Alert.AlertType.INFORMATION, "Not implemented.")
                            }
                            isFocusTraversable = false
                        }
                        
                    }
                    
                    
                    arrayField(petrinetSetup.inhibitorArcIds)
                    arrayField(petrinetSetup.resetArcIds)
                }
                
                
                
                button("Commit and print") {
                    enableWhen(controller.allModelsAreValid)
                    action {
                        settings.commit()
                        println(settings.item)
                    }
                }
            }
        }
    }
    
}
