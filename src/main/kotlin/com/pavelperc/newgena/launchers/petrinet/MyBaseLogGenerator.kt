package com.pavelperc.newgena.launchers.petrinet

import org.processmining.log.models.EventLogArray
import org.processmining.models.GenerationDescription
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.utils.Generator
import org.processmining.utils.ProgressBarCallback
import org.processmining.utils.helpers.GenerationHelper
import org.processmining.utils.helpers.SimpleGenerationHelper

object MyBaseLogGenerator {
    
    public fun generate(
            petrinet: Petrinet,
            initialMarking: Marking,
            finalMarking: Marking,
            description: SimpleGenerationDescription
    ): EventLogArray {
        
        val generationHelper = SimpleGenerationHelper.createHelper(petrinet, initialMarking, finalMarking, description)
        val logArray = generateWithProgressbar(generationHelper, description)
        
        return logArray
        
    }
    
    private fun generateWithProgressbar(helper: GenerationHelper<*, *>, description: GenerationDescription): EventLogArray {
        val progress = 0
        val maxProgress = description.numberOfLogs * description.numberOfTraces
        
        val callback = ProgressBarCallback {
            progress.inc()
            println("progress: $progress from $maxProgress")
        }
        return Generator(callback).generate(helper)
    }
}