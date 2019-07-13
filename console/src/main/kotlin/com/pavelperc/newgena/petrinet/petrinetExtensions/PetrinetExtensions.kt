package com.pavelperc.newgena.petrinet.petrinetExtensions

import org.processmining.models.graphbased.AttributeMapOwner
import org.processmining.models.graphbased.directed.petrinet.*
import org.processmining.models.graphbased.directed.petrinet.elements.*
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import kotlin.reflect.KProperty


/**
 * Replaces given arcs in petrinet with inhibitor and reset arcs.
 * Arc labels are copied from ordinary arcs. (Usually they contain a label.)
 * Arcs are given as ids.
 * @throws IllegalArgumentException if one of given edges not found in petrinet
 */
@Suppress("UNCHECKED_CAST")
@Throws(IllegalArgumentException::class)
fun ResetInhibitorNet.markInhResetArcsByIds(
        inhibitorArcIds: List<String> = listOf(),
        resetArcIds: List<String> = listOf()
) {
    if (inhibitorArcIds.isEmpty() && resetArcIds.isEmpty())
        return
    
    if (inhibitorArcIds.intersect(resetArcIds).isNotEmpty())
        throw IllegalArgumentException("Inhibitor and Reset arc ids for replacing intersect: " +
                "inhibitorARcIds=$inhibitorArcIds, inhibitorARcIds=$resetArcIds")
    
    // map from id to petrinet edge
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


/** Replaces all IR arcs with default arcs. Weights are set as one, or picked from labels. */
fun ResetInhibitorNet.deleteAllInhibitorResetArcs() {
    this.transitions
            .flatMap { transition -> this.getInEdges(transition) }
            .filter { it is ResetArc || it is InhibitorArc }
            .forEach { edge ->
                when (edge) {
                    is ResetArc -> removeResetArc(edge.source, edge.target)
                    is InhibitorArc -> removeInhibitorArc(edge.source, edge.target)
                }
                addArc(edge.source as Place,
                        edge.target as Transition,
                        edge.label.toIntOrNull() ?: 1)
                        .also { it.pnmlId = edge.pnmlId }
            }
}

/** Don't use subnets. pnmlIds are required! */
fun ResetInhibitorNet.deepCopy(): ResetInhibitorNetImpl {
    val copy = ResetInhibitorNetImpl(this.label)
    copy.fastPn = this.fastPn
    
    val newPlacesByIds = this.places.map { place ->
        copy.addPlace(place.label).also { it.pnmlId = place.pnmlId }
    }.map { it.pnmlId to it }.toMap()
    
    val newTransitionsByIds = this.transitions.map { transition ->
        copy.addTransition(transition.label).also { it.pnmlId = transition.pnmlId }
    }.map { it.pnmlId to it }.toMap()
    
    fun PetrinetNode.findPlace() = newPlacesByIds[this.pnmlId] as Place
    fun PetrinetNode.findTrans() = newTransitionsByIds[this.pnmlId] as Transition
    
    this.edges.forEach { edge ->
        val newArc = when (edge) {
            is ResetArc ->
                copy.addResetArc(edge.source.findPlace(), edge.target.findTrans(), edge.label)
            is InhibitorArc ->
                copy.addInhibitorArc(edge.source.findPlace(), edge.target.findTrans(), edge.label)
            is Arc -> {
                if (edge.source is Place) {
                    copy.addArc(edge.source.findPlace(), edge.target.findTrans(), edge.weight)
                } else {
                    copy.addArc(edge.source.findTrans(), edge.target.findPlace(), edge.weight)
                }
            }
            else -> throw IllegalStateException("Unsupported arc type while petrinet deepcopy. Edge: $edge.")
        }
        newArc.pnmlId = edge.pnmlId
    }
    return copy
}


const val DEFAULT_PNML_ID = "noPnmlId"

private class PnmlIdDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return (thisRef as AttributeMapOwner).attributeMap["pnmlId"]?.toString() ?: DEFAULT_PNML_ID
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        (thisRef as AttributeMapOwner).attributeMap.put("pnmlId", value)
    }
}

/**
 * fastPn is fast pnml format, developed for testing this tool.
 * See [com.pavelperc.newgena.petrinet.fastPetrinet.buildFastPetrinet].
 * It is stored near name tag in pnml.
 */
var ResetInhibitorNet.fastPn: String?
    get() = attributeMap["fastPn"] as String?
    set(value) {
        attributeMap.put("fastPn", value)
    }


/** Id, that was stored in original pnml file.*/
var PetrinetEdge<*, *>.pnmlId by PnmlIdDelegate()

/** Id, that was stored in original pnml file.*/
var PetrinetNode.pnmlId by PnmlIdDelegate()


/** Fills [pnmlId] for [Transition]s and [Place]s from labels. */
fun ResetInhibitorNet.makePnmlIdsFromLabels() {
    transitions.forEach { it.pnmlId = it.label }
    places.forEach { it.pnmlId = it.label }
}

/** Fills [pnmlId] for [Arc]s in style: sourceId_targetId. */
fun ResetInhibitorNet.makeArcPnmlIdsFromEnds() {
    edges.forEach { edge -> edge.pnmlId = "${edge.source.pnmlId}_${edge.target.pnmlId}" }
}


/** Fills [pnmlId] for arcs from labels. */
fun List<PetrinetEdge<*, *>>.makePnmlIdsOrdinal(startFrom: Int = 1, name: String = "arc") {
    withIndex().forEach { (i, edge) -> edge.pnmlId = "$name${i + startFrom}" }
}

//fun List<Arc>.makePnmlIdsOrdinalInArcs() = map { it as PetrinetEdge<*,*> }.makePnmlIdsOrdinal()


