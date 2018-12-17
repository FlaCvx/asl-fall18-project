#!/bin/bash

# Bandwidth for Experiment 2.2

user="fla/Desktop"
#user="aslforvms"

read -p "Did you start the servers on port 11212 ? "

echo "Testing bandwidths"
cmd="/home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andS1  2 2.2"
$cmd

cmd="/home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andS2  2 2.2"
$cmd

echo "Sending ping"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.2/ping_bash.sh"
$cmd

echo "Running Write_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.2/W_only/WriteOnly"
$cmd


echo "Running Read_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/Exp_2/2.2/R_only/ReadOnly"
$cmd
