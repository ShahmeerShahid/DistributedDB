import argparse, subprocess, os
import utils.py_utils as utils
import dbConsistency 
import time
import inspect


filename = inspect.getframeinfo(inspect.currentframe()).filename
CWD     = os.path.dirname(os.path.abspath(filename))


DEFAULT_PROXY_PORT='8030'
DEFAULT_NODE_PORT='8026'
ALT_PROXY_PORT='8031'
ALT_NODE_PORT='8027'
ALT_DB_NAME="new_db.db"
ALT_HOSTS_FILENAME="hosts.txt.new"
ALT_HOSTS_FILEPATH=f"{CWD}/../proxy/{ALT_HOSTS_FILENAME}"


def restartProxy() -> None:
    # stop proxy
    subprocess.check_output(["./stop_proxy", DEFAULT_PROXY_PORT], cwd=f"{CWD}/utils")
    # start proxy
    subprocess.check_output(["./start_proxy", DEFAULT_PROXY_PORT], cwd=f"{CWD}/utils")
    
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Add a worker node that runs URLShortner.')
    parser.add_argument('hostName', metavar='hostName', type=str,
                    help='The host name (or IP address) of the node.')
    args = parser.parse_args()
    new_host = args.hostName
    print(f"Adding worker node {new_host}")

    old_hosts = utils.readHosts()
    old_shards = utils.getShardsFromHosts(old_hosts)
    representative_nodes = [shard[0] for shard in old_shards.values()]
    print(f"Chose representative (master) nodes {representative_nodes}")

    for host in [*old_hosts, new_host]:
        print(f"Adding {host} to {ALT_HOSTS_FILEPATH}")
        utils.addHostToFile(host, path=ALT_HOSTS_FILEPATH)

    print(f"Starting URLShortner on {new_host}")
    subprocess.check_output(["ssh", new_host, f"cd {CWD}", "&&", "./startNode"]) # start the default URL Shortner
    time.sleep(5) # wait for process to start
    
    if (len(old_hosts) % 2 == 0):
        # no need to redistribute data
        # rename hosts file
        print("Number of shards remained the same.")
        subprocess.check_output(["./rename_alt_files"], cwd=f"{CWD}/utils")
        dbConsistency.runDbConsistency()
        restartProxy()
        exit()

    print(f"Starting alternate (data redistribution) proxy")
    subprocess.check_output(["./start_proxy", ALT_PROXY_PORT, ALT_NODE_PORT, ALT_HOSTS_FILENAME], cwd=f"{CWD}/utils") # start an alternate proxy used for data redistribution

    # start alternate URL Shortner on each host for data redistribution
    for host in [*old_hosts, new_host]:
        print(f"Starting alternate (data redistribution) URLShortner on node {host}")
        subprocess.check_output(["ssh", host, "cd", CWD, "&&", "./startNode", "-d", ALT_NODE_PORT, ALT_DB_NAME])
    
    for master_node in representative_nodes:
        print(f"Redistributing data from {master_node}")
        subprocess.check_output(["ssh", master_node, "cd", CWD, "&&", "python3", "./utils/sendAllData.py"]) # Send data to alternative proxy to redistribute

    print("Done redistributing data.")

    # rename hosts.txt.new to hosts.txt
    subprocess.check_output(["./rename_alt_files", "-n",ALT_HOSTS_FILEPATH], cwd=f"{CWD}/utils") # move db and rename hosts file
    dbConsistency.runDbConsistency();
        
    #restart proxy
    restartProxy()
    
    print("Killing alternate system")
    out = subprocess.check_output(["./stopOrchestration", "-d"], cwd=f"{CWD}")
    print(out)
    
