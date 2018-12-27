package org.processmining.dialogs.organizational_extension;

import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Resource;
import org.processmining.models.organizational_extension.Role;
import ru.hse.pais.shugurov.widgets.elements.ElementPanel;
import ru.hse.pais.shugurov.widgets.panels.EditableListPanel;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.List;

/**
 * Created by Ivan Shugurov on 07.04.2014.
 *
 * @author Ivan Shugurov;
 *         Created  07.04.2014
 */
public class ResourcePanel extends EditableListPanel<Resource>
{
    private List<Group> groups;
    private boolean isUsingDelaysBetweenActions;


    public ResourcePanel(List<Group> groups, List<Resource> resources, boolean isUsingDelaysBetweenActions)
    {
        super(resources);
        this.groups = groups;
        this.isUsingDelaysBetweenActions = isUsingDelaysBetweenActions;
    }

    @Override
    protected void removeElement(Resource resource, ElementPanel panel)
    {
        super.removeElement(resource, panel);
        resource.removeResource();
    }

    @Override
    protected Resource createNewElement()
    {
        BaseResourceCreationDialog dialog = new ResourceDialog();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        return dialog.getResource();
    }

    @Override
    protected boolean editElement(Resource resource, ElementPanel panel)
    {
        BaseResourceCreationDialog resourceDialog = new ResourceDialog(resource);
        resourceDialog.setVisible(true);
        return resourceDialog.getResource() != null;
    }

    class ResourceDialog extends BaseResourceCreationDialog
    {
        private JComboBox<Group> groupChooser;
        private JComboBox<Role> roleChooser;
        private Group selectedGroup;
        private Role selectedRole;

        public ResourceDialog()
        {
            super(isUsingDelaysBetweenActions);
        }

        public ResourceDialog(Resource resource)
        {
            super(resource, isUsingDelaysBetweenActions);
            selectedGroup = resource.getGroup();
            selectedRole = resource.getRole();
        }

        protected boolean verifyInput()
        {
            boolean isCorrect = true;
            if (selectedGroup == null || selectedRole == null)
            {
                JOptionPane.showMessageDialog(ResourcePanel.this, "Group and role must be specified", "Creation error", JOptionPane.ERROR_MESSAGE);
                isCorrect = false;
            }
            return isCorrect;
        }

        protected JPanel createMainBodyPanel()
        {
            JPanel contentPanel = new JPanel();
            contentPanel.setBackground(new Color(157, 157, 157));
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            groupChooser = new JComboBox<Group>();
            groupChooser.setModel(new ComboBoxModel<Group>()
            {


                @Override
                public Group getSelectedItem()
                {
                    return selectedGroup;
                }

                @Override
                public void setSelectedItem(Object anItem)
                {
                    if (anItem instanceof Group)
                    {
                        selectedGroup = (Group) anItem;
                        roleChooser.setEnabled(true);
                        roleChooser.revalidate();
                        roleChooser.setModel(new RoleChooserModel());
                        selectedRole = null;
                    }
                }

                @Override
                public int getSize()
                {
                    return groups.size();
                }

                @Override
                public Group getElementAt(int index)
                {
                    return groups.get(index);
                }

                @Override
                public void addListDataListener(ListDataListener l)
                {

                }

                @Override
                public void removeListDataListener(ListDataListener l)
                {

                }
            });

            roleChooser = new JComboBox<Role>(new RoleChooserModel());
            roleChooser.setEnabled(resource != null);

            JPanel groupPanel = createPanelWithComboBox("Group: ", groupChooser);
            groupPanel.setBackground(new Color(157, 157, 157));
            JPanel rolePanel = createPanelWithComboBox("Role: ", roleChooser);
            rolePanel.setBackground(new Color(157, 157, 157));

            contentPanel.add(groupPanel);
            contentPanel.add(rolePanel);

            return contentPanel;
        }

        private void setDimensionToStandardComponent(JComponent component)
        {
            setDimensionToComponent(component, new Dimension(110, 25));
        }

        private void setDimensionToComponent(JComponent component, Dimension dimension)
        {
            component.setPreferredSize(dimension);
            component.setMinimumSize(dimension);
            component.setMaximumSize(dimension);
        }

        private JPanel createPanelWithComboBox(String label, JComboBox comboBox)
        {
            JPanel panelWithComboBox = new JPanel();
            panelWithComboBox.setLayout(new BoxLayout(panelWithComboBox, BoxLayout.X_AXIS));
            panelWithComboBox.add(new JLabel(label));
            panelWithComboBox.add(Box.createHorizontalGlue());
            panelWithComboBox.add(comboBox);
            panelWithComboBox.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            setDimensionToStandardComponent(comboBox);
            return panelWithComboBox;
        }

        @Override
        protected void createResource(String resourceName)
        {
            resource = selectedGroup.createResource(resourceName, selectedRole);
        }

        @Override
        protected void modifyResource()
        {
            if (!resource.getRole().equals(selectedRole))
            {
                resource.relocate(selectedGroup, selectedRole);
            }
        }

        class RoleChooserModel implements ComboBoxModel<Role>
        {
            @Override
            public Role getSelectedItem()
            {
                return selectedRole;
            }

            @Override
            public void setSelectedItem(Object anItem)
            {
                if (anItem instanceof Role)
                {
                    selectedRole = (Role) anItem;
                }
            }

            @Override
            public int getSize()
            {
                return selectedGroup == null ? 0 : selectedGroup.getRoles().size();
            }

            @Override
            public Role getElementAt(int index)
            {
                return selectedGroup == null ? null : selectedGroup.getRoles().get(index);
            }

            @Override
            public void addListDataListener(ListDataListener l)
            {

            }

            @Override
            public void removeListDataListener(ListDataListener l)
            {

            }
        }
    }
}


