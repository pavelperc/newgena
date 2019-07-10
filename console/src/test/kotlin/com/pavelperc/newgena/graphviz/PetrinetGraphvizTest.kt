package com.pavelperc.newgena.graphviz

import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonMarking
import com.pavelperc.newgena.petrinet.fastPetrinet.simplePetrinetBuilder
import org.junit.Test

class PetrinetGraphvizTest {
    
    
    @Test
    fun convertAndSave() {
        val petrinet = simplePetrinetBuilder("""
            places:
            p1 p2 p3 p4
            transitions:
            a b c d
            arcs:
            p1-o>a-->p2-->b-->p3-->d-->p4
                     p2->>c-->p3
        """.trimIndent())
        //                    B
        //                /      \
        // p1 -o> A -> p2 ->> C -> p3 -> D -> p4


//        val marking = Marking(listOf(p1, p1, p1, p1, p1, p1, p1))
        val (marking, _) = JsonSettingsBuilder.buildMarkingOnly(JsonMarking().apply { 
            initialPlaceIds = mutableMapOf("p1" to 5, "p2" to 3)
        }, petrinet)
        
        PetrinetDrawer(petrinet, marking, drawArcIds = false).makeGraph("gv/simpleNet.svg")
    }
}