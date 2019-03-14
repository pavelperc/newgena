package com.pavelperc.newgena.models

import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import kotlin.test.assertFailsWith


class PetrinetExtensionsTest {
    
    @Test
    fun markInhibitorAndResetArcsByIds() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("simplePetriNet")
        
        val a = petrinet.addTransition("A")
        val b = petrinet.addTransition("B")
        val c = petrinet.addTransition("C")
        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        
        val arcs = mutableListOf<Arc>()
        arcs += petrinet.addArc(p1, a) // label 1
        arcs += petrinet.addArc(a, p2) // label 2
        arcs += petrinet.addArc(p2, b) // label 3
        arcs += petrinet.addArc(p2, c) // label 4
        arcs += petrinet.addArc(b, p3) // label 5
        arcs += petrinet.addArc(c, p3) // label 6
        arcs += petrinet.addArc(p3, d) // label 7
        arcs += petrinet.addArc(d, p4) // label 8
        //                  B
        //               /     \
        // p1 -> A -> p2 -> C -> p3 -> D -> p4
        
        arcs.withIndex().forEach { (i, arc) ->
            val lbl = (i + 1).toString()
            arc.attributeMap.put("ProM_Vis_attr_label", lbl)
            
            arc.label shouldEqual lbl
        }
        
        
        // arc 9 should not be found.
        assertFailsWith<IllegalArgumentException> {
            petrinet.markInhResetArcsByIds(inhibitorArcIds = listOf("1", "9"))
        }.also { println(it) }
        
        
        // nothing should be changed
        petrinet.getArc(p1, a).shouldNotBeNull()
        
        
        petrinet.markInhResetArcsByIds(
                inhibitorArcIds = listOf("1"),
                resetArcIds = listOf("4")
        )
        
        petrinet.getArc(p1, a).shouldBeNull()
        petrinet.getArc(p2, c).shouldBeNull()
        
        petrinet.getInhibitorArc(p1, a).shouldNotBeNull()
        petrinet.getResetArc(p2, c).shouldNotBeNull()
        
        
        // check if we can change again already changed arcs
        petrinet.markInhResetArcsByIds(
                resetArcIds = listOf("1"),
                inhibitorArcIds = listOf("4")
        )
        
        petrinet.getArc(p1, a).shouldBeNull()
        petrinet.getArc(p2, c).shouldBeNull()
        
        petrinet.getResetArc(p1, a).shouldNotBeNull()
        petrinet.getInhibitorArc(p2, c).shouldNotBeNull()
    }
    
    
    @Test
    fun testPnmlIdProperty() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("simplePetriNet")
        
        val a = petrinet.addTransition("A")
        val b = petrinet.addTransition("B")
        val c = petrinet.addTransition("C")
        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        val arc1 = petrinet.addResetArc(p1, a)
        val arc2 = petrinet.addArc(a, p2)
        val arc3 = petrinet.addInhibitorArc(p2, b)
        petrinet.addArc(p2, c)
        petrinet.addArc(b, p3)
        petrinet.addArc(c, p3)
        petrinet.addArc(p3, d)
        petrinet.addArc(d, p4)
        
        a.pnmlId shouldEqual "null"
        p1.pnmlId shouldEqual "null"
        arc1.pnmlId shouldEqual "null"
        arc2.pnmlId shouldEqual "null"
        arc3.pnmlId shouldEqual "null"
        
        a.pnmlId = "t1"
        p1.pnmlId = "p1"
        arc1.pnmlId = "arc1"
        arc2.pnmlId = "arc2"
        arc3.pnmlId = "arc3"
        
        a.pnmlId shouldEqual "t1"
        p1.pnmlId shouldEqual "p1"
        arc1.pnmlId shouldEqual "arc1"
        arc2.pnmlId shouldEqual "arc2"
        arc3.pnmlId shouldEqual "arc3"
    }
}