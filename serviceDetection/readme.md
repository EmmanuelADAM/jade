# Jade : Agents

## Detection of Registration to a Service

### Jade Agent-Oriented Programming Course Materials

---

- [IncomingAgent](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/agents/IncomingAgent.java) : class
  for an agent who, after a certain period of time, registers for a service. The agent displays the messages it
  receives. Closing the window kills the agent who unsubscribes from his service.
- [ScribeAgent](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/agents/ScribeAgent.java) : 
  class for an agent that subscribe to a service proposed by the Yello Pages to receive information about 
  registry/deregistry of agents to a given service. At each arrival and departure, the agent transmits the names of 
  the members of the group.
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/english/serviceDetection/launch/LaunchAgents.java) : **main
  class**, launch Jade and create the agents

- Initially, 10 agents are launched; one scribe and 9 incoming agents.


