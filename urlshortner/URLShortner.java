package urlshortner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import jdbc.DBHandler;

/**
* This class the URLShortner object.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public class URLShortner {
	// port to listen connection
	
	/**
	* This main method initializes the URLShortner.
	* @param args Unused.
	* @return Nothing.
	*/
	public static void main(String[] args) throws IOException {
		int LOCALPORT = 8026;
		String DBLOCATION = String.format("/virtual/%s/URLShortner/urlshortner.db", System.getProperty("user.name"));
		if(args.length == 2) {
			LOCALPORT = Integer.parseInt(args[0]);
			DBLOCATION = String.format("/virtual/%s/URLShortner/%s", System.getProperty("user.name"), args[1]);
		}

		try {
			System.out.println("Server started.\nListening for connections on port : " + LOCALPORT + " ...\n");
			runServer(LOCALPORT, DBLOCATION); // never return
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	/**
    * spawn threads for incoming request.
    *
    * @param localport
    * @param dbPath the path to the DB File.
    *
    */
	static void runServer(int localport, String dbPath) throws IOException {
		// Create a ServerSocket to listen for connections with
		ServerSocket serverConnect = new ServerSocket(localport);
		DBHandler db = new DBHandler(dbPath);
		URLResponseInit resFiles = new URLResponseInit();
		// we listen until user halts server execution
		while (true) {
			Socket clientSocket = null;
			try {
				// Wait for a connection on the local port
				System.out.println("Waiting for a client ...");
				clientSocket = serverConnect.accept();
				System.out.println("Accepted new connection: " + serverConnect);
				Thread t = new Thread(new URLConnectionHandler(clientSocket, db, resFiles));
				t.start();
				System.out.println("thread spawned for new client.");
			} catch (Exception e) {
				clientSocket.close();
				e.printStackTrace();
			}
		}

	}

}
