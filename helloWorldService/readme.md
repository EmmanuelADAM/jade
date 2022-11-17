# Jade : Agents

## Exemple d'inscription à un service et d'utilisation des Pages Jaunes

---

- [HelloAgent](https://github.com/EmmanuelADAM/jade/blob/english/HelloWorldService/agents/HelloAgent.java) : is a 
  class of agent linked to a grapichal window
    - The agent registers in the yellow pages in the "cordiality" type service, and randomly in the "receptiondesk" or 
      "lobby" sub-service
    - the agent can send a message to all the agent in the service "receptiondesk" or "lobby"
        - To do this, it asks the yellow pages for the list of agents in a service.
    - The agent listens and displays the message sent to it
  - [SimpleGui4Agent](https://github.com/EmmanuelADAM/jade/blob/english/HelloWorldService/gui/SimpleGui4Agent.java) : a 
    small java swing window for a *GuiAgent*.
- [LaunchAgents](https://https://github.com/EmmanuelADAM/jade/blob/english/helloWorldService/launch/LaunchAgents.java)
  : **main class**, launch Jade et creates the agents

- At launch, 10 agents are launched, the number is not limited except by the capacity of the machine. 
