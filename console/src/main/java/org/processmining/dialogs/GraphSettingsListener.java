//package org.processmining.dialogs;
//
//import org.jgraph.event.GraphSelectionEvent;
//import org.jgraph.event.GraphSelectionListener;
//import org.processmining.framework.util.Pair;
//import org.processmining.models.descriptions.TimeDrivenGenerationDescription;
//import org.processmining.models.graphbased.directed.DirectedGraphNode;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.models.jgraph.ProMJGraph;
//import org.processmining.models.jgraph.elements.ProMGraphCell;
//import ru.hse.pais.shugurov.widgets.elements.InputTextElement;
//
//import javax.swing.*;
//import java.util.Map;
//
///**
// * @author Ivan Shugurov
// *         Created  18.05.2014
// */
//public class GraphSettingsListener implements GraphSelectionListener
//{
//    private Object lastClickedCell;
//    private Map<Transition, Pair<Long, Long>> transitionTimes;
//    private ProMJGraph graph;
//
//
//    public GraphSettingsListener(TimeDrivenGenerationDescription description, ProMJGraph graph)
//    {
//        transitionTimes = description.getTime();
//        this.graph = graph;
//    }
//
//    @Override
//    public void valueChanged(GraphSelectionEvent event)
//    {
//        if (event.isAddedCell() && event.getCell() instanceof ProMGraphCell)
//        {
//            if (event.getCell().equals(lastClickedCell))
//            {
//                graph.stopEditing();
//                lastClickedCell = null;
//            }
//            else
//            {
//                lastClickedCell = event.getCell();
//                return;
//            }
//            DirectedGraphNode node = ((ProMGraphCell) event.getCell()).getNode();
//            if (node instanceof Transition)
//            {
//                graph.stopEditing();
//                TransitionTimeDialog dialog = new TransitionTimeDialog((Transition) node);
//                dialog.setVisible(true);
//                graph.clearSelection();
//            }
//        }
//    }
//
//    private class TransitionTimeDialog extends ProMStyleDialog
//    {
//        private InputTextElement timeComponent;
//        private InputTextElement deviationComponent;
//        private long time;
//        private long deviation;
//
//        private TransitionTimeDialog(Transition transition)
//        {
//            super(transition, "Transition settings");
//        }
//
//        @Override
//        protected JPanel getMainPanel()
//        {
//            JPanel timePanel = new JPanel();
//            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
//            Pair<Long, Long> timeDeviationPair = transitionTimes.get(getNode());
//            timeComponent = new InputTextElement("Transition time: ", Long.toString(timeDeviationPair.getFirst()));
//            deviationComponent = new InputTextElement("Max deviation: ", Long.toString(timeDeviationPair.getSecond()));
//            timePanel.add(timeComponent);
//            timePanel.add(deviationComponent);
//            return timePanel;
//        }
//
//
//        @Override
//        protected Transition getNode()
//        {
//            return (Transition) super.getNode();
//        }
//
//        @Override
//        protected boolean verify()
//        {
//            boolean isExternalStateCorrect = super.verify();
//            boolean isCorrect = true;
//            try
//            {
//                time = Long.parseLong(timeComponent.getValue());
//                deviation = Long.parseLong(deviationComponent.getValue());
//            } catch (NumberFormatException e)
//            {
//                isCorrect = false;
//            }
//            isCorrect = isCorrect && time > 0 && deviation >= 0 && time > deviation;
//            if (!isCorrect)
//            {
//                JOptionPane.showMessageDialog(null, "Incorrect time or deviation", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//            return isExternalStateCorrect && isCorrect;
//        }
//
//        @Override
//        protected void save()
//        {
//            super.save();
//            Pair<Long, Long> timeDeviationPair = new Pair<Long, Long>(time, deviation);
//            transitionTimes.put(getNode(), timeDeviationPair);
//        }
//    }
//}
