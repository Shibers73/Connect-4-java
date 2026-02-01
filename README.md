# Connect 4

Default port: 8888
Default address: localhost
Max clients: 2

## Instructions
### Running it through an IDE (best)
- Start a server by running src\main\java\com\sca\connect4\server\GameServer.java as a java application (if not already started) (the default port is 8888, you can change it in src\main\java\com\sca\connect4\server\GameServer.java, in the same file you can change address to your public address if you want it to be online multiplayer)
- Run src\main\java\com\sca\connect4\client\ClientApplication.java as a java application (make sure the ports and host address match)
- Run another client, whether that's you or your friend! (port and address matching, remember)
- Enjoy!

### Running it through CMD (local only, strict port: 8888)
- Download the client and server jar in the latest release
- Run them in separate cmd instances through command: java -jar "path"

## TODO
- Server and client ask for a custom address and port so the built jar is customizable
