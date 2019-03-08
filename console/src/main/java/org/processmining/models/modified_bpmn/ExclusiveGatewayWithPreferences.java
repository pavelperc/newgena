package org.processmining.models.modified_bpmn;

import org.deckfour.xes.model.XTrace;
import org.processmining.models.MovementResult;
import org.processmining.models.abstract_net_representation.Token;
import org.processmining.models.base_bpmn.*;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan Shugurov on 30.12.2014.
 */
public class ExclusiveGatewayWithPreferences extends ExclusiveChoiceGateway
{
    private final Map<? extends SequenceFlow, Integer> preferences;

    protected ExclusiveGatewayWithPreferences(
            Gateway actualGateway, SubProcess parentSubProcess,
            List<SimpleSequenceFlow> inputSequenceFlows,
            List<SimpleSequenceFlow> outputSequenceFlows,
            Map<SimpleSequenceFlow, Integer> preferences)
    {
        super(actualGateway, parentSubProcess, inputSequenceFlows, outputSequenceFlows);
        this.preferences = preferences;
    }

    @Override
    public MovementResult move(XTrace trace)
    {
        MovementResult movementResult = new MovementResult();
        consumeToken(movementResult);

        SimpleSequenceFlow selectedOutFlow = selectFlow();

        if (!selectedOutFlow.hasTokens())
        {
            movementResult.addFilledTokenables(selectedOutFlow);
        }

        selectedOutFlow.addToken(new Token());

        return movementResult;
    }

    private SimpleSequenceFlow selectFlow()  //TODO протестировать на разных примерах
    {
        List<SimpleSequenceFlow> outFlows = getOutputSequenceFlows();

        if (outFlows.size() < 2)
        {
            return outFlows.get(0);
        }
        else
        {
            int[] preferencesArray = formPreferencesArray(outFlows);

            int number = random.nextInt(preferencesArray[preferencesArray.length - 1]) + 1;

            for (int i = 0; i < preferencesArray.length; i++)
            {
                if (number <= preferencesArray[i])
                {
                    return outFlows.get(i);
                }
            }

        }

        throw new IllegalStateException();
    }

    private int[] formPreferencesArray(List<? extends SequenceFlow> outFlows)
    {
        int[] preferencesArray = new int[outFlows.size()];

        for (int i = 0; i < preferencesArray.length; i++)
        {
            SequenceFlow flow = outFlows.get(i);
            int preference = preferences.get(flow);

            if (i == 0)
            {
                preferencesArray[i] = preference;
            }
            else
            {
                preferencesArray[i] = preferencesArray[i - 1] + preference;
            }
        }

        return preferencesArray;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SimpleSequenceFlow> getOutputSequenceFlows()
    {
        return (List)super.getOutputSequenceFlows();
    }

    public static class ExclusiveGatewayWithPreferencesBuilder extends AbstractNodeBuilder<ExclusiveGatewayWithPreferences, Gateway, SimpleSequenceFlow, SimpleMessageFlow>
    {
        private Map<SimpleSequenceFlow, Integer> preferences = new HashMap<SimpleSequenceFlow, Integer>();

        public ExclusiveGatewayWithPreferencesBuilder(Gateway gateway)
        {
            super(gateway);

            if (gateway == null)
            {
                throw new NullPointerException("Gateway cannot be equal to null");
            }
        }

        public ExclusiveGatewayWithPreferencesBuilder outputFlow(SimpleSequenceFlow flow, int preference)
        {
            super.outputFlow(flow);

            preferences.put(flow, preference);

            return this;

        }

        @Override
        public void outputFlow(SimpleSequenceFlow flow)
        {
            throw new IllegalStateException("This method is not available");
        }

        @Override
        public ExclusiveGatewayWithPreferences build()
        {
            return new ExclusiveGatewayWithPreferences(actualNode, (SubProcess) parentSubProcess, inputSequenceFlows,
                    outputSequenceFlows, preferences);
        }

        @Override
        public void incomingMessageFlow(SimpleMessageFlow inputFlow)
        {
            throw new IllegalStateException("Message flows are not allowed for gateways");
        }

        @Override
        public void outgoingMessageFlow(SimpleMessageFlow outputFlow)
        {
            throw new IllegalStateException("Message flows are not allowed for gateways");
        }
    }
}
