package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.graphviz.toGraphviz
import org.junit.Test
import com.pavelperc.newgena.testutils.GraphvizDrawer
import com.pavelperc.newgena.utils.xlogutils.eventNames
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEmpty
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.helpers.SimpleGenerationHelper

class InhibitorResetPetriNetTests : GraphvizDrawer(false) {
    
    @Test
    fun testSimpleGenerationHelper() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addResetArc(p1, a)
        petrinet.addInhibitorArc(p2, a)
        petrinet.addArc(p3, a)
        
        petrinet.addArc(a, p4)
        
        // p1 -->>\
        // p2 ----o|- A --- p4
        // p3 ---/
        
        val initialMarking = Marking(listOf(p3))
        val finalMarking = Marking(listOf(p4))
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false
        )
        
        
        val generationHelper = SimpleGenerationHelper.createFromInhibitorReset(petrinet, initialMarking, finalMarking, description)
        
        val transitions = generationHelper.allModelMovables
        
        val t1 = transitions.first()
        
        t1.inputPlaces.size shouldEqual 1
        t1.inputInhibitorArcPlaces.size shouldEqual 1
        t1.inputResetArcPlaces.size shouldEqual 1
        
        t1.outputPlaces.size shouldEqual 1
    }
    
    @Test
    fun inhibitorNet() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addInhibitorArc(p1, a)
        petrinet.addInhibitorArc(p2, a)
        petrinet.addArc(p3, a)
        
        petrinet.addArc(a, p4)
        
        // p1 ---o\
        // p2 ----o|- A --- p4
        // p3 ---/
        
        val initialMarking = Marking(listOf(p3))
        val finalMarking = Marking(listOf(p4))
        
        forDrawing += petrinet.toGraphviz(initialMarking, "initial marking") to "inhibitorNet/1.svg"
        forDrawing += petrinet.toGraphviz(finalMarking, "final marking") to "inhibitorNet/2.svg"
        
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false
        )
        
        
        val logArray = PetrinetGenerators.generateInhibitorReset(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldEqual listOf("A") }
    }
    
    @Test
    fun resetArcNet() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
//        val b = petrinet.addTransition("B")
//        val c = petrinet.addTransition("C")
//        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addResetArc(p1, a)
        petrinet.addResetArc(p2, a)
        petrinet.addArc(p3, a)
        
        petrinet.addArc(a, p4)
        
        // p1(3) --->>\
        // p2(1) ---->>|- A --- p4
        // p3(2) ---/
        
        val initialMarking = Marking(listOf(p1, p1, p1, p2, p3, p3))
        val finalMarking = Marking(listOf(p3, p4))
        
        forDrawing += petrinet.toGraphviz(initialMarking, "initial marking") to "resetNet/1.svg"
        forDrawing += petrinet.toGraphviz(finalMarking, "final marking") to "resetNet/2.svg"
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false
        )
        
        
        val logArray = PetrinetGenerators.generateInhibitorReset(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldEqual listOf("A") }
    }
}
