package proxy;

import java.io.*;
import java.net.*;

/**
* This class is a Proxy service for the URLShortner program. It is multithreaded and request is handled on its
* own thread.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public class Proxy {
    private static LoadBalancer loadBalancer;
    private static CacheHandler cacheHandler;

    public static void main(final String[] args) throws IOException {
        int NODE_PORT = 8026;
        int PROXY_PORT = 8030;
        String HOSTS_FILE = "./proxy/hosts.txt";


        if(args.length==3){
            PROXY_PORT = Integer.parseInt(args[0]);
            NODE_PORT = Integer.parseInt(args[1]);
            HOSTS_FILE = String.format("./proxy/%s", args[2]);
        }

        loadBalancer = new LoadBalancer(HOSTS_FILE);
        cacheHandler = new CacheHandler();
        
        

        // shardHandlers = new ShardHandler();
        try {
            // Print a start-up message
            System.out.println(
                    "✅ Proxy started on " + InetAddress.getLocalHost().getHostName() + " on port " + PROXY_PORT);
            // And start running the server
            runServer(PROXY_PORT, NODE_PORT ); // never returns
        } catch (final Exception e) {
            System.err.println(e);
        }
    }

    /**
     * runs a single-threaded proxy server on the specified local port. It never
     * returns.
     * 
     * 
     * @return
     */
    public static void runServer(final int localport, final int NODE_PORT) throws IOException {
        // Create a ServerSocket to listen for connections with
        final ServerSocket ss = new ServerSocket(localport);

        // while true
        // serversocket.accept (dont block)
        // if serversocket has data
        // choose socket from socket array to write to
        // write to socket (dont block on response)
        // for socket in socket array
        // check if socket has data
        // write to respective client
        // repeat loop

        while (true) {
            Socket clientSocket = null;
            try {
                // Wait for a connection on the local port
                clientSocket = ss.accept();
                System.out.println("Accepted new connection. " + ss);

                // Start the client-to-server request thread running
                // String nodeHost = loadBalancer.getHost();
                final Thread t = new Thread(
                        new ConnectionHandler(clientSocket, loadBalancer, cacheHandler, NODE_PORT));
                t.start();
            } catch (final Exception e) {
                clientSocket.close();
                e.printStackTrace();
            }
        }
    }
}
