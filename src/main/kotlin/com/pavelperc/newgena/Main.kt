package com.pavelperc.newgena

import org.deckfour.xes.model.XLog
import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.Generator
import org.processmining.utils.ProgressBarCallback
import org.processmining.utils.helpers.GenerationHelper
import org.processmining.utils.helpers.SimpleGenerationHelper
import kotlin.coroutines.experimental.buildSequence
import kotlin.math.max


object MyBaseLogGenerator {
    
    public fun generate(
            petrinet: Petrinet,
            initialMarking: Marking,
            finalMarking: Marking,
            description: SimpleGenerationDescription
    ): EventLogArray {
        
        val generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description)
        val logArray = generateWithProgressbar(generationHelper, description)
        
        return logArray
        
    }
    
    private fun generateWithProgressbar(helper: GenerationHelper<*, *>, description: GenerationDescription): EventLogArray {
        val progress = 0
        val maxProgress = description.numberOfLogs * description.numberOfTraces
        
        val callback = ProgressBarCallback {
            progress.inc()
            println("progress: $progress from $maxProgress")
        }
        return Generator(callback).generate(helper)
    }
}

fun main(args: Array<String>) {
    
    println("Hello world!!")
    
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
    description.numberOfLogs = 4
    description.numberOfTraces = 6
    
    
    val logArray = MyBaseLogGenerator.generate(petrinet, initialMarking, finalMarking, description)
    
    for (i in 0 until logArray.size) {
        println("______________LOG $i")
        val log = logArray.getLog(i)
        log
                .map { it.map { it.attributes["concept:name"] } }
                .also { println(it) }
    }
    
}
//
//fun EventLogArray.iter() = buildSequence {
//    for (i in 0 until size) {
//        yield(getLog(i))
//    }
//}

