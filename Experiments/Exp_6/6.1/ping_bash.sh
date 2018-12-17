#!/bin/bash

EXP="Exp_6"
EXP_DIR="6.1"

Client1='10.0.0.7'
Client2='10.0.0.11'
Client3='10.0.0.5'
Middle1='10.0.0.6'
Middle2='10.0.0.4'
Server1='10.0.0.8'
Server2='10.0.0.10'
Server3='10.0.0.9'

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "ping -w 5 ${Middle1} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom1to4.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "ping -w 5 ${Middle1} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom2to4.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "ping -w 5 ${Middle1} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom3to4.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "pkill -9 ping"

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "ping -w 5 ${Middle2} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom1to5.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "ping -w 5 ${Middle2} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom2to5.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "ping -w 5 ${Middle2} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom3to5.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip1.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip2.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip3.westeurope.cloudapp.azure.com "pkill -9 ping"


ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "ping -w 5 ${Server1} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom4to6.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "ping -w 5 ${Server1} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom5to6.log "

sleep 10

ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "pkill -9 ping"


ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "ping -w 5 ${Server2} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom4to7.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "ping -w 5 ${Server2} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom5to7.log "

sleep 10

ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "pkill -9 ping"


ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "ping -w 5 ${Server3} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom4to8.log "

ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "ping -w 5 ${Server3} > ./asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/pingfrom5to8.log "

sleep 10

ssh foraslvms@storesfm3ou7qrpiuisshpublicip4.westeurope.cloudapp.azure.com "pkill -9 ping"
ssh foraslvms@storesfm3ou7qrpiuisshpublicip5.westeurope.cloudapp.azure.com "pkill -9 ping"

