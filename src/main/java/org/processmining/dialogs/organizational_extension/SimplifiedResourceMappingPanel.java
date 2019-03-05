//package org.processmining.dialogs.organizational_extension;
//
//import org.processmining.models.time_driven_behavior.NoiseEvent;
//import org.processmining.models.time_driven_behavior.ResourceMapping;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.models.organizational_extension.Group;
//import org.processmining.models.organizational_extension.Resource;
//import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
//import ru.hse.pais.shugurov.widgets.panels.*;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Map;
//
///**
// * @author Ivan Shugurov
// *         Created on 21.04.2014.
// */
//public class SimplifiedResourceMappingPanel extends EmptyPanel
//{
//    private SingleChoicePanel<? extends Object> transitionsColumn;
//    private MultipleChoicePanel<Resource> resourceColumn;
//    private Map<Object, ResourceMapping> mapping;
//    private JPanel contentPanel;
//    private Collection<Resource> allResources = new ArrayList<Resource>();
//
//    public SimplifiedResourceMappingPanel(Petrinet petrinet, final TimeDrivenGenerationDescription description)
//    {
//        this.mapping = description.getResourceMapping();
//        fillResourceMappingWithDefaultValues(petrinet, description);
//        for (Group group : description.getResourceGroups())
//        {
//            allResources.addAll(group.getResources());
//        }
//
//        allResources.addAll(description.getSimplifiedResources());
//        JPanel header = new JPanel(new GridLayout(1, 2));
//        header.setMaximumSize(new Dimension(2000, 200));
//        contentPanel = new JPanel(new GridLayout(1, 2));
//
//        header.add(new JLabel("Transitions", SwingConstants.CENTER));
//        header.add(new JLabel("Resources", SwingConstants.CENTER));
//
//        Collection possibleActions = new ArrayList(petrinet.getTransitions());
//        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();
//        if (description.isUsingNoise() && noiseDescription.isUsingExternalTransitions())
//        {
//            for (NoiseEvent event : noiseDescription.getArtificialNoiseEvents())
//            {
//                possibleActions.add(event.getActivity());
//            }
//        }
//        transitionsColumn = new SingleChoicePanel<Object>(possibleActions);
//        transitionsColumn.addSelectionListener(new TransitionsColumnSelectionListener());
//
//        contentPanel.add(transitionsColumn);
//        fillColumnsWithEmptyLists();
//
//        add(header);
//        add(contentPanel);
//    }
//
//    private void fillResourceMappingWithDefaultValues(Petrinet petrinet, TimeDrivenGenerationDescription description)
//    {
//        for (Transition transition : petrinet.getTransitions())
//        {
//            if (!mapping.containsKey(transition))
//            {
//                mapping.put(transition, new ResourceMapping());
//            }
//        }
//        TimeDrivenGenerationDescription.TimeNoiseDescription noiseDescription = description.getNoiseDescription();
//        if (description.isUsingNoise() && noiseDescription.isUsingExternalTransitions())
//        {
//            for (NoiseEvent noiseEvent : noiseDescription.getArtificialNoiseEvents())
//            {
//                if (!mapping.containsKey(noiseEvent.getActivity()))
//                {
//                    mapping.put(noiseEvent.getActivity(), new ResourceMapping());
//                }
//            }
//        }
//    }
//
//    private void fillColumnsWithEmptyLists()
//    {
//        resourceColumn = new MultipleChoicePanel<Resource>(Collections.<Resource>emptyList());
//        contentPanel.add(resourceColumn);
//    }
//
//    private void fillResourcesColumn(ResourceMapping mappingOfSelectedTransition)
//    {
//        contentPanel.remove(resourceColumn);
//        resourceColumn = new MultipleChoicePanel<Resource>(allResources, mappingOfSelectedTransition.getSelectedSimplifiedResources());
//        resourceColumn.addSelectionListener(new ResourcesColumnListener());
//        contentPanel.add(resourceColumn, 1);
//        contentPanel.revalidate();
//    }
//
//    private class TransitionsColumnSelectionListener implements SelectionListener
//    {
//        @Override
//        public void selected(Object selectedTransition)
//        {
//            ResourceMapping mappingOfSelectedTransition = mapping.get(selectedTransition);
//            fillResourcesColumn(mappingOfSelectedTransition);
//        }
//
//        @Override
//        public void deselected(Object deselectedOption)
//        {
//            contentPanel.remove(resourceColumn);
//            fillColumnsWithEmptyLists();
//            contentPanel.revalidate();
//        }
//
//        @Override
//        public void selectionChanged(Object previousSelectedTransition, Object newSelectedTransition)
//        {
//            ResourceMapping mappingOfSelectedTransition = mapping.get(newSelectedTransition);
//            fillResourcesColumn(mappingOfSelectedTransition);
//        }
//    }
//
//    private class ResourcesColumnListener extends SelectionListenerAdapter<Resource>
//    {
//
//        @Override
//        public void selected(Resource selectedResource)
//        {
//            ResourceMapping mappingOfSelectedTransition = mapping.get(transitionsColumn.getChosenOption());
//            mappingOfSelectedTransition.addSelectedSimplifiedResource(selectedResource);
//        }
//
//        @Override
//        public void deselected(Resource deselectedResource)
//        {
//            ResourceMapping mappingOfSelectedTransition = mapping.get(transitionsColumn.getChosenOption());
//            mappingOfSelectedTransition.removeSelectedSimplifiedResource(deselectedResource);
//        }
//    }
//}
