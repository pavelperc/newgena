package com.pavelperc.newgena.imports

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.testutils.GraphvizDrawer
import org.junit.Test
import org.processmining.models.semantics.petrinet.Marking

class PetriNetLoaderTest : GraphvizDrawer(false) {
    
    @Test
    fun loadPnml() {

//        println(Paths.get("").toAbsolutePath().toString())
        
        val marking = Marking()
        val petrinet = PetriNetLoader.loadPetriNet("examples/pnml/conjunction.pnml", marking)
        
        forDrawing += petrinet.toGraphviz(marking) to "loadPnml/Conjunction.svg"
        
        
        
        println(petrinet)
        
    }
}