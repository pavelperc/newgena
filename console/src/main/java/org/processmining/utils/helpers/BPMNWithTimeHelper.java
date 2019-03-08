package org.processmining.utils.helpers;

import org.processmining.models.*;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.bpmn_with_time.MovableWithTime;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.util.*;

/**
 * Created by Ivan on 01.09.2015.
 */
public class BPMNWithTimeHelper implements GenerationHelper<Movable, TokenWithTime>
{
    private final Collection<StartEvent> startEvents;
    private final Collection<MovableWithTime> connectivityElements;
    private final Collection<MovableWithTime> actualMovables;
    private BPMNWithTimeGenerationDescription description;
    private boolean initialState = true;
    private Set<Tokenable> flowsWithTokens = new HashSet<>();
    private Collection<? extends AbstractSubProcess> subProcesses;
    private Queue<TokenWithTime> extraMovables = new PriorityQueue<>(); //TODO можно реализовать чуть оптимальнее, если хранить в очереди, а потом доставать их оттуда и сравнивать с другими элементами
    private Random random = new Random();


    protected BPMNWithTimeHelper(BPMNWithTimeHelperInitializer initializer, BPMNWithTimeGenerationDescription description)
    {
        this.description = description;

        StartEvent startEvent = initializer.getStartEvent();
        if (startEvent == null)
        {
            startEvents = initializer.getStartEvents();
        }
        else
        {
            startEvents = Collections.singletonList(startEvent);
        }

        connectivityElements = initializer.getConnectivityElements();
        actualMovables = initializer.getActualMovables();
        this.subProcesses = initializer.getSubProcesses();
    }

    public static BPMNWithTimeHelper creteHelper(BPMNDiagram diagram, BPMNWithTimeGenerationDescription description)
    {
        BPMNWithTimeHelperInitializer initializer = new BPMNWithTimeHelperInitializer(diagram, description);
        initializer.initialize();
        return new BPMNWithTimeHelper(initializer, description);
    }


    @Override
    public GenerationDescription getGenerationDescription()
    {
        return description;
    }

    @Override
    public List<Movable> getAllModelMovables()
    {
        List<Movable> allMovables = new ArrayList<Movable>();
        allMovables.addAll(connectivityElements);
        allMovables.addAll(actualMovables);

        return allMovables;
    }

    @Override
    public List<TokenWithTime> getExtraMovables()
    {
        return new ArrayList<>(extraMovables);
    }

    @Override
    public void moveToInitialState()
    {
        if (initialState)
        {
            return;
        }

        initialState = true;

        for (Tokenable flowWithTokens : flowsWithTokens)
        {
            flowWithTokens.removeAllTokens();
        }

        for (StartEvent startEvent : startEvents)
        {
            startEvent.moveToInitialState();
        }

        for (Tokenable tokenableWithToken : flowsWithTokens)
        {
            tokenableWithToken.removeAllTokens();
        }

        for (AbstractSubProcess subProcess : subProcesses)
        {
            subProcess.stopSubProcess();
        }

        extraMovables.clear();

        flowsWithTokens.clear();
    }

    @Override
    //TODO  if we have several pool (several start events). can we specify start time for each one?
    public Movable chooseNextMovable()
    {
        if (initialState)
        {
            for (Event startEvent : startEvents)
            {
                if (startEvent.checkAvailability())
                {
                    return startEvent;
                }
            }

            initialState = false;
        }

        List<MovableWithTime> enabledEarliestMovables = new ArrayList<MovableWithTime>();
        parseMovables(enabledEarliestMovables, connectivityElements);
        parseMovables(enabledEarliestMovables, actualMovables);

        if (enabledEarliestMovables.isEmpty() && extraMovables.isEmpty())
        {
            return null;
        }
        else
        {
            if (extraMovables.isEmpty())
            {
                return pickRandomMovable(enabledEarliestMovables);
            }
            else
            {
                if (enabledEarliestMovables.isEmpty())
                {
                    return chooseExtraMovable();
                }
                else
                {
                    long movableTimestamp = enabledEarliestMovables.get(0).getTimestamp();
                    long extraMovableTimestamp = extraMovables.peek().getTimestamp();

                    if (movableTimestamp == extraMovableTimestamp)
                    {
                        if (random.nextBoolean())
                        {
                            // move extra movable
                            return chooseExtraMovable();
                        }
                        else
                        {
                            //fire a node
                            return pickRandomMovable(enabledEarliestMovables);
                        }
                    }
                    else
                    {
                        if (movableTimestamp < extraMovableTimestamp)
                        {
                            return pickRandomMovable(enabledEarliestMovables);
                        }
                        else
                        {
                            return chooseExtraMovable();
                        }
                    }
                }

            }
        }
    }

    protected Movable chooseExtraMovable()
    {
        List<TokenWithTime> possibleExtraMovables = new ArrayList<TokenWithTime>();
        TokenWithTime extraToken = extraMovables.remove();
        long timestamp = extraToken.getTimestamp();

        while (!extraMovables.isEmpty() && extraMovables.peek().getTimestamp() == timestamp)
        {
            possibleExtraMovables.add(extraMovables.remove());
        }

        if (possibleExtraMovables.isEmpty())
        {
            extraMovables.remove(extraToken);
            return extraToken;
        }
        else
        {
            possibleExtraMovables.add(extraToken);
            TokenWithTime chosenToken = pickRandomMovable(possibleExtraMovables);
            possibleExtraMovables.remove(chosenToken);
            extraMovables.addAll(possibleExtraMovables);

            return chosenToken;
        }
    }

    private <T extends MovableWithTime> T pickRandomMovable(List<T> movables)
    {
        int index = random.nextInt(movables.size());
        return movables.remove(index);
    }

    protected void parseMovables(List<MovableWithTime> enabledEarliestMovables, Collection<? extends MovableWithTime> movablesToCheck)
    {
        long timestamp;

        if (enabledEarliestMovables.isEmpty())
        {
            timestamp = Long.MAX_VALUE;
        }
        else
        {
            timestamp = enabledEarliestMovables.get(0).getTimestamp();
        }

        for (MovableWithTime movable : movablesToCheck)
        {
            if (movable.checkAvailability())
            {
                long movableTimestamp = movable.getTimestamp();

                if (movableTimestamp < timestamp)
                {
                    enabledEarliestMovables.clear();
                    enabledEarliestMovables.add(movable);
                    timestamp = movableTimestamp;
                }
                else
                {
                    if (movableTimestamp == timestamp)
                    {
                        enabledEarliestMovables.add(movable);
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssessedMovementResult handleMovementResult(MovementResult movementResult)
    {
        boolean eligibleReplay = false;
        boolean endReached = false;

        @SuppressWarnings("unchecked")
        Collection<Tokenable<Token>> emptiedTokenables = movementResult.getEmptiedTokenables();
        flowsWithTokens.removeAll(emptiedTokenables);

        @SuppressWarnings("unchecked")
        Collection<SequenceFlow> filledFlows = movementResult.getFilledTokenables();
        flowsWithTokens.addAll(filledFlows);

        extraMovables.addAll(movementResult.getProducedExtraMovables());
        extraMovables.removeAll(movementResult.getConsumedExtraMovables());

        if (flowsWithTokens.isEmpty() && extraMovables.isEmpty())
        {
            eligibleReplay = true;
            endReached = true;
        }

        return new AssessedMovementResult(endReached, eligibleReplay);
    }
}
