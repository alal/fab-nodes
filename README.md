# fab-nodes  

Batch manage nodes from hourly billing vps provider for [Fabric](http://www.fabfile.org/).  
You can use it for other tools(eg. [salt-ssh](https://docs.saltstack.com/en/latest/topics/ssh/index.html)) too, you may need to edit the format.  

Supports: [DigitalOcean](https://www.digitalocean.com/?refcode=d42615d39f8d) [Vultr](http://www.vultr.com/?ref=6816232).  
Capable of: list, create, delete, reboot, rebuild, shutdown, boot, list sshkeys.  

Output fomart:  
```  
'root@196.200.123.138',  # node-1    active 512mb nyc1  
'root@160.244.216.16',   # node-2    active 512mb nyc2  
'root@102.132.187.14',   # node-3    active 512mb nyc3  
'root@192.242.241.233',  # node-4    active 512mb sfo1  
'root@129.197.109.71',   # node-5    active 512mb sgp1  
'root@198.213.114.167',  # node-6    active 512mb nyc1  
'root@108.172.175.130',  # node-7    active 512mb nyc2  
```  
Paste those in your fabfile.py.  


### Usage  

If you don't want to play with scala, just run the the jar file with java:  
```  
java -jar fab-nodes-assembly-0.1.jar -m list -v vultr  
 ```  
Generate a token/key from you provider's control panel and export them before you start.  
```  
export DO_TOKEN=<your DO TOKEN>  # DO for DigitalOcean, VT for Vultr, set the one you are using.  
export VT_KEY=<your VT KEY>  
```  
fab-nodes 0.1  
Usage: fab-nodes [options]  

  -f | --hasFilter  
        filter list result  
  -s <value> | --start <value>  
        start is low No.(included)  
  -e <value> | --end <value>  
        end is the high No. (included)  
  -p <value> | --prefix <value>  
        prefix like yorg/testing  
  -m <value> | --mode <value>  
        list create delete reboot rebuild shutdown boot sshkeys  
  -v <value> | --provider <value>  
        digitalocean vultr  
  --help  
        prints this usage text  

If you need create new nodes, sshkey ID is needed(add a SSH Key to control panel first).  
```  
export DO_SSHKEYID=<the DO sshkey ID form '-m sshkeys -v digitalocean'>  
export VT_SSHKEYID=<the VT sshkey ID form '-m sshkeys -v vultr'>  
```  
