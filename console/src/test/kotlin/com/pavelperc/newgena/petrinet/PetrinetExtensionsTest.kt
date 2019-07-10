package com.pavelperc.newgena.petrinet

import com.pavelperc.newgena.petrinet.petrinetExtensions.*
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge
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
        arcs += petrinet.addArc(p1, a) // pnmlId = arc1
        arcs += petrinet.addArc(a, p2) // pnmlId = arc2
        arcs += petrinet.addArc(p2, b) // pnmlId = arc3
        arcs += petrinet.addArc(p2, c) // pnmlId = arc4
        arcs += petrinet.addArc(b, p3) // pnmlId = arc5
        arcs += petrinet.addArc(c, p3) // pnmlId = arc6
        arcs += petrinet.addArc(p3, d) // pnmlId = arc7
        arcs += petrinet.addArc(d, p4) // pnmlId = arc8
        
        petrinet.makePnmlIdsFromLabels()
        arcs.makePnmlIdsOrdinal()
        //                  B
        //               /     \
        // p1 -> A -> p2 -> C -> p3 -> D -> p4
        
        
        // arc 9 should not be found.
        assertFailsWith<IllegalArgumentException> {
            petrinet.markInhResetArcsByIds(inhibitorArcIds = listOf("arc1", "arc9"))
        }.also { println(it) }
        
        
        // nothing should be changed
        petrinet.getArc(p1, a).shouldNotBeNull()
        
        
        petrinet.markInhResetArcsByIds(
                inhibitorArcIds = listOf("arc1"),
                resetArcIds = listOf("arc4")
        )
        
        petrinet.getArc(p1, a).shouldBeNull()
        petrinet.getArc(p2, c).shouldBeNull()
        
        petrinet.getInhibitorArc(p1, a).shouldNotBeNull()
        petrinet.getResetArc(p2, c).shouldNotBeNull()
        
        
        // check if we can change again already changed arcs
        petrinet.markInhResetArcsByIds(
                resetArcIds = listOf("arc1"),
                inhibitorArcIds = listOf("arc4")
        )
        
        petrinet.getArc(p1, a).shouldBeNull()
        petrinet.getArc(p2, c).shouldBeNull()
        
        petrinet.getResetArc(p1, a).shouldNotBeNull()
        petrinet.getInhibitorArc(p2, c).shouldNotBeNull()
    }
    
    @Test
    fun testPnmlIdAutoFill() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("simplePetriNet")
    
        val a = petrinet.addTransition("a")
        val b = petrinet.addTransition("b")
    
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        val edges = mutableListOf<PetrinetEdge<*,*>>()
        edges += petrinet.addResetArc(p1, a)
        edges += petrinet.addInhibitorArc(p2, a)
        edges += petrinet.addArc(p3, a)
        edges += petrinet.addArc(a, p4)
        edges += petrinet.addArc(p4, b)
        
        val nodes = listOf(a, b, p1, p2, p3, p4)
        nodes.forEach { it.pnmlId shouldEqual DEFAULT_PNML_ID }
        edges.forEach { it.pnmlId shouldEqual DEFAULT_PNML_ID }
        
        petrinet.makePnmlIdsFromLabels()
        nodes.map { it.pnmlId } shouldEqual listOf("a", "b", "p1", "p2", "p3", "p4")
        
        edges.makePnmlIdsOrdinal()
        edges.map { it.pnmlId } shouldEqual listOf("arc1", "arc2", "arc3", "arc4", "arc5")
        
        petrinet.makeArcPnmlIdsFromEnds()
        edges.map { it.pnmlId } shouldEqual listOf("p1_a", "p2_a", "p3_a", "a_p4", "p4_b")
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
        
        a.pnmlId shouldEqual DEFAULT_PNML_ID
        p1.pnmlId shouldEqual DEFAULT_PNML_ID
        arc1.pnmlId shouldEqual DEFAULT_PNML_ID
        arc2.pnmlId shouldEqual DEFAULT_PNML_ID
        arc3.pnmlId shouldEqual DEFAULT_PNML_ID
        
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