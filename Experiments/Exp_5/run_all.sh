#!/bin/bash


user="fla/Desktop"
#user="foraslvms"

EXP="Exp_5"
EXP_DIR="5.1"


read -p "Did you start the servers on port 11212 ? "



echo "Running Read_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/ReadOnly"
$cmd


EXP="Exp_5"
EXP_DIR="5.2"

echo "Running Read_Only experiments"
cmd="/home/${user}/asl-fall18-project/Experiments/${EXP}/${EXP_DIR}/R_only/ReadOnly"
$cmd

