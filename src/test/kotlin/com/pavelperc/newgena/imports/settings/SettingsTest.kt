package com.pavelperc.newgena.imports.settings

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl

@kotlin.ExperimentalUnsignedTypes
class SettingsTest {
    
    private fun Any.toJson(): String {
        val mapper = ObjectMapper()
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }
    
    
    @Test
    fun `save settings to json`() {
        val petrinet = ResetInhibitorNetImpl("simplePetriNet")
    
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
        
        
        val settings = Settings()
        
        settings.marking.initialPlaceIds = mutableListOf("p1", "p2", "p3")
        settings.marking.finalPlaceIds = mutableListOf("p4")
        
        
        
        println("simple: " + settings.toJson())
        
        settings.staticPriorities = Settings.StaticPriorities().apply {
            this.maxPriority = 10
            this.transitionIdsToPriorities = mutableMapOf(
                    a.id.toString() to 1,
                    b.id.toString() to 2,
                    c.id.toString() to 3,
                    d.id.toString() to 4
            )
        }
        println("\n\nstatic priorities: " + settings.toJson())
        
//        settings.generationDescription = TimeDrivenGenerationDescription()
//        println("\n\ntime driven: " + settings.toJson())
        
        
        
    }
}