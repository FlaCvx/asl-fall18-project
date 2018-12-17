#!/bin/bash

MODE='W'
Client1='10.0.0.7'
Client2='10.0.0.11'
Client3='10.0.0.5'
Middle1='10.0.0.6'
Middle2='10.0.0.4'
Server1='10.0.0.8'
Server2='10.0.0.10'
Server3='10.0.0.9'

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "ping -w 5 ${Server1} > ./asl-fall18-project/Experiments/Exp_2/2.2/pingfrom1to6.log " &

sleep 20

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "pkill -9 ping"




ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "ping -w 5 ${Server2} > ./asl-fall18-project/Experiments/Exp_2/2.2/pingfrom1to7.log " &

sleep 20

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "pkill -9 ping"

