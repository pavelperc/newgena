package org.processmining.utils.helpers;

import org.processmining.models.*;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.descriptions.ModifiedBPMNGenerationDescription;
import org.processmining.models.descriptions.BasicBPMNGenerationDescription;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.util.*;

/**
 * Created by Ivan Shugurov on 22.12.2014.
 */
public class SimpleBPMNHelper implements GenerationHelper<Movable, Token>
{
    //TODO куча ворнингов
    protected static final Random random = new Random();
    private final Collection<StartEvent> startEvents;
    private final GenerationDescription generationDescription;
    private final Collection<? extends Movable> connectivityElements;
    private final Collection<? extends Movable> actualMovables;
    private final Collection<? extends SimpleMessageFlow> messageFlows;
    private final Collection<? extends AbstractSubProcess> subProcesses;
    private boolean initialState = true;
    private Set<Tokenable> flowsWithTokens = new HashSet<Tokenable>();

    protected SimpleBPMNHelper(BPMNHelperInitializer initializer, GenerationDescription description)
    {
        StartEvent startEvent = initializer.getStartEvent();
        if (startEvent == null)
        {
            //TODO а я вообще поддерживаю такие случаи? вроде как, нет
            startEvents = initializer.getStartEvents();
        }
        else
        {
            startEvents = Collections.singletonList(startEvent);
        }

        connectivityElements = initializer.getConnectivityElements();
        actualMovables = initializer.getActualMovables();
        this.generationDescription = description;
        this.subProcesses = initializer.getSubProcesses();

        messageFlows = initializer.getAllMessageFlows();
    }

    public static SimpleBPMNHelper createSimpleHelper(BPMNDiagram diagram, BasicBPMNGenerationDescription generationDescription)
    {
        checkParameters(diagram, generationDescription);

        BPMNHelperInitializer initializer = BPMNHelperInitializer.createSimpleBPMNHelperInitializer(diagram);
        initializer.initialize();
        return new SimpleBPMNHelper(initializer, generationDescription);
    }

    public static SimpleBPMNHelper createModifiedHelper(BPMNDiagram diagram, ModifiedBPMNGenerationDescription description)
    {
        ModifiedBPMNHelperInitializer initializer = new ModifiedBPMNHelperInitializer(diagram, description);
        initializer.initialize();
        return new SimpleBPMNHelper(initializer, description);
    }

    protected static void checkParameters(BPMNDiagram diagram, GenerationDescription generationDescription) throws NullPointerException
    {
        if (diagram == null)
        {
            throw new NullPointerException("BPMN diagram cannot be equal to null");
        }

        if (generationDescription == null)
        {
            throw new NullPointerException("Generation description cannot be equal to null");
        }
    }

    @Override
    public GenerationDescription getGenerationDescription()
    {
        return generationDescription;
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
    public List<Token> getExtraMovables()
    {
        return new ArrayList<Token>();
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

        for (SimpleMessageFlow messageFlow : messageFlows)
        {
            messageFlow.removeAllTokens();
        }

        for (AbstractSubProcess subProcess : subProcesses)
        {
            subProcess.stopSubProcess();
        }

        flowsWithTokens.clear();
    }

    @Override
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


        Movable enabledConnectivityElement = findEnabledConnectivityElement();

        if (enabledConnectivityElement != null)
        {
            return enabledConnectivityElement;
        }

        List<Movable> enabledMovables = findEnabledMovables();

        if (enabledMovables.isEmpty())
        {
            System.out.println("null"); //TODO delete
            return null;
        }
        else
        {
            return pickRandomMovable(enabledMovables);
        }
    }

    private Movable findEnabledConnectivityElement()
    {

        for (Movable movable : connectivityElements)
        {
            if (movable.checkAvailability())
            {
                return movable;
            }
        }

        return null;
    }

    private List<Movable> findEnabledMovables()
    {

        List<Movable> enabledMovables = new ArrayList<Movable>();

        for (Movable movable : actualMovables)
        {
            if (movable.checkAvailability())
            {
                enabledMovables.add(movable);
            }
        }

        return enabledMovables;

    }

    private Movable pickRandomMovable(List<Movable> movables)
    {
        int index = random.nextInt(movables.size());
        return movables.remove(index);
    }

    @Override
    public AssessedMovementResult handleMovementResult(MovementResult movementResult)
    {
        boolean eligibleReplay = false;        //TODO а нужна ли вообще?
        boolean endReached = false;

        @SuppressWarnings("unchecked")
        Collection<Tokenable<Token>> emptiedTokenables = movementResult.getEmptiedTokenables();
        flowsWithTokens.removeAll(emptiedTokenables);

        @SuppressWarnings("unchecked")
        Collection<SequenceFlow> filledFlows = movementResult.getFilledTokenables();
        flowsWithTokens.addAll(filledFlows);

        if (flowsWithTokens.isEmpty())
        {
            eligibleReplay = true;
            endReached = true;
        }

        return new AssessedMovementResult(endReached, eligibleReplay);
    }
}
