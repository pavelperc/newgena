package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.complexResourceMapping
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.delayNoDeviation
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.fastGroups
import com.pavelperc.newgena.testutils.petrinetUtils.simplePetrinetBuilder
import com.pavelperc.newgena.utils.xlogutils.drawEvents
import org.junit.Test
import org.processmining.log.models.EventLogArray
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
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
        
        
        val startTime = Instant.parse("2000-01-01T00:00:00Z")
        
        val settings = JsonSettings()
        settings.isUsingTime = true
        settings.timeDescription.apply {
            generationStart = startTime
            
            transitionIdsToDelays = delayNoDeviation(
                    "t1" to 5L,
                    "t2" to 5L,
                    "delay" to 1L
            )
            
            isUsingLifecycle = true
            isSeparatingStartAndFinish = false
            
            isUsingSynchronizationOnResources = true
            
            isUsingResources = true
            resourceGroups = fastGroups("::res")
            
            transitionIdsToResources = mutableMapOf(
                    "t1" to complexResourceMapping("res"),
                    "t2" to complexResourceMapping("res")
                    
            )
            minimumIntervalBetweenActions = 0
            maximumIntervalBetweenActions = 0
        }
        
        settings.petrinetSetup.marking.apply {
            initialPlaceIds = mutableMapOf("p1" to 1, "p0" to 1)
        }
        
        settings.numberOfLogs = 1
        settings.numberOfTraces = 10
        
        val logs = generate(petrinet, settings)
        
        println(logs.getLog(0).joinToString("\n---\n") { trace ->
            trace.drawEvents(startTime.toEpochMilli(), timeGranularity = 1000, drawLifeCycle = true, drawRes = true)
        })
        
        
    }
}