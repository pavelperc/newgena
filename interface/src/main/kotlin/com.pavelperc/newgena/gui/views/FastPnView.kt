package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.ArtificialIntelligence
import com.pavelperc.newgena.gui.app.MyApp
import com.pavelperc.newgena.gui.customfields.confirmIf
import com.pavelperc.newgena.gui.customfields.confirmed
import com.pavelperc.newgena.gui.customfields.docButton
import com.pavelperc.newgena.gui.customfields.toggleCheckbox
import com.pavelperc.newgena.petrinet.fastPetrinet.FAST_PN_VERSION
import com.pavelperc.newgena.petrinet.fastPetrinet.buildFastPetrinet
import com.pavelperc.newgena.petrinet.fastPetrinet.generateFastPn
import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.utils.common.emptyMarking
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*


class FastPnView(
        override var petrinet: ResetInhibitorNet? = null,
        val onUpdatePetrinet: (petrinet: ResetInhibitorNet) -> Unit = {}
) : View("FastPn Editor"), PetrinetDrawProvider {
    companion object {
        val fastPnScope = Scope()
    }
    
    val petrinetName = SimpleStringProperty(petrinet?.label ?: "net1")
    
    val fastPnTextProp = SimpleStringProperty(
            petrinet?.let {
                it.fastPn ?: generateFastPn(it)
            } ?: """
        // sample petrinet
        places:
        p1 p2 p3 p4
        transitions:
        a b c d
        arcs:
        p1-->a-3->p2->>b-->p3-->c-->p4
                  p2-o>d-->p3
    """.trimIndent())
    
    override fun requestPetrinetUpdate() {
        petrinet = buildFastPetrinet(fastPnTextProp.value, petrinetName.value)
    }
    
    override val markings: Pair<Marking, Marking>
        get() = emptyMarking() to emptyMarking()
    
    override val pnmlLocationForDrawing: String?
        get() = null
    
    // do not use default scope in petrinetImageView, because of problems with draw button in main settings.
    val petrinetImageView = find<PetrinetImageView>(fastPnScope, "petrinetDrawProvider" to this@FastPnView)
    
    
    lateinit var textArea: TextArea
    
    override val root = vbox {
        splitpane {
            vbox {
                hbox {
                    paddingLeft = 5.0
                    alignment = Pos.CENTER_LEFT
                    label("Name:")
                    textfield(petrinetName) {
                        hgrow = Priority.ALWAYS
                        style {
                            backgroundColor += Color.TRANSPARENT
                        }
                    }
                }
                textArea = textarea(fastPnTextProp) {
                    useMaxSize = true
                    vgrow = Priority.ALWAYS
                    promptText = "write fastPn here"
                    
                    font = Font.font("Consolas", 16.0)
                    
                    focusedProperty().onChange { focused ->
                        // disable only adjust, reset and zoom buttons
                        petrinetImageView.imageViewer.buttonActionsEnabled = !focused
                    }
                }
                
                hbox {
                    button("Done") {
                        shortcut("Ctrl+S")
                        tooltip("Ctrl+S")
                        
                        action {
                            try {
                                requestPetrinetUpdate()
                                onUpdatePetrinet(petrinet!!)
                                close()
                            } catch (e: Exception) {
                                error("Can not complete editing.", e.message)
                            }
                        }
                    }
                    button("refactor") {
                        shortcut("Ctrl+F")
                        tooltip("Ctrl+F")
                        action {
                            refactorDialog().show()
                        }
                    }
                    button("about") {
                        action {
                            dialog("About FastPn") {
                                paddingAll = 0.0
                                textarea("""
                                    FastPn is a simple petrinet format.
                                    Used version is $FAST_PN_VERSION.
                                    Syntax Example:
                                    ```
                                    // the description contains three sections: places, transitions, arcs.
                                    places: // ids
                                    p1 p2 p3 p4 // separated by space or in separate lines
                                    transitions:
                                    // transition labels can be placed in round brackets.
                                    a(A) b
                                    // May contain spaces and other symbols.
                                    // Closing round bracket should be escaped like this: '\)'.
                                    c d(I (am\) label.)
                                    
                                    // support blank lines
                                    arcs:
                                    p1-o>a-->p2-->b ---> p3-->d-->p4 // arcs may be chained.
                                             p2->>c -5-> p3 // normal arcs may contain weights
                                             // support spacing between arrows.
                                    ```
                                    good arcs: --> ----> -24---> ---24--> ---o> -o> --->> ->>
                                    bad arcs: -> -1> 25-->  --25> --0>
                                    
                                    another Example:
                                    ```
                                    places:
                                    p1 p2 p4 p5 p6 p7
                                    transitions:
                                    a(A) b c x(X) y
                                    arcs:
                                    p2-->a
                                    p2---------------->>b
                                    p1---------------->>b
                                    p1-->a--6-->p4--4-->b-->p7
                                                p4--2-->c-->p7
                                    p6 --o> c
                                    p6-->y-->p5-->x-->p6
                                    ```
                                """.trimIndent()) {
                                    style {
                                        fontFamily = "Consolas"
                                        backgroundColor += Color.TRANSPARENT
                                    }
                                    isEditable = false
                                    useMaxSize = true
                                    vgrow = Priority.ALWAYS
                                    prefWidth = MyApp.WINDOW_WIDTH
                                    prefHeight = MyApp.WINDOW_WIDTH
                                }
                                useMaxSize = true
                                vgrow = Priority.ALWAYS
                            }
                        }
                    }
                }
            }
            setDividerPositions(0.3, 0.7)
            petrinetImageView.root.attachTo(this)
            
        }
    }
    
    private fun customDialog(title: String, rootBuilder: UIComponent.() -> Parent): Stage {
        // this code is copied from [dialog] tornadofx function,
        // but doesn't contains dialog label. (I can make my own instead.) 
        val fragment = builderFragment(title, scope, rootBuilder)
        
        val stage = fragment.openWindow(StageStyle.UTILITY, Modality.APPLICATION_MODAL)!!
        stage.sizeToScene()
        return stage
    }
    
    private fun refactorDialog() = customDialog("Replace") {
        vbox {
            alignment = Pos.CENTER
            hbox {
                alignment = Pos.CENTER
                label("Replace") {
                    style {
                        fontSize = 1.3.em
                        fontWeight = FontWeight.BOLD
                    }
                }
                docButton("""
                    This refactoring tries not to change section
                    headings (like places:, transitions: ...).
                    You can undo this refactor by clicking Ctrl+Z.
                """.trimIndent(), "Replace")
            }
            
            val isRegex = SimpleBooleanProperty(false)
            val txtFrom = SimpleStringProperty("")
            val txtTo = SimpleStringProperty("")
            
            hbox {
                alignment = Pos.CENTER
                textfield(txtFrom)
                
                hbox {
                    // just need
                    alignment = Pos.CENTER
                    toggleCheckbox("Regex", isRegex) {
                        hgrow = Priority.NEVER
                        isFocusTraversable = false
                        vgrow = Priority.ALWAYS
                        useMaxHeight = true
                        paddingHorizontal = 5.0
                    }
                    docButton("""
                        You can use regex while refactoring.
                        For example: replace `place(\d+)` with `p$1`
                        - this regex will keep numbers in places.
                    """.trimIndent(), "Regex")
                }
            }
            
            textfield(txtTo)
            
            button("ok") {
                addEventHandler(KeyEvent.KEY_PRESSED) { ev ->
                    if (ev.code === KeyCode.ENTER) {
                        fire()
                        ev.consume()
                    }
                }
                action {
                    // we need to prevent clearing input history in the textArea,
                    // so we use textArea replace method
                    val oldText = textArea.text
                    
                    val replaceRegex = if (isRegex.value) Regex(txtFrom.value) else null
                    
                    // try not to change section names
                    val newText = oldText.split("places:", "transitions:", "arcs:").map {
                        if (replaceRegex != null) {
                            it.replace(replaceRegex, txtTo.value)
                        } else {
                            it.replace(txtFrom.value, txtTo.value)
                        }
                    }.let { (bPl, bTr, bArc, aArc) ->
                        // bPl = before Places
                        "${bPl}places:${bTr}transitions:${bArc}arcs:${aArc}"
                    }
                    
                    
                    // just replaceText throws out of bounds sometimes in undo
                    // just a very bad javafx bug in undo
                    if (oldText.length < newText.length) {
                        textArea.deleteText(0, oldText.length)
                        textArea.appendText(newText)
                    } else {
                        textArea.replaceText(0, oldText.length, newText)
                    }
//                        textArea.replaceText(0, oldText.length, newText)
                    
                    
                    this@customDialog.close()
                }
            }
        }
    }
    
    fun setOnCloseAction(myStage: Stage?) {
        myStage?.onCloseRequest = EventHandler { event ->
            if(!confirmed("Cancel editing?", "Your changes will be lost.")) {
                event.consume() // cancels closing
            }
        }
    }
    
    init {
        //        profile("Graphviz, loading engine:") {
//            // graphviz: speedup first draw
//            Graphviz.useDefaultEngines()
//        }
//        importStylesheet(Styles::class)
        petrinetImageView.draw()
        
//        modalStage?.onCloseRequest = EventHandler { event ->
//            if(!confirmed("Cancel editing?", "Your changes will be lost.")) {
//                event.consume() // cancels closing
//            }
//        }
    }
}
