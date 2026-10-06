# Jade : Agents

## Agent et LLM

---

Jade Agent-Oriented Programming Course Materials

To allow an agent to use a LLM to interact with the user, one good way is to use OLLAMA.

1. Download [ollama](https://ollama.com/) from the main page
2. Choose a LLM to download on your computer (ex. ``ollama pull yourModel``) 
   - *list of models :* [Ollama Models](https://ollama.com/search)*
   - you can choose a little one (+-4b) to store on your disk or a cloud one (look at a free one)
3. Start ollama as a server ``ollama serve``

---

## International Cuisine

You can try the agent ``AgentLLM`` that make a connection to your llm via ollama to chat..
  - choose the model you want try
  - choose the city where you plan to have a diner
  - the discussion will be about going to a restaurant (in french for the moment) 
     - the BlaBla agent will ask to a weather API about the weather (see Meteo class)
       - it informs about the weather nature (very cold, cold, temperate, hot, very hot) and the temperature
     - the season is also get from the date and the city
  - blabla agent get the question of the user (like "what can cook for the diner ?"
  - the first execution is always long (depending on the llm and the computer) due to the loading of the llm
   
**For Meteo**: 
   - Use [OpenWeatherMap API](https://openweathermap.org/api)
   - create our own key (free access with limitations) here : [Get API Key](https://home.openweathermap.org/users/sign_up)
   - and replace the key in the Meteo class

---

## LLM votes

A Borda vote is done between agent that use their own personality to mak a choice, via a LLM, among a list of restaurant.
See  [ollamaVotes](ollamaVotes) and launch [LanceurVote](LanceurVote)


---