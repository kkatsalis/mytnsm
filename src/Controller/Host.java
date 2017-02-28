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
    int cpu_cores;
    int cpu_power;
    int memory;
    int storage;
    int bandwidth;
    
    public Host(int hostId,Configuration config) {
        
        this.hostID=hostId;
        this._config=config;
        this.ip=String.valueOf(_config.getHost_machine_config()[hostId].get("ip"));
        this.cpu_cores=Integer.valueOf((String)_config.getHost_machine_config()[hostId].get("cpu_cores"));
        this.cpu_power=Integer.valueOf((String)_config.getHost_machine_config()[hostId].get("cpu_power"));
        this.memory=Integer.valueOf((String)_config.getHost_machine_config()[hostId].get("memory"));  
        this.storage=Integer.valueOf((String)_config.getHost_machine_config()[hostId].get("storage"));
        this.bandwidth=Integer.valueOf((String)_config.getHost_machine_config()[hostId].get("bandwidth"));
        

    }

	public int getHostID() {
		return hostID;
	}

	public String getIp() {
		return ip;
	}

	

	public Configuration get_config() {
		return _config;
	}

	public int getCpu_cores() {
		return cpu_cores;
	}

	public int getCpu_power() {
		return cpu_power;
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
