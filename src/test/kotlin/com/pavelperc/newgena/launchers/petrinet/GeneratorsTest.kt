package com.pavelperc.newgena.launchers.petrinet

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeIn
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEmpty
import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.model.impl.XAttributeMapImpl
import org.deckfour.xes.model.impl.XLogImpl
import org.junit.Test
import org.processmining.framework.util.Pair
import org.processmining.log.models.EventLogArray
import org.processmining.log.models.impl.EventLogArrayImpl
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


class GeneratorsTest {
    
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
        
        val description = SimpleGenerationDescription(
                isRemovingUnfinishedTraces = true,
                isUsingNoise = false,
                numberOfLogs = 6,
                numberOfTraces = 8
        )
        
        
        // launch generator
        val logArray = Generators.generateSimple(petrinet, initialMarking, finalMarking, description)
        
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
        val petrinet: Petrinet = PetrinetImpl("net1")
        
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
        val initialMarking = Marking(listOf(p0))
//        val initialMarking = Marking(listOf(p0, p2))
//        val finalMarking = Marking(listOf(p1, p2)) // ???? why the result is empty
        val finalMarking = Marking(listOf(p3))
        
        val description = SimpleGenerationDescription(
                isUsingNoise = false,
                isRemovingUnfinishedTraces = true, // works very strange!!
                isRemovingEmptyTraces = true // works very strange!! TODO tests for isRemovingEmptyTraces and isRemovingUnfinishedTraces
        )
        
        var logArray = Generators.generateSimple(petrinet, initialMarking, finalMarking, description)
    
//        println(logArray.eventNames())
//        return
        
        logArray.eventNames().shouldBeEmpty()
        
        initialMarking.add(p2) // why we don't add another p3 to final ???
        logArray = Generators.generateSimple(petrinet, initialMarking, finalMarking, description)
        
        logArray.eventNames()
                .shouldNotBeEmpty()
                .forEach { trace -> trace shouldEqual listOf("t0", "t1") } // why t1 is repeating just once?
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
        
        val description = GenerationDescriptionWithStaticPriorities(
                maxPriority = 100,
                numberOfLogs = 6,
                numberOfTraces = 8,
                priorities = mapOf(
                        a to 1,
                        b to 2,
                        c to 3, // should always choose c instead of b
                        d to 4
                )
        )
        
        // launch generator
        val logArray = Generators.generateWithPriorities(petrinet, initialMarking, finalMarking, description)
        
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
        
        val description = TimeDrivenGenerationDescription(
                time = mutableMapOf(
                        a to Pair(100L, 0L), // delay in seconds, deviation
                        b to Pair(200L, 0L),
                        c to Pair(300L, 0L),
                        d to Pair(400L, 0L)
                ),
                isUsingNoise = false,
                isUsingResources = false,
                minimumIntervalBetweenActions = 10,
                maximumIntervalBetweenActions = 10
        )
        // fails with npe without this line)
        TimeDrivenLoggingSingleton.init(description)
        
        description.generationStart.set(2000, Calendar.DECEMBER, 1, 0, 0)
        val startDate: Date = description.generationStart.time!!
        
        
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
                ?: throw IllegalStateException("No timestamp was found in event $name")
    
    private fun EventLogArray.toSeq(): Sequence<XLog> = sequence {
        0.until(size).forEach { i -> yield(getLog(i)) }
    }
    private fun EventLogArray.toList() = this.toSeq().toList()
    
    private fun XTrace.eventNames() = map { event -> event.name }
    private fun XLog.eventNames() = map { trace -> trace.eventNames() }
    
    private fun EventLogArray.eventNames() = toList().flatMap { log -> log.eventNames() }
    
}