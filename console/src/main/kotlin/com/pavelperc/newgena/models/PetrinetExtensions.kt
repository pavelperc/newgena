package com.pavelperc.newgena.models

import org.processmining.models.graphbased.AbstractGraphElement
import org.processmining.models.graphbased.AttributeMapOwner
import org.processmining.models.graphbased.directed.petrinet.*
import org.processmining.models.graphbased.directed.petrinet.elements.*
import kotlin.reflect.KProperty


/**
 * Replaces given arcs in petrinet with inhibitor and reset arcs.
 * Arcs are given as ids.
 * @throws IllegalArgumentException if one of given edges not found in petrinet
 */
fun ResetInhibitorNet.markInhResetArcsByIds(
        inhibitorArcIds: List<String> = listOf(),
        resetArcIds: List<String> = listOf()
) {
    
    // map from pair of ids to petrinet edge
    val inEdges = this.transitions
            .flatMap { transition -> this.getInEdges(transition) }
            .map { edge ->
                // warning is because of type erasure!!
                // can not do safe cast..
                
                
                edge.pnmlId to edge as PetrinetEdge<Place, Transition>
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
                .also { it.pnmlId = edge.pnmlId }
    }
    
    inhibitorEdges.forEach { edge ->
        this.removeArc(edge.source, edge.target)
        this.addInhibitorArc(edge.source, edge.target, edge.label)
                .also { it.pnmlId = edge.pnmlId }
    }
}


private class PnmlIdDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return (thisRef as AttributeMapOwner).attributeMap["pnmlId"]?.toString() ?: "null"
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        (thisRef as AttributeMapOwner).attributeMap.put("pnmlId", value)
    }
}

/** Id, that was stored in original pnml file.*/
var Transition.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var Place.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var Arc.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var ResetArc.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var InhibitorArc.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var PetrinetEdge<*, *>.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var PetrinetNode.pnmlId by PnmlIdDelegate()


fun ResetInhibitorNet.makePnmlIdsFromLabels() {
    transitions.forEach { it.pnmlId = it.label }
    places.forEach { it.pnmlId = it.label }
}

fun List<PetrinetEdge<*, *>>.makePnmlIdsOrdinal(startFrom: Int = 1) {
    withIndex().forEach { (i, edge) -> edge.pnmlId = "arc${i + startFrom}" }
}

//fun List<Arc>.makePnmlIdsOrdinalInArcs() = map { it as PetrinetEdge<*,*> }.makePnmlIdsOrdinal()


