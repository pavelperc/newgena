package com.pavelperc.newgena.launchers

import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.GenerationDescriptionWithStaticPriorities
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.descriptions.TimeDrivenGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.GenerationCallback
import org.processmining.utils.Generator
import org.processmining.utils.emptyCallback
import org.processmining.utils.helpers.SimpleGenerationHelper
import org.processmining.utils.helpers.StaticPrioritiesGenerationHelper
import org.processmining.utils.helpers.TimeDrivenGenerationHelper


/**
 * Common class to start all generators with Petrinet.
 */
object PetrinetGenerators {
    
    
    /** Generation kit for petri net */
    data class GenerationKit(
            val petrinet: ResetInhibitorNet,
            val initialMarking: Marking,
            val finalMarking: Marking,
            val description: GenerationDescription
    )
    
    fun generateFromKit(generationKit: GenerationKit, callback: GenerationCallback = { _, _ -> }): EventLogArray {
        val logArray = with(generationKit) {
            //        petrinet.toGraphviz(initialMarking, saveToSvg = "gv/simpleExample/simple.svg")
            
            when (description) {
                is SimpleGenerationDescription -> PetrinetGenerators.generateSimple(
                        petrinet,
                        initialMarking,
                        finalMarking,
                        description,
                        callback)
                is GenerationDescriptionWithStaticPriorities -> PetrinetGenerators.generateWithPriorities(
                        petrinet,
                        initialMarking,
                        finalMarking,
                        description,
                        callback)
                is TimeDrivenGenerationDescription -> PetrinetGenerators.generateWithTime(
                        petrinet, // TODO: pavel ADAPT TIMEDRIVEN TO INHIBITOR AND RESET NETS.
                        initialMarking,
                        finalMarking,
                        description,
                        callback)
                
                else -> throw IllegalStateException("Unsupported type of generation description")
            }
        }
        return logArray
    }
    
    
    fun generateSimple(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: SimpleGenerationDescription,
            callback: GenerationCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description)
        return Generator(generationHelper, callback).generate()
    }
    
    
    fun generateWithPriorities(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: GenerationDescriptionWithStaticPriorities,
            callback: GenerationCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = StaticPrioritiesGenerationHelper.createStaticPrioritiesGenerationHelper(petrinet, initialMarking, finalMarking, description)
        return Generator(generationHelper, callback).generate()
    }
    
    
    fun generateWithTime(
            petrinet: PetrinetGraph,
            initialMarking: Marking,
            finalMarking: Marking,
            description: TimeDrivenGenerationDescription,
            callback: GenerationCallback = emptyCallback
    ): EventLogArray {
        val generationHelper = TimeDrivenGenerationHelper.createInstance(petrinet, initialMarking, finalMarking, description)
        return Generator(generationHelper, callback).generate()
    }
}