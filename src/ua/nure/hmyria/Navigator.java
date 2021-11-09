package ua.nure.hmyria;

import jade.core.Agent;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aima.core.environment.wumpusworld.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Navigator extends Agent {
    private EfficientHybridWumpusAgent wumpusAgent;
    private String speleologistMessage;
    private ACLMessage reply;

    final AgentPosition START_POSITION = new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH);

    final HashMap<WumpusAction, String> actionDict = new HashMap<WumpusAction, String>(){{
        put(WumpusAction.FORWARD, "Forward");
        put(WumpusAction.TURN_LEFT, "Turn left");
        put(WumpusAction.TURN_RIGHT, "Turn right");
        put(WumpusAction.GRAB, "Grab");
        put(WumpusAction.SHOOT, "Shoot");
        put(WumpusAction.CLIMB, "Climb");
    }};

    protected void setup() {
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("navigator");
        serviceDescription.setName("navigator");
        agentDescription.addServices(serviceDescription);
        try {
            DFService.register(this, agentDescription);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        wumpusAgent = new EfficientHybridWumpusAgent(4, 4, START_POSITION);
        addBehaviour(new MessageWaitingServer());
    }

    protected void takeDown() {
        System.out.println("Navigator-agent " + getAID().getName() + " terminating.");
    }

    private class MessageWaitingServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                speleologistMessage = msg.getContent();
                reply = msg.createReply();

                addBehaviour(new ActionServer());
            }
            else {
                block();
            }
        }
    }

    private class ActionServer extends OneShotBehaviour {
        public void action() {
            Pattern pattern = Pattern.compile("stench|breeze|glitter|bump|scream", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(speleologistMessage);

            WumpusPercept percept = new WumpusPercept();
            while(matcher.find()) {
                String found = matcher.group().toLowerCase();
                if (found.equals("stench"))
                    percept.setStench();
                if (found.equals("breeze"))
                    percept.setBreeze();
                if (found.equals("glitter"))
                    percept.setGlitter();
                if (found.equals("bump"))
                    percept.setBump();
                if (found.equals("scream"))
                    percept.setScream();
            }

            WumpusAction action = wumpusAgent.act(percept).get();
            if (action != null) {
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(actionDict.get(action));
            }
            else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("navigator couldn`t help agent");
            }
            myAgent.send(reply);

            if (action == WumpusAction.CLIMB) {
                myAgent.doDelete();
            }
        }
    }
}

