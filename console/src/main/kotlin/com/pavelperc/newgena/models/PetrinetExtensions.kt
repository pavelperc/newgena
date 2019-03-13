package com.pavelperc.newgena.models

import org.processmining.models.graphbased.directed.petrinet.*
import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.graphbased.directed.petrinet.elements.Transition


/**
 * Replaces given arcs in petrinet with inhibitor and reset arcs.
 * Arcs are given as ids.
 * @throws IllegalArgumentException if one of given edges not found in petrinet
 */
fun ResetInhibitorNet.markInhResetArcsByIds(
        inhibitorArcIds: List<String> = listOf(),
        resetArcIds: List<String> = listOf()
) {
    
    // map from pair of labels to petrinet edge
    val inEdges = this.transitions
            .flatMap { transition -> this.getInEdges(transition)}
            .map { edge ->
                // warning is because of type erasure!!
                // can not do safe cast..
                
                // USE LABEL BECAUSE OF PROM PNML LOADER
                edge.label to edge as PetrinetEdge<Place, Transition>
            }
            .toMap()
    
    val resetEdges = resetArcIds.map {
        inEdges[it] ?: throw IllegalArgumentException("Can not mark arc $it as reset, " +
                "because it has not been found among incoming edges of transitions. All ids:${inEdges.keys}")
    }
    
    val inhibitorEdges = inhibitorArcIds.map {
        inEdges[it] ?: throw IllegalArgumentException("Can not mark arc $it as inhibitor, " +
                "because it has not been found among incoming edges of transitions. All ids:${inEdges.keys}")
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