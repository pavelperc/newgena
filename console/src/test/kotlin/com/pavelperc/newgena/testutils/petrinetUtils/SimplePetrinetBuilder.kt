package com.pavelperc.newgena.testutils.petrinetUtils

import com.pavelperc.newgena.models.makeArcPnmlIdsFromEnds
import com.pavelperc.newgena.models.makePnmlIdsFromLabels
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import java.lang.IllegalArgumentException

/**
 * Convert string representation of petrinet.
 * example:
 * ```
 * places:
 * p1 // or in one line with space separator
 * p2
 *
 * // support blank lines
 * transitions:
 * t1
 * t2
 * arcs:
 * p1-->t1
 * p1-o>t2
 * p2->>t1
 * ```
 */
fun simplePetrinetBuilder(descr: String): ResetInhibitorNetImpl {
    // splitting
    val lines = descr.lines().filter { it.isNotBlank() }
    val trInd = lines.indexOf("transitions:")
    val arcInd = lines.indexOf("arcs:")
    require(trInd != -1) { "not found `transitions:` for descr:\n$descr." }
    require(arcInd != -1) { "not found `arcs:` for descr:\n$descr." }
    
    val placesStr = lines.subList(1, trInd).flatMap { it.split(" ") }
    val transitionsStr = lines.subList(trInd + 1, arcInd).flatMap { it.split(" ") }
    val arcsStr = lines.subList(arcInd + 1, lines.size).flatMap { it.split(" ") }
    
    // building:
    val petrinet = ResetInhibitorNetImpl("net1")
    
    // making nodes
    val nodesByLabel = placesStr.map { it to petrinet.addPlace(it) }.toMap() +
            transitionsStr.map { it to petrinet.addTransition(it) }.toMap()
    
    fun String.node() = nodesByLabel[this]
    fun String.place() = node() as Place
    fun String.trans() = nodesByLabel[this] as Transition
    
    // making arcs
    val arcRegex = Regex("""([\w\d_]+)-([-o>])>([\w\d_]+)""")
    arcsStr.forEach { arcLabel ->
        // split regex
        val (_, src, arcType, dest) = arcRegex.matchEntire(arcLabel)
                ?.groupValues ?: throw IllegalArgumentException("Can not parse arc in line $arcLabel.")
        
        when (arcType) {
            "-" ->
                if (src.node() is Place)
                    petrinet.addArc(src.place(), dest.trans())
                else petrinet.addArc(src.trans(), dest.place())
            "o" -> petrinet.addInhibitorArc(src.place(), dest.trans())
            ">" -> petrinet.addResetArc(src.place(), dest.trans())
        }
    }
    
    // making pnmlId
    petrinet.makePnmlIdsFromLabels()
    petrinet.makeArcPnmlIdsFromEnds()
    
    return petrinet
}