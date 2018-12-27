package org.processmining.dialogs;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.processmining.models.GenerationDescription;
import org.processmining.models.descriptions.ModifiedBPMNGenerationDescription;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
import ru.hse.pais.shugurov.widgets.panels.EmptyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 26.12.2014.
 */
public class BPMNGraphSettingsListener implements GraphSelectionListener
{
    private final ProMJGraph graph;
    private final BPMNDiagram diagram;
    private final ModifiedBPMNGenerationDescription description;
    private Object lastClickedCell = null;

    public BPMNGraphSettingsListener(ModifiedBPMNGenerationDescription description, ProMJGraph graph, BPMNDiagram diagram)
    {
        checkConstructorParameters(description, graph, diagram);
        this.graph = graph;
        this.diagram = diagram;
        this.description = description;
    }

    private void checkConstructorParameters(GenerationDescription description, ProMJGraph graph, BPMNDiagram diagram) throws NullPointerException
    {
        if (description == null)
        {
            throw new NullPointerException("Description cannot be equal to null");
        }

        if (graph == null)
        {
            throw new NullPointerException("PromJGraph cannot be equal to null");
        }

        if (diagram == null)
        {
            throw new NullPointerException("BPMN diagram cannot be equal to null");
        }

    }

    @Override
    public void valueChanged(GraphSelectionEvent event)
    {
        if (event.isAddedCell() && event.getCell() instanceof ProMGraphCell)
        {
            if (event.getCell() == lastClickedCell)
            {
                graph.stopEditing();
                lastClickedCell = null;
            }
            else
            {
                lastClickedCell = event.getCell();
                return;
            }
            DirectedGraphNode node = ((ProMGraphCell) event.getCell()).getNode();
            if (node instanceof Gateway)
            {
                if (((Gateway) node).getGatewayType() == Gateway.GatewayType.DATABASED)
                {
                    Collection<BPMNEdge<?, ?>> outputFlows = diagram.getOutEdges(node);
                    if (outputFlows.size() > 1)
                    {
                        graph.stopEditing();
                        GatewayDialog dialog = new GatewayDialog((Gateway) node);
                        dialog.setVisible(true);
                        graph.clearSelection();
                    }
                }
            }
        }
    }

    private class GatewayDialog extends JDialog
    {
        private final Gateway gateway;
        private JPanel extraPanel;
        private Map<Flow, InputTextElement> targetNodesToTextInputs;
        private Map<Flow, String> edgeToOldLabel = null;

        private GatewayDialog(final Gateway gateway)
        {
            this.gateway = gateway;

            setModal(true);
            setTitle("Gateway settings");

            initMainContent();

            JPanel buttonsPanel = getButtonsPanel();
            add(buttonsPanel, BorderLayout.SOUTH);

            pack();
            setResizable(false);

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Dimension dialogSize = getPreferredSize();
            setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);

            addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    returnInitialLabelsOfEdges();
                }

                @Override
                public void windowClosed(WindowEvent e)
                {
                    returnInitialLabelsOfEdges();
                }
            });
        }

        private void initMainContent()
        {
            if (extraPanel != null)
            {
                remove(extraPanel);
            }
            extraPanel = new EmptyPanel();
            extraPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            add(extraPanel, BorderLayout.CENTER);

            @SuppressWarnings("unchecked")
            Collection<Flow> outEdges = (Collection) diagram.getOutEdges(gateway);

            int edgeNumber = 1;
            edgeToOldLabel = new HashMap<Flow, String>();

            targetNodesToTextInputs = new HashMap<Flow, InputTextElement>();

            Map<Flow, Integer> preferences = description.getPreferences(gateway);

            Dimension inputElementDimension = new Dimension(250, 50);

            for (Flow flow : outEdges)
            {
                AttributeMap attributeMap = flow.getAttributeMap();
                String oldLabel = (String) attributeMap.get(AttributeMap.LABEL);
                edgeToOldLabel.put(flow, oldLabel);

                String newLabel = "Flow " + edgeNumber;
                attributeMap.put(AttributeMap.LABEL, newLabel);
                edgeNumber++;
                graph.update(flow);

                int preference = preferences.get(flow);

                InputTextElement inputPreferenceElement = new InputTextElement(newLabel, Integer.toString(preference));
                inputPreferenceElement.setSize(inputElementDimension);
                inputPreferenceElement.setPreferredSize(inputElementDimension);
                inputPreferenceElement.setMinimumSize(inputElementDimension);
                inputPreferenceElement.setMaximumSize(inputElementDimension);

                targetNodesToTextInputs.put(flow, inputPreferenceElement);
                extraPanel.add(inputPreferenceElement);
            }

            int height = inputElementDimension.height * outEdges.size();

            Dimension panelDimension = new Dimension(inputElementDimension.width - 7, height - 7);

            extraPanel.setPreferredSize(panelDimension);
            extraPanel.setSize(panelDimension);
            extraPanel.setMinimumSize(panelDimension);
            extraPanel.setMaximumSize(panelDimension);

            graph.revalidate();
        }


        private JPanel getButtonsPanel()
        {
            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
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
                    Map<Flow, Integer> preferences = description.getPreferences(gateway);

                    for (Map.Entry<Flow, InputTextElement> entry : targetNodesToTextInputs.entrySet())
                    {
                        InputTextElement element = entry.getValue();

                        String preferenceString = element.getValue();

                        try
                        {
                            int preference = Integer.parseInt(preferenceString);

                            if (preference < ModifiedBPMNGenerationDescription.MIN_PREFERENCE)
                            {
                                JOptionPane.showMessageDialog(null, "Preference cannot be less than " + ModifiedBPMNGenerationDescription.MIN_PREFERENCE);
                                return;
                            }

                            if (preference > ModifiedBPMNGenerationDescription.MAX_PREFERENCE)
                            {
                                JOptionPane.showMessageDialog(null, "Preference cannot be more than " + ModifiedBPMNGenerationDescription.MAX_PREFERENCE);
                                return;
                            }

                            Flow flow = entry.getKey();
                            preferences.put(flow, preference);

                        } catch (NumberFormatException e1)
                        {
                            JOptionPane.showMessageDialog(null, "Incorrect preference");
                            return;
                        }
                    }

                    dispose();
                }
            });
            buttonsPanel.add(Box.createHorizontalGlue());
            buttonsPanel.add(saveButton);
            buttonsPanel.add(Box.createHorizontalStrut(10));
            buttonsPanel.add(cancelButton);
            buttonsPanel.add(Box.createHorizontalGlue());
            return buttonsPanel;
        }

        private void returnInitialLabelsOfEdges()
        {
            for (Map.Entry<Flow, String> entry : edgeToOldLabel.entrySet())
            {
                BPMNEdge edge = entry.getKey();
                String oldLabel = entry.getValue();
                AttributeMap attributeMap = edge.getAttributeMap();
                attributeMap.put(AttributeMap.LABEL, oldLabel);
            }
        }
    }
}
