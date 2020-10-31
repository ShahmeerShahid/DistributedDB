from typing import *
import time
import subprocess
import utils.py_utils as utils
import inspect


PING_DELAY=1 # delay in seconds between ping to hosts
LIST_REFRESH_DELAY=20 # delay in seconds between rereading list of hosts

def displayUI(shards):
    """
    Get the current state of the system and print it to stdout
    """
    iteration = 0
    while True:

        if iteration % PING_DELAY == 0:
            outputs = [f"{utils.bcolors.WARNING}Pinging nodes every {PING_DELAY} second(s).{utils.bcolors.ENDC}\n\n"]

            for shard_num in shards:
                outputs.append(shardStringBuilder(shard_num, shards[shard_num]))
            outputs.append(f"\nRefreshing list of hosts in {utils.bcolors.UNDERLINE}{LIST_REFRESH_DELAY-iteration} seconds.{utils.bcolors.ENDC}")
            utils.clear()
            print("\n".join(outputs))

        if iteration % LIST_REFRESH_DELAY == 0:
            shards = utils.getShardsFromHosts(utils.readHosts()) 
            iteration = 0

        time.sleep(1)
        iteration += 1

def shardStringBuilder(shard_num: int, hosts: List[str]) -> str:
    """
    Generate a string representation of a shard
    """
    outputs = [f"{utils.bcolors.BOLD}SHARD #{shard_num}{utils.bcolors.ENDC}", "Hosts:"]
    for host in hosts:
        outputs.append(hostStringBuilder(host))
    
    return "\n".join(outputs) + "\n"

def hostStringBuilder(host: str) -> str:
    """
    Generate a string representation of a host based on host status
    """
    if utils.checkIfHostActive(host):
        return f"    ► {utils.bcolors.OKGREEN}{host} is UP {utils.bcolors.ENDC}✅"
    else:
        return f"    ► {utils.bcolors.FAIL}{host} is DOWN {utils.bcolors.ENDC}⛔"

if __name__ == "__main__":
    hosts = utils.readHosts()
    shards = utils.getShardsFromHosts(hosts)
    print('\r')
    displayUI(shards)