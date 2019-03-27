package com.pavelperc.newgena.graphviz

import com.pavelperc.newgena.models.pnmlId
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.Arc
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import org.processmining.models.semantics.petrinet.Marking
import java.io.File


private fun convert(
        petrinet: PetrinetGraph,
        marking: List<Place>,
        finalMarking: List<Place>,
        graphLabel: String,
        drawArcIds: Boolean
): MutableGraph {
    
    return graph(directed = true) {
        graph[RankDir.LEFT_TO_RIGHT]
        graph["label" eq graphLabel]
        
        val places = petrinet.places.map {
            val label = Label.of(it.pnmlId)
            mutNode(label).add(Shape.CIRCLE, label.external())
        }
        
        val transitions = petrinet.transitions.map {
            mutNode(it.pnmlId).add(
                    Shape.RECTANGLE,
//                    Label.of("${it.label}(${it.pnmlId})")
                    Label.of(it.label)
            )
        }
        
        // drawing arcs
        petrinet.edges.forEach { edge ->
            
            val labelStr =
                    if (drawArcIds) "${edge.label}(${edge.pnmlId})"
                    else if (edge.label == "1" || edge !is Arc) ""
                    else edge.label
            
            val arc = (edge.source.pnmlId - edge.target.pnmlId)[Label.of(labelStr)]
            when (edge) {
                is InhibitorArc -> arc[Arrow.DOT.open()]
                is ResetArc -> arc[Arrow.NORMAL.and(Arrow.NORMAL)]
            }
        }
        
        // reset default labels.
        places.forEach { it[Label.of("   ")] }
        drawTokens(marking)
        drawTokens(finalMarking, true)

//        
//        if (petrinet is ICPetrinet) {
//            
//        }
    }
}

private fun drawTokens(marking: List<Place>, isFinalMarking: Boolean = false) {
    // No token
//    allPlaceNodes.forEach { it[Label.of("   ")] }
    
    // grouping places by labels
    val counts = marking.groupBy { it.pnmlId }.mapValues { it.value.size }
    counts.forEach { pnmlId, count ->
        val node = mutNode(pnmlId)
        // circles
        val circle = "●"
//        val circle = "•"
//        val circle = "*"
        val points = when {
            count > 3 -> "$count $circle"
//                    else -> circle.repeat(count).chunked(3).joinToString("<br/>")
            else -> circle.repeat(count)
        }
        
        val oldLabel = (node.attrs()["label"] as? Label)?.value() ?: ""
        val oldLabelMod = if (oldLabel.trimIndent() != "") "$oldLabel<br/>" else ""
        val label =
                if (isFinalMarking)
                    Label.html("$oldLabelMod<font color=\"lightseagreen\">$points</font>")
                else Label.html("<font color=\"black\">$points</font>")
//        println("New label for node $pnmlId: ${label.value()}")
        node[label]
    }
}


//
///** Converts petrinet to mutable graphviz graph*/
//fun ICPetrinet.toGraphviz(
//        marking: Marking = Marking(),
//        graphLabel: String = this.label,
//        saveToSvg: String? = null
//) : MutableGraph {
//    
//    val petrinet = ICPetrinetImpl(this);
//    
//}


/** Converts petrinet to mutable graphviz graph*/
fun PetrinetGraph.toGraphviz(
        initialMarking: Marking = Marking(),
        graphLabel: String = this.label,
        saveToSvg: String? = null,
        finalMarking: Marking = Marking(),
        drawArcIds: Boolean = true
) = convert(this, initialMarking.toList(), finalMarking.toList(), graphLabel, drawArcIds)
        .also { graph ->
            if (saveToSvg != null)
                graph.toGraphviz().render(Format.SVG).toFile(File(saveToSvg))
        }