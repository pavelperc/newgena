package org.processmining.utils.helpers;

import org.processmining.models.*;

import java.util.*;

/**
 * Created by Ivan Shugurov on 23.10.2014.
 */

/**
 * @param <T> type of objects which can contain tokens, for example, places in Petri nets
 * @param <K> type of objects it's possible to move through. For example, transitions in Petri nets
 * @param <F> type of additional movables possible during a replay
 */
public abstract class BaseGenerationHelper<T extends Tokenable, K extends Movable, F extends Movable> implements GenerationHelper<K, F>
{
    protected static final Random random = new Random();
    private final Set<T> initialMarking;
    private final Set<T> finalMarking;
    private final List<K> allModelMovables;
    private final List<T> allTokenables;
    private final List<F> extraMovables = new LinkedList<F>();
    private final GenerationDescription description;
    public BaseGenerationHelper(Collection<T> initialMarking, Collection<T> finalMarking, Collection<K> allModelMovables, Collection<T> allTokenables, GenerationDescription description)
    {
        this.initialMarking = new HashSet<T>(initialMarking);
        this.finalMarking = new HashSet<T>(finalMarking);
        this.allModelMovables = new ArrayList<K>(allModelMovables);
        this.allTokenables = new ArrayList<T>(allTokenables);
        this.description = description;
        moveToInitialState();
    }

    @Override
    public GenerationDescription getGenerationDescription()
    {
        return description;
    }

    protected abstract void putInitialToken(T tokenable);

    @Override
    public List<K> getAllModelMovables()
    {
        return Collections.unmodifiableList(allModelMovables);
    }


    @Override
    public List<F> getExtraMovables()
    {
        return Collections.unmodifiableList(extraMovables);
    }

    @Override
    public void moveToInitialState()
    {
        removeAllTokens();
        extraMovables.clear();
        for (T initialTokenable : initialMarking)
        {
            putInitialToken(initialTokenable);
        }
    }

    protected void removeAllTokens()
    {
        for (Tokenable tokenable : allTokenables)
        {
            tokenable.removeAllTokens();
        }
    }

    protected <L extends Movable> L pickRandomMovable(List<L> movables)
    {
        int index = random.nextInt(movables.size());
        return movables.get(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssessedMovementResult handleMovementResult(MovementResult movementResult)
    {
        if (movementResult == null)
        {
            throw new IllegalArgumentException("Movement result cannot be null");
        }

        extraMovables.removeAll(movementResult.getConsumedExtraMovables());
        extraMovables.addAll(movementResult.getProducedExtraMovables());

        boolean replayCompleted = tokensOnlyInFinalMarking();
        return new AssessedMovementResult(replayCompleted, true);
    }

    //returns true if final marking was reached
    protected boolean tokensOnlyInFinalMarking()
    {
        for (Tokenable tokenable : allTokenables)
        {
            if (tokenable.hasTokens())
            {
                if (!finalMarking.contains(tokenable))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
