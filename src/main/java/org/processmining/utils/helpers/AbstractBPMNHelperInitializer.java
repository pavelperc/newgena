package org.processmining.utils.helpers;

import org.processmining.models.Movable;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;

import java.util.*;

/**
 * Created by Ivan on 17.09.2015.
 */
public abstract class AbstractBPMNHelperInitializer<
        T extends Token, SF extends SequenceFlow<T>,
        MF extends MessageFlow<T>, M extends Movable>
{
    private final BPMNDiagram diagram;
    protected Map<NodeID, AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>> subProcessBuilders =
            new HashMap<>();
    protected Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF>>
            nodesToActualMovableBuilders = new HashMap<>();
    protected SortedMap<Integer, Collection<AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>>> levelsToSubProcessBuilders =
            new TreeMap<>();
    private AbstractStartEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> startEventBuilder;
    private Map<Swimlane, AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess>> poolsToStartEventBuilders;
    private List<M> connectivityElements = new ArrayList<>();
    private List<M> actualMovables = new ArrayList<>();
    private Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF>>
            nodesToConnectivityElementBuilders = new HashMap<>();
    private List<SF> allSequenceFlows = new ArrayList<>();
    private List<MF> allMessageFlows = new ArrayList<>();
    private Map<Flow, SF> diagramFlowsToLoggableFlows = new HashMap<>();
    private Map<NodeID, AbstractSubProcess> builtSubProcesses = new HashMap<>();


    protected AbstractBPMNHelperInitializer(BPMNDiagram diagram)
    {
        this.diagram = diagram;
    }

    public void initialize()
    {
        findSubProcesses();

        parseEvents();

        parseFlows();

        initNodeBuilders();

        addFlowsToNodes();

        addMessageFlowsToNodes();

        buildSubProcesses();

        buildNodes();
    }


    protected void findSubProcesses()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess : diagram.getSubProcesses())
        {
            Collection<?> inputEdges = diagram.getInEdges(subProcess);
            Collection<?> outputEdges = diagram.getOutEdges(subProcess);

            if (subProcess.isBCollapsed())
            {
                continue;
            }

            if (inputEdges.isEmpty() && outputEdges.isEmpty())
            {
                //treats cases when elements of sub processes are directly connected to flows from outside sub process boundaries.
                // No special activities are required in such cases
            }
            else
            {
                AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess> subProcessBuilder = createSubProcessBuilder(subProcess);
                nodesToActualMovableBuilders.put(subProcess.getId(), (AbstractNodeBuilder) subProcessBuilder);  //TODO ����
                subProcessBuilders.put(subProcess.getId(), subProcessBuilder);

                int level = 0;
                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess parent = subProcess.getParentSubProcess();

                while (parent != null)
                {
                    level++;
                    parent = parent.getParentSubProcess();
                }

                Collection<AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>> buildersAtThisLevel = levelsToSubProcessBuilders.get(level);

                if (buildersAtThisLevel == null)
                {
                    buildersAtThisLevel = new ArrayList<>();
                    levelsToSubProcessBuilders.put(level, buildersAtThisLevel);
                }

                buildersAtThisLevel.add(subProcessBuilder);
            }
        }
    }


    private void parseEvents()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.Event event : diagram.getEvents())
        {
            switch (event.getEventType())
            {
                case START:
                    if (event.getParentSubProcess() == null)
                    {
                        if (event.getParentPool() == null)
                        {
                            if (startEventBuilder == null)
                            {
                                startEventBuilder = createStartEventBuilder(event);
                            }
                            else
                            {
                                throw new IllegalArgumentException("Too many start events");
                            }
                        }
                        else
                        {
                            if (poolsToStartEventBuilders == null)
                            {
                                poolsToStartEventBuilders = new HashMap<>();
                            }

                            AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess> existingBuilder =
                                    poolsToStartEventBuilders.put(event.getParentPool(), createStartEventBuilder(event));

                            if (existingBuilder != null)
                            {
                                throw new IllegalArgumentException("More than one start event in a pool");
                            }
                        }
                    }
                    else
                    {
                        putSubProcessBuilderInsteadOfEvent(event);
                    }
                    break;
                case END:
                    if (event.getEventTrigger() == org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger.CANCEL)
                    {
                        AbstractEndEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> endCancelEventBuilder = createEndCancelEventBuilder(event);
                        nodesToConnectivityElementBuilders.put(event.getId(), endCancelEventBuilder);
                    }
                    else
                    {
                        AbstractEndEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> endEventBuilder = createEndEventBuilder(event);
                        nodesToConnectivityElementBuilders.put(event.getId(), endEventBuilder);
                    }
                    break;
                case INTERMEDIATE:
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported event type");
            }
        }

    }

    protected void parseFlows()
    {
        for (Flow flow : diagram.getFlows())
        {
            SF sequenceFlow = createSequenceFlow(flow);
            diagramFlowsToLoggableFlows.put(flow, sequenceFlow);
            allSequenceFlows.add(sequenceFlow);

            if (!subProcessBuilders.isEmpty())
            {
                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess sourceParentSubProcess =
                        flow.getSource().getParentSubProcess();

                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess targetParentSubProcess =
                        flow.getTarget().getParentSubProcess();

                if (sourceParentSubProcess != null && sourceParentSubProcess == targetParentSubProcess)
                {
                    AbstractSubProcessBuilder<? extends Movable, SF, MF, ? extends AbstractSubProcess> subProcessBuilder =
                            subProcessBuilders.get(sourceParentSubProcess.getId());

                    if (subProcessBuilder != null)
                    {
                        subProcessBuilder.internalSequenceFlow(sequenceFlow);
                    }
                }
                else
                {
                    if (targetParentSubProcess != null && flow.getTarget() instanceof org.processmining.models.graphbased.directed.bpmn.elements.Event)
                    {
                        AbstractSubProcessBuilder<? extends Movable, SF, MF, ? extends AbstractSubProcess> subProcessBuilder =
                                subProcessBuilders.get(targetParentSubProcess.getId());

                        if (subProcessBuilder != null)
                        {
                            subProcessBuilder.internalSequenceFlow(sequenceFlow);
                        }
                    }
                }
            }
        }
    }

    protected void addMessageFlowsToNodes()
    {
        for (org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow flow : diagram.getMessageFlows())
        {
            BPMNNode sourceNode = flow.getSource();
            NodeID sourceId = sourceNode.getId();

            BPMNNode targetNode = flow.getTarget();
            NodeID targetId = targetNode.getId();

            MF loggableMessageFlow = createMessageFlow(flow);
            allMessageFlows.add(loggableMessageFlow);

            AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF> sourceBuilder =
                    nodesToActualMovableBuilders.get(sourceId);
            if (sourceBuilder == null)
            {
                throw new IllegalArgumentException("Node \"" + sourceNode.getLabel() + "\" cannot be a source of a message flow");
            }

            sourceBuilder.outgoingMessageFlow(loggableMessageFlow);


            AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF> targetBuilder =
                    nodesToActualMovableBuilders.get(targetId);
            if (targetBuilder == null)
            {
                throw new IllegalArgumentException("Node \"" + targetNode.getLabel() + "\" cannot be a target of a message flow");
            }

            targetBuilder.incomingMessageFlow(loggableMessageFlow);
        }
    }


    protected List<M> buildNodes(Collection<? extends AbstractNodeBuilder> builders)
    {
        List<M> builtMovables = new ArrayList<>();

        for (AbstractNodeBuilder<? extends M, BPMNNode, SF, MF> builder : builders)
        {
            BPMNNode node = builder.getActualNode();

            org.processmining.models.graphbased.directed.bpmn.elements.SubProcess nativeParentSubProcess = node.getParentSubProcess();
            if (nativeParentSubProcess != null)
            {
                AbstractSubProcess loggableSubProcess = builtSubProcesses.get(nativeParentSubProcess.getId());
                builder.parentSubProcess(loggableSubProcess);
            }

            M movableDiagramElement = builder.build(); //TODO ���� ����. ��������, ���� ���-�� ������� ���������...
            builtMovables.add(movableDiagramElement);
        }

        return builtMovables;
    }

    protected void buildSubProcesses()
    {
        for (Map.Entry<Integer, Collection<AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>>> entry : levelsToSubProcessBuilders.entrySet())
        {
            int level = entry.getKey();

            for (AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess> builder : entry.getValue())
            {
                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualSubProcess = builder.getActualNode();
                NodeID id = actualSubProcess.getId();

                if (level > 0)
                {
                    org.processmining.models.graphbased.directed.bpmn.elements.SubProcess actualParentSubProcess =
                            actualSubProcess.getParentSubProcess();

                    AbstractSubProcess loggableParentSubProcess = builtSubProcesses.get(actualParentSubProcess.getId());
                    builder.parentSubProcess(loggableParentSubProcess);
                }

                AbstractSubProcess subProcess = builder.build();
                builtSubProcesses.put(id, subProcess);
            }
        }
    }

    protected abstract AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess> createSubProcessBuilder(org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess);

    protected abstract AbstractEndEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> createEndCancelEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event event);

    protected abstract AbstractEndEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> createEndEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event event);

    protected abstract AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess> createStartEventBuilder(org.processmining.models.graphbased.directed.bpmn.elements.Event event);

    protected abstract SF createSequenceFlow(Flow flow);

    protected void initNodeBuilders()
    {
        findActivities();

        findGateways();

    }

    protected abstract void findGateways();

    protected abstract void findActivities();

    protected void putSubProcessBuilderInsteadOfEvent(org.processmining.models.graphbased.directed.bpmn.elements.Event event)
    {
        org.processmining.models.graphbased.directed.bpmn.elements.SubProcess subProcess = event.getParentSubProcess();
        AbstractNodeBuilder<? extends AbstractSubProcess, org.processmining.models.graphbased.directed.bpmn.elements.SubProcess, SF, MF> builder =
                subProcessBuilders.get(subProcess.getId());
        nodesToActualMovableBuilders.put(event.getId(), builder);
    }

    protected abstract MF createMessageFlow(org.processmining.models.graphbased.directed.bpmn.elements.MessageFlow flow);


    protected void addFlowsToNodes()   //TODO ��������, �� ����� ����������(
    {
        for (SF sequenceFlow : allSequenceFlows)
        {
            Flow actualFlow = sequenceFlow.getFlow();
            BPMNNode source = actualFlow.getSource();

            boolean flowWasAddedToNode = false;

            if (startEventBuilder == null)
            {
                for (AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess> startEventBuilder : poolsToStartEventBuilders.values())
                {
                    flowWasAddedToNode = addOutputFlowToStartEvent(sequenceFlow, source, startEventBuilder);

                    if (flowWasAddedToNode)
                    {
                        break;
                    }
                }
            }
            else
            {
                flowWasAddedToNode = addOutputFlowToStartEvent(sequenceFlow, source, startEventBuilder);
            }

            if (!flowWasAddedToNode)
            {
                addOutputFlow(sequenceFlow, source);
            }

            BPMNNode target = actualFlow.getTarget();
            addInputFlow(sequenceFlow, target);
        }
    }


    protected boolean addOutputFlowToStartEvent(SF sequenceFlow, BPMNNode source, AbstractStartEventBuilder<? extends Event, SF, MF, ? extends AbstractSubProcess> startEventBuilder)
    {
        boolean flowWasAdded = false;

        if (source == startEventBuilder.getActualNode())
        {
            startEventBuilder.outputFlow(sequenceFlow);
            flowWasAdded = true;
        }

        return flowWasAdded;
    }

    protected void addOutputFlow(SF sequenceFlow, BPMNNode source)
    {
        if (checkIfNodeIsIntermediateCancelEvent(source))
        {
            org.processmining.models.graphbased.directed.bpmn.elements.Event event = (org.processmining.models.graphbased.directed.bpmn.elements.Event) source;
            BPMNNode boundingNode = event.getBoundingNode();

            getSubProcessBuilders().get(boundingNode.getId()).cancelOutputFlow(sequenceFlow);
        }
        else
        {
            if (findBuilder(source.getId()) == null)
            {
                int sdf = 90;
            }
            findBuilder(source.getId()).outputFlow(sequenceFlow);
        }
    }

    protected boolean checkIfNodeIsIntermediateCancelEvent(BPMNNode source)
    {
        if (source instanceof org.processmining.models.graphbased.directed.bpmn.elements.Event)
        {
            org.processmining.models.graphbased.directed.bpmn.elements.Event event = (org.processmining.models.graphbased.directed.bpmn.elements.Event) source;

            if (event.getEventTrigger() == org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger.CANCEL)
            {
                if (event.getEventType() == org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType.INTERMEDIATE)
                {
                    return true;
                }
            }

        }

        return false;
    }

    protected void addInputFlow(SF sequenceFlow, BPMNNode target)
    {
        AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF> targetBuilder = findBuilder(target.getId());
        targetBuilder.inputFlow(sequenceFlow);
    }

    protected AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF> findBuilder(NodeID id)
    {
        AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF> sourceBuilder = nodesToConnectivityElementBuilders.get(id);

        if (sourceBuilder == null)
        {
            sourceBuilder = nodesToActualMovableBuilders.get(id);
        }
        return sourceBuilder;
    }

    protected void buildNodes()
    {
        connectivityElements = buildNodes(nodesToConnectivityElementBuilders.values());


        for (AbstractNodeBuilder builder : nodesToActualMovableBuilders.values())
        {
            if (!(builder instanceof AbstractSubProcessBuilder))
            {
                BPMNNode node = builder.getActualNode();

                org.processmining.models.graphbased.directed.bpmn.elements.SubProcess nativeParentSubProcess = node.getParentSubProcess();
                if (nativeParentSubProcess != null)
                {
                    AbstractSubProcess loggableSubProcess = builtSubProcesses.get(nativeParentSubProcess.getId());
                    builder.parentSubProcess(loggableSubProcess);
                }

                actualMovables.add((M) builder.build());
            }
        }

        actualMovables.addAll((Collection<? extends M>) builtSubProcesses.values());
    }


    //getters

    public BPMNDiagram getDiagram()
    {
        return diagram;
    }

    public StartEvent getStartEvent()
    {
        if (startEventBuilder == null)
        {
            return null;
        }

        return startEventBuilder.build();
    }

    public Collection<StartEvent> getStartEvents()
    {
        if (poolsToStartEventBuilders == null)
        {
            return null;
        }

        List<StartEvent> startEvents = new ArrayList<>();

        for (AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess> builder : poolsToStartEventBuilders.values())
        {
            StartEvent event = builder.build();
            startEvents.add(event);
        }

        return startEvents;
    }

    //TODO write nice settrs instead of getters?
    public Collection<M> getConnectivityElements()
    {
        return connectivityElements;
    }

    public Collection<M> getActualMovables()
    {
        return actualMovables;
    }

    public List<MF> getAllMessageFlows()
    {
        return allMessageFlows;
    }

    protected AbstractStartEventBuilder getStartEventBuilder()
    {
        return startEventBuilder;
    }

    protected Map<Swimlane, AbstractStartEventBuilder<? extends StartEvent, SF, MF, ? extends AbstractSubProcess>> getPoolsToStartEventBuilders()
    {
        return poolsToStartEventBuilders;
    }

    protected Map<Flow, SF> getDiagramFlowsToLoggableFlows()
    {
        return diagramFlowsToLoggableFlows;
    }

    protected List<SF> getAllSequenceFlows()
    {
        return allSequenceFlows;
    }

    protected Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF>> getNodesToConnectivityElementBuilders()
    {
        return nodesToConnectivityElementBuilders;
    }

    protected Map<NodeID, AbstractNodeBuilder<? extends Movable, ? extends BPMNNode, SF, MF>> getNodesToActualMovableBuilders()
    {
        return nodesToActualMovableBuilders;
    }

    protected Map<NodeID, AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>> getSubProcessBuilders()
    {
        return subProcessBuilders;
    }

    protected Map<NodeID, ? extends AbstractSubProcess> getBuiltSubProcesses()
    {
        return builtSubProcesses;
    }

    protected SortedMap<Integer, Collection<AbstractSubProcessBuilder<? extends T, SF, MF, ? extends AbstractSubProcess>>> getLevelsToSubProcessBuilders()
    {
        return levelsToSubProcessBuilders;
    }

    public Collection<AbstractSubProcess> getSubProcesses()
    {
        List<AbstractSubProcess> subProcesses = new ArrayList<>();

        for (AbstractSubProcess subProcess : builtSubProcesses.values())
        {
            subProcesses.add(subProcess);
        }

        return subProcesses;
    }
}
