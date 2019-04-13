package com.pavelperc.newgena.graphviz

import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.models.semantics.petrinet.Marking

class PetrinetGraphvizTest {
    
    
    @Test
    fun convertAndSave() {
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
        
        PetrinetDrawer(petrinet, marking).makeGraph("gv/simpleNet.svg")
    }
    
    
    @Test
    fun resetInhibitorArcs() {
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
        val b = petrinet.addTransition("B")
        val c = petrinet.addTransition("C")
        val d = petrinet.addTransition("D")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addResetArc(p1, a)
        petrinet.addArc(a, p2)
        petrinet.addInhibitorArc(p2, b)
        petrinet.addArc(p2, c)
        petrinet.addArc(b, p3)
        petrinet.addArc(c, p3)
        petrinet.addArc(p3, d)
        petrinet.addArc(d, p4)
        //                  B
        //               /     \
        // p1 -> A -> p2 -> C -> p3 -> D -> p4


//        val marking = Marking(listOf(p1, p1, p1, p1, p1, p1))
//        val marking = Marking(listOf(p1))
        val marking = Marking(listOf(p1, p1))
        
        PetrinetDrawer(petrinet, marking).makeGraph("gv/resetInhibitorArcs.svg")
    }
    
    
}