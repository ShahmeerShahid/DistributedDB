from typing import *
import os
import inspect
import requests

filename = inspect.getframeinfo(inspect.currentframe()).filename
CWD     = os.path.dirname(os.path.abspath(filename))


HOSTS_FILE_PATH = f"{CWD}/../../proxy/hosts.txt"

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def readHosts(path: str = HOSTS_FILE_PATH) -> List[str]:
    """
    Read hosts from hosts file.
    """

    with open(path, "r") as hosts_file:
        hosts = [line.rstrip() for line in hosts_file]
    return hosts

def addHostToFile(hostName: str, path: str = HOSTS_FILE_PATH) -> None:
    """
    Add hostName to hosts.txt file on a new line
    """
    with open(path, "a") as hosts_file:
        hosts_file.write(f"{hostName}\n")
    return None


def getShardsFromHosts(hosts: List[str]) -> Dict[int, str]:
    """
    Assign hosts to their respective shard.

    Return shard dictionary.
    """

    shards = {}
    num_shards = len(hosts) // 2
    for i in range(num_shards):
        shards[i] = []

    current_shard = 0
    for host in hosts:
        shards[current_shard].append(host)
        current_shard = (current_shard + 1) % num_shards

    return shards

def checkIfHostActive(host: str) -> bool:
    """
    Returns True iff URLShortner is running on host
    """
    try:
        requests.get(f"http://{host}:8026/??")
    except requests.exceptions.ConnectionError:
        return False
    return True

def clear():
    os.system('clear')