package proxy;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* This class is a ConnectionHandler which handles connections coming into the proxy server.
* It looks up the hash using the Load Balancer and forwards the traffic to where the data can
* be found. Once the node replies, it forwards the data back to the client.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
class ConnectionHandler extends Thread {
    Socket clientSocket;
    InputStream streamFromClient;
    OutputStream streamToClient;
    String node;
    int nodePort;
    Socket nodeSocket;
    byte[] request;
    byte[] reply;
    InputStream streamFromNode;
    private LoadBalancer loadBalancer;
    private String incomingReqString;
    private String longResource;
    private CacheHandler cacheHandler;

    /**
    * Initialize the Connection handler.
    *
    * @param clientSocket the incoming client socket connection.
    * @param loadBalancer the current load balancer instance.
    * @param cacheHandler the current cache handler instance.
    * @param nodePort the port to connect to a URLShortner node with.
    *
    */
    public ConnectionHandler(Socket clientSocket, LoadBalancer loadBalancer, CacheHandler cacheHandler, int nodePort) {
        this.clientSocket = clientSocket;
        this.nodePort = nodePort;
        this.request = new byte[1024];
        this.reply = new byte[4096];
        this.loadBalancer = loadBalancer;
        this.cacheHandler = cacheHandler;
    }

    /**
    * Handler connection between client and proxy. Data is read from client and sent to the
    * respective handler to deal with the request. Once the handler responds back the data is
    * sent back to the client and socket connections are closed.
    *
    */
    @Override
    public void run() {
        try {
            // init streams from client
            streamFromClient = clientSocket.getInputStream();
            streamToClient = clientSocket.getOutputStream();

            int bytesReadIncoming = streamFromClient.read(request);
            nodeSocket = handleRequest(bytesReadIncoming);
            if (nodeSocket != null) {
                streamFromNode = nodeSocket.getInputStream();

                int bytesRead;
                // this current thread reads response from node and forwards to client.
                try {
                    BufferedReader input;
                    String line;
                    while ((bytesRead = streamFromNode.read(reply)) != -1) {
                        if (longResource == null) {
                            input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(reply)));
                            while ((line = input.readLine()) != null) {
                                if (line.contains("Location:")) {
                                    System.out.println(String.format("Cached result %s ", incomingReqString));
                                    this.cacheHandler.save(incomingReqString, line);
                                }
                                if (line.isEmpty()) {
                                    break;
                                }
                            }
                        }
                        streamToClient.write(reply, 0, bytesRead);
                        streamToClient.flush();
                    }
                    System.out.println("Done replying to client");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    /*
     * close client and node socket connections.
     */
    private void closeConnections() {
        try {
            if (nodeSocket != null)
                nodeSocket.close();
            if (clientSocket != null)
                clientSocket.close();
            System.out.println("Closed Connection to client.");
        } catch (IOException e) {
            System.out.println("error closing socket connections.");
        }
    }

    /**
    * Handle incoming request.
    *
    * @param bytesRead the number of bytes read from a client.
    * @return Socket instance from the node we are reading from.
    *
    */
    public Socket handleRequest(int bytesRead) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(request);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            String input = bufferedReader.readLine();
            System.out.println("Request: " + input);

            Pattern pput = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
            Matcher mput = pput.matcher(input);

            Pattern pget = Pattern.compile("^(\\S+)\\s+/(\\S+)\\s+(\\S+)$");
            Matcher mget = pget.matcher(input);

            if (mput.matches()) {
                return putHandler(mput, bytesRead);
            } else if (mget.matches()) {
                return getHandler(mget, bytesRead);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.err.println("request handler error");
            return null;
        }
    }

    /**
    * Handle incoming PUT request.
    *
    * @param mput the incoming matched PUT request.
    * @param bytesRead the number of bytes read from a client.
    * @return Socket instance from the node we are reading from.
    *
    */
    private Socket putHandler(Matcher mput, int bytesRead) {

        String shortResource = mput.group(1);
        this.cacheHandler.remove(shortResource);
        Shard shard = this.loadBalancer.getShard(shortResource);
        return shard.forwardWriteRequest(request, bytesRead, nodePort);
    }

    /**
    * Handle incoming GET request.
    *
    * @param mget the incoming matched GET request.
    * @param bytesRead the number of bytes read from a client.
    * @return Socket instance from the node we are reading from.
    *
    */
    private Socket getHandler(Matcher mget, int bytesRead) {
        incomingReqString = mget.group(2);
        longResource = this.cacheHandler.checkLocalCache(incomingReqString);
        if (longResource == null) {
            Shard shard = this.loadBalancer.getShard(incomingReqString);
            return shard.forwardReadRequest(request, bytesRead, nodePort);
        } else {
            this.cacheHandler.replyToClient(longResource, streamToClient);
            return null;
        }
    }
}