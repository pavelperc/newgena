package com.pavelperc.newgena.graphviz

import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.toGraphviz
import org.junit.Assert.*
import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking
import java.io.File

class PetrinetGraphvizTest {
    
    
    @Test
    fun convert() {
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
        
        
//        val marking = Marking(listOf(p1, p1, p1, p1, p1, p1, p1))
        val marking = Marking(listOf(p1, p1, p1, p1, p1))
//        val marking = Marking(listOf(p1, p1))
        
        petrinet.toGraphviz(marking, saveToSvg = "gv/simpleNet.svg")
    }
}