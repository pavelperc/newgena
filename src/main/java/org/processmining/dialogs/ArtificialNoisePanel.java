package org.processmining.dialogs;

import org.processmining.models.time_driven_behavior.NoiseEvent;
import ru.hse.pais.shugurov.widgets.elements.ElementPanel;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.panels.EditableListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Provides mechanism for creating, editing and removing noise transitions
 *
 * @author Ivan Shugurov
 */
public class ArtificialNoisePanel extends EditableListPanel<NoiseEvent>
{
    private final boolean useTime;

    /**
     * @param existentNoiseEvents collection of noise transitions which already exist
     */
    public ArtificialNoisePanel(List<NoiseEvent> existentNoiseEvents, boolean useTime)
    {
        super(existentNoiseEvents);
        this.useTime = useTime;
    }

    @Override
    protected NoiseEvent createNewElement()
    {
        ArtificialNoiseDialog dialog = new ArtificialNoiseDialog(useTime);
        dialog.setVisible(true);
        return dialog.getNoiseEvent();
    }

    @Override
    protected boolean editElement(NoiseEvent event, ElementPanel elementPanel)
    {
        ArtificialNoiseDialog dialog = new ArtificialNoiseDialog(event, useTime);
        dialog.setVisible(true);
        return dialog.getNoiseEvent() != null;
    }

    private static class ArtificialNoiseDialog extends JDialog //TODO не нравится с булевской переменной useTime
    {
        private final boolean useTime;
        private InputTextElement nameInput;
        private NoiseEvent noiseEvent;
        private InputTextElement timeComponent;
        private InputTextElement deviationComponent;

        private ArtificialNoiseDialog()
        {
            this(true);
        }

        private ArtificialNoiseDialog(boolean useTime)
        {
            this(null, useTime);
        }

        public ArtificialNoiseDialog(NoiseEvent event)
        {
            this(event, true);
        }

        public ArtificialNoiseDialog(NoiseEvent event, boolean useTime)
        {
            this.noiseEvent = event;
            this.useTime = useTime;
            init();
        }

        private void init()
        {
            setTitle("Artificial event");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setModal(true);
            setResizable(false);
            Dimension dialogDimension = new Dimension(200, 150);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((int) (screenSize.getWidth() - dialogDimension.getWidth()) / 2,
                    (int) (screenSize.getHeight() - dialogDimension.getHeight()) / 2);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(new Color(157, 157, 157));
            JPanel buttonsPanel = getButtonsPanel();
            String eventName;
            if (noiseEvent == null)
            {
                eventName = "";
            }
            else
            {
                eventName = noiseEvent.getActivity().toString();
            }
            nameInput = new InputTextElement("Event name: ", eventName);


            contentPanel.add(nameInput);
            contentPanel.add(Box.createVerticalStrut(5));
            JPanel timePanel = getTimePanel();
            contentPanel.add(timePanel);
            contentPanel.add(Box.createVerticalStrut(5));
            contentPanel.add(buttonsPanel);
            contentPanel.add(Box.createVerticalStrut(5));
            add(contentPanel);
            pack();
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
                    String eventName = nameInput.getValue();
                    if (eventName.isEmpty())
                    {
                        JOptionPane.showMessageDialog(null, "Incorrect event name", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    else
                    {
                        if (noiseEvent == null)
                        {
                            noiseEvent = new NoiseEvent(eventName);
                        }
                        else
                        {
                            noiseEvent.setActivity(eventName);
                        }
                        if (useTime)
                        {
                            try
                            {
                                long executionTime = Long.parseLong(timeComponent.getValue());
                                long deviationTime = Long.parseLong(deviationComponent.getValue());
                                if (executionTime > 0 && deviationTime >= 0 && executionTime < deviationTime)
                                {
                                    JOptionPane.showMessageDialog(null, "Incorrect time or deviation", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                else
                                {
                                    noiseEvent.setExecutionTime(executionTime);
                                    noiseEvent.setMaxTimeDeviation(deviationTime);
                                }

                            } catch (NumberFormatException e1)
                            {
                                JOptionPane.showMessageDialog(null, "Incorrect time occurs", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    dispose();
                }
            });
            buttonsPanel.add(Box.createHorizontalGlue());
            buttonsPanel.add(saveButton);
            buttonsPanel.add(Box.createHorizontalStrut(10));
            if (noiseEvent != null)
            {
                JButton deleteButton = new JButton("Delete");
                deleteButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        noiseEvent = null;
                        dispose();
                    }
                });
                buttonsPanel.add(deleteButton);
                buttonsPanel.add(Box.createHorizontalStrut(10));
            }
            buttonsPanel.add(cancelButton);
            buttonsPanel.add(Box.createHorizontalGlue());
            return buttonsPanel;
        }

        public NoiseEvent getNoiseEvent()
        {
            return noiseEvent;
        }

        private JPanel getTimePanel()
        {
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
            long executionTime;
            long deviation;
            if (noiseEvent == null)
            {
                executionTime = NoiseEvent.DEFAULT_EXECUTION_TIME;
                deviation = NoiseEvent.DEFAULT_MAX_DEVIATION_TIME;
            }
            else
            {
                executionTime = noiseEvent.getExecutionTime();
                deviation = noiseEvent.getMaxTimeDeviation();
            }
            if (useTime)
            {
                timeComponent = new InputTextElement("Transition time: ", Long.toString(executionTime));
                deviationComponent = new InputTextElement("Max deviation: ", Long.toString(deviation));
                timePanel.add(timeComponent);
                timePanel.add(deviationComponent);
            }
            return timePanel;
        }
    }
}
