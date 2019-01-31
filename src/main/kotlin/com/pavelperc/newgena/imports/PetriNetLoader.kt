package com.pavelperc.newgena.imports

import org.deckfour.xes.model.XLog
import org.processmining.contexts.cli.CLIContext
import org.processmining.contexts.cli.CLIPluginContext
import org.processmining.framework.plugin.GlobalContext
import org.processmining.framework.plugin.PluginContext
import org.processmining.framework.plugin.impl.PluginContextIDImpl
import org.processmining.models.connections.GraphLayoutConnection
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.processmining.plugins.pnml.Pnml
import org.processmining.plugins.pnml.importing.PnmlImportUtils
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileInputStream
import java.lang.Exception
import java.util.*


object PetriNetLoader {
    
    
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
    
    fun loadPetriNet(path: String, markingToPut: Marking): Petrinet {
        val pnml = loadPnml(path)
        
        val net = PetrinetFactory.newPetrinet(pnml.label)!!
        
        pnml.convertToNet(net, markingToPut, GraphLayoutConnection(net))
        
        return net
    }
}