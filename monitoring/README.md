# Modern monitoring tools

Plain-Swing, drop-in replacements for the two JADE monitoring windows most
teaching examples end up opening, whose late-90s look ages the whole
platform: `jade.tools.DummyAgent.DummyAgent` and `jade.tools.sniffer.Sniffer`.

No new dependency is added: everything here is written against
`external-libraries/JadeUPHF.jar` and plain `javax.swing`, exactly like the
rest of this repository.

## What's inside

| Package | Content |
|---|---|
| `monitoring.ui` | Shared, reusable modern Swing components: `ModernTheme` (light/dark palette), `ModernButton`, `RoundedPanel`, `ModernScrollBarUI`, `StatusPill`, `PerformativeColors`, `ModernLogPanel`, `SequenceCanvas`. Free to reuse in any other agent GUI of this repository. |
| `monitoring.agents.ModernDummyAgent` + `monitoring.gui.ModernDummyAgentGui` | Replacement for `jade.tools.DummyAgent.DummyAgent`: a full ACL message composer (performative, receivers, reply-to, language, encoding, ontology, protocol, conversation-id, in-reply-to, reply-with, content), a live sent/received history with Reply, Edit (set as current) and Delete, and Open/Save for both a single message and the whole history. |
| `monitoring.agents.ModernSnifferAgent` + `monitoring.gui.ModernSnifferGui` | Replacement for `jade.tools.sniffer.Sniffer`: a live, filterable message table plus a sequence-diagram view (one lifeline per agent, one arrow per message), colored by performative, with Save log to file. Can watch an agent with **zero code changes** (see below) as well as agents that report themselves in-process. |
| `monitoring.MessageBus` / `monitoring.Monitor` | The in-JVM publish/subscribe bus behind the Sniffer: fed either by the real platform `SniffOn` mechanism or by an agent explicitly reporting its own messages. |
| `monitoring.io.MessageLogFile` | Save/load of a single ACL message or a whole log to/from a plain text file, used by both tools. |
| `monitoring.agents.DemoPingPongAgent` | A tiny, already-instrumented ping-pong agent used by the demo launcher below. |
| `monitoring.launch.*` | Ready-to-run launchers. |

## Try it in one command

```
java monitoring.launch.LaunchModernMonitoringDemo
```

Opens a Modern Sniffer together with two instrumented `ping`/`pong` agents:
the message table and the sequence diagram fill up immediately, nothing to
configure.

```
java monitoring.launch.LaunchModernDummy
```

Opens a Modern Dummy Agent next to a Modern Sniffer: any message composed
and sent from the Dummy Agent window immediately shows up in the Sniffer.

Both launchers set `Profile.GUI = "true"` like the rest of this repository,
so JADE's own RMA window (the platform's agent list) still opens too - set
it to `"false"` if you only want the modern windows.

## Watching an agent with zero code changes

Type an agent's local name in the Sniffer's "Watch an agent" field and
click **Watch**: this sends the real FIPA/JADE `SniffOn` request to the AMS
- the exact mechanism `jade.tools.sniffer.Sniffer` itself uses - so the
platform starts forwarding a copy of every message that agent sends or
receives. **No change to that agent's code is needed**, and it works even
for agents you did not write, or agents running on a remote container.
Click the agent's chip (`name  ×`) to stop watching it (sends `SniffOff`).

> **This only works for agents whose class extends `jade.core.Agent` directly**
> (e.g. `pingPong.AgentPingPong`). It does **not** work for agents extending
> `jade.gui.GuiAgent`/`jade.gui.AgentWindowed` - i.e. most of this
> repository's windowed examples (`AuctioneerAgent`, `ParticipantAgent`,
> `AgenceAgent`, ...). For those, use `Monitor.send`/`Monitor.received`
> below instead. See "Known limitations" for details.

## Plugging the Sniffer into your own agent (in-process alternative)

If you would rather not depend on the platform's AMS forwarding, or you are
watching a `GuiAgent`/`AgentWindowed`-based agent (see above), an agent can
report its own messages directly: replace `send(msg)` with `Monitor.send(this, msg)`,
and report every message you `receive()`/`blockingReceive()` with
`Monitor.received(this, msg)`.

```java
import monitoring.Monitor;
// ...
ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
// ...
Monitor.send(this, msg);          // instead of: send(msg);
```

```java
ACLMessage msg = receive(template);
if (msg != null) {
    Monitor.received(this, msg);  // add this line
    // ... your existing handling
}
```

## Launching from an existing Jade profile

Both tools are plain agent classes, so they slot into any
`Profile.AGENTS` string exactly like the historical tools did:

```java
prop.setProperty(Profile.AGENTS,
    "myDummy:monitoring.agents.ModernDummyAgent;"
  + "mySniffer:monitoring.agents.ModernSnifferAgent");
```

## Known limitations

- **"Watch an agent" (zero instrumentation) does not work for agents whose
  class extends `jade.gui.GuiAgent` or `jade.gui.AgentWindowed`** - which is
  most of the windowed examples in this repository (`AuctioneerAgent`,
  `ParticipantAgent`, `AgenceAgent`, `TravellerAgent`, ...). In this JadeUPHF
  build, the platform's own `SniffOn`/`ToolNotifier` mechanism never
  delivers a `SentMessage`/`PostedMessage` event for these agents, even
  though their messages are actually sent/received correctly - this was
  confirmed experimentally (a plain `jade.core.Agent` sending the exact same
  `ContractNetInitiator`-based CFP is sniffed correctly; a `GuiAgent`-based
  one sending the identical CFP is not, regardless of how long you wait
  after confirming the watch). For any `GuiAgent`/`AgentWindowed`-based
  agent, use the in-process alternative below (`Monitor.send`/`Monitor.received`)
  instead - it does not depend on this platform mechanism and works
  unconditionally, for any agent class. It only works for agents whose class
  extends `jade.core.Agent` directly (e.g. `pingPong.AgentPingPong`).
- The "Watch an agent" field resolves a local name on the sniffer's own
  platform; it does not (yet) offer a live tree of every agent/container
  currently running, unlike the original Sniffer's agent tree.
- If an agent is both watched via `SniffOn` and separately calls
  `Monitor.send`/`Monitor.received` itself, the same message can be
  reported twice.
- The Dummy Agent composer covers every field the historical `AclGui`
  exposes except the reply-by date and the raw envelope tab (comments,
  ACL representation, payload encoding, ...), and there is no per-agent
  mobility GUI lifecycle (dispose/restore on move/freeze) since no example
  in this repository uses agent mobility.
