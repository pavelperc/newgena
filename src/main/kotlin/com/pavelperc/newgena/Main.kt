package com.pavelperc.newgena

import com.pavelperc.newgena.launchers.petrinet.MyBaseLogGenerator
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking

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

