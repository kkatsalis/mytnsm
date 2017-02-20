/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author kostas
 */
public class Slot {
    
    int slotId;
    long startTime;
    long endTime;
    Configuration _config;
    
    List<ServiceRequest>[] _services2Activate; //list per provider 
    List<ServiceRequest>[] _services2Remove;   //list per provider
    
    @SuppressWarnings("unchecked")
	public Slot(int id,Configuration config){
    
        _config=config;
        
        _services2Activate=new ArrayList[_config.getProviders_number()];
        _services2Remove=new ArrayList[_config.getProviders_number()];
                 
        for (int i = 0; i < _config.getProviders_number(); i++) {
            _services2Activate[i]=new ArrayList<>();
            _services2Remove[i]=new ArrayList<>();
       }
    }

    
    public int getSlotId() {
        return slotId;
    }
   

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<ServiceRequest>[] getServiceRequests2Activate() {
        return _services2Activate;
    }


    public List<ServiceRequest>[] getServiceRequests2Remove() {
        return _services2Remove;
    }


    
            
}
