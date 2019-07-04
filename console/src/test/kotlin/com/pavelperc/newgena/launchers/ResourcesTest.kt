package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.loaders.settings.JsonResources
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.JsonTimeDescription
import com.pavelperc.newgena.models.makeArcPnmlIdsFromEnds
import com.pavelperc.newgena.models.makePnmlIdsFromLabels
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.complexResourceMapping
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.delayNoDeviation
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.fastGroups
import com.pavelperc.newgena.testutils.petrinetUtils.simplePetrinetBuilder
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.xlogutils.eventNames
import com.pavelperc.newgena.utils.xlogutils.printEvents
import com.pavelperc.newgena.utils.xlogutils.toList
import org.junit.Test
import org.processmining.log.models.EventLogArray
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.models.time_driven_behavior.ResourceMapping
import java.lang.IllegalArgumentException
import java.time.Instant

class ResourcesTest {
    
    
    /** Test version of launch from petrinet, settings and marking */
    fun generate(
            petrinet: ResetInhibitorNet,
            settings: JsonSettings
    ): EventLogArray {
        val builder = JsonSettingsBuilder(petrinet, settings)
        val generationDescription = builder.buildDescription()
        val (initialMarking, finalMarking) = builder.buildMarking()
        
        val generationKit = PetrinetGenerators.GenerationKit(
                petrinet,
                initialMarking = initialMarking,
                finalMarking = finalMarking,
                description = generationDescription
        )
        return PetrinetGenerators.generateFromKit(generationKit)
    }
    
    
    @Test
    fun resourceRace() {
        
        val petrinet = simplePetrinetBuilder("""
            places:
            p0 p1 p2
            transitions:
            t1 delay t2
            arcs:
            p1-->t1
            p0-->delay
            delay-->p2
            p2-->t2
        """.trimIndent())
        
        // (p1) ---------------> t1   ----  resource
        // (p0)-> delay -> (p2) -> t2    --/
        
        val settings = JsonSettings()
        settings.isUsingTime = true
        settings.timeDescription.apply {
            generationStart = Instant.parse("2000-01-01T00:00:00Z")
            
            transitionIdsToDelays = delayNoDeviation(
                    "t1" to 5L,
                    "t2" to 5L,
                    "delay" to 1L
            )
            
            isUsingLifecycle = false
            
            resourceGroups = fastGroups("group:role:res")
            
            transitionIdsToResources = mutableMapOf(
                    "t1" to complexResourceMapping("res")
            )
        }
        
        settings.petrinetSetup.marking.apply {
            initialPlaceIds = mutableMapOf("p1" to 1, "p0" to 1)
        }
        
        settings.numberOfLogs = 1
        settings.numberOfTraces = 10
        
        val logs = generate(petrinet, settings)

        println(logs.getLog(0).joinToString("\n---\n") { it.printEvents() })
        
        
    }
}