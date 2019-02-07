package com.pavelperc.newgena.imports

import org.processmining.models.connections.GraphLayoutConnection
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.plugins.pnml.Pnml
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileInputStream
import java.lang.Exception


object PnmlLoader {
    
    
    private fun loadPnml(path: String): Pnml {
    
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()
        
        val inputStream = FileInputStream(path)
        
        xpp.setInput(inputStream, null)
        var eventType = xpp.eventType
    
        val pnml = Pnml()
        
        while (eventType != 2) {
            eventType = xpp.next()
        }
    
        if (xpp.name == "pnml") {
            pnml.importElement(xpp, pnml)
        } else {
            pnml.log("pnml", xpp.lineNumber, "Expected pnml")
        }
    
        if (pnml.hasErrors()) {
            throw Exception("Error. Log of PNML import: ${pnml.log}")
        } else {
            return pnml
        }
        
    }
    
    
    fun loadPetrinet(
            path: String
    ): PetrinetWithSettings {
        
        val pnml = loadPnml(path)
        
        val net = PetrinetFactory.newResetInhibitorNet(pnml.label)!!
        
        val marking = Marking()
        pnml.convertToNet(net, marking, GraphLayoutConnection(net))
        
        return PetrinetWithSettings(net, marking)
    }
    
    data class PetrinetWithSettings(
            val petrinet: PetrinetGraph,
            val marking: Marking
    ) {
        
    }

}