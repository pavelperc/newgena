package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.customfields.statusLabel
import com.pavelperc.newgena.petrinet.fastPetrinet.generateFastPn
import com.pavelperc.newgena.petrinet.fastPetrinet.simplePetrinetBuilder
import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.utils.common.emptyMarking
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.stage.StageStyle
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color


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
        petrinet = simplePetrinetBuilder(fastPnTextProp.value, petrinetName.value)
    }
    
    override val markings: Pair<Marking, Marking>
        get() = emptyMarking() to emptyMarking()
    
    override val pnmlLocationForDrawing: String?
        get() = null
    
    // do not reuse petrinetImageView, because of problems in draw button
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
                            dialog("Replace", stageStyle = StageStyle.UTILITY) {
                                val tfFrom = textfield()
                                val tfTo = textfield()
                                
                                alignment = Pos.CENTER
                                button("ok") {
                                    addEventHandler(KeyEvent.KEY_PRESSED) { ev ->
                                        if (ev.getCode() === KeyCode.ENTER) {
                                            fire()
                                            ev.consume()
                                        }
                                    }
                                    action {
                                        // we need to prevent clearing input history in the textArea,
                                        // so we use textArea replace method
                                        val oldText = textArea.text
                                        val newText = oldText.replace(tfFrom.text, tfTo.text)
                                        
                                        // just replaceText throws out of bounds sometimes in undo
                                        // just a very bad javafx bug in undo
                                        if (oldText.length < newText.length) {
                                            textArea.replaceText(0, oldText.length, "")
                                            textArea.appendText(newText)
                                        } else {
                                            textArea.replaceText(0, oldText.length, newText)
                                        }
                                        
                                        this@dialog.close()
                                    }
                                }
                            }?.show()
                            
                        }
                    }
                }
            }
            setDividerPositions(0.3, 0.7)
            petrinetImageView.root.attachTo(this)
            
        }
    }
    
    init {
//        profile("Graphviz, loading engine:") {
//            // graphviz: speedup first draw
//            Graphviz.useDefaultEngines()
//        }
        
        petrinetImageView.draw()
    }
}
