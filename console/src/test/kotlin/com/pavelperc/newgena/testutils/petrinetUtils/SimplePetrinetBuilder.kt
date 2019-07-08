package com.pavelperc.newgena.testutils.petrinetUtils

import com.pavelperc.newgena.models.makeArcPnmlIdsFromEnds
import com.pavelperc.newgena.models.makePnmlIdsFromLabels
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl

/**
 * Convert string representation of petrinet.
 * Example:
 * ```
 * places:
 * p1 p2 p3 p4 // or in separate lines
 * transitions:
 * a b c d
 * 
 * // support blank lines
 * arcs:
 * p1-o>a-->p2-->b-->p3-->d-->p4
 *          p2->>c-->p3 // arcs may be chained!!
 * ```
 */
fun simplePetrinetBuilder(descr: String, name: String = "net1"): ResetInhibitorNet {
    // splitting
    val lines = descr.lines()
            .map { it.substringBefore("//").trim() }
            .filter { it.isNotBlank() }
    // search sections:
    val trInd = lines.indexOf("transitions:")
    val arcInd = lines.indexOf("arcs:")
    require(trInd != -1) { "not found `transitions:` for descr:\n$descr." }
    require(arcInd != -1) { "not found `arcs:` for descr:\n$descr." }
    
    val placesStr = lines.subList(1, trInd).flatMap { it.split(" ") }
    val transitionsStr = lines.subList(trInd + 1, arcInd).flatMap { it.split(" ") }
    val arcsStr = lines.subList(arcInd + 1, lines.size).flatMap { it.split(" ") }
    
    // building:
    val petrinet = ResetInhibitorNetImpl(name)
    
    // making nodes
    val nodesByLabel = placesStr.map { it to petrinet.addPlace(it) }.toMap() +
            transitionsStr.map { it to petrinet.addTransition(it) }.toMap()
    
    fun String.node() = nodesByLabel[this]
    fun String.place() = node() as Place
    fun String.trans() = nodesByLabel[this] as Transition
    
    // making arcs
    val arcLineRegex = Regex("""([\w\d_]+)|(-[-o>]>)""")
    arcsStr.forEach { arcLine ->
        fun addArc(src: String, arcType: String, dst: String) {
            when (arcType) {
                "-->" ->
                    if (src.node() is Place)
                        petrinet.addArc(src.place(), dst.trans())
                    else petrinet.addArc(src.trans(), dst.place())
                "-o>" -> petrinet.addInhibitorArc(src.place(), dst.trans(), "")
                "->>" -> petrinet.addResetArc(src.place(), dst.trans(), "")
            }
        }
        // split regex
        arcLineRegex.findAll(arcLine)
                .windowed(3, 2)
                .forEach { window ->
                    val (srcMatch, arcMatch, dstMatch) = window
//                    println("Window: ${window.map { it.value }}")
                    fun err(token: MatchResult) = IllegalStateException("Unexpected ${token.value} in line $arcLine")
                    
                    // check if we got sequence: id, arc, id
                    val src = srcMatch.groups[1]?.value ?: throw err(srcMatch)
                    val arcType = arcMatch.groups[2]?.value ?: throw err(arcMatch)
                    val dst = dstMatch.groups[1]?.value ?: throw err(dstMatch)
                    
                    addArc(src, arcType, dst)
                }
        
    }
    
    // making pnmlId
    petrinet.makePnmlIdsFromLabels()
    petrinet.makeArcPnmlIdsFromEnds()
    
    return petrinet
}