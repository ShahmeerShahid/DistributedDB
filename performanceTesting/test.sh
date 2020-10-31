#!/bin/bash

# random    proxy   numThread   numReads    numWrites
./runDistributedTests random dh2020pc05 8 4000 0
./runDistributedTests random dh2020pc05 8 8000 0
./runDistributedTests random dh2020pc05 8 12000 0
./runDistributedTests random dh2020pc05 8 16000 0
./runDistributedTests random dh2020pc05 8 20000 0
./runDistributedTests random dh2020pc05 8 24000 0
./runDistributedTests random dh2020pc05 8 28000 0
./runDistributedTests random dh2020pc05 8 32000 0
./runDistributedTests random dh2020pc05 8 36000 0
./runDistributedTests random dh2020pc05 8 40000 0
