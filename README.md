## Armee
Load testing tool for Big Data Hub. Work in progress. 

DISCLAIMER: Use at your own risk

###Prereq (for building yourself)
```
Check if nodejs is installed (node -v)
Install latest domjs module: (npm install domjs or jsdom)
```

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
OR 
http://localhost:1335 in browser (http://<masternode>:<apiport> in config.yaml)
```
