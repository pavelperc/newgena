package com.pavelperc.newgena.loaders.pnml

import org.processmining.plugins.pnml.Pnml
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream


object PnmlLoader {
    
    
    private fun loadPnml(path: String): Pnml {
        // copied from some prom class
        
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
    
    fun loadPetrinetWithOwnParser(path: String) = PnmlOwnParser.parseFromFile(File(path))
    
    
//    @Deprecated("Use loadPetrinetWithOwnParser instead.")
//    fun loadPetrinet(
//            path: String
//    ): Pair<ResetInhibitorNet, Marking> {
//        val pnml = loadPnml(path)
//        
//        val petrinet = PetrinetFactory.newResetInhibitorNet(pnml.label)!!
//        petrinet.makePnmlIdsFromLabels()
//        petrinet.edges.toList().makePnmlIdsOrdinal()
//        
//        val marking = Marking()
//        
//        pnml.convertToNet(petrinet, marking, GraphLayoutConnection(petrinet))
//        
//        return petrinet to marking
//    }
}

