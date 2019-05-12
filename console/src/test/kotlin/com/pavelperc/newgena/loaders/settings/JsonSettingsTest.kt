package com.pavelperc.newgena.loaders.settings

import org.junit.Test
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import java.time.Instant

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
        
        settings.petrinetSetup.marking.initialPlaceIds = mutableMapOf("p1" to 1, "p2" to 1, "p3" to 1)
        settings.petrinetSetup.marking.finalPlaceIds = mutableMapOf("p4" to 1)
        
        
        settings.staticPriorities!!.apply {
            this.maxPriority = 10
            this.transitionIdsToPriorities = mutableMapOf(
                    a.label to 1,
                    b.label to 2,
                    c.label to 3,
                    d.label to 4
            )
        }
        
        settings.timeDescription.apply {
            isUsingResources = true
            resourceGroups = mutableListOf(
                    JsonResources.Group(
                            name = "group1",
                            roles = mutableListOf(
                                    JsonResources.Role(
                                            name = "role1",
                                            resources = mutableListOf(
                                                    JsonResources.Resource("resource1", 100L, 200L)
                                            ))
                            )
                    )
            )
            transitionIdsToResources = mutableMapOf(
                    "transition1" to JsonResources.ResourceMapping(
                            fullResourceNames = mutableListOf(JsonResources.ResourceMapping.FullResourceName(
                                    "group1", "role1", "resource1"
                            )),
                            simplifiedResourceNames = mutableListOf()
                    ),
                    "transition2" to JsonResources.ResourceMapping(
                            fullResourceNames = mutableListOf(JsonResources.ResourceMapping.FullResourceName(
                                    "group1", "role1", "resource1"
                            )),
                            simplifiedResourceNames = mutableListOf()
                    ),
                    "transition3" to JsonResources.ResourceMapping(
                            fullResourceNames = mutableListOf(),
                            simplifiedResourceNames = mutableListOf("simpleResource1", "simpleResource2")
                    )
            
            )
            transitionIdsToDelays = mutableMapOf("t1" to JsonTimeDescription.DelayWithDeviation(2L, 1L))
        }
        
        
        settings.isUsingNoise = false
        settings.isUsingTime = true
        
//        settings.timeDescription?.timeDrivenNoise = null
        
        var json = settings.toJson()
        json = JsonSettings.fromJson(json).toJson()
        
        println("time driven:\n$json")
    }
}