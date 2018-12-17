#!/bin/bash


user="fla/Desktop"
#user="foraslvms"

EXP="Exp_5"
EXP_DIR="5.2"
num_e="5"

read -p "Did you start the servers on port 11212 ? "

echo "Testing bandwidths"
cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andM1 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C2andM1 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C3andM1 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C1andM2 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C2andM2 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_C3andM2 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M1andS1 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M1andS2 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M1andS3 ${num_e} ${EXP_DIR}"
$cmd


cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M2andS1 5 ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M2andS2 ${num_e} ${EXP_DIR}"
$cmd

cmd=" /home/${user}/asl-fall18-project/Experiments/IPERF/test_bandwidth_between_M2andS3 ${num_e} ${EXP_DIR}"
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
