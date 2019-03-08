//package org.processmining.dialogs.organizational_extension;
//
//import org.processmining.models.organizational_extension.Group;
//import ru.hse.pais.shugurov.widgets.elements.ElementPanel;
//import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
//import ru.hse.pais.shugurov.widgets.panels.EditableListPanel;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.List;
//
///**
// * @author Ivan Shugurov;
// *         Created  04.04.2014
// */
//public class GroupPanel extends EditableListPanel<Group>
//{
//    public GroupPanel(List<Group> existentElements)
//    {
//        super(existentElements);
//    }
//
//    @Override
//    protected Group createNewElement()
//    {
//        GroupDialog dialog = new GroupDialog();
//        dialog.setVisible(true);
//        return dialog.getGeneratedGroup();
//    }
//
//    @Override
//    protected boolean editElement(Group existingGroup, ElementPanel elementPanel)
//    {
//        GroupDialog dialog = new GroupDialog(existingGroup);
//        dialog.setVisible(true);
//        return dialog.getGeneratedGroup() != null;
//    }
//
//
//}
//
//class GroupDialog extends JDialog
//{
//    private Group generatedGroup;
//    private InputTextElement nameInput;
//
//    public GroupDialog()
//    {
//        this(null);
//    }
//
//    public GroupDialog(Group existingGroup)
//    {
//        generatedGroup = existingGroup;
//        JPanel contentPanel = new JPanel();
//        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS
//        ));
//        contentPanel.setBackground(new Color(157, 157, 157));
//        JPanel namePanel = getNamePanel();
//        JPanel buttonsPanel = getButtonsPanel();
//
//        contentPanel.add(namePanel);
//        contentPanel.add(Box.createVerticalStrut(5));
//        contentPanel.add(buttonsPanel);
//        contentPanel.add(Box.createVerticalStrut(5));
//
//        add(contentPanel);
//        setBasicSettings();
//    }
//
//    private JPanel getButtonsPanel()
//    {
//        JPanel buttonsPanel = new JPanel();
//        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
//        buttonsPanel.setBackground(new Color(157, 157, 157));
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                dispose();
//            }
//        });
//        JButton saveButton = new JButton("Save");
//        saveButton.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                String newName = nameInput.getValue();
//                if (newName.isEmpty())
//                {
//                    JOptionPane.showMessageDialog(null, "Incorrect name", "Error", JOptionPane.ERROR_MESSAGE);
//                }
//                else
//                {
//                    if (generatedGroup == null)
//                    {
//                        generatedGroup = new Group(newName);
//                    }
//                    else
//                    {
//                        generatedGroup.setName(newName);
//                    }
//                    dispose();
//                }
//            }
//        });
//        buttonsPanel.add(Box.createHorizontalStrut(3));
//        buttonsPanel.add(saveButton);
//        buttonsPanel.add(Box.createHorizontalStrut(10));
//        if (generatedGroup != null)
//        {
//            JButton deleteButton = new JButton("Delete");
//            deleteButton.addActionListener(new ActionListener()
//            {
//                @Override
//                public void actionPerformed(ActionEvent e)
//                {
//                    generatedGroup = null;
//                    dispose();
//                }
//            });
//            buttonsPanel.add(deleteButton);
//            buttonsPanel.add(Box.createHorizontalStrut(10));
//        }
//        buttonsPanel.add(cancelButton);
//        buttonsPanel.add(Box.createHorizontalStrut(3));
//        return buttonsPanel;
//    }
//
//    private JPanel getNamePanel()
//    {
//        String name;
//        if (generatedGroup == null)
//        {
//            name = "";
//        }
//        else
//        {
//            name = generatedGroup.getName();
//        }
//        nameInput = new InputTextElement("Group name: ", name);
//        return nameInput;
//    }
//
//    private void setBasicSettings()
//    {
//        setTitle("Resource group");
//        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//        setModal(true);
//        setResizable(false);
//        pack();
//        Dimension dialogDimension = getPreferredSize();
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        setLocation((int) (screenSize.getWidth() - dialogDimension.getWidth()) / 2,
//                (int) (screenSize.getHeight() - dialogDimension.getHeight()) / 2);
//    }
//
//    public Group getGeneratedGroup()
//    {
//        return generatedGroup;
//    }
//}
