package com.pavelperc.newgena.launchers.petrinet

import org.amshove.kluent.shouldBeIn
import org.amshove.kluent.shouldEqual
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.junit.Test
import org.processmining.framework.util.Pair
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities

import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.TimeDrivenLoggingSingleton
import java.lang.IllegalStateException
import java.util.*
import kotlin.test.assertTrue


class AllGeneratorsTest {
    
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
    fun priorityPetriNet() {
        
        
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
            println(log.map { trace -> trace.map { event -> event.name } })
            
            log.size shouldEqual description.numberOfTraces
            for (trace in log) {
                trace.size shouldEqual 3
                val names = trace.map { event -> event.name }
                
                names[0] shouldEqual "A"
                names[2] shouldEqual "D"
                names[1] shouldEqual "C" // because of priority
//                names[1] shouldBeIn listOf("B", "C")
            }
        }
    }
    
    @Test
    fun timeDrivenPetriNet() {
        
        
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
        
        val description = TimeDrivenGenerationDescription()
        
        description.time = mutableMapOf(
                a to Pair(100L, 0L), // delay in seconds, deviation
                b to Pair(200L, 0L),
                c to Pair(300L, 0L),
                d to Pair(400L, 0L)
        )
        // fails with npe without this line)
        TimeDrivenLoggingSingleton.init(description)
        
        description.isUsingNoise = false
        description.isUsingResources = false
        description.isRemovingUnfinishedTraces = true
        
        description.generationStart.set(2000, Calendar.DECEMBER, 1, 0, 0)
        val startDate = description.generationStart.time!!
        
        description.minimumIntervalBetweenActions = 10
        description.maximumIntervalBetweenActions = 10
        
        
        // launching generator
        val logArray = Generators.generateWithTime(petrinet, initialMarking, finalMarking, description)
        
        logArray.size shouldEqual description.numberOfLogs
        
        for (i in 0 until logArray.size) {
            val log = logArray.getLog(i)

//            println("______________LOG $i")
//            println(log.map { trace -> trace.eventNames() })
            
            log.size shouldEqual description.numberOfTraces
            for (trace in log) {
//                trace.size shouldEqual 3
                
                val names = trace.map { event -> event.name }
                val timestamps = trace.map { event -> event.time }
                // diffs in seconds
                val diffs = timestamps.map { date -> date.time - startDate.time }.map { it / 1000 }
                
                println(names)
//                println(timestamps)
                println(diffs)
                
                // just copied from output))
                
                val case1 = listOf("A", "A", "B", "B", "D", "D") to
                        listOf(0L, 100L, 110L, 310L, 320L, 720L)
                
                val case2 = listOf("A", "A", "C", "C", "D", "D") to
                        listOf(0L, 100L, 110L, 410L, 420L, 820L)
                
                (names to diffs) shouldBeIn listOf(case1, case2)
            }
        }
    }
    
    private val XEvent.name
        get() = attributes["concept:name"].toString()
    
    private val XEvent.time
        get() = XTimeExtension.instance().extractTimestamp(this)
                ?: throw IllegalStateException("No timeStamp in event $name")
    
    
    private fun XTrace.eventNames() = map { event -> event.name }
    
}