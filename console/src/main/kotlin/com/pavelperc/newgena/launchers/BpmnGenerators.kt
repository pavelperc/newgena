package com.pavelperc.newgena.launchers

import org.processmining.log.models.EventLogArray
import org.processmining.models.descriptions.BasicBPMNGenerationDescription
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram
import org.processmining.utils.BPMNLoggingSingleton
import org.processmining.utils.Generator
import org.processmining.utils.helpers.SimpleBPMNHelper

object BpmnGenerators {
    
    fun generateSimple(diagram: BPMNDiagram, description: BasicBPMNGenerationDescription = BasicBPMNGenerationDescription()): EventLogArray {
        
        description.isUsingResources = !diagram.getSwimlanes().isEmpty() || !diagram.getPools().isEmpty()
        
        BPMNLoggingSingleton.init(description.isUsingResources)
        
        
        val helper = SimpleBPMNHelper.createSimpleHelper(diagram, description)
        return Generator(helper).generate()
    }
}