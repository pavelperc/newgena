package com.pavelperc.newgena.petrinet.output

import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.xml.parsers.DocumentBuilderFactory
import java.io.OutputStream
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

open class XmlBuilder() {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    
    fun Node.element(name: String, vararg attributes: Pair<String, String>, init: Element.() -> Unit = {}) {
        val e = document.createElement(name)
        e.init()
        attributes.forEach { (type, text) ->
            e.setAttribute(type, text)
        }
        this.appendChild(e)
    }
    
    fun Node.text(data: String) {
        val text = document.createTextNode(data)
        this.appendChild(text)
    }
    
    fun createRoot(name: String, init: Element.() -> Unit = {}) {
        document.element(name, init = init)
    }
    
    fun exportStream(out: OutputStream, indents: Int = 2) {
        val tr = TransformerFactory.newInstance().newTransformer()
        tr.setOutputProperty(OutputKeys.INDENT, "yes")
        tr.setOutputProperty(OutputKeys.METHOD, "xml")
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indents.toString())
        
        // send DOM to stream
        tr.transform(DOMSource(document),
                StreamResult(out))
    }
    
    fun exportStr(indents: Int = 2): String {
        val ba = ByteArrayOutputStream()
        exportStream(ba, indents)
        return String(ba.toByteArray())
    }
}

class PnmlXmlBuilder : XmlBuilder() {
    fun Element.pname(name: String) {
        element("name") {
            element("text") {
                text(name)
            }
        }
    }
    
    fun Element.ptext(text: String) {
        element("text") {
            text(text)
        }
    }
}

/** [fastPn] is fast pnml format, developed for testing this tool.
 * See [com.pavelperc.newgena.petrinet.fastPetrinet.simplePetrinetBuilder].
 * It will be stored inside tag fastPn near the petrinet name.*/
fun makePnmlStr(
        petrinet: ResetInhibitorNet,
        fastPn: String? = null,
        indents: Int = 2
): String {
    val ba = ByteArrayOutputStream()
    makePnml(petrinet, ba, fastPn, indents)
    
    return String(ba.toByteArray())
}

/** [fastPn] is fast pnml format, developed for testing this tool.
 * See [com.pavelperc.newgena.petrinet.fastPetrinet.simplePetrinetBuilder]. 
 * It will be stored inside tag fastPn near the petrinet name.*/
fun makePnml(
        petrinet: ResetInhibitorNet,
        out: OutputStream,
        fastPn: String? = null,
        indents: Int = 2
) {
    val builder = PnmlXmlBuilder()
    with(builder) {
        createRoot("pnml") {
            element("net",
                    "id" to "net1",
                    "type" to "http://www.pnml.org/version-2009/grammar/pnmlcoremodel") {
                pname(petrinet.label)
                
                // write fastPn
                if (fastPn != null) {
                    element("fastPn") {
                        text(fastPn)
                    }
                }
                
                element("page", "id" to "n0") {
                    // write places
                    
                    petrinet.places.forEach { place ->
                        element("place",
                                "id" to place.pnmlId) {
                            // todo add initialMarking
                        }
                    }
                    
                    // write transitions
                    petrinet.transitions.forEach { transition ->
                        element("transition",
                                "id" to transition.pnmlId) {
                            pname(transition.label)
                        }
                    }
                    
                    petrinet.edges.forEach { edge ->
                        element("arc",
                                "id" to edge.pnmlId,
                                "source" to edge.source.pnmlId,
                                "target" to edge.target.pnmlId) {
                            
                            fun nameAndType(name: String, type: String) {
                                pname(name)
                                element("arctype") {
                                    ptext(type)
                                }
                            }
                            when(edge) {
                                is Arc -> nameAndType(edge.weight.toString(), "normal")
                                is ResetArc -> nameAndType(edge.label, "reset")
                                is InhibitorArc -> nameAndType(edge.label.toString(), "inhibitor")
                                else -> throw IllegalStateException("Unknown edge type: $edge")
                            }
                        }
                    }
                }
            }
        }
    }
    builder.exportStream(out, indents)
}

fun main() {
    val builder = XmlBuilder()
    
    with(builder) {
        createRoot("pnml") {
            element("net",
                    "id" to "net1",
                    "type" to "http://www.pnml.org/version-2009/grammar/pnmlcoremodel") {
                
                text("hello")
            }
        }
    }
    
    println(builder.exportStr())
}


