#!/bin/sh
rm -rf $HOME/armee/*
mkdir $HOME/armee
mkdir $HOME/armee/target
sbt clean
sbt fastOptJS
sbt update
sbt assembly
cp -rp src/main/resources/config.yaml $HOME/armee
cp -rp src/main/scala/io/armee/scripts/* $HOME/armee
cp -rp target/scala-2.11/* $HOME/armee/target
mv $HOME/armee/target/armee-assembly-0.0.1-SNAPSHOT.jar $HOME/armee/armee.jar
cp LICENSE $HOME/armee
chmod 700 $HOME/armee/start_master.sh
chmod 700 $HOME/armee/start_worker.sh
chmod 700 $HOME/armee/start_shell.sh
echo
echo Go to $HOME/armee , run start_master.sh first, start_worker.sh second and finally start_shell.sh

