#!/bin/bash


user="fla/Desktop"
#user="foraslvms"

EXP="Exp_6"
EXP_DIR="6.1"
num_e="1"

read -p "Did you start the servers on port 11212 ? "


echo "Running Write_Only 1 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/W_only/1Middleware/1_1Middleware_1_Server"
$cmd

echo "Running Write_Only 2 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/W_only/1Middleware/2_1Middleware_3_Server"
$cmd

echo "Running Write_Only 3 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/W_only/2Middleware/3_2Middleware_1_Server"
$cmd

echo "Running Write_Only 4 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/W_only/2Middleware/4_2Middleware_3_Server"
$cmd

echo "Running Read_Only 1 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/1Middleware/1_1Middleware_1_Server"
$cmd

echo "Running Write_Only 2 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/1Middleware/2_1Middleware_3_Server"
$cmd

echo "Running Write_Only 3 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/2Middleware/3_2Middleware_1_Server"
$cmd

echo "Running Write_Only 4 experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/2Middleware/4_2Middleware_3_Server"
$cmd

