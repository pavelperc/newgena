package com.pavelperc.newgena.launchers

import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.Generator
import org.processmining.utils.ProgressBarCallback
import org.processmining.utils.helpers.SimpleGenerationHelper
import org.processmining.utils.helpers.StaticPrioritiesGenerationHelper
import org.processmining.utils.helpers.TimeDrivenGenerationHelper

/**
 * Common class to start all generators with Petrinet.
 */
object PetrinetGenerators {
    
    data class GenerationKit<G : GenerationDescription>(
            val petrinet: ResetInhibitorNet,
            val initialMarking: Marking,
            val finalMarking: Marking,
            val description: G
    )
    
    fun generateSimple(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: SimpleGenerationDescription,
            callback: ProgressBarCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description)
        return Generator(callback).generate(generationHelper)
    }
    
    
    fun generateWithPriorities(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: GenerationDescriptionWithStaticPriorities,
            callback: ProgressBarCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = StaticPrioritiesGenerationHelper.createStaticPrioritiesGenerationHelper(petrinet, initialMarking, finalMarking, description)
        return Generator(callback).generate(generationHelper)
    }
    
    
    fun generateWithTime(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: TimeDrivenGenerationDescription,
            callback: ProgressBarCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = TimeDrivenGenerationHelper.createInstance(petrinet, initialMarking, finalMarking, description)
        return Generator(callback).generate(generationHelper)
    }
    
    fun getConsoleCallback(description: GenerationDescription): ProgressBarCallback {
        var progress = 0
        val maxProgress = description.numberOfLogs * description.numberOfTraces
        return ProgressBarCallback {
            progress++
            println("progress: $progress from $maxProgress")
        }
    }
    
    val emptyCallback = ProgressBarCallback { }
}