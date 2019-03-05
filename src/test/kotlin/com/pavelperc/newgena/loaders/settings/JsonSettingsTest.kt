package com.pavelperc.newgena.loaders.settings

import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl

@kotlin.ExperimentalUnsignedTypes
class JsonSettingsTest {
    
    
    
    @Test
    fun `save and load json settings`() {
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
        
        
        val settings = JsonSettings()
        
        settings.marking.initialPlaceIds = mutableListOf("p1", "p2", "p3")
        settings.marking.finalPlaceIds = mutableListOf("p4")
        
        
        settings.staticPriorities = JsonSettings.StaticPriorities().apply {
            this.maxPriority = 10
            this.transitionIdsToPriorities = mutableMapOf(
                    a.id.toString() to 1,
                    b.id.toString() to 2,
                    c.id.toString() to 3,
                    d.id.toString() to 4
            )
        }
        
        
        settings.isUsingNoise = false
        settings.isUsingTime = true
        
//        settings.timeDescription?.timeDrivenNoise = null
        
        var json = settings.toJson()
        json = JsonSettings.fromJson(json).toJson()
        
        println("time driven:\n$json")
    }
}