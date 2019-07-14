package com.pavelperc.newgena.petrinet.fastPetrinet

import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.petrinet.petrinetExtensions.makeArcPnmlIdsFromEnds
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import com.pavelperc.newgena.utils.common.plusAssign
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.elements.*
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl


const val FAST_PN_VERSION = "1.0"
/**
 * Convert string representation of petrinet. (fastPn format).
 * Example:
 * ```
 * places:
 * p1 p2 p3 p4 // separated by space or in separate lines
 * transitions:
 * // transition names can be placed in round brackets.
 * a(A) b
 * // May contain spaces and other symbols.
 * // Closing round bracket should be escaped like this: '\)'.
 * c d(I (am\) label.)
 *
 * // support blank lines
 * arcs:
 * p1-o>a-->p2-->b--->p3-->d --> p4 // arcs may be chained.
 *          p2->>c-5->p3 // normal arcs may contain weights
 *          // support spacing between arrows.
 * ```
 * good arcs: --> ----> -24---> ---24--> ---o> -o> --->> ->>
 *
 * bad arcs: -> -1> 25-->  --25> --0>
 *
 * See tests form more examples.
 */
fun buildFastPetrinet(descr: String, name: String = "net1"): ResetInhibitorNet {
    // splitting
    val lines = descr.lines()
            .map { it.substringBefore("//").trim() }
            .filter { it.isNotBlank() }
    // search sections:
    val plInd = lines.indexOf("places:")
    val trInd = lines.indexOf("transitions:")
    val arcInd = lines.indexOf("arcs:")
    require(plInd != -1) { "not found `places:` block." }
    require(trInd != -1) { "not found `transitions:` block." }
    require(arcInd != -1) { "not found `arcs:` block." }
    
    val placesStr = lines.subList(1, trInd).flatMap { it.split(" ") }
    
    // id, with optional label in round brackets, with closing round bracket escape
    val trRegex = Regex("""(?<id>[\w\d_]+)(\((?<label>([^)]|\\\))+)\))?""")
    
    val transitionMatches = lines.subList(trInd + 1, arcInd)
            .flatMap { line ->
                trRegex.findAll(line).toList()
            }
    val arcsStr = lines.subList(arcInd + 1, lines.size)
    
    // building:
    val petrinet = ResetInhibitorNetImpl(name)
    petrinet.fastPn = descr
    
    // making places
    val placesByLabel = placesStr.map { placeStr ->
        val place = petrinet.addPlace(placeStr)
        place.pnmlId = placeStr
        placeStr to place
    }.toMap()
    
    // making transitions
    val transitionsByLabel = transitionMatches.map { parsed ->
        val id = parsed.groups["id"]!!.value
        val label = parsed.groups["label"]?.value
                ?.replace("\\)", ")")
                ?: id
        val tr = petrinet.addTransition(label)
        tr.pnmlId = id
        id to tr
    }.toMap()
    
    val nodesByLabel = placesByLabel + transitionsByLabel
    
    val repeatingIds = petrinet.nodes.groupingBy { it.pnmlId }.eachCount().filterValues { it > 1 }.keys
    if (repeatingIds.isNotEmpty()) {
        throw IllegalArgumentException("Can not build fastPn. Found repeating pnml ids: $repeatingIds")
    }
    
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

/** Generates fastPn from [petrinet].
 * May change [petrinet] if its ids contain invalid symbols!!! (like `-`).*/
fun generateFastPn(
        petrinet: ResetInhibitorNet,
        longerArcs: Boolean = false,
        arcSpacing: Boolean = false
): String {
    val idRegex = Regex("""[\w\d_]+""")
    petrinet.nodes.forEach { node ->
        if (node.pnmlId.contains('-')) {
            println("Replacing `-` with `_` in pnmlId ${node.pnmlId}")
            node.pnmlId = node.pnmlId.replace('-', '_')
        }
        if (!idRegex.matches(node.pnmlId)) {
            throw IllegalStateException("Can not generate fastPn, " +
                    "because node id ${node.pnmlId} contains other symbols except digits, letters and underscore.")
        }
    }
    
    val sb = StringBuilder("// generated by gena\n")
    sb += "places:\n"
    sb += petrinet.places.joinToString(" ", postfix = "\n") { it.pnmlId }
    
    sb += "transitions:\n"
    sb += petrinet.transitions.joinToString(" ", postfix = "\n") {
        if (it.label == it.pnmlId)
            it.pnmlId
        else "${it.pnmlId}(${it.label.replace(")", "\\)")})"
    }
    
    sb += "arcs:\n"
    sb += generateChains(petrinet, longerArcs, arcSpacing).joinToString("\n")
    
    return sb.toString()
}


/** Generates chains for fastPn, using this algorithm:
 * 1. Find the node with minimal input arcs and at least one output arc.
 * 2. Try to make a random chain, removing arcs from graph.
 * 3. go to 1, if any arcs left.
 */
private fun generateChains(
        petrinet: ResetInhibitorNet,
        longerArcs: Boolean,
        arcSpacing: Boolean
): List<String> {
    // nodes are just ids
    val nodes = petrinet.nodes.map { it.pnmlId }
    
    // stores arc arrows or nulls.
    val adjMatrix = nodes.map { rowNode ->
        rowNode to nodes
                .map { colNode -> colNode to null }
                .toMap<String, String?>().toMutableMap()
    }.toMap()
    
    
    val inputCounts = mutableMapOf<String, Int>()
    // zeros are present
    nodes.forEach { node -> inputCounts[node] = 0 }
    // zeroes are dropped
    val outputCounts = mutableMapOf<String, Int>()
    
    // fill adjMatrix
    petrinet.edges.forEach { edge ->
        val src = edge.source.pnmlId
        val dst = edge.target.pnmlId
        check(src != dst) { "Bad edge $edge with equal src and dst: $src." }
        
        adjMatrix.getValue(src)[dst] = when (edge) {
            is Arc ->
                if (edge.weight > 1)
                    "-${edge.weight}->"
                else if (longerArcs)
                    "--->"
                else "-->"
            is ResetArc -> if (longerArcs) "-->>" else "->>"
            is InhibitorArc -> if (longerArcs) "--o>" else "-o>"
            else -> throw IllegalStateException("unknown edge type: $edge")
        }
        outputCounts[src] = (outputCounts[src] ?: 0) + 1
        inputCounts[dst] = (inputCounts[dst] ?: 0) + 1
    }
//        println(adjMatrix.entries.joinToString("\n") { (k, v) -> "$k=$v" })
//        
//        println("inputs:")
//        println(inputCounts)
//        println("outputs:")
//        println(outputCounts)
    
    // making chains:
    val chains = mutableListOf<String>()
    while (true) {
        // find one with minimal input nodes count, but containing output nodes
        val chainStart = inputCounts.toList()
                .filter { it.first in outputCounts }
                .minBy { it.second }?.first
                ?: break
        
        // making chain:
        var curr = chainStart
        val chain = StringBuilder(chainStart)
        while (true) {
            val (outNode, arc) = adjMatrix.getValue(curr).entries
                    .filter { (_, arc) -> arc != null }
                    .map { (outNode, arc) -> outNode to arc!! }
                    .firstOrNull() ?: break
            chain += if (arcSpacing) " $arc " else arc
            chain += outNode
            
            fun MutableMap<String, Int>.decrease(key: String) {
                set(key, getValue(key) - 1)
            }
            inputCounts.decrease(outNode)
            outputCounts.decrease(curr)
            outputCounts.remove(curr, 0) // remove if 0
            
            adjMatrix.getValue(curr)[outNode] = null
            curr = outNode
        }
        
        chains += chain.toString()
    }
    return chains
}
