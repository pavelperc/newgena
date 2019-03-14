package com.pavelperc.newgena.loaders.pnml

import com.pavelperc.newgena.graphviz.toGraphviz
import com.pavelperc.newgena.models.pnmlId
import com.pavelperc.newgena.testutils.GraphvizDrawer
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Test

class PnmlLoaderTest : GraphvizDrawer(false) {
    
    @Test
    fun testLoadPnmlOwnParser() {
        
        println(System.getProperty("user.dir"))
        
        val (petrinet, marking) = PnmlLoader.loadPetrinetOwnParser("../examples/petrinet/simple.pnml")
        
        petrinet.places.shouldNotBeEmpty()
        petrinet.transitions.shouldNotBeEmpty()
        
        petrinet.places.map { it.pnmlId } shouldContainSame listOf(1, 2, 3, 4).map { "place$it" }
        
        petrinet.transitions.map { it.pnmlId } shouldContainSame listOf(1, 3, 4, 5).map { "transition$it" }
        
        petrinet.edges.map { it.pnmlId } shouldContainSame listOf(1, 2, 4, 5, 6, 9, 10, 11).map { "arc$it" }
        
        petrinet.edges.map { it.source.pnmlId to it.target.pnmlId } shouldContainSame listOf(
                "place1" to "transition3",
                "transition3" to "place2",
                "place2" to "transition1",
                "transition1" to "place3",
                "place3" to "transition4",
                "place2" to "transition5",
                "transition4" to "place4",
                "transition5" to "place3"
        )
        
        forDrawing += petrinet.toGraphviz(marking) to "loadPnml/simple.svg"
        
        
    }

//    @Test
//    fun testLip6() {
//        // http://pnml.lip6.fr/documentation.html
//        
//        val file = File("../examples/petrinet/conjunction.pnml") 
//        // Load the document. No fall back to any compatible type (false).
//        // Fall back takes place between an unknown Petri Net type and the CoreModel.
//        val rc = PNMLUtils.importPnmlDocument(file, false)
//        // Determine the Petri Net Document type... See code snippets below
//    
//    
//        val ptDoc = rc as fr.lip6.move.pnml.ptnet.hlapi.PetriNetDocHLAPI
//    }
}