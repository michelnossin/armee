## Armee
Load testing tool for Big Data Hub. Work in progress. 

DISCLAIMER: Use at your own risk

###To build:
```
Clone the repository.
cd <cloned dir>
./build.sh
```

###To configure:
```
Change $HOME/armee/config.yaml
```

### Optional: distribute installation
````
cp -rp $HOME/armee <othernode>:<home_dir_user>/armee>
````
###To Start:
```
cd $HOME/armee
start_master.sh (once)
start_worker.sh (on 1 or more hosts, eg on the master host or some other host(s))
```
###To use:
```
start_shell.sh (once, on any host)
```