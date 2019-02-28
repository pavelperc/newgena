package org.processmining.dialogs.organizational_extension;

import kotlin.ULong;
import kotlin.ULongKt;
import org.processmining.models.organizational_extension.Resource;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Ivan Shugurov on 09.09.2014.
 */
public abstract class BaseResourceCreationDialog extends JDialog   //TODO выполнять проверку времени
{
    private static final int DEFAULT_MARGIN = 5;
    protected Resource resource;
    private InputTextElement nameInput;
    private boolean isUsingDelaysBetweenActions;
    private InputTextElement minDelayBetweenActionsInput;
    private InputTextElement maxDelayBetweenActionsInput;

    public BaseResourceCreationDialog(boolean isUsingDelaysBetweenActions)
    {
        this(null, isUsingDelaysBetweenActions);
    }

    public BaseResourceCreationDialog(Resource resource, boolean isUsingDelaysBetweenActions)
    {
        this.resource = resource;
        this.isUsingDelaysBetweenActions = isUsingDelaysBetweenActions;
        init();
    }

    private void init()
    {
        if (resource == null)
        {
            setTitle("Create resource");
        }
        else
        {
            setTitle("Modify resource");
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        Dimension dialogDimension = new Dimension(200, 150);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screenSize.getWidth() - dialogDimension.getWidth()) / 2,
                (int) (screenSize.getHeight() - dialogDimension.getHeight()) / 2);
        JPanel contentPanel = createContentPanel();
        add(contentPanel);
        pack();
    }

    private JPanel createContentPanel()
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(157, 157, 157));

        JPanel namePanel = createNamePanel();
        JPanel mainBodyPanel = createMainBodyPanel();

        contentPanel.add(namePanel);
        contentPanel.add(Box.createVerticalStrut(DEFAULT_MARGIN));

        if (isUsingDelaysBetweenActions)
        {
            long minDelay;
            long maxDelay;
            if (resource == null)
            {
                minDelay = Resource.Companion.getDEFAULT_MIN_DELAY_BETWEEN_ACTIONS();
                maxDelay = Resource.Companion.getDEFAULT_MAX_DELAY_BETWEEN_ACTIONS();
            }
            else
            {
                minDelay = resource.getMinDelayBetweenActions();
                maxDelay = resource.getMaxDelayBetweenActions();
            }
            minDelayBetweenActionsInput = new InputTextElement("Min delay between actions", Long.toString(minDelay));
            maxDelayBetweenActionsInput = new InputTextElement("Max delay between actions", Long.toString(maxDelay));

            contentPanel.add(minDelayBetweenActionsInput);
            contentPanel.add(maxDelayBetweenActionsInput);
            contentPanel.add(Box.createVerticalStrut(DEFAULT_MARGIN));
        }

        if (mainBodyPanel != null)
        {
            contentPanel.add(Box.createVerticalStrut(DEFAULT_MARGIN));
            contentPanel.add(mainBodyPanel);
            contentPanel.add(Box.createVerticalStrut(DEFAULT_MARGIN));
        }


        JPanel buttonsPanel = createButtonsPanel();
        contentPanel.add(buttonsPanel);
        add(contentPanel);
        return contentPanel;
    }

    protected abstract boolean verifyInput();

    private JPanel createButtonsPanel()
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
                saveButtonPressed();
            }
        });
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(saveButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        if (resource != null)
        {
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    deleteButtonPressed();
                }
            });
            buttonsPanel.add(deleteButton);
            buttonsPanel.add(Box.createHorizontalStrut(10));
        }
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        return buttonsPanel;
    }

    protected void deleteButtonPressed()
    {
        resource = null;
        dispose();
    }

    private void saveButtonPressed()
    {
        String resourceName = nameInput.getValue();
        if (resourceName.isEmpty())
        {
            JOptionPane.showMessageDialog(BaseResourceCreationDialog.this, "Resource name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        long minDelayBetweenActions = Resource.Companion.getDEFAULT_MIN_DELAY_BETWEEN_ACTIONS();
        long maxDelayBetweenActions = Resource.Companion.getDEFAULT_MAX_DELAY_BETWEEN_ACTIONS();
        if (minDelayBetweenActionsInput != null && maxDelayBetweenActionsInput != null)
        {

            try
            {
                minDelayBetweenActions = Long.parseLong(minDelayBetweenActionsInput.getValue());
                if (minDelayBetweenActions < 0)
                {
                    JOptionPane.showMessageDialog(null, "Min delay cannot be negative");
                    return;
                }
            } catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(null, "Min delay is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
            try
            {
                maxDelayBetweenActions = Long.parseLong(maxDelayBetweenActionsInput.getValue());
                if (maxDelayBetweenActions < 0)
                {
                    JOptionPane.showMessageDialog(null, "Max delay cannot be negative", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(null, "Max delay is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
        }
        if (minDelayBetweenActions > maxDelayBetweenActions)
        {
            JOptionPane.showMessageDialog(null, "Min delay must be less or equal to max delay", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (verifyInput())
        {
            if (resource == null)
            {
                createResource(resourceName);
                resource.setDelayBetweenActions(minDelayBetweenActions, maxDelayBetweenActions);
            }
            else
            {
                resource.setName(resourceName);
                resource.setDelayBetweenActions(minDelayBetweenActions, maxDelayBetweenActions);
                modifyResource();
            }
            dispose();
        }
    }

    protected abstract void createResource(String resourceName);

    protected void modifyResource()
    {
    }

    ;

    protected JPanel createMainBodyPanel()
    {
        return null;
    }

    private JPanel createNamePanel()
    {
        if (resource == null)
        {
            nameInput = new InputTextElement("Name: ", "");
        }
        else
        {
            nameInput = new InputTextElement("Name: ", resource.getName());
        }
        return nameInput;
    }

    public Resource getResource()
    {
        return resource;
    }
}
