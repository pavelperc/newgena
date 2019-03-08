package com.pavelperc.newgena.models

import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import kotlin.test.assertFailsWith


class PetrinetExtensionsTest {
    
    @Test
    fun markInhibitorAndResetArcs() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("simplePetriNet")
        
        val a = petrinet.addTransition("A")
        val b = petrinet.addTransition("B")
        val c = petrinet.addTransition("C")
        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addArc(p1, a)
        petrinet.addArc(a, p2)
        petrinet.addArc(p2, b)
        petrinet.addArc(p2, c)
        petrinet.addArc(b, p3)
        petrinet.addArc(c, p3)
        petrinet.addArc(p3, d)
        petrinet.addArc(d, p4)
        //                  B
        //               /     \
        // p1 -> A -> p2 -> C -> p3 -> D -> p4
        
        
        // "A" to "p2" should not be found.
        assertFailsWith<IllegalArgumentException> {
            petrinet.markInhResetArcs(inhibitorArcLabels = setOf("p1" to "A", "A" to "p2"))
        }.also { println(it) }
        
        // nothing should be changed
        petrinet.getArc(p1, a).shouldNotBeNull()
        
        
        petrinet.markInhResetArcs(
                inhibitorArcLabels = setOf("p1" to "A"),
                resetArcLabels = setOf("p2" to "C")
        )
        
        petrinet.getArc(p1, a).shouldBeNull()
        petrinet.getArc(p2, c).shouldBeNull()
        
        petrinet.getInhibitorArc(p1, a).shouldNotBeNull()
        petrinet.getResetArc(p2, c).shouldNotBeNull()
    }
}