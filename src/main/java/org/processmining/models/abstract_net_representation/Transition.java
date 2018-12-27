package org.processmining.models.abstract_net_representation;

import org.processmining.models.GenerationDescription;
import org.processmining.models.Movable;

import java.util.Random;


/**
 * @author Ivan Shugurov
 *         Created  02.12.2013
 */
//T - type of place
public abstract class Transition<T extends Place> extends AbstractPetriNode implements Movable
{
    protected static final Random random = new Random();
    private T[] inputPlaces;
    private T[] outputPlaces;

    protected Transition
            (
                    org.processmining.models.graphbased.directed.petrinet.elements.Transition node,
                    GenerationDescription generationDescription,
                    T[] inPlaces, T[] outPlaces
            )
    {
        super(node, generationDescription);
        this.inputPlaces = inPlaces;
        this.outputPlaces = outPlaces;
    }

    public boolean checkAvailability()
    {
        boolean isAvailable = true;
        for (AbstractPetriNode inEdge : inputPlaces)
        {
            Place place = ((Place) inEdge);
            if (!place.hasTokens())
            {
                isAvailable = false;
                break;
            }
        }
        return isAvailable;
    }

    public T[] getOutputPlaces()  //TODO а нормально так выставлять поля?
    {
        return outputPlaces;
    }

    public T[] getInputPlaces()  //TODO а нормально так выставлять поля?
    {
        return inputPlaces;
    }

}
