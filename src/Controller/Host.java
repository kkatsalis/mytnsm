/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Enumerators.EMachineTypes;
import Statistics.HostStats;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    
    HostStats _hostStats; // A number of measurements is taken for every Host per Slot
    
    CopyOnWriteArrayList<VM> _VMs;    // A list with VMs per Host Machine;
    
    public Host(int hostId,Configuration config) {
        
        this.hostID=hostId;
        this._config=config;
        this.ip=String.valueOf(_config.host_machine_config[hostId].get("ip"));
        this.cpu=Integer.valueOf((String)_config.host_machine_config[hostId].get("cpu"));
        this.memory=Integer.valueOf((String)_config.host_machine_config[hostId].get("memory"));  
        this.storage=Integer.valueOf((String)_config.host_machine_config[hostId].get("storage"));
        this.bandwidth=Integer.valueOf((String)_config.host_machine_config[hostId].get("bandwidth"));
        
        
        this._hostStats=new HostStats(); 
        this._VMs=new CopyOnWriteArrayList<>();     // A list with VMs per Host Machine;
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

	public HostStats get_hostStats() {
		return _hostStats;
	}

	public CopyOnWriteArrayList<VM> get_VMs() {
		return _VMs;
	}

    
    
    

    
    
    
}
