package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.setFinalMarking
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.setInitialMarking
import com.pavelperc.newgena.testutils.launchers.justGenerate
import com.pavelperc.newgena.testutils.petrinetUtils.simplePetrinetBuilder
import com.pavelperc.newgena.utils.xlogutils.exportXml
import org.deckfour.xes.model.XAttributeMap
import org.deckfour.xes.model.buffered.XAttributeMapBufferedImpl
import org.deckfour.xes.model.buffered.XTraceBufferedImpl
import org.junit.Test


class BigLogTest {
    
    @Test
    fun bufferedTraceTest() {
        
        val petrinet = simplePetrinetBuilder("""
            places:
            p1 p2
            transitions:
            t1 t2
            arcs:
            p1-->t1-->p2-->t2-->p1
        """.trimIndent())

        val settings = JsonSettings().apply { 
            setInitialMarking("p1" to 1)
            setFinalMarking()
            maxNumberOfSteps = 500_000
            numberOfLogs = 10
            numberOfTraces = 5
            isRemovingUnfinishedTraces = false
        }


        val builder = JsonSettingsBuilder(petrinet, settings)
        val generationDescription = builder.buildDescription()
        val (initialMarking, finalMarking) = builder.buildMarking()

        val generationKit = PetrinetGenerators.GenerationKit(
                petrinet,
                initialMarking = initialMarking,
                finalMarking = finalMarking,
                description = generationDescription
        )
        
        var oldPercents = 0
        val logs = PetrinetGenerators.generateFromKit(generationKit) { progress, maxProgress -> 
            val percents = progress * 100 / maxProgress
            if (percents != oldPercents) {
                println("$percents %")
            }
            oldPercents = percents
        }
        
        logs.exportXml("../xes-out/big/big.xes")
    }
}