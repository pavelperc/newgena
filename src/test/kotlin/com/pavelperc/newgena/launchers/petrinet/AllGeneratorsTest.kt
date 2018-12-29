package com.pavelperc.newgena.launchers.petrinet

import org.amshove.kluent.shouldBeIn
import org.amshove.kluent.shouldEqual
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.junit.Test
import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities

import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.ProgressBarCallback


class AllGeneratorsTest {
    
    private fun getConsoleCallback(description: GenerationDescription): ProgressBarCallback {
        var progress = 0
        val maxProgress = description.numberOfLogs * description.numberOfTraces
        return ProgressBarCallback {
            progress++
            println("progress: $progress from $maxProgress")
        }
    }
    
    
    @Test
    fun simplePetriNet() {
        
        
        val petrinet: Petrinet = PetrinetImpl("net1")
        
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
//    val finalMarking = Marking(listOf(p3))
        
        val description = SimpleGenerationDescription()
        description.isRemovingUnfinishedTraces = true
        description.isUsingNoise = false
        description.numberOfLogs = 6
        description.numberOfTraces = 8
        
        // launch generator
        val logArray = Generators.generateSimple(petrinet, initialMarking, finalMarking, description)
        
        logArray.size shouldEqual description.numberOfLogs
        
        for (i in 0 until logArray.size) {
            val log = logArray.getLog(i)
            
            println("______________LOG $i")
            println(log.map { trace -> trace.eventNames() })
    
    
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
    fun prioritySimpleTest() {
        
        
        val petrinet: Petrinet = PetrinetImpl("net1")
        
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
//    val finalMarking = Marking(listOf(p3))
        
        val description = GenerationDescriptionWithStaticPriorities(100)
        
        description.isRemovingUnfinishedTraces = true
        description.putPriorities(mapOf(
                a to 1,
                b to 2,
                c to 3, // should always choose c instead of b
                d to 4
        ))
        description.numberOfLogs = 6
        description.numberOfTraces = 8
        
        // launch generator
        val logArray = Generators.generateWithPriorities(petrinet, initialMarking, finalMarking, description)
        
        logArray.size shouldEqual description.numberOfLogs
        
        for (i in 0 until logArray.size) {
            val log = logArray.getLog(i)
            
            println("______________LOG $i")
            println(log.map { trace -> trace.eventNames() })
            
            log.size shouldEqual description.numberOfTraces
            for (trace in log) {
                trace.size shouldEqual 3
                val names = trace.eventNames()
                
                names[0] shouldEqual "A"
                names[2] shouldEqual "D"
                names[1] shouldEqual "C" // because of priority
//                names[1] shouldBeIn listOf("B", "C")
            }
        }
    }
    
    private fun XTrace.eventNames() = map { event -> event.attributes["concept:name"].toString() }
    
    //    private fun EventLogArray.toSeq(): Sequence<XLog> = sequence {
//        for (i in 0 until size) {
//            yield(getLog(i))
//        }
//    }
}