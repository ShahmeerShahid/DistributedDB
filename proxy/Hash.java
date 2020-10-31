package proxy;

/**
* This class is used to generate a Hash used by the load balancer to select a shard where the data will be present.
*
* @author  Ali Raza, Jefferson Zhong, Shahmeer Shahid
* @version 1.0
*/
public class Hash {
    public static int getHash(String shortURL, int numShards) {
        int sum = 0;
        for (int i = 0; i < shortURL.length(); i++)
            sum += (int) shortURL.charAt(i);

        return sum % numShards;
    }
}
