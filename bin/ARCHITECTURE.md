# Chat Application Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Java Chat Application                       │
│                          Version 2.0.0                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌──────────────────┐
│   Client CLI     │         │   Client GUI     │
│  (ChatClientCLI) │         │   (ClientUI)     │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         └────────────┬───────────────┘
                      │
                 TCP Socket
                      │
         ┌────────────▼────────────┐
         │    ChatServer (Core)    │
         │  ┌──────────────────┐   │
         │  │  Virtual Thread  │   │
         │  │    Executor      │   │
         │  └──────────────────┘   │
         │  ┌──────────────────┐   │
         │  │ ConcurrentHash   │   │
         │  │  Map<Clients>    │   │
         │  └──────────────────┘   │
         └─────────────────────────┘
                      │
         ┌────────────┼────────────┐
         │            │            │
    ┌────▼───┐   ┌───▼────┐  ┌───▼────┐
    │Client 1│   │Client 2│  │Client N│
    │Handler │   │Handler │  │Handler │
    └────────┘   └────────┘  └────────┘
```

## Package Structure

```
com.chatapp
│
├── server/
│   ├── ChatServer.java          [Core server with virtual threads]
│   └── ChatServerCLI.java       [CLI launcher]
│
├── client/
│   ├── ChatClient.java          [Core client with async I/O]
│   └── ChatClientCLI.java       [CLI launcher]
│
├── ui/
│   ├── ServerUI.java            [Swing GUI for server]
│   ├── ClientUI.java            [Swing GUI for client]
│   └── TextAreaOutputStream.java [Console redirect utility]
│
└── common/
    ├── Message.java             [Message record]
    ├── MessageType.java         [Message type enum]
    └── ChatConfig.java          [Configuration manager]
```

## Component Interactions

### Server Flow

```
1. Server Start
   ↓
2. Bind to Port
   ↓
3. Listen Loop ──────┐
   ↓                 │
4. Accept Client     │
   ↓                 │
5. Spawn Handler ────┤
   (Virtual Thread)  │
   ↓                 │
6. Register User     │
   ↓                 │
7. Broadcast Join    │
   ↓                 │
8. Message Loop      │
   ↓                 │
9. Disconnect ───────┘
   ↓
10. Broadcast Leave
```

### Client Flow

```
1. Client Start
   ↓
2. Connect to Server
   ↓
3. Spawn Listener Thread
   (Virtual Thread)
   ↓
4. Register Username
   ↓
5. Main Loop:
   ├─→ Send Message
   └─→ Receive Message (async)
   ↓
6. Disconnect (/quit)
   ↓
7. Cleanup
```

## Data Flow - Message Broadcasting

```
Client A                    Server                     Client B

  │                           │                           │
  │  "Hello World"            │                           │
  ├──────────────────────────>│                           │
  │                           │                           │
  │                      [Broadcast]                      │
  │                           │                           │
  │                           ├──────────────────────────>│
  │                           │  "[Alice] Hello World"    │
  │                           │                           │
  │  "[Alice] Hello World"    │                           │
  │<──────────────────────────┤                           │
  │                           │                           │
```

## Thread Model

```
Main Thread
  │
  ├─→ Server Socket Listener (Virtual Thread)
  │     │
  │     ├─→ Client Handler 1 (Virtual Thread)
  │     │     ├─→ Read Loop
  │     │     └─→ Write Messages
  │     │
  │     ├─→ Client Handler 2 (Virtual Thread)
  │     │     ├─→ Read Loop
  │     │     └─→ Write Messages
  │     │
  │     └─→ Client Handler N (Virtual Thread)
  │           ├─→ Read Loop
  │           └─→ Write Messages
  │
  └─→ UI Event Thread (Swing)
        └─→ Button clicks, text updates
```

## Class Relationships

```
┌─────────────────┐
│   ChatServer    │
│  (AutoCloseable)│
├─────────────────┤
│ - port          │
│ - clients       │◄────────────┐
│ - executor      │             │
├─────────────────┤             │
│ + start()       │             │
│ + stop()        │             │
│ + broadcast()   │             │
└────────┬────────┘             │
         │                      │
         │ contains         creates
         │                      │
         ▼                      │
┌─────────────────┐             │
│ ClientConnection│             │
│    (record)     │─────────────┘
├─────────────────┤
│ - username      │
│ - out           │
│ - socket        │
└─────────────────┘


┌─────────────────┐
│   ChatClient    │
│  (AutoCloseable)│
├─────────────────┤
│ - host          │
│ - port          │
│ - socket        │
│ - messageHandler│◄─────────┐
├─────────────────┤          │
│ + connect()     │          │
│ + sendMessage() │          │
│ + disconnect()  │          │
└─────────────────┘          │
                             │
                      ┌──────┴──────┐
                      │  Consumer   │
                      │  <String>   │
                      └─────────────┘


┌─────────────────┐       ┌─────────────────┐
│    Message      │       │  MessageType    │
│    (record)     │──────>│     (enum)      │
├─────────────────┤       ├─────────────────┤
│ - type          │       │ USER            │
│ - sender        │       │ SYSTEM          │
│ - content       │       │ ERROR           │
│ - timestamp     │       └─────────────────┘
├─────────────────┤
│ + user()        │
│ + system()      │
│ + error()       │
│ + format()      │
└─────────────────┘
```

## Configuration Flow

```
application.properties
         │
         ▼
   ChatConfig
   (static utility)
         │
         ├─→ Server reads max clients
         ├─→ Server reads default port
         ├─→ Client reads reconnect settings
         └─→ All read app metadata
```

## Logging Architecture

```
Application Code
      │
      ▼
  SLF4J API
      │
      ▼
   Logback
      │
      ├─→ Console Appender ──> stdout
      │
      └─→ File Appender ────> logs/chat-app.log
                               (daily rotation)
```

## Testing Strategy

```
Unit Tests (JUnit 5)
│
├─→ MessageTest
│   └─→ Test record creation & formatting
│
├─→ ChatConfigTest
│   └─→ Test configuration loading
│
├─→ ChatServerTest
│   └─→ Test server lifecycle
│       ├─→ Start/stop
│       ├─→ Client management
│       └─→ AutoCloseable
│
└─→ ChatClientTest
    └─→ Test client operations
        ├─→ Connection
        ├─→ Message sending
        └─→ AutoCloseable
```

## Deployment Architecture

```
Development:
  IDE (VS Code) ──> Maven ──> Run Locally

Build:
  Source Code ──> Maven Package ──> JAR with Dependencies

Distribution:
  chat-application-2.0.0.jar
         │
         ├─→ java -jar (GUI Server)
         ├─→ java -cp (CLI Server)
         └─→ java -cp (CLI Client)

Production (Future):
  ┌────────────────┐
  │   Load         │
  │   Balancer     │
  └────┬───────────┘
       │
       ├─→ Server Instance 1
       ├─→ Server Instance 2
       └─→ Server Instance N
```

## Technology Stack

```
┌─────────────────────────────────────┐
│          Application Layer          │
│    (Chat Server/Client Logic)       │
├─────────────────────────────────────┤
│         Framework Layer             │
│   Swing UI │ SLF4J │ JUnit 5        │
├─────────────────────────────────────┤
│           Java 21 LTS               │
│  Records │ Virtual Threads │ etc.   │
├─────────────────────────────────────┤
│              JVM                    │
│     (OpenJDK 21 - Temurin)         │
├─────────────────────────────────────┤
│         Operating System            │
│          (Windows 11)               │
└─────────────────────────────────────┘
```

## Security Considerations (Future)

```
Current:
  Client ←──────TCP Socket──────→ Server
         (Plaintext)

Future (TLS):
  Client ←──────TLS/SSL──────→ Server
         (Encrypted)
         
Future (Auth):
  Client ──→ Login Request ──→ Server
         ←── JWT Token ←─────┘
         ──→ Messages (with token) ──→
```

---

**Architecture Version**: 2.0.0  
**Last Updated**: November 17, 2025  
**Built with**: Java 21 + Maven 3.9 + Modern Design Patterns
