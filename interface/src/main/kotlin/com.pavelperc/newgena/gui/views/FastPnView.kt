package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.petrinet.fastPetrinet.simplePetrinetBuilder
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.profile
import guru.nidi.graphviz.engine.Graphviz
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*

class FastPnView : View("My View"), PetrinetDrawProvider {
    
    val fastPnTextProp = SimpleStringProperty("""
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
        p6--o>c
        p6-->y-->p5-->x-->p6
    """.trimIndent())
    
    override var petrinet: ResetInhibitorNet? = null
    
    init {
        profile("Graphviz, loading engine:") {
            // graphviz: speedup first draw
            Graphviz.useDefaultEngines()
        }
    }
    
    
    override fun requestPetrinetUpdate() {
        petrinet = simplePetrinetBuilder(fastPnTextProp.value)
    }
    override val markings: Pair<Marking, Marking>
        get() = emptyMarking() to emptyMarking()
    
    override val pnmlLocationForDrawing: String?
        get() = null
    
                
    val petrinetImageView = find<PetrinetImageView>("petrinetDrawProvider" to this@FastPnView)
    
    override val root = vbox {
        splitpane {
            vbox { 
                textarea(fastPnTextProp) {
                    useMaxSize = true
                    vgrow = Priority.ALWAYS
                    promptText = "write fastPn here"
                    
                    font = Font.font("Consolas")
                }
            }
            setDividerPositions(0.3, 0.7)
            petrinetImageView.root.attachTo(this)
            
        }
    }
}
