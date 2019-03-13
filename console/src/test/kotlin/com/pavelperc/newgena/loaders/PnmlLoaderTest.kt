package com.pavelperc.newgena.loaders

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.testutils.GraphvizDrawer
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Test

class PnmlLoaderTest : GraphvizDrawer(false) {
    
    @Test
    fun loadPnml() {
        
        println(System.getProperty("user.dir"))
        
        val (petrinet, marking) = PnmlLoader.loadPetrinet("../examples/petrinet/conjunction.pnml")
        
        petrinet.places.shouldNotBeEmpty()
        petrinet.transitions.shouldNotBeEmpty()
        
        petrinet.places.map { it.label } shouldContainSame listOf("place5", "place6", "place7", "place8")
        
        petrinet.transitions.map { it.label } shouldContainSame listOf("transition1", "transition2")
        
        petrinet.edges.map { it.label } shouldContainSame listOf("arc3", "arc4", "arc5", "arc6", "arc7")
        
        forDrawing += petrinet.toGraphviz(marking) to "loadPnml/conjunction.svg"
        
        
        println(petrinet)
        
    }
}