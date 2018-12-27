package org.processmining.dialogs.organizational_extension;

import org.processmining.models.organizational_extension.Group;
import org.processmining.models.organizational_extension.Role;
import ru.hse.pais.shugurov.widgets.elements.ElementPanel;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.panels.EditableListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Ivan Shugurov;
 *         Created  04.04.2014
 */
public class RolePanel extends EditableListPanel<Role>
{
    private Group group;

    public RolePanel(Group group)
    {
        super(new ArrayList<Role>(group.getRoles()));
        this.group = group;
    }

    @Override
    protected void removeElement(Role role, ElementPanel panel)
    {
        super.removeElement(role, panel);
        group.removeRole(role);
    }

    @Override
    protected Role createNewElement()
    {
        RoleDialog dialog = new RoleDialog(group);
        dialog.setVisible(true);
        return dialog.getGeneratedRole();
    }

    @Override
    protected boolean editElement(Role role, ElementPanel elementPanel)
    {
        RoleDialog dialog = new RoleDialog(group, role);
        dialog.setVisible(true);
        return dialog.getGeneratedRole() != null;
    }
}

class RoleDialog extends JDialog
{
    private Role generatedRole;
    private Group group;
    private InputTextElement nameInput;

    RoleDialog(Group group)
    {
        this(group, null);
    }

    RoleDialog(Group group, Role existingRole)
    {
        generatedRole = existingRole;
        this.group = group;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS
        ));
        contentPanel.setBackground(new Color(157, 157, 157));
        JPanel namePanel = getNamePanel();
        JPanel buttonsPanel = getButtonsPanel();

        contentPanel.add(namePanel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalStrut(5));

        add(contentPanel);
        setBasicSettings();
    }

    private JPanel getButtonsPanel()
    {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBackground(new Color(157, 157, 157));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String newName = nameInput.getValue();
                if (newName.isEmpty())
                {
                    JOptionPane.showMessageDialog(null, "Incorrect name", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    if (generatedRole == null)
                    {
                        generatedRole = group.createRole(newName);
                    }
                    else
                    {
                        generatedRole.setName(newName);
                    }
                    dispose();
                }
            }
        });
        buttonsPanel.add(Box.createHorizontalStrut(3));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        if (generatedRole != null)
        {
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    generatedRole = null;
                    dispose();
                }
            });
            buttonsPanel.add(deleteButton);
            buttonsPanel.add(Box.createHorizontalStrut(10));
        }
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(3));
        return buttonsPanel;
    }

    private JPanel getNamePanel()
    {
        String name;
        if (generatedRole == null)
        {
            name = "";
        }
        else
        {
            name = generatedRole.getName();
        }
        nameInput = new InputTextElement("Role name: ", name);
        return nameInput;
    }

    public Role getGeneratedRole()
    {
        return generatedRole;
    }

    private void setBasicSettings()
    {
        setTitle("Resource group");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        pack();
        Dimension dialogDimension = getPreferredSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screenSize.getWidth() - dialogDimension.getWidth()) / 2,
                (int) (screenSize.getHeight() - dialogDimension.getHeight()) / 2);
    }
}
