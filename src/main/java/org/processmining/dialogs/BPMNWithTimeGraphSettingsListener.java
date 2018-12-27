package org.processmining.dialogs;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.processmining.models.descriptions.BPMNWithTimeGenerationDescription;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.utils.distribution.ConfiguredLongDistribution;
import org.processmining.utils.distribution.UniformDistribution;
import ru.hse.pais.shugurov.widgets.TypicalColors;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

/**
 * Created by Ivan on 11.08.2015.
 */
public class BPMNWithTimeGraphSettingsListener implements GraphSelectionListener
{
    private Object selectedItem;
    private ProMJGraph graph;
    private BPMNWithTimeGenerationDescription description;

    public BPMNWithTimeGraphSettingsListener(BPMNWithTimeGenerationDescription description, ProMJGraph graph)
    {
        this.description = description;
        this.graph = graph;
    }

    @Override
    public void valueChanged(GraphSelectionEvent event)
    {
        if (event.isAddedCell() && event.getCell() instanceof ProMGraphCell)
        {

            if (selectedItem == null)
            {
                selectedItem = event.getCell();
            }
            else
            {
                if (selectedItem == event.getCell())
                {
                    graph.stopEditing();
                    selectedItem = null;

                    DirectedGraphNode node = ((ProMGraphCell) event.getCell()).getNode();

                    if (node instanceof Activity && !(node instanceof SubProcess))
                    {
                        JDialog dialog = new ActivityTimeDialog((Activity) node);
                        dialog.setVisible(true);
                    }

                }
                else
                {
                    selectedItem = event.getCell();
                }
            }
        }
    }

    private class ActivityTimeDialog extends ProMStyleDialog
    {
        private InputTextElement minTimeComponent;
        private InputTextElement maxTimeComponent;
        private InputTextElement scriptInputComponent;
        private JPanel scriptPanel;
        private JRadioButton uniformDistribution;
        private JRadioButton script;
        private long minTime;
        private long maxTime;

        private ActivityTimeDialog(Activity activity)
        {
            super(activity, "Activity settings");
        }

        @Override
        protected JPanel getMainPanel()
        {
            final JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
            final ConfiguredLongDistribution distribution = description.getExecutionTimeDistribution(getNode());

            final String path;

            if (description.getTimeScriptPath(getNode()) == null)
            {
                path = "";
            }
            else
            {
                path = description.getTimeScriptPath(getNode());
            }

            boolean usingScript = description.isUsingTimeScript(getNode());

            uniformDistribution = new JRadioButton("Uniform");
            uniformDistribution.setSelected(!usingScript);
            uniformDistribution.setBackground(TypicalColors.LIGHT_GRAY);

            script = new JRadioButton("Script");
            script.setBackground(TypicalColors.LIGHT_GRAY);

            uniformDistribution.setForeground(TypicalColors.TEXT_COLOR);
            script.setForeground(TypicalColors.TEXT_COLOR);
            script.setSelected(usingScript);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(uniformDistribution);
            buttonGroup.add(script);

            JPanel horizontalPanel = new JPanel();
            horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));

            horizontalPanel.add(Box.createHorizontalGlue());
            horizontalPanel.add(uniformDistribution);
            horizontalPanel.add(Box.createHorizontalStrut(20));
            horizontalPanel.add(script);
            horizontalPanel.add(Box.createHorizontalGlue());
            horizontalPanel.setBackground(TypicalColors.LIGHT_GRAY);

            timePanel.add(horizontalPanel);

            if (usingScript)
            {
                addScriptPathPanel(path, timePanel);
            }
            else
            {
                addTimeComponents(timePanel, distribution);
            }

            uniformDistribution.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    timePanel.remove(scriptPanel);

                    if (minTimeComponent == null)
                    {
                        addTimeComponents(timePanel, distribution);
                    }
                    else
                    {
                        timePanel.add(minTimeComponent);
                        timePanel.add(maxTimeComponent);
                    }
                    timePanel.revalidate();
                }
            });

            script.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    timePanel.remove(minTimeComponent);
                    timePanel.remove(maxTimeComponent);

                    addScriptPathPanel(path, timePanel);
                }
            });

            return timePanel;
        }

        private void addTimeComponents(JPanel timePanel, ConfiguredLongDistribution distribution)
        {
            minTimeComponent = new InputTextElement("Min execution time: ", Long.toString(distribution.getMin()));
            maxTimeComponent = new InputTextElement("Max execution time: ", Long.toString(distribution.getMax()));

            timePanel.add(minTimeComponent);
            timePanel.add(maxTimeComponent);
        }

        private void addScriptPathPanel(String path, JPanel timePanel)
        {
            if (scriptPanel == null)
            {
                scriptPanel = new JPanel();
                scriptPanel.setLayout(new BoxLayout(scriptPanel, BoxLayout.Y_AXIS));

                JPanel innerPanel = new JPanel();
                innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
                innerPanel.setBackground(TypicalColors.LIGHT_GRAY);

                scriptPanel.add(innerPanel);

                scriptPanel.setBackground(TypicalColors.LIGHT_GRAY);

                description.getTimeScriptPath(getNode());

                scriptInputComponent = new InputTextElement("Script", path,
                        new Dimension(300, 50));
                scriptInputComponent.setTextFieldSize(new Dimension(400, 50));
                innerPanel.add(scriptInputComponent);

                JButton fileButton = new JButton("choose");
                innerPanel.add(fileButton);

                fileButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        String currentPath = scriptInputComponent.getValue();

                        if (currentPath.isEmpty())
                        {
                            currentPath = Paths.get("").toAbsolutePath().toString();
                        }

                        JFileChooser fileChooser = new JFileChooser(currentPath);
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Python file", "py");
                        fileChooser.setFileFilter(filter);

                        int returnValue = fileChooser.showOpenDialog(null);

                        if (returnValue == JFileChooser.APPROVE_OPTION)
                        {
                            String path = fileChooser.getSelectedFile().getAbsolutePath();
                            scriptInputComponent.setValue(path);
                        }
                    }
                });

                scriptPanel.add(Box.createVerticalStrut(53));
            }

            timePanel.add(scriptPanel);

            timePanel.revalidate();
        }


        @Override
        protected Activity getNode()
        {
            return (Activity) super.getNode();
        }

        @Override
        protected boolean verify()
        {
            boolean isExternalStateCorrect = super.verify();
            boolean isCorrect = true;

            if (uniformDistribution.isSelected())
            {
                try
                {
                    minTime = Long.parseLong(minTimeComponent.getValue());
                    maxTime = Long.parseLong(maxTimeComponent.getValue());
                } catch (NumberFormatException e)
                {
                    isCorrect = false;
                }

                isCorrect = isCorrect && minTime >= 0 && maxTime >= minTime;

                if (!isCorrect)
                {
                    JOptionPane.showMessageDialog(null, "Incorrect time", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                if (script.isSelected())
                {
                    String path = scriptInputComponent.getValue();

                    isCorrect = !path.isEmpty();

                    if (!isCorrect)
                    {
                        JOptionPane.showMessageDialog(null, "Incorrect path", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            return isExternalStateCorrect && isCorrect;
        }

        @Override
        protected void save()
        {
            super.save();
            UniformDistribution distribution = new UniformDistribution();

            if (uniformDistribution.isSelected())
            {
                ConfiguredLongDistribution configuredDistribution = new ConfiguredLongDistribution(distribution, minTime, maxTime);
                description.setExecutionTimeDistribution(getNode(), configuredDistribution);
                description.setUsingTimeScript(getNode(), false);
            }
            else
            {
                String path = scriptInputComponent.getValue();
                description.setTimeScriptPath(getNode(), path);
                description.setUsingTimeScript(getNode(), true);
            }
        }
    }
}
