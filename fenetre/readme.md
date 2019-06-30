#Jade, agents et fenêtre

Les agents liés à des fenêtres héritent de la classe GuiAgent qui leurs permettent de recevoir des événements lancés par les interfaces graphiques.

Dans les codes du package fenêtre, un agent a envoie un message à trois agents b,c,d dès que le bouton go est cliqué.
Les agents recevant le message l'affiche dans leurs fenêtres.

LaunchAgent.java permet de lancer ces agents.
