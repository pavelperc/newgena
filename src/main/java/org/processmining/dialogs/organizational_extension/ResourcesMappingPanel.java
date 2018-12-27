package org.processmining.dialogs.organizational_extension;

import org.processmining.models.time_driven_behavior.NoiseEvent;
import org.processmining.models.time_driven_behavior.ResourceMapping;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.organizational_extension.Role;
import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;
import ru.hse.pais.shugurov.widgets.panels.MultipleChoicePanel;
import ru.hse.pais.shugurov.widgets.panels.SelectionListenerAdapter;
import ru.hse.pais.shugurov.widgets.panels.SingleChoicePanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * @author Ivan Shugurov
 *         Created  14.04.2014
 */
public class ResourcesMappingPanel extends EmptyPanel
{
    private SingleChoicePanel<Object> transitionsColumn;
    private Map<Object, ResourceMapping> mapping;
    private JPanel contentPanel;
    private MultipleChoicePanel<Group> groupsColumn;
    private MultipleChoicePanel<Role> rolesColumn;
    private MultipleChoicePanel<Resource> resourceColumn;
    private TimeDrivenGenerationDescription description;

    public ResourcesMappingPanel(Petrinet petrinet, final TimeDrivenGenerationDescription description)
    {
        this.description = description;
        this.mapping = description.getResourceMapping();
        fillResourceMappingWithDefaultValues(petrinet, description);
        JPanel header = new JPanel(new GridLayout(1, 4));
        header.setMaximumSize(new Dimension(2000, 200));
        contentPanel = new JPanel(new GridLayout(1, 4));
        header.add(new JLabel("Transitions", SwingConstants.CENTER));
        header.add(new JLabel("Groups", SwingConstants.CENTER));
        header.add(new JLabel("Roles", SwingConstants.CENTER));
        header.add(new JLabel("Resources", SwingConstants.CENTER));


        Collection possibleActions = new ArrayList(petrinet.getTransitions());         //TODO просто скопипастил код с SimplifiedResource mapping
        TimeDrivenGenerationDescription.NoiseDescription noiseDescription = description.getNoiseDescription();
        if (description.isUsingNoise() && noiseDescription.isUsingExternalTransitions())
        {
            possibleActions.addAll(noiseDescription.getArtificialNoiseEvents());
        }
        transitionsColumn = new SingleChoicePanel<Object>(possibleActions);
        transitionsColumn.addSelectionListener(new TransitionsColumnSelectionListener());

        contentPanel.add(transitionsColumn);
        fillColumnsWithEmptyLists();

        add(header);
        add(contentPanel);
    }

    private void fillResourceMappingWithDefaultValues(Petrinet petrinet, TimeDrivenGenerationDescription description)     //TODO просто скопипастил код с SimplifiedResource mapping
    {
        for (Transition transition : petrinet.getTransitions())  //TODO я бы пристально помотрел на оба этих метода
        {
            if (!mapping.containsKey(transition))
            {
                mapping.put(transition, new ResourceMapping());
            }
        }
        TimeDrivenGenerationDescription.NoiseDescription noiseDescription = description.getNoiseDescription();
        if (description.isUsingNoise() && noiseDescription.isUsingExternalTransitions())
        {
            for (NoiseEvent noiseEvent : noiseDescription.getArtificialNoiseEvents())
            {
                if (!mapping.containsKey(noiseEvent.getActivity()))
                {
                    mapping.put(noiseEvent.getActivity(), new ResourceMapping());
                }
            }
        }
    }

    private void fillColumnsWithEmptyLists()
    {
        groupsColumn = new MultipleChoicePanel<Group>(Collections.<Group>emptyList());
        groupsColumn.addSelectionListener(new GroupColumnSelectionListener());
        rolesColumn = new MultipleChoicePanel<Role>(Collections.<Role>emptyList());
        resourceColumn = new MultipleChoicePanel<Resource>(Collections.<Resource>emptyList());
        contentPanel.add(groupsColumn);
        contentPanel.add(rolesColumn);
        contentPanel.add(resourceColumn);
    }

    private void handleSelectedTransition(Object selectedTransition)
    {
        ResourceMapping mappingOfSelectedTransition;
        if (selectedTransition instanceof NoiseEvent)
        {
            Object activity = ((NoiseEvent) selectedTransition).getActivity();
            mappingOfSelectedTransition = mapping.get(activity);
        }
        else
        {
            mappingOfSelectedTransition = mapping.get(selectedTransition);
        }
        fillGroupColumn(mappingOfSelectedTransition);
    }

    private void fillRolesColumn(ResourceMapping mappingOfSelectedTransition)
    {
        contentPanel.remove(rolesColumn);
        Collection<Group> allSelectedGroups = mappingOfSelectedTransition.getSelectedGroups();
        Collection<Role> allRoles = new TreeSet<Role>();
        for (Group group : allSelectedGroups)
        {
            allRoles.addAll(group.getRoles());
        }
        rolesColumn = new MultipleChoicePanel<Role>(allRoles, mappingOfSelectedTransition.getSelectedRoles());
        rolesColumn.addSelectionListener(new RolesColumnSelectionListener());
        contentPanel.add(rolesColumn, 2);
        fillResourcesColumn(mappingOfSelectedTransition);
    }

    private void fillGroupColumn(ResourceMapping mappingOfSelectedTransition)
    {
        contentPanel.remove(groupsColumn);
        Collection<Group> selectedGroups = mappingOfSelectedTransition.getSelectedGroups();
        Collection<Group> resourceGroups = new TreeSet<Group>(description.getResourceGroups());
        groupsColumn = new MultipleChoicePanel<Group>(resourceGroups, selectedGroups);
        groupsColumn.addSelectionListener(new GroupColumnSelectionListener());
        contentPanel.add(groupsColumn, 1);
        fillRolesColumn(mappingOfSelectedTransition);
    }

    private void fillResourcesColumn(ResourceMapping mappingOfSelectedTransition)
    {
        contentPanel.remove(resourceColumn);
        Collection<Resource> availableResources = new TreeSet<Resource>();
        Collection<Resource> selectedRoles = new TreeSet<Resource>(mappingOfSelectedTransition.getSelectedResources());
        for (Role selectedRole : mappingOfSelectedTransition.getSelectedRoles())
        {
            availableResources.addAll(selectedRole.getResources());
        }
        resourceColumn = new MultipleChoicePanel<Resource>(availableResources, selectedRoles);
        resourceColumn.addSelectionListener(new ResourcesColumnListener());
        contentPanel.add(resourceColumn, 3);
        contentPanel.revalidate();
    }

    private ResourceMapping getMappingForActivity()
    {
        Object selectedActivity = transitionsColumn.getChosenOption();
        ResourceMapping mappingOfSelectedTransition;
        if (selectedActivity instanceof NoiseEvent)
        {
            Object activity = ((NoiseEvent) selectedActivity).getActivity();
            mappingOfSelectedTransition = mapping.get(activity);
        }
        else
        {
            mappingOfSelectedTransition = mapping.get(selectedActivity);

        }
        return mappingOfSelectedTransition;
    }

    private class GroupColumnSelectionListener extends SelectionListenerAdapter<Group>
    {
        @Override
        public void selected(Group selectedGroup)
        {
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.addSelectedGroup(selectedGroup);
            fillRolesColumn(mappingOfSelectedTransition);
        }

        @Override
        public void deselected(Group deselectedGroup)
        {
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.removeSelectedGroup(deselectedGroup);
            fillRolesColumn(mappingOfSelectedTransition);
        }
    }

    private class TransitionsColumnSelectionListener extends SelectionListenerAdapter<Object>
    {
        @Override
        public void selected(Object selectedTransition)
        {
            handleSelectedTransition(selectedTransition);
        }

        @Override
        public void deselected(Object deselectedOption)
        {
            contentPanel.remove(groupsColumn);
            contentPanel.remove(rolesColumn);
            contentPanel.remove(resourceColumn);
            fillColumnsWithEmptyLists();
            contentPanel.revalidate();
        }

        @Override
        public void selectionChanged(Object previousSelectedTransition, Object newSelectedTransition)
        {
            handleSelectedTransition(newSelectedTransition);
        }
    }

    private class RolesColumnSelectionListener extends SelectionListenerAdapter<Role>
    {

        @Override
        public void selected(Role selectedRole)
        {
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.addSelectedRole(selectedRole);
            fillResourcesColumn(mappingOfSelectedTransition);
        }

        @Override
        public void deselected(Role deselectedRole)
        {
            contentPanel.remove(resourceColumn);
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.removeSelectedRole(deselectedRole);
            fillResourcesColumn(mappingOfSelectedTransition);
        }
    }

    private class ResourcesColumnListener extends SelectionListenerAdapter<Resource>
    {

        @Override
        public void selected(Resource selectedResource)
        {
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.addSelectedResource(selectedResource);
        }

        @Override
        public void deselected(Resource deselectedResource)
        {
            ResourceMapping mappingOfSelectedTransition = getMappingForActivity();
            mappingOfSelectedTransition.removeSelectedResources(deselectedResource);
        }
    }

}



