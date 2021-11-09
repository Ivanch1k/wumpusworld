package ua.nure.hmyria;

import aima.core.environment.wumpusworld.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;

public class Speleologist extends Agent {
    protected EfficientHybridWumpusAgent wumpusAgent;
    protected AID environmentAid;
    protected AID navigatorAid;
    protected WumpusPercept percept;
    protected WumpusAction offeredAction;

    final AgentPosition START_POS = new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH);

    final HashMap<String, String[]> feelDict = new HashMap<String, String[]>() {{
        put("stench", new String[] {"I feel stench here", "There is a stench", "It is a strong stench here", "Hey! Is it stecnh here?"});
        put("breeze", new String[] {"I feel breeze here", "There is a breeze", "It is a cool breeze here", "Brr, it is breeze."});
        put("glitter", new String[] {"I see glitter here", "There is a glitter", "It is a glitter here", "Finally! I found glitter"});
        put("bump", new String[] {"I feel bump here", "There is a bump", "It is a bump here", "I think it is a bump"});
        put("scream", new String[] {"I hear scream here", "There is a scream", "It is a loud scream here", "I heard scary scream"});
    }};

    protected void setup() {
        environmentAid = getAgent("environment");
        navigatorAid = getAgent("navigator");

        wumpusAgent = new EfficientHybridWumpusAgent(4, 4, START_POS);
        addBehaviour(new SpeleologistBehaviour(this));
    }

    private AID getAgent(String agentType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentType);
        template.addServices(sd);
        AID agentAid = null;
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length == 0) {
                System.out.println("There are no " + agentType + " found!");
            } else {
                System.out.println("Found the following " + agentType + " agent:");
                agentAid = result[0].getName();
                System.out.println(agentAid.getName());
                return agentAid;
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return agentAid;
    }

    protected void takeDown() {
        System.out.println("Speleologist-agent " + getAID().getName() + " terminating.");
    }
}
