#!/bin/sh
rm -rf $HOME/armee/*
mkdir $HOME/armee
sbt update
sbt assembly
cp -rp target/scala-2.12/armee-assembly-0.0.1-SNAPSHOT.jar $HOME/armee/armee.jar
cp -rp src/main/resources/config.yaml $HOME/armee
cp -rp src/main/scala/io/armee/scripts/* $HOME/armee
chmod 700 $HOME/armee/start_master.sh
chmod 700 $HOME/armee/start_worker.sh
chmod 700 $HOME/armee/start_shell.sh
echo
echo Go to $HOME/armee , run start_master.sh first, start_worker.sh second and finally start_shell.sh

