package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.graphviz.PetrinetDrawer
import com.pavelperc.newgena.testutils.GraphvizDrawer
import com.pavelperc.newgena.utils.common.markingOf
import com.pavelperc.newgena.utils.xlogutils.eventNames
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Test
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.helpers.SimpleGenerationHelper

class InhibitorResetPetrinetTest : GraphvizDrawer(false) {
    
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
        
        
        val generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description)
        
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
        
        forDrawing += PetrinetDrawer(petrinet, initialMarking, finalMarking).makeGraph() to "inhibitorNet/1.svg"
        
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false
        )
        
        
        val logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldEqual listOf("A") }
    }
    
    @Test
    fun resetArcNet() {
        val petrinet = ResetInhibitorNetImpl("resetInhibitorNet")
        
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
        
        forDrawing += PetrinetDrawer(petrinet, initialMarking, finalMarking).makeGraph() to "resetNet/1.svg"
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false
        )
        
        
        val logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldEqual listOf("A") }
    }
    
    
    @Test
    fun resetArcNetRandomFiring() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        
        petrinet.addResetArc(p1, a)
        petrinet.addArc(a, p2, 2)
        
        // p1(7) --->> A --2-> p2
        
        val initialMarking = markingOf(p1 to 7)
        val finalMarking = markingOf(p2 to 4)
        
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false,
                isRemovingUnfinishedTraces = true,
                isRemovingEmptyTraces = true,
                maxNumberOfSteps = 4, // all steps should fire
                numberOfTraces = 20,
                numberOfLogs = 1
        )
        
        val logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace.size shouldEqual 2 } // generate two tokens twice
    }
    
    
    @Test
    fun priorityInhResetNet() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("simplePetriNet")

//        val a = petrinet.addTransition("A")
        val b = petrinet.addTransition("B")
        val c = petrinet.addTransition("C")
        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addArc(p2, b)
        petrinet.addResetArc(p2, c)
        petrinet.addArc(b, p3)
        petrinet.addArc(c, p3)
        petrinet.addInhibitorArc(p3, d)
        petrinet.addArc(p1, d)
        petrinet.addArc(d, p4)
        //       B          p1
        //   /     \         \
        // p2 ->> C -> p3 --o D -> p4
        
        
        val description = GenerationDescriptionWithStaticPriorities(
                maxPriority = 100,
                numberOfLogs = 6,
                numberOfTraces = 8,
                priorities = mapOf(
                        b to 1,
                        c to 2, // should always choose c instead of b
                        d to 3 // will be chosen first
                )
        )
        
        var logArray = PetrinetGenerators.generateWithPriorities(
                petrinet,
                Marking(listOf(p2, p2, p1)),
                Marking(listOf(p3, p4)),
                description
        )
        logArray.eventNames().shouldNotBeEmpty().forEach { it shouldEqual listOf("D", "C") }
        
        description.putPriorities(mapOf(
                b to 3, // first
                c to 2,
                d to 1
        ))
    
        logArray = PetrinetGenerators.generateWithPriorities(
                petrinet,
                Marking(listOf(p2, p2, p1)),
                Marking(listOf(p3, p3, p1)),
                description
        )
        logArray.eventNames().shouldNotBeEmpty().forEach { it shouldEqual listOf("B", "B") }
        
        
    }
}
