package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.customfields.actionedAutoCompletion
import com.pavelperc.newgena.gui.customfields.delayHack
import com.pavelperc.newgena.gui.customfields.intSpinner
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*


/** Small arrows to swap elements in the listView. */
fun <T> EventTarget.upDownPanel(objects: ObservableList<T>, item: T, op: VBox.() -> Unit = {}) {
    vbox {
        addClass(Styles.upDownPanel)
        button("⇑") {
            action {
                val idx = objects.indexOf(item)
                if (idx > 0) {
                    objects.swap(idx, idx - 1)
                }
            }
        }
        button("⇓") {
            action {
                val idx = objects.indexOf(item)
                if (idx < objects.size - 1) {
                    objects.swap(idx, idx + 1)
                }
            }
        }
        op()
    }
}


/** Allows to edit a map<String, Int>.
 * Hint is an additional representation of predefined values. */
class IntMapEditor(
        initialObjects: Map<String, Int>,
        title: String = "Map Editor",
        val intValueRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
        val predefinedValuesToHints: Map<String, String?> = emptyMap(),
        /** Name of the hints for predefined values. */
        private val hintName: String = "hint",
        private val fillDefaultButton: Boolean = false,
        /** Name of the values. First letter is uppercase. */
        private val valuesName: String = "alues",
        val onSuccess: (Map<String, Int>) -> Unit = {}
) : Fragment(title) {
    
    private val hintNameUpper = hintName.first().toUpperCase() + hintName.substring(1)
    private val valuesNameUpper = valuesName.first().toUpperCase() + valuesName.substring(1)
    
    private val predefinedHintsToValues = predefinedValuesToHints
            .filter { (_, v) -> v != null && v.isNotEmpty() }
            .map { (k, v) -> v!! to k }
            .toMap()
    
    val showHint = SimpleBooleanProperty(predefinedValuesToHints.size > 0)
    
    
    data class MutablePair(
            var string: String,
            var int: Int
    ) {
        fun toPair() = string to int
    }
    
    inner class MutablePairModel(initial: MutablePair) : ItemViewModel<MutablePair>(initial) {
        val stringProp = bind(MutablePair::string)
        val intProp = bind(MutablePair::int, autocommit = true)
        
        val hintProp = SimpleStringProperty(predefinedValuesToHints[stringProp.value] ?: "")
        
        init {
            // setup a hint
            stringProp.onChange { newString ->
                // reset the hint if the value is unknown
                hintProp.value = predefinedValuesToHints[newString] ?: ""
            }
            hintProp.onChange { hint ->
                // update the value if the hint was found.
                if (hint in predefinedHintsToValues) {
                    stringProp.value = predefinedHintsToValues[hint]
                }
            }
        }
        
        fun toPair() = stringProp.value to intProp.value
    }
    
    
    val objects = initialObjects.entries.map { (k, v) -> MutablePair(k, v) }.observable()
    
    override val root = vbox {
        header()
        
        listview(objects) {
            isEditable = true
            
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.DELETE && selectedItem != null) {
                    objects.remove(selectedItem)
                }
            }
            
            focusedProperty().onChange { focused ->
                if (!focused && editingIndex == -1) {
                    selectionModel.clearSelection()
                }
            }
            
            useMaxSize = true
            vgrow = Priority.ALWAYS
            
            cellFormat {
                oneCell(it)
            }
        }
    }
    
    // ---HEADER:---
    fun EventTarget.header() {
        vbox {
            style {
                spacing = 10.px
                padding = box(10.px)
            }
            hbox {
                alignment = Pos.CENTER_LEFT
                label("Add: ") {
                    tooltip("Press enter inside a text field to add.") {
                        delayHack(100)
                    }
                }
                
                val model = MutablePairModel(MutablePair("", 1))
                
                val numberProp = model.intProp
                val textProp = model.stringProp
                val hintProp = model.hintProp
                
                fun commit(): Boolean {
                    if (!textProp.value.isBlank() && model.commit()) {
                        objects.add(model.item.copy())
                        numberProp.value = 1
                        return true
                    }
                    return false
                }
                
                // text
                val tfText = textfield(textProp) {
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    prefWidth = 150.0
                    promptText = "Click enter to add."
                    
                    action {
                        if (commit()) {
                            selectAll()
                        }
                    }
                    validator(ValidationTrigger.OnChange()) { newString ->
                        when {
                            newString == null -> error("Should not be null.")
                            objects.map { it.string }.contains(newString) -> error("Duplicate.")
                            else -> null
                        }
                    }
                    
                    actionedAutoCompletion(predefinedValuesToHints.keys.toList())
                }
                
                // hint
                textfield(hintProp) {
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    prefWidth = 150.0
                    
                    removeWhen { showHint.not() }
                    promptText = "Search by $hintName"
                    action {
                        if (commit()) {
                            selectAll()
                            tfText.selectAll()
                        }
                    }
                    
                    actionedAutoCompletion(predefinedHintsToValues.keys.toList())
                }
                
                
                // number
                intSpinner(numberProp, intValueRange) {
                    maxWidth = 100.0
                    editor.action {
                        commit()
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
                        onSuccess(objects.map { it.toPair() }.toMap())
                        close()
                    }
                }
                
            }
            if (predefinedValuesToHints.size > 0) {
                borderpane {
                    useMaxWidth = true
                    vgrow = Priority.ALWAYS
                    left = checkbox("Show $hintName", showHint) {
                        isFocusTraversable = false
                    }
                    if (fillDefaultButton) {
                        right = button("fill with default") {
                            action {
                                objects.setAll(predefinedValuesToHints.keys.map { MutablePair(it, 1) })
                            }
                        }
                    }
                }
            }
            
            // Sorting:
            hbox {
                alignment = Pos.CENTER_LEFT
                addClass(Styles.sortingPanel)
                label("Sort: ")
                button("${valuesNameUpper}↓") {
                    alignment = Pos.BOTTOM_CENTER
                    action {
                        objects.sortBy { it.string }
                    }
                }
                button("${valuesNameUpper}↑") {
                    action {
                        objects.sortByDescending { it.string }
                    }
                }
                button("${hintNameUpper}↓") {
                    removeWhen { showHint.not() }
                    action {
                        objects.sortBy { predefinedValuesToHints[it.string] ?: "" }
                    }
                }
                button("${hintNameUpper}↑") {
                    removeWhen { showHint.not() }
                    action {
                        objects.sortByDescending { predefinedValuesToHints[it.string] ?: "" }
                    }
                }
                button("Number↓") {
                    action {
                        objects.sortBy { it.int }
                    }
                }
                button("Number↑") {
                    action {
                        objects.sortByDescending { it.int }
                    }
                }
            }
        }
    }
    
    // -------- ONE CELL --------
    private fun ListCell<MutablePair>.oneCell(mutablePair: MutablePair) {
        val model = MutablePairModel(mutablePair)
        
        val intProp = model.intProp
        val stringProp = model.stringProp
        val hintProp = model.hintProp
        
        addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                cancelEdit()
            }
        }
        
        
        graphic = hbox {
            alignment = Pos.CENTER_LEFT
            upDownPanel(objects, item)
            
            // look:
            hbox {
                alignment = Pos.CENTER
                hgrow = Priority.ALWAYS
                removeWhen { editingProperty() }
                style {
                    padding = box(0.px, 10.px, 0.px, 5.px)
                }
                
                label(stringProp) {
                    setId(Styles.contentLabel)
                    hgrow = Priority.ALWAYS
                    useMaxSize = true
                }
                label(hintProp) {
                    paddingLeft = 5
                    
                    removeWhen { showHint.not() }
//                    useMaxSize = true
//                    hgrow = Priority.ALWAYS
                    setId(Styles.contentLabel)
                }
            }
            
            // edit
            hbox {
                alignment = Pos.CENTER
                hgrow = Priority.ALWAYS
                removeWhen { editingProperty().not() }
                
                
                editingProperty().onChange { isEditing ->
                    // stopped editing
                    if (!isEditing) {
//                        println("stopped editing!!")
                        model.hintProp.value = predefinedValuesToHints[stringProp.value] ?: ""
                        model.rollback()
                    }
                }
                
                fun commit() {
                    if (model.commit()) {
                        commitEdit(model.item!!)
                    }
                }
                
                // value
                val tfValue = textfield(stringProp) {
                    hgrow = Priority.ALWAYS
                    useMaxWidth = true
                    prefWidth = 100.0
                    
                    action { commit() }
                    promptText = "Edit value."
                    
                    actionedAutoCompletion(predefinedValuesToHints.keys.toList())
                    validator(ValidationTrigger.OnChange()) { newString ->
                        when {
                            newString == null -> error("Should not be null.")
                            newString.isBlank() -> error("Should not be blank.")
                            objects.any { it.string == newString && it.string != item.string } -> error("Duplicate.")
                            else -> null
                        }
                    }
                }
                // jump on value when editing starts.
                whenVisible { tfValue.requestFocus() }
                
                // hint
                textfield(hintProp) {
                    prefWidth = 100.0
                    useMaxWidth = true
                    hgrow = Priority.ALWAYS
                    promptText = "Search by $hintName"
                    
                    removeWhen { showHint.not() }
                    
                    action { commit() }
                    actionedAutoCompletion(predefinedHintsToValues.keys.toList())
                }
            }
            
            // look and edit
            intSpinner(intProp, intValueRange) {
                maxWidth = 100.0
            }
            
            button(graphic = Styles.closeIcon()) {
                addClass(Styles.intMapDeleteButton)
                isFocusTraversable = false
                action { objects.remove(item) }
            }
        }
    }
}