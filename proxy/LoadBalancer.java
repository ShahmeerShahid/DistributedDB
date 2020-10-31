package proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;

import javax.imageio.IIOException;

/**
* This class is used to generate a Load Balancer for URLShortner program.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public class LoadBalancer {
    Deque<String> hostsQueue;
    public ArrayList<String> hostsArray;
    private static ArrayList<Shard> shards;

    /**
    * Initialize the Load Balancer.
    *
    * @param hostFile the file to grab hosts from.
    * @throw
    *
    */
    LoadBalancer(String hostsFile) throws IOException {
        this.hostsArray = readHosts(hostsFile);
        // this.hostsQueue = new ArrayDeque<String>(hostsArray);
        assignHostsToShard(hostsArray);
    }
    
    // 1. Switch load balancer to eventually use the 'usage' script output to select the best host.

    // Always have 1/5 hosts as backup

    Shard getShard(String shortURL) {
        int hash = Hash.getHash(shortURL, shards.size());
        return shards.get(hash);
    }

    /***
     * get load balanced host using round robin
     * @return host
     */
    String getHost() {
        String host = this.hostsQueue.removeFirst();
        this.hostsQueue.addLast(host);
        return host;
    }

    void addHost(String host) {}
    void removeHost(String host) {}
    
    /***
     * read hosts from hosts.txt file.
     * @return host
     */
    private ArrayList<String> readHosts(final String hostFile) throws IOException {
        String s;
        ArrayList<String> hosts = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(hostFile))) {
            while ((s = br.readLine()) != null) {
                hosts.add(s);
            }
        } catch (final IIOException exc) {
            System.err.println("I/O Error in reading host file: " + exc);
        }
        return hosts;
    }

    /***
     * assign a node to its respective shard.
     */
    private void assignHostsToShard(ArrayList<String> hosts) {
        int numHosts = hosts.size();
        shards = new ArrayList<Shard>();
        ArrayList<ArrayList<String>> shardLists = new ArrayList<ArrayList<String>>();
        int numShards = (int) Math.floor(numHosts / 2);
        System.out.println(String.format("Assigning %d nodes to %d shards.", numHosts, numShards));

        for (int i = 0; i < numShards; i++)
            shardLists.add(new ArrayList<String>());

        for (int i = 0; i < numHosts; i++) {
            shardLists.get(i % numShards).add(hosts.get(i));
            System.out.println(String.format("Node %d (%s) assigned to Shard %d", i, hosts.get(i), i % numShards));
        }

        for (int i = 0; i < numShards; i++)
            shards.add(new Shard(shardLists.get(i), i));
    }

}
