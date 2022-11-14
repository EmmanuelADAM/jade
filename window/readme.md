# Jade : Agents & Windows

## Basic example of direct communication via a dialog window.

---

Jade Agent-Oriented Programming Course Materials

The agents here are of the `AgentWindowed` type, they have a simple window (`SimpleWindow4Agent`) to post messages to.
 - This window also has 1 button, which can be activated.
 - A click on the button posts an event of type `GuiEvent`  (`SimpleWindow4Agent.OK_EVENT` (= à 1)) to the agent
 - Closing the window posts an event of type `GuiEvent` (`SimpleWindow4Agent.QUIT_EVENT` (= à -1)) to the agent
 - By default, the event reaction function `protected void onGuiEvent(GuiEvent ev)` kills the agent on window closing


- [SenderAgentWithWindow](https://github.com/EmmanuelADAM/jade/blob/english/window/agents/SenderAgentWithWindow.java) :
  agent of type  *AgentWindowed*
    - in reaction to the button click, it sends a simple text message to 3 agents "b", "c" & "d"
- [ReceiverAgentWithWindow](https://github.com/EmmanuelADAM/jade/blob/english/window/agents/ReceiverAgentWithWindow.java) :
  agent of type  *AgentWindowed*
    - the agent has a cyclic behavior which displays on its window the messages it receives
- [LaunchAgents](https://github.com/EmmanuelADAM/jade/blob/english/window/launch/LaunchAgents.java) : **main 
  class** launches Jade and creates the agents

- At launch, 4 agents are launched: "a" able to send a message; "b", "c", "d" waiting for messages

---