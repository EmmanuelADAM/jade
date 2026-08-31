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
| `monitoring.agents.ModernDummyAgent` + `monitoring.gui.ModernDummyAgentGui` | Replacement for `jade.tools.DummyAgent.DummyAgent`: compose and send an arbitrary ACL message (performative, receiver, conversation id, content), see every sent/received message in a live table with a detail viewer. |
| `monitoring.agents.ModernSnifferAgent` + `monitoring.gui.ModernSnifferGui` | Replacement for `jade.tools.sniffer.Sniffer`: a live, filterable message table plus a sequence-diagram view (one lifeline per agent, one arrow per message), colored by performative. |
| `monitoring.MessageBus` / `monitoring.Monitor` | The plumbing between the two: an in-JVM publish/subscribe bus any agent can report its sent/received messages to. |
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

## Plugging the Sniffer into your own agent

Unlike the original `Sniffer`, which taps directly into the platform's
message router (it can therefore watch any agent, including ones on remote
containers, without their cooperation), `ModernSnifferAgent` only sees what
is explicitly reported to it, and only within the same JVM. In exchange it
needs no low-level platform hook and is trivial to add to any example of
this repository: replace `send(msg)` with `Monitor.send(this, msg)`, and
report every message you `receive()`/`blockingReceive()` with
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

Launch a `monitoring.agents.ModernSnifferAgent` in the same Jade profile as
the instrumented agents and its window fills up live, with no further
wiring.

## Launching from an existing Jade profile

Both tools are plain agent classes, so they slot into any
`Profile.AGENTS` string exactly like the historical tools did:

```java
prop.setProperty(Profile.AGENTS,
    "myDummy:monitoring.agents.ModernDummyAgent;"
  + "mySniffer:monitoring.agents.ModernSnifferAgent");
```

## Known limitations

- The Sniffer only sees agents running in the same JVM that call
  `Monitor.send`/`Monitor.received` - it is a teaching-friendly companion
  to the real `jade.tools.sniffer.Sniffer`, not a full replacement for
  platform-wide, cross-container debugging.
- If both the sender and the receiver of a message are instrumented, the
  message is reported twice (once as `SENT`, once as `RECEIVED`), which is
  intentional: it lets you see both sides' timing, but means the sequence
  diagram may draw two close arrows for a single real exchange.
