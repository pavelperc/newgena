package org.processmining.utils.helpers;

import org.processmining.models.*;
import org.processmining.models.abstract_net_representation.Place;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.abstract_net_representation.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Иван on 27.10.2014.
 */
public abstract class PetriNetGenerationHelper<T extends Place, K extends Transition, F extends Token> extends BaseGenerationHelper<T, K, F>
{

    public PetriNetGenerationHelper(Collection<T> initialMarking, Collection<T> finalMarking, Collection<K> allTransitions, Collection<T> allPlaces, GenerationDescription description)
    {
        super(initialMarking, finalMarking, allTransitions, allPlaces, description);
    }

    @Override
    public Movable chooseNextMovable()
    {
        List<Transition> enabledTransitions = findEnabledTransitions();

        Movable movable;

        if (enabledTransitions.isEmpty() && getExtraMovables().isEmpty())
        {
            return null;
        }
        else
        {
            if (enabledTransitions.isEmpty())
            {
                movable = pickRandomMovable(getExtraMovables());
            }
            else
            {
                if (getExtraMovables().isEmpty())
                {
                    movable = pickRandomMovable(enabledTransitions);
                }
                else
                {
                    boolean moveThroughTransition = random.nextBoolean();
                    if (moveThroughTransition)
                    {
                        movable = pickRandomMovable(enabledTransitions);
                    }
                    else
                    {
                        movable = pickRandomMovable(getExtraMovables());
                    }
                }
            }
        }

        return movable;
    }

    protected List<Transition> findEnabledTransitions()
    {
        List<Transition> enabledTransitions = new ArrayList<Transition>();

        for (Transition transition : getAllModelMovables())
        {
            if (transition.checkAvailability())
            {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }

}
