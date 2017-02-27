/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author kostas
 */
public class Host {

    Configuration _config;
    
    int hostID;
    String ip;
    int cpu;
    int memory;
    int storage;
    int bandwidth;
    
    public Host(int hostId,Configuration config) {
        
        this.hostID=hostId;
        this._config=config;
        this.ip=String.valueOf(_config.host_machine_config[hostId].get("ip"));
        this.cpu=Integer.valueOf((String)_config.host_machine_config[hostId].get("cpu"));
        this.memory=Integer.valueOf((String)_config.host_machine_config[hostId].get("memory"));  
        this.storage=Integer.valueOf((String)_config.host_machine_config[hostId].get("storage"));
        this.bandwidth=Integer.valueOf((String)_config.host_machine_config[hostId].get("bandwidth"));
        

    }

	public int getHostID() {
		return hostID;
	}

	public String getIp() {
		return ip;
	}

	public int getCpu() {
		return cpu;
	}

	public int getMemory() {
		return memory;
	}

	public int getStorage() {
		return storage;
	}

	public int getBandwidth() {
		return bandwidth;
	}



    
    
    

    
    
    
}
