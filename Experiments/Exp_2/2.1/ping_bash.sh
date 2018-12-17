#!/bin/bash


ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "ping -w 5 10.0.0.8 > ./asl-fall18-project/Experiments/Exp_2/2.1/pingfrom1to6.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "ping -w 5 10.0.0.8 > ./asl-fall18-project/Experiments/Exp_2/2.1/pingfrom2to6.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "ping -w 5 10.0.0.8 > ./asl-fall18-project/Experiments/Exp_2/2.1/pingfrom3to6.log "


sleep 10

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "pkill -9 ping"

