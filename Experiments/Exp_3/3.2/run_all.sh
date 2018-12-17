#!/bin/bash


user="fla/Desktop"
#user="foraslvms"

EXP="Exp_3"
EXP_DIR="3.2"


read -p "Did you start the servers on port 2222 ? "

echo "Testing bandwidths"
cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andM1 3 3.2"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C2andM1 3 3.2"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C3andM1 3 3.2"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M1andS1 3 3.2"
$cmd

echo "Sending ping"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/ping_bash.sh"
$cmd

echo "Running Write_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/W_only/WriteOnly"
$cmd


echo "Running Read_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/ReadOnly"
$cmd
