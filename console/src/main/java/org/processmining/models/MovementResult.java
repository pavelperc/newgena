package org.processmining.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ivan Shugurov on 07.10.2014.
 */
public class MovementResult<T extends Movable>
{

    private final List<Tokenable> emptiedTokenables = new ArrayList<Tokenable>();
    private final List<Tokenable> filledTokenables = new ArrayList<Tokenable>();
    private final List<T> consumedExtraMovables = new ArrayList<T>();
    private final List<T> producedExtraMovables = new ArrayList<T>();
    private boolean isActualStep;

    public MovementResult()
    {
        this(true);
    }


    public MovementResult(boolean isActualStep)
    {
        this.isActualStep = isActualStep;
    }


    public List<Tokenable> getEmptiedTokenables()
    {
        return emptiedTokenables;
    }

    public List<Tokenable> getFilledTokenables()
    {
        return filledTokenables;
    }

    public void addEmptiedTokenable(Tokenable tokenable) //TODO не проверяю на null
    {
        emptiedTokenables.add(tokenable);
    }

    public void addAllEmptiedTokenables(Collection<? extends Tokenable> tokenables)
    {
        emptiedTokenables.addAll(tokenables);
    }

    public void addFilledTokenables(Tokenable tokenable)  //TODO не проверяю на null
    {
        filledTokenables.add(tokenable);
    }

    public void addAllFilledTokenables(Collection<? extends Tokenable> tokenables)
    {
        filledTokenables.addAll(tokenables);
    }

    public boolean isActualStep()
    {
        return isActualStep;
    }

    public void setActualStep(boolean isActualStep)
    {
        this.isActualStep = isActualStep;
    }

    public void addConsumedExtraToken(T token)
    {
        checkMovable(token);
        consumedExtraMovables.add(token);
    }

    public void addConsumedExtraTokens(Collection<T> token)
    {
        consumedExtraMovables.addAll(token);
    }

    public void addProducedExtraToken(T movable)
    {
        checkMovable(movable);
        producedExtraMovables.add(movable);
    }

    public List<T> getConsumedExtraMovables()
    {
        return Collections.unmodifiableList(consumedExtraMovables);
    }

    public List<T> getProducedExtraMovables()
    {
        return Collections.unmodifiableList(producedExtraMovables);
    }

    private void checkMovable(T movable)
    {
        if (movable == null)
        {
            throw new IllegalArgumentException("Movable cannot be null");
        }
    }

}
