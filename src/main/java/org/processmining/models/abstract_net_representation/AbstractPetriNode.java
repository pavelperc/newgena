package org.processmining.models.abstract_net_representation;

import org.processmining.models.GenerationDescription;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

/**
 * @author Ivan Shugurov
 *         Created  02.12.2013
 */
public abstract class AbstractPetriNode
{
    private PetrinetNode node;
    private GenerationDescription generationDescription;

    protected AbstractPetriNode
            (
                    PetrinetNode node,
                    GenerationDescription generationDescription
            )
    {
        this.node = node;
        this.generationDescription = generationDescription;
    }

    public PetrinetNode getNode()
    {
        return node;
    }

    public NodeID getNodeId()
    {
        return node.getId();
    }

    public GenerationDescription getGenerationDescription()
    {
        return generationDescription;
    }

    @Override
    public String toString()
    {
        return "AbstractPetriNode{" +
                "node=" + node +
                '}';
    }
}
