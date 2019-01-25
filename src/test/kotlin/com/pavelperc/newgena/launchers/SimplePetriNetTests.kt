package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.testutils.GraphvizDrawer
import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.utils.xlogutils.eventNames
import org.amshove.kluent.*
import org.junit.Test
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking

class SimplePetriNetTests : GraphvizDrawer(false) {
    
    @Test
    fun simplePetriNet() {
        
        
        val petrinet: Petrinet = PetrinetImpl("simplePetriNet")
        
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
        
        
        val initialMarking = Marking(listOf(p1))
        val finalMarking = Marking(listOf(p4))
        
        forDrawing += petrinet.toGraphviz(initialMarking) to "simplePetriNet.svg"
        
        val description = SimpleGenerationDescription(
                isRemovingUnfinishedTraces = true,
                isUsingNoise = false,
                numberOfLogs = 6,
                numberOfTraces = 8
        )
        
        
        // launch generator
        val logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        
        logArray.size shouldEqual description.numberOfLogs
        
        for (i in 0 until logArray.size) {
            val log = logArray.getLog(i)
            
            println("______________LOG $i")
            println(log.eventNames())
            
            
            log.size shouldEqual description.numberOfTraces
            for (trace in log) {
                trace.size shouldEqual 3
                val names = trace.eventNames()
                
                names[0] shouldEqual "A"
                names[2] shouldEqual "D"
                names[1] shouldBeIn listOf("B", "C")
            }
        }
    }
    
    @Test
    fun conjunction() {
        val petrinet: Petrinet = PetrinetImpl("conjunction")
        
        val t0 = petrinet.addTransition("t0")
        val t1 = petrinet.addTransition("t1")
        
        val p0 = petrinet.addPlace("p0")
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        
        petrinet.addArc(p0, t0)
        petrinet.addArc(t0, p1)
        petrinet.addArc(p1, t1)
        petrinet.addArc(p2, t1)
        petrinet.addArc(t1, p3)
        // p0
        // |
        // t0
        // |
        // p1    p2
        //  \    /
        //    t1
        //     |
        //    p3
        val initialMarking = Marking(listOf(p0, p0))
//        val initialMarking = Marking(listOf(p0, p2))
//        val finalMarking = Marking(listOf(p1, p2)) // ???? why the result is empty
        val finalMarking = Marking(listOf(p3))
        
        forDrawing += petrinet.toGraphviz(initialMarking, "has no traces") to "conjunction/1.svg"
        
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false,
                isRemovingUnfinishedTraces = false, // works very strange!!
                isRemovingEmptyTraces = false // works very strange!! TODO tests for isRemovingEmptyTraces and isRemovingUnfinishedTraces
        )
        
        description.isRemovingUnfinishedTraces shouldEqual false
        description.isRemovingEmptyTraces shouldEqual false
        
        
        var logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        
        logArray.eventNames().shouldBeEmpty()
        
        initialMarking.add(p2)
        finalMarking.add(p1) // should not finish p3
        forDrawing += petrinet.toGraphviz(initialMarking, "added p2") to "conjunction/2.svg"
        forDrawing += petrinet.toGraphviz(finalMarking, "expected result") to "conjunction/3.svg"
        
        logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        println(logArray.eventNames())
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldContainSame listOf("t0", "t0", "t1") }
        
        initialMarking.addAll(listOf(p1, p2, p2))
        finalMarking.addAll(listOf(p3, p3))
        finalMarking.remove(p1)
        forDrawing += petrinet.toGraphviz(initialMarking, "added p1, p2, p2") to "conjunction/4.svg"
        forDrawing += petrinet.toGraphviz(finalMarking, "new expected final marking. ?????") to "conjunction/5.svg"
        
        logArray = PetrinetGenerators.generateSimple(petrinet, initialMarking, finalMarking, description)
        
        println(logArray.eventNames())
        
        // !!!
        logArray.eventNames().shouldNotBeEmpty()
    }
}