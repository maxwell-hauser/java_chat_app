# Chat Application - Quick Start Guide

## Prerequisites
- Java 21+ installed
- Maven 3.9+ installed

## Quick Commands

### Build
```powershell
mvn clean package
```

### Test
```powershell
mvn test
```

### Run GUI Server (Easiest)
```powershell
java -jar target/chat-application-2.0.0.jar
```
Click "Start Server" button, note the port number.

### Run GUI Client
```powershell
mvn exec:java -Dexec.mainClass="com.chatapp.ui.ClientUI"
```
Enter host (localhost), port (from server), and username.

### Run CLI Server
```powershell
java -cp target/chat-application-2.0.0.jar com.chatapp.server.ChatServerCLI verbose
```

### Run CLI Client
```powershell
java -cp target/chat-application-2.0.0.jar com.chatapp.client.ChatClientCLI localhost <PORT>
```

## Complete Example Session

**Terminal 1 (Server):**
```powershell
cd "g:\My Drive\GITHUB\java\chat_app"
mvn clean package
java -jar target/chat-application-2.0.0.jar
# Note the port number shown (e.g., 58324)
```

**Terminal 2 (Client 1):**
```powershell
cd "g:\My Drive\GITHUB\java\chat_app"
mvn exec:java -Dexec.mainClass="com.chatapp.ui.ClientUI"
# In GUI: localhost, port 58324, username "Alice"
```

**Terminal 3 (Client 2):**
```powershell
cd "g:\My Drive\GITHUB\java\chat_app"
mvn exec:java -Dexec.mainClass="com.chatapp.ui.ClientUI"
# In GUI: localhost, port 58324, username "Bob"
```

Now Alice and Bob can chat!

## Troubleshooting

### "Cannot find Maven"
```powershell
scoop install maven
```

### "Port already in use"
The server auto-selects a free port. Just note the port shown in the UI/logs.

### "Connection refused"
1. Make sure server is running
2. Check port number matches
3. Try "localhost" as hostname

## Configuration

Edit `src/main/resources/application.properties`:
```properties
chat.max.clients=50
chat.default.port=8080
chat.verbose=false
```

## Project Structure

```
src/main/java/com/chatapp/
├── server/      # Server implementations
├── client/      # Client implementations
├── ui/          # GUI components
└── common/      # Shared utilities

src/test/java/com/chatapp/
└── ...          # Unit tests
```

## Key Features

✨ Multi-client support (50+ concurrent)
✨ GUI and CLI interfaces
✨ Real-time messaging
✨ Unique username enforcement
✨ Graceful disconnect handling
✨ Professional logging
✨ Comprehensive tests

## Documentation

- **Full README**: `README_NEW.md`
- **Modernization Details**: `MODERNIZATION_SUMMARY.md`
- **This Guide**: `QUICK_START.md`

---
**Version 2.0.0** | Built with Java 21 ☕
