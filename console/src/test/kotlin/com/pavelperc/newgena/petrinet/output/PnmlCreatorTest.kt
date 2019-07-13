package com.pavelperc.newgena.petrinet.output

import com.pavelperc.newgena.loaders.pnml.PnmlOwnParser
import com.pavelperc.newgena.petrinet.fastPetrinet.buildFastPetrinet
import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class PnmlCreatorTest {
    
    
    @Test
    fun saveSimplePetrinet() {
        val fastPn = """
            places:
            p1 p2 p3 p4
            transitions:
            a(A) b c d(D)
            arcs:
            p1-->a-->p2-o>b-->p3-->d-33->p4
                     p2->>c-->p3
        """.trimIndent()
        val petrinet = buildFastPetrinet(fastPn, "complex1")
        // descr is copied to fastPn field
        petrinet.fastPn shouldEqual fastPn
        
        petrinet.label shouldEqual "complex1"
        petrinet.places.map { it.pnmlId }
        
        val pnml = makePnmlStr(petrinet)
        
        println(pnml)
        val (restored, _) = PnmlOwnParser.parseFromString(pnml)
        
        petrinet.places.map { it.pnmlId } shouldContainSame restored.places.map { it.pnmlId }
        petrinet.transitions.map { it.pnmlId } shouldContainSame restored.transitions.map { it.pnmlId }
        petrinet.transitions.map { it.label } shouldContainSame restored.transitions.map { it.label }
        petrinet.edges.map { it.pnmlId } shouldContainSame restored.edges.map { it.pnmlId }
        
        restored.fastPn.shouldNotBeNull() shouldEqual fastPn
    }
}