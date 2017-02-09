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

    int hostID;
    Configuration _config;
    String _nodeName;
    
    CopyOnWriteArrayList<VM> _VMs;    // A list with VMs per Host Machine;
    
    HostStats _hostStats; // A number of measurements is taken for every Host per Slot
    Resources _resources;
    
    public Host(int hostId,Configuration config,String nodeName) {
        
        this.hostID=hostId;
        this._config=config;
        this._nodeName=nodeName;
    
        this._VMs=new CopyOnWriteArrayList<>();     // A list with VMs per Host Machine;
        this._resources=new Resources();
        
        loadResourcesSpecification();
    }

    
    
    
    public void createNewStatsObject(){
        this._hostStats=new HostStats();
    }
    public String getNodeName() {
        return _nodeName;
    }

    public HostStats getHostStats() {
        return _hostStats;
    }

    public CopyOnWriteArrayList<VM> getVMs() {
        return _VMs;
    }

    public int getHostID() {
        return hostID;
    }

    private void loadResourcesSpecification() {
        
        _resources.setCpu(_config.getCpu_host());
        _resources.setMemory(_config.getMemory_host());
        _resources.setStorage(_config.getStorage_host());
        _resources.setBandwidth(_config.getBandwidth_host());
        
    }

    
    
    
}
