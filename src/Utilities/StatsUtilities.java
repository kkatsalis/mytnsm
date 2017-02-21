/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Controller.Host;
import Utilities.WebUtilities;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import org.json.JSONException;

import Controller.*;

/**
 *
 * @author kostas
 */
public class StatsUtilities {
    
    Configuration _config;
    WebUtilities web_utilities;
    
    public StatsUtilities(Configuration config) {
        this._config=config;
        this.web_utilities=new WebUtilities(config);
    }
     
    
    public void updateAllHostStatisObjects(int slot, int measurement) throws IOException, JSONException {
       
        String host_identifier;
        Hashtable host_config;
        
        for (int i = 0; i < _config.getHosts_number(); i++) {
            
        	host_config=_config.getHost_machine_config()[i];
        	host_identifier=(String)host_config.get("ip");
        			
            Hashtable parameters=web_utilities.retrieveHostStats(host_identifier,slot,measurement);
            
            // To Do send to DB
        }
            
        
        
        
    }
   
    public void updateVmStatistics(int slot, int measurement) throws IOException {
          
    	// toDO
   
           
    }
    
   
    
     public void updateSimulatorStatistics(int slot, int measurement ){
     
         
        String[] data = { 
              
                    String.valueOf(_config.getSimulationID()),
                    String.valueOf(_config.getRunID()),
                    String.valueOf(_config.getAlgorithm()),
                
                    String.valueOf(stats.getSlot()),
                    String.valueOf(providerID),
                    String.valueOf(stats.getNetBenefit()),
                    
                    String.valueOf(stats.getVmsRequested()[providerID]),
                    String.valueOf(stats.getVmsSatisfied()[providerID]),
                    String.valueOf(stats.getVmsDeleted()[providerID]),
                    String.valueOf(stats.getSmallVmsRequested()[providerID]),
                    String.valueOf(stats.getSmallVmsSatisfied()[providerID]),
                    String.valueOf(stats.getMediumVmsRequested()[providerID]),
                    String.valueOf(stats.getMediumVmsSatisfied()[providerID]),
                    String.valueOf(stats.getLargeVmsRequested()[providerID]),
                    String.valueOf(stats.getLargeVmsSatisfied()[providerID]),
                    String.valueOf(stats.getNumberOfActiveVMs()[providerID]),
                  
                    String.valueOf(stats.getActiveVMsSlot()[providerID]),
                    String.valueOf(stats.getVmsRequestedSlot()[providerID]),
                    String.valueOf(stats.getVmsSatisfiedSlot()[providerID]),
                    String.valueOf(stats.getSmallVmsRequestedSlot()[providerID]),
                    String.valueOf(stats.getSmallVmsSatisfiedSlot()[providerID]),
                    String.valueOf(stats.getMediumVmsRequestedSlot()[providerID]),
                    String.valueOf(stats.getMediumVmsSatisfiedSlot()[providerID]),
                    String.valueOf(stats.getLargeVmsRequestedSlot()[providerID]),
                    String.valueOf(stats.getLargeVmsSatisfiedSlot()[providerID]),
                    String.valueOf(stats.getNumberOfActiveVMsSlot()[providerID])
                   
                    };
                    
                    _db.getMp_simulatorStats().inject(data);
     
     
     }
     
     public void updateWebClientStatistics2DB(int slot, WebRequestStats stats){
     
        String[] data = { 
            
                    String.valueOf(_config.getSimulationID()),
                    String.valueOf(_config.getRunID()),
                    String.valueOf(_config.getAlgorithm()),
                
                    String.valueOf(stats.getSlot()),
                    String.valueOf(stats.getClientID()),       
                    String.valueOf(stats.getProviderID()),
                    String.valueOf(stats.getServiceID()),
                    String.valueOf(stats.getResponseTime()),
                    String.valueOf(stats.getType())
                    };
                    
                    _db.getMp_webClientServiceStats().inject(data);
     
     
     }

    public void updateWebClientStatistics2DBPerSlot(int slot, WebRequestStatsSlot[] _webRequestStatsSlot) {
        
       int numberOfRequests=0;
       double averageResponseTime=0;
        
        for (int p = 0; p < _config.getProvidersNumber(); p++) {
            for (int s = 0; s < _config.getServicesNumber(); s++) {
                
                numberOfRequests=_webRequestStatsSlot[slot].getNumberOfRequests()[p][s];
                averageResponseTime=_webRequestStatsSlot[slot].getResponseTime()[p][s];
                averageResponseTime=(double)averageResponseTime/numberOfRequests;
                
                   String[] data = { 
            
                    String.valueOf(_config.getSimulationID()),
                    String.valueOf(_config.getRunID()),
                    String.valueOf(_config.getAlgorithm()),
                
                    String.valueOf(slot),
                 
                    String.valueOf(p),
                    String.valueOf(s),
                    String.valueOf(numberOfRequests),
                    String.valueOf(averageResponseTime)
                    };
                    
                    _db.getMp_webClientServiceStatsSlot().inject(data);
                
                
                
            }
        }
        
      
    }
     
     
}
