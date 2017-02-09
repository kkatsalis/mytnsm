/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

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
public class DBUtilities {
    
    DBClass _db;
    Host[] _hosts;
    WebUtilities _webUtilities;

    Configuration _config;
    
    public DBUtilities(Host[] _hosts, WebUtilities _webUtilities,DBClass db,Configuration config) {
        
        this._db = db;
        this._hosts = _hosts;
        this._webUtilities = _webUtilities;
        _config=config;
    }
     
    
    public void updateAllHostStatistics(int slot, int measurement) throws IOException, JSONException {
       
        
        for (int i = 0; i < _hosts.length; i++) {
            
            Hashtable parameters=_webUtilities.retrieveStatsHost(_hosts[i].getNodeName(), slot,measurement);
            
            if(parameters!=null){
                _hosts[i].getHostStats().setSlot(String.valueOf(parameters.get("slot")));
                _hosts[i].getHostStats().setMeasurement(String.valueOf(parameters.get("measurement")));
                _hosts[i].getHostStats().setHostname(String.valueOf(parameters.get("Hostname")));
                _hosts[i].getHostStats().setTime(String.valueOf(parameters.get("Time")));
                _hosts[i].getHostStats().setArch(String.valueOf(parameters.get("Arch")));
                _hosts[i].getHostStats().setPhysical_CPUs(String.valueOf(parameters.get("Physical_CPUs")));
                _hosts[i].getHostStats().setCount(String.valueOf(parameters.get("Count")));
                _hosts[i].getHostStats().setRunning(String.valueOf(parameters.get("Running")));
                _hosts[i].getHostStats().setBlocked(String.valueOf(parameters.get("Blocked")));
                _hosts[i].getHostStats().setPaused(String.valueOf(parameters.get("Paused")));
                _hosts[i].getHostStats().setShutdown(String.valueOf(parameters.get("Shutdown")));
                _hosts[i].getHostStats().setShutoff(String.valueOf(parameters.get("Shutoff")));
                _hosts[i].getHostStats().setCrashed(String.valueOf(parameters.get("Crashed")));
                _hosts[i].getHostStats().setActive(String.valueOf(parameters.get("Active")));
                _hosts[i].getHostStats().setInactive(String.valueOf(parameters.get("Inactive")));
                _hosts[i].getHostStats().setCPU_percentage(String.valueOf(parameters.get("CPU_percentage")));
                _hosts[i].getHostStats().setTotal_hardware_memory_KB(String.valueOf(parameters.get("Total_hardware_memory_KB")));
                _hosts[i].getHostStats().setTotal_memory_KB(String.valueOf(parameters.get("Total_memory_KB")));
                _hosts[i].getHostStats().setTotal_guest_memory_KB(String.valueOf(parameters.get("Total_guest_memory_KB")));                    

                List<NetRateStats> interfaces=(List<NetRateStats>)parameters.get("netRates");

                for (int j = 0; j < interfaces.size(); j++) {
                    _hosts[i].getHostStats().getInterfacesRates().add(new NetRateStats());

                    _hosts[i].getHostStats().getInterfacesRates().get(j).setInterface(interfaces.get(j).getInterface());
                    _hosts[i].getHostStats().getInterfacesRates().get(j).setTimeStamp(interfaces.get(j).getTimeStamp());
                    _hosts[i].getHostStats().getInterfacesRates().get(j).setKbps_in(interfaces.get(j).getKbps_in());
                    _hosts[i].getHostStats().getInterfacesRates().get(j).setKbps_out(interfaces.get(j).getKbps_out());
                }
            }
            
        }
        
        
    }
    public void updateAllHostStatistics2DB(int slot,int _currentInstance){
        
        for (int i = 0; i < _hosts.length; i++) {
              
            String[] data = {
                            
                String.valueOf(slot),
                String.valueOf(_currentInstance),
                _hosts[i].getHostStats().getTime(),
                _hosts[i].getHostStats().getHostName(),
                _hosts[i].getHostStats().getArch(),
                _hosts[i].getHostStats().getPhysical_CPUs(),
                _hosts[i].getHostStats().getCount(),
                _hosts[i].getHostStats().getRunning(),
                _hosts[i].getHostStats().getBlocked(),
                _hosts[i].getHostStats().getPaused(),
                _hosts[i].getHostStats().getShutdown(),
                _hosts[i].getHostStats().getShutoff(),
                _hosts[i].getHostStats().getCrashed(),
                _hosts[i].getHostStats().getActive(),
                _hosts[i].getHostStats().getInactive(),
                _hosts[i].getHostStats().getCPU_percentage(),
                _hosts[i].getHostStats().getTotal_hardware_memory_KB(),
                _hosts[i].getHostStats().getTotal_memory_KB(),
                _hosts[i].getHostStats().getTotal_guest_memory_KB()
              
             };
            
            _db.getMp_hostStats().inject(data);
    
        }
    
    
    
    
    }
    public void updateAllHostStatisticsInterfacesDB(int slot, int measurement){
    
        for (int i = 0; i < _hosts.length; i++) {
            
            List<NetRateStats> interfaces=_hosts[i].getHostStats().getInterfacesRates();

                for (int j = 0; j < interfaces.size(); j++) {
                    
                    String[] data = { 
                    String.valueOf(slot),
                    String.valueOf(measurement),
                    String.valueOf(_hosts[i].getNodeName()),    
                    _hosts[i].getHostStats().getInterfacesRates().get(j).getInterface(),
                    _hosts[i].getHostStats().getInterfacesRates().get(j).getTimeStamp(),
                    String.valueOf(_hosts[i].getHostStats().getInterfacesRates().get(j).getKbps_in()),
                    String.valueOf(_hosts[i].getHostStats().getInterfacesRates().get(j).getKbps_out())
                    };
                    
                      _db.getMp_hostIinterfaceStats().inject(data);
                }
     
    
        }
    
    }
   
    public void updateActiveVMStatistics(int slot, int currentInstance) throws IOException {
           
        System.out.println("instance: "+currentInstance);
           
        for (int i = 0; i < _hosts.length; i++) {
            List<VMStats> _allVMStats=_webUtilities.retrieveStatsVMsPerHost(_hosts[i].getNodeName(), slot, currentInstance);
            
            for (Iterator<VMStats> iterator = _allVMStats.iterator(); iterator.hasNext();) {
                VMStats next = iterator.next();
                
               //Find the VM and add stats
                for (int j = 0; j < _hosts[i].getVMs().size(); j++) {
                    if(_hosts[i].getVMs().get(j).getVmName().equals(next.getDomain_name())){
                        _hosts[i].getVMs().get(j).setStats(next);
                    
                        updateActiveVMStatistics2DB(slot, currentInstance,_hosts[i].getNodeName(),next);
                    }
                }
                
            }
            
        }
           
    }
    
    private void updateActiveVMStatistics2DB(int slot, int measurement,String hostName,VMStats vmStats){
    
     String[] data = { 
                    String.valueOf(slot),
                    String.valueOf(measurement),
                    String.valueOf(hostName),    
                    String.valueOf(vmStats.getDomain_ID()),    
                    String.valueOf(vmStats.getDomain_name()),    
                    String.valueOf(vmStats.getCPU_ns()),    
                    String.valueOf(vmStats.getCPU_percentage()),    
                    String.valueOf(vmStats.getMem_bytes()),    
                    String.valueOf(vmStats.getMem_percentage()),    
                    String.valueOf(vmStats.getBlock_RDRQ()),    
                    String.valueOf(vmStats.getBlock_WRRQ()),    
                    String.valueOf(vmStats.getNet_RXBY()),    
                    String.valueOf(vmStats.getNet_TXBY()),
                    };
                    
                      _db.getMp_vmStats().inject(data);
    }
    
    
     public void updateABStatistics2DB(int slot, int measurement, ABStats abStats){
    
     String[] data = { 
                    String.valueOf(slot),
                    String.valueOf(measurement),
                    String.valueOf(abStats.getClientName()),
                    String.valueOf(abStats.getServer_Software()),
                    String.valueOf(abStats.getServer_Hostname()),
                    String.valueOf(abStats.getDocument_Path()),
                    String.valueOf(abStats.getServer_Port()),
                    String.valueOf(abStats.getDocument_Length_bytes()),
                    String.valueOf(abStats.getConcurrency_Level()),
                    String.valueOf(abStats.getComplete_requests()),
                    String.valueOf(abStats.getFailed_requests_number()),
                    String.valueOf(abStats.getFailed_requests_Connect()),
                    String.valueOf(abStats.getFailed_requests_Receive()),
                    String.valueOf(abStats.getFailed_requests_Length()),
                    String.valueOf(abStats.getFailed_requests_Exceptions()),
                    String.valueOf(abStats.getNon_2xx_responses()),
                    String.valueOf(abStats.getKeep_Alive_requests()),
                    String.valueOf(abStats.getTotal_transferred_bytes()),
                    String.valueOf(abStats.getHTML_transferred_bytes()),
                    String.valueOf(abStats.getTime_taken_for_tests_seconds()),
                    String.valueOf(abStats.getRequests_per_second_mean()),
                    String.valueOf(abStats.getTime_per_request_mean()),
                    String.valueOf(abStats.getTime_per_request_mean_across_all_concurrent_requests()),
                    String.valueOf(abStats.getTransfer_rate_received()),
                    String.valueOf(abStats.getConnection_Times_Connect_min()),
                    String.valueOf(abStats.getConnection_Times_Connect_mean()),
                    String.valueOf(abStats.getConnection_Times_Connect_sd()),
                    String.valueOf(abStats.getConnection_Times_Connect_median()),
                    String.valueOf(abStats.getConnection_Times_Connect_max()),
                    String.valueOf(abStats.getConnection_Times_Processing_min()),
                    String.valueOf(abStats.getConnection_Times_Processing_mean()),
                    String.valueOf(abStats.getConnection_Times_Processing_sd()),
                    String.valueOf(abStats.getConnection_Times_Processing_median()),
                    String.valueOf(abStats.getConnection_Times_Processing_max()),
                    String.valueOf(abStats.getConnection_Times_Waiting_min()),
                    String.valueOf(abStats.getConnection_Times_Waiting_mean()),
                    String.valueOf(abStats.getConnection_Times_Waiting_sd()),
                    String.valueOf(abStats.getConnection_Times_Waiting_median()),
                    String.valueOf(abStats.getConnection_Times_Waiting_max()),
                    String.valueOf(abStats.getConnection_Times_Total_min()),
                    String.valueOf(abStats.getConnection_Times_Total_mean()),
                    String.valueOf(abStats.getConnection_Times_Total_sd()),
                    String.valueOf(abStats.getConnection_Times_Total_median()),
                    String.valueOf(abStats.getConnection_Times_Total_max()),
                    String.valueOf(abStats.getPercentage_50()),
                    String.valueOf(abStats.getPercentage_66()),
                    String.valueOf(abStats.getPercentage_75()),
                    String.valueOf(abStats.getPercentage_80()),
                    String.valueOf(abStats.getPercentage_90()),
                    String.valueOf(abStats.getPercentage_95()),
                    String.valueOf(abStats.getPercentage_98()),
                    String.valueOf(abStats.getPercentage_99()),
                    String.valueOf(abStats.getPercentage_100())
                    
                    
                    };
                    
                      _db.getMp_webClientABStats().inject(data);
    }
    
     public void updateSimulatorStatistics2DB(int slot, SimulatorStats stats,int providerID){
     
         
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
