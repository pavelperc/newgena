package com.pavelperc.newgena.graphviz

import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.*
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.Node
import org.processmining.models.graphbased.directed.petrinet.Petrinet
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode

object PetrinetGraphviz {
    
    fun convert(petrinet: Petrinet, marking: List<PetrinetNode> = emptyList()) : MutableGraph {
        
        return graph(directed = true) {
            graph[RankDir.LEFT_TO_RIGHT]
    
    
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
                val points = "O".repeat(count)
                node[Label.raw(points)]
            }
        }
    }
}