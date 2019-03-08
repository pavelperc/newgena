package org.processmining.dialogs;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ivan on 12.10.2015.
 */
public abstract class ProMStyleDialog extends JDialog
{
    private AbstractDirectedGraphNode node;
    private InputTextElement nameInput;

    public ProMStyleDialog(AbstractDirectedGraphNode node, String title)
    {
        this.node = node;
        init(title);
        initState();
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(157, 157, 157));
        nameInput = new InputTextElement("Name: ", node.getLabel());
        JPanel mainPanel = getMainPanel();
        JPanel buttonsPanel = getButtonsPanel();

        contentPanel.add(nameInput);
        if (mainPanel != null)
        {
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(mainPanel);
        }
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalStrut(5));
        add(contentPanel);
        pack();
    }

    private void init(String title)
    {
        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        Dimension dialogDimension = new Dimension(200, 150);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screenSize.getWidth() - dialogDimension.getWidth()) / 2,
                (int) (screenSize.getHeight() - dialogDimension.getHeight()) / 2);
    }

    protected void initState()
    {
    }

    protected AbstractDirectedGraphNode getNode()
    {
        return node;
    }

    /**
     * @return whether input is correct or not.
     */
    protected boolean verify()
    {
        boolean isInputCorrect = !nameInput.getValue().isEmpty();
        if (!isInputCorrect)
        {
            JOptionPane.showMessageDialog(null, "Incorrect name", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return isInputCorrect;
    }

    protected void save()
    {
        node.getAttributeMap().put(AttributeMap.LABEL, nameInput.getValue());
    }

    protected abstract JPanel getMainPanel();

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
                boolean isInputCorrect = verify();
                if (isInputCorrect)
                {
                    save();
                    dispose();
                }
            }
        });
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(saveButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        return buttonsPanel;
    }
}
