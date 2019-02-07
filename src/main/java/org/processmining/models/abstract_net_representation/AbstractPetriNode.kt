package org.processmining.models.abstract_net_representation

import org.processmining.models.GenerationDescription
import org.processmining.models.graphbased.NodeID
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode

/**
 * @author Ivan Shugurov
 * Created  02.12.2013
 * 
 * Wrapper for [PetrinetNode]. Contains [generationDescription].
 */
abstract class AbstractPetriNode protected constructor(
        open val node: PetrinetNode,
        open val generationDescription: GenerationDescription
) {
    val nodeId: NodeID
        get() = node.id
    
    override fun toString() = "AbstractPetriNode{node=$node}"
}
