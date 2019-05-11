package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.IntegerProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.Duration
import javafx.util.StringConverter
import org.processmining.models.time_driven_behavior.GranularityTypes
import tornadofx.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class SettingsView : View("Settings") {
    
    private val controller by inject<SettingsUIController>()
    
    private val settings = controller.settingsModel
    private val petrinetSetup = controller.petrinetSetupModel
    private val marking = controller.markingModel
    private val staticPriorities = controller.staticPrioritiesModel
    private val noise = controller.noiseModel
    private val time = controller.timeModel
    private val timeNoise = controller.timeNoiseModel
    
    override val root = VBox()
    
    private var saidHello = false
    
    override fun onDock() {
        super.onDock()
        if (!saidHello) {
            saidHello = true
            runAsync {
                runLater(Duration(200.0)) {
                    notification("Hello!", duration = 1000) {
                        position(Pos.TOP_CENTER)
                    }
                }
            }
        }
    }
    
    init {
        with(root) {
            scrollableFieldset {
                field("outputFolder") {
                    textfield(settings.outputFolder).required()
                    
                    button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                        action {
                            controller.requestOutputFolderChooseDialog()
                        }
                        isFocusTraversable = false
                    }
                }
                petrinetSetupPanel()
                
                // marking:
                
                fieldset("Marking") {
                    addClass(Styles.fieldSetFrame)
                    
                    val validatePlaces: Validator<Map<String, Int>> = xx@{ map ->
                        if (map.values.any { it <= 0 })
                            return@xx error("All place counts should be positive.")
                        
                        val input: Set<String> = map.keys
                        val unknown = input - input.intersect(controller.placeIdsWithHints.keys)
                        if (controller.petrinet != null && unknown.isNotEmpty()) {
                            warning("Not found places: $unknown")
                        } else {
                            null
                        }
                    }
                    
                    checkboxField(marking.isUsingInitialMarkingFromPnml)
                    intMapField(
                            marking.initialPlaceIds,
                            mapValidator = validatePlaces,
                            predefinedValuesToHints = { controller.placeIdsWithHints },
                            hintName = "label"
                    )
                    intMapField(
                            marking.finalPlaceIds,
                            mapValidator = validatePlaces,
                            predefinedValuesToHints = { controller.placeIdsWithHints },
                            hintName = "label"
                    )
                }
                
                
                intField(settings.numberOfLogs, nonNegative = true)
                intField(settings.numberOfTraces, nonNegative = true)
                intField(settings.maxNumberOfSteps, nonNegative = true)

//                intSpinnerField(settings.numberOfLogs, nonNegativeRange)
//                intSpinnerField(settings.numberOfTraces, nonNegativeRange)
//                intSpinnerField(settings.maxNumberOfSteps, nonNegativeRange)
                
                
                checkboxField(settings.isRemovingEmptyTraces)
                checkboxField(settings.isRemovingUnfinishedTraces)
                
                
                // --- NOISE ---
                checkboxField(settings.isUsingNoise) {
                    action {
                        if (isSelected)
                            settings.isUsingStaticPriorities.value = false
                    }
                }
                
                foldingFieldSet("Noise", settings.isUsingNoise) {
                    intField(noise.noiseLevel, validRange = 1..100, fieldOp = {
                        slider(1..100, noise.noiseLevel.value) {
                            blockIncrement = 1.0
                            valueProperty().bindBidirectional(noise.noiseLevel as IntegerProperty)
                        }
                    }) {
                        minWidth = 50.0
                        maxWidth = 50.0
                    }
                    
                    checkboxField(noise.isSkippingTransitions)
                    checkboxField(noise.isUsingExternalTransitions)
                    checkboxField(noise.isUsingInternalTransitions)
                    arrayField(
                            noise.internalTransitionIds,
                            predefinedValuesToHints = { controller.transitionIdsWithHints },
                            hintName = "labels"
                    )
                    
                    
                    field("artificialNoiseEvents") {
                        
                        readOnlyTextField(noise.artificialNoiseEvents, { newList ->
                            newList.let { if (it.isEmpty()) "Empty." else it.joinToString("; ") }
                        })
                        
                        button("Edit") {
                            action {
                                NoiseEventsEditor(noise.artificialNoiseEvents.value) { events ->
                                    noise.artificialNoiseEvents.value = events.toMutableList()
                                }.openWindow(escapeClosesWindow = false)
                            }
                        }
                    }
                    
                    
                    foldingFieldSet("timeDrivenNoise", settings.isUsingTime) {
                        checkboxField(timeNoise.isUsingTimestampNoise)
                        checkboxField(timeNoise.isUsingLifecycleNoise)
                        checkboxField(timeNoise.isUsingTimeGranularity)
                        intField(timeNoise.maxTimestampDeviationSeconds)
                        
                        field("granularityType") {
                            combobox(timeNoise.granularityType, GranularityTypes.values().toList())
                        }
                    }
                }
                
                // --- PRIORITIES ---
                
                checkboxField(settings.isUsingStaticPriorities) {
                    action {
                        if (isSelected) {
                            settings.isUsingTime.value = false
                            settings.isUsingNoise.value = false
                        }
                    }
                }
                
                val validatePriorities: ValidationContext.(map: Map<String, Int>) -> ValidationMessage? = xx@{ map ->
                    if (map.values.any { it !in 1..staticPriorities.maxPriority.value })
                        return@xx error("All priorities should be in 1...maxPriority")
                    
                    if (controller.petrinet == null) {
                        return@xx null
                    }
                    
                    val input: Set<String> = map.keys
                    val unknown = input - input.intersect(controller.transitionIdsWithHints.keys)
                    if (unknown.isNotEmpty()) {
                        warning("Not found transitions: $unknown")
                        
                    } else if (map.size < controller.transitionIdsWithHints.size) {
                        return@xx warning("Not enough priorities defined!!")
                    } else {
                        null
                    }
                }
                
                foldingFieldSet("Static priorities", settings.isUsingStaticPriorities) {
                    intField(staticPriorities.maxPriority, nonNegative = true) {
                        textProperty().onChange {
                            staticPriorities.validate() // run both field to validate
                        }
                    }
                    intMapField(
                            staticPriorities.transitionIdsToPriorities,
                            predefinedValuesToHints = { controller.transitionIdsWithHints },
                            hintName = "label",
                            fillDefaultButton = true
                    ) { map ->
                        validatePriorities(map)
                    }
                }
                
                timeDescription()
            }
            
            // ------- Non scrollable part --------
            form {
                fieldset {
                    settingsLoadingPanel()
                    
                    button("Generate logs!") {
                        enableWhen(controller.allModelsAreValid)
                        shortcut("Ctrl+G")
                        tooltip("Ctrl+G")
                        
                        action {
                            try {
                                val generationKit = controller.prepareGenerationKit()
                                
                                val view = find<GenerationView>(mapOf(
                                        "generationKit" to generationKit,
                                        "outputFolder" to settings.outputFolder.value)
                                )
                                replaceWith(view, ViewTransition.Slide(0.2.seconds))
                                
                            } catch (e: Exception) {
                                alert(Alert.AlertType.ERROR, "Couldn't apply settings:", e.message)
                                e.printStackTrace()
                            }
                            
                        }
                        
                    }
                }
            }
        }
    }
    
    // ---TIME---
    private fun EventTarget.timeDescription() {
        checkboxField(settings.isUsingTime) {
            action {
                if (isSelected)
                    settings.isUsingStaticPriorities.value = false
            }
        }
        
        foldingFieldSet("Time and Resources", settings.isUsingTime) {
            field("generationStart") {
                val timeConverter = object : StringConverter<Instant>() {
                    
                    override fun toString(obj: Instant) = obj.toString()
                    override fun fromString(string: String) =
                            try {
                                Instant.parse(string)
                            } catch (e: DateTimeParseException) {
                                Instant.now()
                            }
                }
                
                textfield(time.generationStart, timeConverter) {
                    validator(ValidationTrigger.OnBlur) { newString ->
                        try {
                            Instant.parse(newString)
                            null
                        } catch (e: DateTimeParseException) {
                            error("Bad time, format example: 2007-12-03T10:15:30.00Z")
                        }
                    }
                    action {
                        time.validationContext.validate(time.generationStart)
                    }
                }
                button("Now") {
                    action {
                        time.generationStart.value = Instant.now()
                    }
                }
            }
            
            checkboxField(time.isUsingResources)
            checkboxField(time.isSeparatingStartAndFinish)
            
            intField(time.minimumIntervalBetweenActions, nonNegative = true)
            intField(time.maximumIntervalBetweenActions, nonNegative = true)
            
            field("transitionIdsToDelays") {
                val Status = object {
                    val incorrect = "Ids doesn't match with model transitions."
                    val correct = "Correct."
                    val unknown = "Unknown: Petrinet is not loaded or empty"
                    val empty = "Empty."
                }
                
                fun getStatus(): String {
                    val delayIds = time.transitionIdsToDelays.value.keys
                    val petrinetIds = controller.transitionIdsWithHints.keys
                    
                    return when {
                        petrinetIds.isEmpty() -> Status.unknown
                        delayIds.isEmpty() -> Status.empty
                        petrinetIds != delayIds -> Status.incorrect
                        else -> Status.correct
                    }
                }
                
                val label = textfield(getStatus()) {
                    hgrow = Priority.ALWAYS
                    isEditable = false
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }
                time.transitionIdsToDelays.addValidator(label) {
                    getStatus().let { status ->
                        label.text = status
                        when (status) {
                            Status.correct, Status.unknown -> null
                            else -> warning(status)
                        }
                    }
                }
                controller.petrinetProp.onChange {
                    label.text = getStatus()
                }
                button("Edit") {
                    action {
                        TransitionDelaysEditor(time.transitionIdsToDelays.value,
                                controller.transitionIdsWithHints) { newMap ->
                            time.transitionIdsToDelays.value = newMap.toMutableMap()
                        }.openWindow(escapeClosesWindow = false)
                    }
                }
            }
            
            // ---RESOURCES---
            
            checkboxField(time.isUsingResources)
            
            foldingFieldSet("Resources", time.isUsingResources) {
                hgrow = Priority.ALWAYS
                
                checkboxField(time.isUsingComplexResourceSettings)
                checkboxField(time.isUsingSynchronizationOnResources)
                
                arrayField(time.simplifiedResources)
                
                field("resourceGroups") {
                    readOnlyTextField(time.resourceGroups, { newList ->
                        newList.flatMap {
                            it.roles.flatMap {
                                it.resources.map { it.name }
                            }
                        }.let { if (it.isEmpty()) "Empty." else it.joinToString("; ") }
                    })
                    
                    button("Edit") {
                        action {
                            ResourceGroupsEditor(time.resourceGroups.value) { groups ->
                                time.resourceGroups.value = groups.toMutableList()
                            }.openWindow(escapeClosesWindow = false)
                        }
                    }
                }
                
                field("transitionIdsToResources") {
                    // TODO status text for transitionIdsToResources 
//                                    val Status = object {
//                                        val incorrect = "Ids doesn't match with model transitions."
//                                        val correct = "Correct."
//                                        val unknown = "Unknown: Petrinet is not loaded or empty"
//                                        val empty = "Empty."
//                                    }
//    
//                                    fun getStatus(): String {
//                                        val delayIds = time.transitionIdsToDelays.value.keys
//                                        val petrinetIds = controller.transitionIdsWithHints.keys
//        
//                                        return when {
//                                            petrinetIds.isEmpty() -> Status.unknown
//                                            delayIds.isEmpty() -> Status.empty
//                                            petrinetIds != delayIds -> Status.incorrect
//                                            else -> Status.correct
//                                        }
//                                    }
//    
//                                    val label = textfield(getStatus()) {
//                                        hgrow = Priority.ALWAYS
//                                        isEditable = false
//                                        style {
//                                            backgroundColor += Color.TRANSPARENT
//                                        }
//                                    }
//                                    time.transitionIdsToDelays.addValidator(label) {
//                                        getStatus().let { status ->
//                                            label.text = status
//                                            when (status) {
//                                                Status.correct, Status.unknown -> null
//                                                else -> warning(status)
//                                            }
//                                        }
//                                    }
//                                    controller.petrinetProp.onChange {
//                                        label.text = getStatus()
//                                    }
                    
                    button("Edit") {
                        action {
                            ResourceMappingEditor(
                                    time.transitionIdsToResources.value,
                                    controller.transitionIdsWithHints,
                                    time.simplifiedResources.value,
                                    time.resourceGroups.value
                            ) { newMapping ->
                                time.transitionIdsToResources.value = newMapping.toMutableMap()
                            }.openWindow(escapeClosesWindow = false)
                        }
                    }
                }
            }
        }
    }
    
    
    // ---LOADING SETTINGS---
    private fun EventTarget.settingsLoadingPanel() {
        hbox {
            //            button("print") {
//                enableWhen(controller.allModelsAreValid)
//                action {
//                    settings.commit()
//                    println(controller.jsonSettings)
//                }
//            }
            vbox {
                // just to bind tooltip!
                button("Save settings") {
                    shortcut("Ctrl+S")
                    controller.allModelsAreValid.onChange { valid ->
                        toggleClass(Styles.redButton, !valid)
                        this@vbox.tooltip(if (!valid)
                            "Some settings are invalid!" else null)
                    }
                    
                    enableWhen(controller.allModelsAreValid.and(controller.settingsAreNotSaved))
                    
                    action {
                        val result: Boolean
                        if (controller.hasNewSettings.value) {
                            result = controller.saveJsonSettingsAs()
                        } else {
                            result = controller.saveJsonSettings(controller.jsonSettingsPath.value)
                        }
                        
                        if (result)
                            notification("Settings were saved.")
                    }
                }
            }
            
            button("New settings") {
                action {
                    confirmIf(controller.settingsAreNotSaved.value,
                            "Settings may be not saved.", "Continue?") {
                        
                        controller.makeNewSettings()
                    }
                    
                }
            }
            button("Load settings") {
                action {
                    if (controller.settingsAreNotSaved.value
                            && !confirmed("Settings may be not saved.", "Continue?")) {
                        return@action
                    }
                    
                    try {
                        if (!controller.loadJsonSettings())
                            return@action // when we canceled the fileChooser.
                    } catch (e: Exception) {
                        error("Broken json settings:", e.message)
                        return@action
                    }
                    
                    try {
                        controller.loadPetrinet()
                        notification {
                            title("Settings and petrinet are loaded.")
                            text("Wow, Nothing crashed!")
                        }
                    } catch (e: Exception) {
                        error("Failed to load petrinet:", e.message)
                    }
                }
            }
            
        }
        
        hbox {
            label("Loaded settings: ")
            label(controller.jsonSettingsPath)
            hiddenWhen(controller.hasNewSettings)
        }
        label("Unsaved Settings") {
            visibleWhen(controller.hasNewSettings)
        }
    }
    
    // ---PETRINET SETUP---
    fun EventTarget.petrinetSetupPanel() {
        fieldset("petrinetSetup") {
            addClass(Styles.fieldSetFrame)
            
            field("petrinetFile") {
                
                textfield(petrinetSetup.petrinetFile).required()
                var btnLoadPetrinet: Button? = null
                // select file
                button(graphic = FontAwesomeIconView(FontAwesomeIcon.FILE)) {
                    action {
                        if (controller.requestPetrinetFileChooseDialog()) {
                            btnLoadPetrinet?.fire()
                        }
                    }
                    isFocusTraversable = false
                }
                btnLoadPetrinet = button("Load model") {
                    //                    enableWhen(controller.isPetrinetDirty)
                    
                    toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                    action {
                        // may crash
                        try {
                            controller.loadPetrinet()
                            notification("Petrinet loaded", "okey, okey...") { hideAfter(Duration(2000.0)) }
                        } catch (e: Exception) {
                            error("Failed to load model:", e.message)
                        }
                    }
                    isFocusTraversable = false
                }
                button("draw") {
                    enableWhen(controller.isPetrinetUpdated)
                    action {
                        try {
                            val petrinetImage = find<PetrinetImageView>()
                            petrinetImage.draw()
                            petrinetImage.openWindow(owner = this@SettingsView.currentStage)
                            
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Failed to update arcs and draw.", e.message)
                        }
                    }
                }
            }
            
            val validateEdges: Validator<List<String>> = { list ->
                val input = list.toSet()
                val unknown = input - input.intersect(controller.inputEdgeIdsWithHints.keys)
                if (controller.petrinet != null && unknown.isNotEmpty()) {
                    warning("Not found input edges: $unknown")
                } else null
            }
            
            arrayField(
                    petrinetSetup.inhibitorArcIds,
                    listValidator = validateEdges,
                    predefinedValuesToHints = { controller.inputEdgeIdsWithHints },
                    hintName = "hint"
            )
            arrayField(
                    petrinetSetup.resetArcIds,
                    listValidator = validateEdges,
                    predefinedValuesToHints = { controller.inputEdgeIdsWithHints },
                    hintName = "hint"
            )
            // TODO restore inhibitor arcs!!
        }
    }
    
}
