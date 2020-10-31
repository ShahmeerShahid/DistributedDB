#!/bin/bash

# random    proxy   numThread   numReads    numWrites

for i in {1..10}
do
    kill -9 $(lsof -i :8026 -s TCP:LISTEN | awk '{print $2}' | tail -n 1)
    
    python3 random_tests.py dh2020pc05 $(hostname) 1 100 0
    sleep 30
done