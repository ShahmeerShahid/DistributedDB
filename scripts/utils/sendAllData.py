#!/usr/bin/python3

import random, string, subprocess
import requests, threading
import sqlite3
import db_utils

"""
read database using select *

send short and long as put request to new proxy server

check response status and resend if failed.....
"""

def getAllData():
    conn = db_utils.createDbConn()
    cursor = conn.cursor()
    return db_utils.getAllData(cursor)    

def sendPUTRequest(short, long, host, port):
    response1 = {"success": 0, "fail": 0}
    while (requests.put(f"http://{host}:{port}/?short={short}&long={long}").status_code != 200):
        continue
    

if __name__== '__main__':
    data = getAllData()
    host = "dh2020pc05"
    port = 8031
    print(f"Sending {len(data)} rows from database to {host}:{port}")

    for row in data:
        sendPUTRequest(row[0], row[1], host, port)


