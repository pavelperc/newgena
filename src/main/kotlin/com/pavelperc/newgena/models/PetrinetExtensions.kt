package com.pavelperc.newgena.models

import org.processmining.models.graphbased.directed.petrinet.*
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition


/**
 * Replaces given arcs in petrinet with inhibitor and reset arcs.
 * Arcs are given as pairs of place and transition labels.
 * @throws IllegalArgumentException if one of given edges not found in petrinet
 */
fun ResetInhibitorNet.markInhResetArcs(
        inhibitorArcLabels: Set<Pair<String, String>> = setOf(),
        resetArcLabels: Set<Pair<String, String>> = setOf()
) {
    
    // map from pair of labels to petrinet edge
    val inEdges = this.transitions
            .flatMap { transition -> this.getInEdges(transition) }
            .map { edge ->
                // warning is because of type erasure!!
                // can not do safe cast..
                edge as PetrinetEdge<Place, Transition>
            }
            .map { it -> (it.source.label!! to it.target.label!!) to it }
            .toMap()
    
    val resetEdges = resetArcLabels.map {
        inEdges[it] ?: throw IllegalArgumentException("Can not mark arc $it as reset, " +
                "because it has not been found among incoming edges of transitions.")
    }
    
    val inhibitorEdges = inhibitorArcLabels.map {
        inEdges[it] ?: throw IllegalArgumentException("Can not mark arc $it as inhibitor, " +
                "because it has not been found among incoming edges of transitions.")
    }
    
    resetEdges.forEach { edge ->
        this.removeArc(edge.source, edge.target)
        this.addResetArc(edge.source, edge.target, edge.label)
    }
    
    inhibitorEdges.forEach { edge ->
        this.removeArc(edge.source, edge.target)
        this.addInhibitorArc(edge.source, edge.target, edge.label)
    }
}