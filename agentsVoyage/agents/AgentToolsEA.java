package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * tools to help the use of the Directory Facilitator (registration to a
 * service, find agents that have declared a service)
 * 
 * @author revised by Emmanuel ADAM
 */
public final class AgentToolsEA {

	private AgentToolsEA() {
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
		final DFAgentDescription model = new DFAgentDescription();
		final ServiceDescription service = new ServiceDescription();
		service.setType(typeService);
		service.setName(nameService);
		model.addServices(service);
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
		final DFAgentDescription model = new DFAgentDescription();
		final ServiceDescription service = new ServiceDescription();
		service.setType(typeService);
		service.setName(nameService);
		model.addServices(service);
		int nbOthers = 0;
		AID[] result = null;
		try { 
			final DFAgentDescription[] agentsDescription = DFService.search(myAgent, model);
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

}
