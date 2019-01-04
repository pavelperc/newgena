package com.pavelperc.newgena.launchers

import com.pavelperc.newgena.testutils.eventNames
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.amshove.kluent.shouldBeIn
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory
import org.processmining.models.graphbased.directed.bpmn.elements.Event
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway
import org.processmining.plugins.bpmn.Bpmn

class BPMNComplexTest {
    
    
    fun generateDiagram(): BPMNDiagram {
        val diagram = BPMNDiagramFactory.newBPMNDiagram("Example")
        val startEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, true, null)
        val endEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, true, null)
        
        val activity1 = diagram.addActivity("A1", false, false, false, false, false)
        diagram.addFlow(startEvent, activity1, "1")
        
        val gateway1 = diagram.addGateway("g1", Gateway.GatewayType.PARALLEL)
        diagram.addFlow(activity1, gateway1, "1")
        
        val activity2 = diagram.addActivity("A2", false, false, false, false, false)
        val activity3 = diagram.addActivity("A3", false, false, false, false, false)
        
        diagram.addFlow(gateway1, activity2, "1")
        diagram.addFlow(activity2, activity3, "1")
        
        val gateway2 = diagram.addGateway("g2", Gateway.GatewayType.PARALLEL)
        
        diagram.addFlow(activity3, gateway2, "1")
        diagram.addFlow(gateway2, endEvent, "1")
        
        val subProcess = diagram.addSubProcess("subprocess", false, false, false, false, false)
        
        
        val subprocessStartEvent = diagram.addEvent("START EVENT", Event.EventType.START, null, null, subProcess, true, null)
        val subprocessEndEvent = diagram.addEvent("END EVENT", Event.EventType.END, null, null, subProcess, true, null)
        
        val gateway3 = diagram.addGateway("g3", Gateway.GatewayType.DATABASED, subProcess)
        val gateway4 = diagram.addGateway("g4", Gateway.GatewayType.DATABASED, subProcess)
        
        val subprocessActivity1 = diagram.addActivity("SA1", false, false, false, false, false, subProcess)
        val subprocessActivity2 = diagram.addActivity("SA2", false, false, false, false, false, subProcess)
        
        diagram.addFlow(subprocessStartEvent, gateway3, "1")
        diagram.addFlow(gateway3, subprocessActivity1, "1")
        diagram.addFlow(subprocessActivity1, gateway4, "1")
        diagram.addFlow(gateway4, subprocessActivity2, "1")
        diagram.addFlow(subprocessActivity2, gateway3, "1")
        diagram.addFlow(gateway4, subprocessEndEvent, "to sub-process end event")
        
        diagram.addFlow(gateway1, subProcess, "2")
        diagram.addFlow(subProcess, gateway2, "2")
        
        return diagram
    }
    
    
    @Test
    fun simpleBPMN() {
        val diagram = generateDiagram()
        
        val logArray = BpmnGenerators.generateSimple(diagram)
        
//        println(logArray.eventNames().joinToString("\n"))
        
        logArray.eventNames().forEach { trace ->
            trace.size shouldBeGreaterOrEqualTo  2
            trace.first() shouldEqual "A1"
            trace.last() shouldBeIn listOf("A3", "SA1")
        }
        
    }
}