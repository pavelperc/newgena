package com.pavelperc.newgena.imports

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.testutils.GraphvizDrawer
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Test

class PnmlLoaderTest : GraphvizDrawer(false) {
    
    @Test
    fun loadPnml() {
        
        
        val (petrinet, marking) = PnmlLoader.loadPetrinet("examples/petrinet/conjunction.pnml")
        
        petrinet.places.shouldNotBeEmpty()
        petrinet.transitions.shouldNotBeEmpty()
        
        forDrawing += petrinet.toGraphviz(marking) to "loadPnml/conjunction.svg"
        
        
        println(petrinet)
        
    }
}