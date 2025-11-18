# Java Socket Chat App

A simple multi-client chat application implemented in Java with both CLI and Swing UI front-ends. A server accepts multiple clients, enforces unique usernames, and broadcasts messages. Clients can send messages and see system/user events.

## Features
- Multi-client chat via TCP sockets
- Username prompt with uniqueness enforcement
- Broadcast of join/leave and user messages
- CLI server/client and Swing-based GUI server/client
- Optional verbose logs in CLI server ("loquacious" mode)

## Source Layout
- `src/ChatServerCLI.java` — Console server. Picks a free random port; optional verbose mode.
- `src/ChatClientCLI.java` — Console client. Connects to a fixed port (4378) by default.
- `src/UI/ServerUI.java` — GUI server. Shows selected port and server logs.
- `src/UI/ClientUI.java` — GUI client. Enter name + server port, then send messages.
- `src/UI/TextOutput.java` — Helper to route `System.out` to a `JTextArea`.

## Requirements
- Java 8+ (tested on modern JDKs)
- Windows PowerShell (commands below are Windows-friendly)
- Internet access to download a small dependency for server port selection

### External Dependency
Both server variants use a tiny library to pick a free local port:
- Maven coordinates: `me.alexpanov:free-port-finder:1.0`

Download the JAR once and keep it in a local `lib` folder.

```powershell
# From the project root: .\chat_app
New-Item -ItemType Directory -Force -Path .\lib | Out-Null
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/me/alexpanov/free-port-finder/1.0/free-port-finder-1.0.jar" -OutFile ".\lib\free-port-finder-1.0.jar"
```

## Build (javac)
Compile classes into `target\classes`.

```powershell
# From the project root: .\chat_app
New-Item -ItemType Directory -Force -Path .\target\classes | Out-Null
javac -d .\target\classes -cp .\lib\free-port-finder-1.0.jar .\src\ChatServerCLI.java .\src\ChatClientCLI.java .\src\UI\*.java
```

Notes:
- On Windows, classpath entries are separated by `;` at runtime.
- The UI classes are in the `UI` package; the CLI classes are in the default package.

## Run
Open multiple terminals for server and clients.

### Option A: GUI Server + GUI Clients (recommended)
1) Start the server GUI (it will pick and display a free port):
```powershell
java -cp ".\target\classes;.\n\lib\free-port-finder-1.0.jar" UI.ServerUI
```
2) Start one or more GUI clients and connect using the port shown by the server:
```powershell
java -cp .\target\classes UI.ClientUI
```
- Enter a unique Name and the server Port, then click Start.
- Type messages and click Send. Use the Stop button to disconnect.

### Option B: CLI Server + GUI Clients
1) Start the CLI server. Add `loquacious` to print the chosen port and connection events:
```powershell
java -cp ".\target\classes;.
\lib\free-port-finder-1.0.jar" ChatServerCLI loquacious
```
2) Start GUI clients and connect to the printed port:
```powershell
java -cp .\target\classes UI.ClientUI
```

### Option C: CLI Server + CLI Client (see port note)
The CLI client currently connects to port `4378` by default and accepts an optional server IP argument (defaults to `localhost`).

```powershell
# Server (random port; see limitation below)
java -cp ".\target\classes;.
\lib\free-port-finder-1.0.jar" ChatServerCLI loquacious

# Client (connects to localhost:4378)
java -cp .\target\classes ChatClientCLI
# Or specify IP
java -cp .\target\classes ChatClientCLI 127.0.0.1
```

Important: because the CLI server selects a random free port while the CLI client is hardcoded to `4378`, they won’t connect out-of-the-box. Use the GUI client (Option B) to enter the printed server port, or adjust the CLI code to use a fixed, agreed-upon port.

## Usage Tips
- Server prompts each client to enter a username; names must be unique.
- Type `/quit` in the client to disconnect.
- In CLI server, supply `loquacious` to see extra logs.

## Troubleshooting
- **Port mismatch / connection refused**: Ensure clients connect to the exact port shown by the server (GUI server displays it; CLI server prints it only in `loquacious` mode). The CLI client uses port `4378`; it won’t connect to a server on a different port.
- **`ClassNotFoundException: me.alexpanov.net.FreePortFinder`**: Make sure the JAR is downloaded to `lib/` and included on the classpath for server runs/compiles.
- **`Address already in use`**: Choose a different port (GUI server auto-selects a free one), or wait for the OS to release the port.
- **Firewall prompts**: Allow Java to communicate on private networks or temporarily disable strict firewall rules while testing locally.
- **`INVALID NAME` from server**: Pick a non-empty, unique username.

## Development Notes
- Packages: `UI.*` are in the `UI` package; CLI classes are in the default package. Keep this in mind when running with `java -cp`.
- IDEs: The repo includes `.idea/` metadata. You can import the project into IntelliJ IDEA or Eclipse; just ensure the Free Port Finder dependency is configured (via Maven or by adding the JAR to the module dependencies).
- Enhancements you might consider:
  - Pass server port as a CLI argument to both client and server.
  - Persist chat history to a file.
  - Add simple auth or a `/who` command.
  - Support remote hosts in the GUI client (currently uses `localhost`).

---
If you want, I can update the CLI to accept a `--port` argument so CLI server and client interoperate cleanly. Let me know and I’ll wire it up.
