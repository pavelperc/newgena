package com.pavelperc.newgena.petrinet.fastPetrinet

import com.pavelperc.newgena.petrinet.petrinetExtensions.makeArcPnmlIdsFromEnds
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl

/**
 * Convert string representation of petrinet. (fastPn format)
 * Example:
 * ```
 * places:
 * p1 p2 p3 p4 // separated by space or in separate lines
 * transitions:
 * a(A) b c d // transition names in round brackets (but without spaces!)
 *
 * // support blank lines
 * arcs:
 * p1-o>a-->p2-->b--->p3-->d-->p4 // arcs may be chained!!
 *          p2->>c-5->p3 // normal arcs may contain weights
 * ```
 * good arcs: --> ----> -24---> ---24--> ---o> -o> --->> ->>
 *
 * bad arcs: -> -1> 25-->  --25> --0>
 * 
 * See tests form more examples.
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
    
    // making places
    val placesByLabel = placesStr.map { placeStr ->
        val place = petrinet.addPlace(placeStr)
        place.pnmlId = placeStr
        placeStr to place
    }.toMap()
    
    // making transitions
    val trRegex = Regex("""(?<id>[\w\d_]+)(\((?<label>[\w\d_]+)\))?""")
    val transitionsByLabel = transitionsStr.map { trStr ->
        val parsed = trRegex.matchEntire(trStr) ?: throw IllegalArgumentException("Can not parse transition $trStr.")
        val id = parsed.groups["id"]!!.value
        val label = parsed.groups["label"]?.value ?: id
        val tr = petrinet.addTransition(label)
        tr.pnmlId = id
        id to tr
    }.toMap()
    
    val nodesByLabel = placesByLabel + transitionsByLabel
    
    fun String.node() = nodesByLabel[this] ?: throw IllegalArgumentException("Unexpected node $this")
    fun String.place() = node() as? Place ?: throw IllegalArgumentException("Unexpected place $this")
    fun String.trans() = nodesByLabel[this] as? Transition
            ?: throw IllegalArgumentException("Unexpected transition $this")
    
    // making arcs
    val arcLineRegex = Regex("""([\w\d_]+)|(-+\d*-*[-o>]>)""")
    arcsStr.forEach { arcLine ->
        fun addArc(src: String, arc: String, dst: String) {
            when {
                arc.endsWith("->") -> {
                    val weight = arc.trim('-', '>').toIntOrNull() ?: 1
                    
                    if (src.node() is Place)
                        petrinet.addArc(src.place(), dst.trans(), weight)
                    else petrinet.addArc(src.trans(), dst.place(), weight)
                }
                arc.endsWith("o>") -> petrinet.addInhibitorArc(src.place(), dst.trans(), "1")
                arc.endsWith(">>") -> petrinet.addResetArc(src.place(), dst.trans(), "1")
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
    
    // making pnmlId for arcs
    petrinet.makeArcPnmlIdsFromEnds()
    
    return petrinet
}