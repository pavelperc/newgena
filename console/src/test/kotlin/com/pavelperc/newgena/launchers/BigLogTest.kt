package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.setFinalMarking
import com.pavelperc.newgena.testutils.jsonSettingsHelpers.setInitialMarking
import com.pavelperc.newgena.petrinet.fastPetrinet.buildFastPetrinet
import com.pavelperc.newgena.utils.xlogutils.exportXml
import org.junit.Test
import org.processmining.utils.percentCallBack


class BigLogTest {
    
    @Test
    fun bufferedTraceTest() {
        
        val petrinet = buildFastPetrinet("""
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
            numberOfLogs = 2
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
        
        val logs = PetrinetGenerators.generateFromKit(generationKit, percentCallBack { percents, _ ->
            println("$percents %")
        })
        
        logs.exportXml("../xes-out/big/big.xes")
    }
}