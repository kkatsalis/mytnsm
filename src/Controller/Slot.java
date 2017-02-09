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
    
    int id;
    long startTime;
    long endTime;
    Configuration _config;
    List<ServiceRequest>[] _serviceRequests2Activate; //list per provider
    List<ServiceRequest>[] _serviceRequests2Remove;   //list per provider
    
    List<VMRequest>[] _vmRequests2Activate; //list per provider
    List<VMRequest>[] _vmRequests2Remove;   //list per provider
    
    public Slot(int id,Configuration config){
    
        _config=config;
        _serviceRequests2Activate=new ArrayList[config.getProvidersNumber()];
        _serviceRequests2Remove=new ArrayList[config.getProvidersNumber()];

        _vmRequests2Activate=new ArrayList[config.getProvidersNumber()];
        _vmRequests2Remove=new ArrayList[config.getProvidersNumber()];
                 
        for (int i = 0; i < config.getProvidersNumber(); i++) {
            _serviceRequests2Activate[i]=new ArrayList<>();
            _serviceRequests2Remove[i]=new ArrayList<>();
            _vmRequests2Activate[i]=new ArrayList<>();
            _vmRequests2Remove[i]=new ArrayList<>();
       }
    }

    public void clearSlotHeapLists(){
    
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            _serviceRequests2Activate[i]=null;
        }
        _serviceRequests2Activate=null;
        
        
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            _serviceRequests2Remove[i]=null;
        }
        _serviceRequests2Remove=null;
    }
    
    public int getId() {
        return id;
    }

    public List<ServiceRequest>[] getServiceRequests2Activate() {
        return _serviceRequests2Activate;
    }

    public List<ServiceRequest>[] getServiceRequests2Remove() {
        return _serviceRequests2Remove;
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

    public List<VMRequest>[] getVmRequests2Activate() {
        return _vmRequests2Activate;
    }

    public void setVmRequests2Activate(List<VMRequest>[] _vmRequests2Activate) {
        this._vmRequests2Activate = _vmRequests2Activate;
    }

    public List<VMRequest>[] getVmRequests2Remove() {
        return _vmRequests2Remove;
    }

    public void setVmRequests2Remove(List<VMRequest>[] _vmRequests2Remove) {
        this._vmRequests2Remove = _vmRequests2Remove;
    }

   
    
            
}
