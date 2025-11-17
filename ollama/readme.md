# Jade : Agents

## Agent et LLM

---

Jade Agent-Oriented Programming Course Materials

To allow an agent to use a LLM to interact with the user, one good way is to use OLLAMA.

1. Download [ollama](https://ollama.com/) from the main page
2. Choose a LLM to download on your computer (ex. ``ollama pull granite.3.``) 
   - *list of models :* [Ollama Models](https://github.com/ollama/ollama)*
   - [granite3.3](https://ollama.com/library/granite3.3) is a good one (free)
3. Start ollama as a server ``ollama serve``

You can try the agent ``AgentLLM`` that just make a connection to your llm via ollama to chat..
  - the first execution is always long (depending on the llm and the computer) due to the loading of the llm

- TODO :
- A human discuss with a BlaBla agent that uses a LLM to answer
 - the discussion will be about going to a restaurant 
   - the agent will ask to a weather agent about the weather (see Meteo class)
   - in parallel the blabla agent ask about taste preferences
   - the blabla agent will propose a restaurant according to the weather, and the taste preferences, and some advices regarding the clothes.
   
**For Meteo**: 
   - Use [OpenWeatherMap API](https://openweathermap.org/api)
   - create our own key (free access with limitations) here : [Get API Key](https://home.openweathermap.org/users/sign_up)
   - and replace the key in the Meteo class
 

---