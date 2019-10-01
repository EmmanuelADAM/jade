package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * tools to help the use of the JAde platform : <br>
 * - registration to a service, find agents that have declared a service<br>
 * - generetaion of a topic
 * @author revised by Emmanuel ADAM
 * @version 191001, works with Java10
 */
public final class AgentToolsEA {

	/**
	 * create an Agent Description (model of a service)
	 *
	 * @param typeService
	 *            type of the service
	 * @param nameService
	 *            name of the service (can  be null)
	 * @return the model of the service
	 */
	public static DFAgentDescription createAgentDescription(final String typeService, final String nameService) {
		var model = new DFAgentDescription();
		var service = new ServiceDescription();
		service.setType(typeService);
		service.setName(nameService);
		model.addServices(service);
		return model;
	}
	
	/**
	 * register an agent to a service with the Directory Facilitator
	 * 
	 * @param myAgent
	 *            agent that have to be registered
	 * @param typeService
	 *            type of the service
	 * @param nameService
	 *            name of the service (could be null but it is prefered to use a
	 *            name (like the agent name)
	 */
	public static void register(final Agent myAgent, final String typeService, final String nameService) {
		var model = createAgentDescription(typeService, nameService);
		try {
			DFService.register(myAgent, model);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/***
	 * search agents that are registered to a typical service
	 * 
	 * @param myAgent
	 *            agent that asks for the search (it will be omitted from the
	 *            result)
	 * @param typeService
	 *            type of the service
	 * @param nameService
	 *            name of the service (can be null)
	 * @return AIDs of the agents that are registered to (_typeService,
	 *         _nameService), do not include the AID of myAgent
	 */
	public static AID[] searchAgents(final Agent myAgent, final String typeService, final String nameService) {
		var model = createAgentDescription(typeService, nameService);
		int nbOthers = 0;
		AID[] result = null;
		try { 
			var agentsDescription = DFService.search(myAgent, model);
			if (agentsDescription != null) {
				result = new AID[agentsDescription.length];
				for (int i = 0; i < agentsDescription.length; i++) {
					final AID otherAID = agentsDescription[i].getName();
					if (!otherAID.equals(myAgent.getAID())) {
						result[nbOthers++] = otherAID;
					}
				}
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		return result;
	}

		/**create a topic base on a name
	 * @param agent agent that ask for the creation or access to the topic
	 * @param topicName name of the topic
	 * @return AID of the topic*/
	public static AID generateTopicAID(Agent agent, String topicName) {
		AID topic=null;
		TopicManagementHelper topicHelper = null;
		try {
			topicHelper = (TopicManagementHelper) agent.getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic(topicName);
			topicHelper.register(topic);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return topic;
	}
}
