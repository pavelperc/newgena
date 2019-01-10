package com.pavelperc.newgena.graphviz

import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.Node
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode
import org.processmining.models.semantics.petrinet.Marking
import java.io.File


private fun convert(petrinet: Petrinet, marking: List<PetrinetNode> = emptyList()): MutableGraph {
    
    return graph(directed = true) {
        graph[RankDir.LEFT_TO_RIGHT]
        graph["label" eq petrinet.label]
        
        
        val places = petrinet.places.map {
            val label = Label.of(it.label)
            mutNode(label).add(Shape.CIRCLE, label.external())
        }
        places.forEach { it[Label.of("")] }
        
        val transitions = petrinet.transitions.map { mutNode(it.label).add(Shape.RECTANGLE) }
        
        petrinet.edges.forEach { it.source.label - it.target.label } // minus connects nodes
        
        
        val counts = marking.groupBy { it.label!! }.mapValues { it.value.size }
        
        counts.forEach { label, count ->
            val node = mutNode(label)
            // circles
            val points =
                    if (count > 6) "$count \u23FA"
                    else "\u23FA".repeat(count).chunked(3).joinToString("\n")
            
            node[Label.raw(points)]
        }
    }
}

fun Petrinet.drawGraphviz(filename: String, marking: List<PetrinetNode> = emptyList()) =
        convert(this, marking).toGraphviz().render(Format.SVG).toFile(File(filename))


fun Petrinet.toGraphviz(markingNodes: List<PetrinetNode> = emptyList()) = convert(this, markingNodes)

/** Converts petrinet to mutable graphviz graph*/
fun Petrinet.toGraphviz(marking: Marking = Marking()) = convert(this, marking.toList())