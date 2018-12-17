#!/bin/bash

# Bandwidth for Experiment 2.1

user="fla/Desktop"
#user="foraslvms"


read -p "Did you start the servers on port 11212 ? "

echo "Testing bandwidths"
cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andS1 2 2.1"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C2andS1 2 2.1"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C3andS1 2 2.1"
$cmd

echo "Sending ping"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.1/ping_bash.sh"
$cmd

echo "Running Write_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.1/W_only/WriteOnly"
$cmd


echo "Running Read_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.1/R_only/ReadOnly"
$cmd
