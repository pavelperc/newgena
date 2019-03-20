package com.pavelperc.newgena.graphviz

import com.pavelperc.newgena.models.pnmlId
import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph
import org.processmining.models.graphbased.directed.petrinet.elements.InhibitorArc
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc
import org.processmining.models.semantics.petrinet.Marking
import java.io.File


private fun convert(petrinet: PetrinetGraph, marking: List<Place>, graphLabel: String): MutableGraph {
    
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
            val arc = (edge.source.pnmlId - edge.target.pnmlId)[Label.of("${edge.label}(${edge.pnmlId})")]
            when (edge) {
                is InhibitorArc -> arc[Arrow.DOT.open()]
                is ResetArc -> arc[Arrow.NORMAL.and(Arrow.NORMAL)]
            }
        }
        
        drawTokens(places, marking)
//        
//        if (petrinet is ICPetrinet) {
//            
//        }
    }
}

private fun drawTokens(allPlaceNodes: List<MutableNode>, marking: List<Place>) {
    // No token
    allPlaceNodes.forEach { it[Label.of("   ")] }
    
    // grouping places by labels
    val counts = marking.groupBy { it.pnmlId }.mapValues { it.value.size }
    counts.forEach { label, count ->
        val node = mutNode(label)
        // circles
        val circle = "●"
//        val circle = "•"
//        val circle = "*"
        val points =
                when {
                    count > 6 -> "$count $circle"
                    else -> circle.repeat(count).chunked(3).joinToString("\n")
                }
        node[Label.raw(points)]
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
        marking: Marking = Marking(),
        graphLabel: String = this.label,
        saveToSvg: String? = null
) = convert(this, marking.toList(), graphLabel)
        .also { graph ->
            if (saveToSvg != null)
                graph.toGraphviz().render(Format.SVG).toFile(File(saveToSvg))
        }