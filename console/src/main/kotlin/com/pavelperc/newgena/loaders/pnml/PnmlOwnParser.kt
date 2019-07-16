package com.pavelperc.newgena.loaders.pnml

import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.models.semantics.petrinet.Marking
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory

object PnmlOwnParser {
    
    class ParserException(message: String) : IllegalStateException(message)
    
    private operator fun NodeList.get(index: Int) = item(index)
    
    private fun Element.elements(name: String) =
            getElementsByTagName(name)
                    .run { List<Node>(length) { i -> item(i) } }
                    .map { it as Element }
    
    private fun Element.element(name: String) = elements(name).getOrNull(0)
            ?: throw ParserException("No element with name $name was found in ${this.nodeName}")
    
    private fun Element.elementOpt(name: String) = elements(name).firstOrNull()
    
    private fun Element.attr(name: String) = getAttribute(name)
    
    
    private fun Element.pText() = elementOpt("text")?.textContent
    private fun Element.pName() = elementOpt("name")?.pText()
    
    
    fun parseFromFile(file: File): Pair<ResetInhibitorNetImpl, Marking> =
            parseFromStream(FileInputStream(file))
    
    fun parseFromString(pnml: String): Pair<ResetInhibitorNetImpl, Marking> =
            parseFromStream(ByteArrayInputStream(pnml.toByteArray()))
    
    
    fun parseFromStream(input: InputStream): Pair<ResetInhibitorNetImpl, Marking> {
        val doc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
        //optional, but recommended (removes whitespaces an so on
        doc.getDocumentElement().normalize()
        
        val pnml = doc.documentElement
        val net1 = pnml.element("net")
        
        val netName = net1.pName()!!
//        println("name: $netName")
        
        
        // creating prom petriNet
        val petrinet = ResetInhibitorNetImpl(netName)
        val marking = Marking()
        
        petrinet.fastPn = net1.elementOpt("fastPn")?.textContent
        
        // how will this work with multiple pages???
        // what if there will be links to the second page from the first??
        net1.elements("page").forEach { page ->
            val places = page.elements("place")
            val transitions = page.elements("transition")
            val arcs = page.elements("arc")

//            println("places:")
            places.forEach { place ->
                val id = place.attr("id")
                val name = place.pName()
                val markingCount = place.elementOpt("initialMarking")
                        ?.pText()?.toInt() ?: 0
//                println("id: $id, name:$name, initialMarking: $markingCount")
                
                val pnPlace = petrinet.addPlace(name ?: id)
                pnPlace.pnmlId = id
                
                repeat(markingCount) {
                    marking += pnPlace
                }
            }

//            println("transitions:")
            transitions.forEach { transition ->
                val id = transition.attr("id")
                val name = transition.pName()
                
                val pnTransition = petrinet.addTransition(name ?: id)
                pnTransition.pnmlId = id
            }
            
            val idsToPlaces = petrinet.places.map { it.pnmlId to it }.toMap()
            val idsToTransitions = petrinet.transitions.map { it.pnmlId to it }.toMap()
            
            val idsToNodes = idsToPlaces + idsToTransitions

//            println("arcs:")
            arcs.forEach { arc ->
                val id = arc.attr("id")
                val source = arc.attr("source")
                val target = arc.attr("target")
                val name = arc.pName()
                val arcType = arc.elementOpt("arctype")?.pText() ?: "normal"

//                println("id: $id, name:$name, source:$source, target:$target, arctype=$arcType")
                
                val srcNode = idsToNodes.get(source)
                        ?: throw ParserException("Unknown source id $source in arc $id")
                val trgNode = idsToNodes.getValue(target)
                        ?: throw ParserException("Unknown target id $source in arc $id")
                
                val pnEdge = when (arcType) {
                    "reset" -> petrinet.addResetArc(srcNode as Place, trgNode as Transition, name ?: id)
                    "inhibitor" -> petrinet.addInhibitorArc(srcNode as Place, trgNode as Transition, name ?: id)
                    "normal" -> {
                        val weight = name?.toIntOrNull() ?: 1
                        if (srcNode is Place) {
                            petrinet.addArc(srcNode, trgNode as Transition, weight)
                        } else {
                            petrinet.addArc(srcNode as Transition, trgNode as Place, weight)
                        }
                    }
                    else -> throw ParserException("Unsupported arc type $arcType in arc $id")
                }
                pnEdge.pnmlId = id
            }
        }
        return petrinet to marking
    }
}