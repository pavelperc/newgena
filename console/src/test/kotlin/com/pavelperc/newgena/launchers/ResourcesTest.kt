package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.complexResourceMapping
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.delayNoDeviation
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.fastGroups
import com.pavelperc.newgena.testutils.launchers.justGenerate
import com.pavelperc.newgena.testutils.petrinetUtils.simplePetrinetBuilder
import com.pavelperc.newgena.utils.xlogutils.*
import org.amshove.kluent.shouldBeIn
import org.junit.Test
import org.processmining.log.models.EventLogArray
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import java.time.Instant

class ResourcesTest {
    
    @Test
    fun resourceRace() {
        
        val petrinet = simplePetrinetBuilder("""
            places:
            p0 p1 p2
            transitions:
            t1 delay t2
            arcs:
            p1-->t1
            p0-->delay-->p2-->t2
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
                    "t2" to 4L,
                    "delay" to 1L
            )
            
            isUsingLifecycle = true
            isSeparatingStartAndFinish = true
            
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
        
        val logs = justGenerate(petrinet, settings)
        
        println(logs.allTraces.joinToString("\n---\n") { trace ->
            trace.drawEvents(startTime.toEpochMilli(), timeGranularity = 1000, drawLifeCycle = true, drawRes = true)
        })
        
        fun normedTime(time: Long) = (time - startTime.toEpochMilli()).toInt() / 1000
        
        
        val case1 = listOf(
                listOf("delay", null, "start", 0),
                listOf("t1", "res", "start", 0),
                listOf("delay", null, "complete", 1),
                listOf("t1", "res", "complete", 5),
                listOf("t2", "res", "start", 5),
                listOf("t2", "res", "complete", 9)
        )
        
        val case2 = listOf(
                listOf("t1", "res", "start", 0),
                listOf("delay", null, "start", 0), // reversed
                listOf("delay", null, "complete", 1),
                listOf("t1", "res", "complete", 5),
                listOf("t2", "res", "start", 5),
                listOf("t2", "res", "complete", 9)
        )
        
        logs.allTraces.forEach { trace ->
            trace.map {
                listOf(
                        it.name,
                        it.resource,
                        it.lifecycle,
                        normedTime(it.time)
                )
            } shouldBeIn arrayOf(case1, case2)
        }
    }
}