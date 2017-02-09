/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Statistics.ABStats;
import Statistics.NetRateStats;
import Statistics.VLCStats;
import java.util.List;

/**
 *
 * @author kostas
 */
public class WebClient {
    
    Configuration _config;
    String _clientName;
    String _apHostName;
    int _providerID;
    ABStats abStats;
    VLCStats vlcStats;
    NetRateStats netRateStats;
    Controller _controller;
    
    public WebClient(Configuration config,int clientID,int providerID, String clientName, Controller controller) {
        
        this._config=config;
        
        this._clientName=clientName;
        this._providerID=providerID;
        this._apHostName=String.valueOf(config.getAssociatedAPsPerClient().get("client_"+clientID+"_ap"));
        this._controller=controller;
        
        abStats=new ABStats();
        vlcStats=new VLCStats();
        netRateStats=new NetRateStats();
    }

    public int getProviderID() {
        return _providerID;
    }

    public String getClientName() {
        return _clientName;
    }

    public String getApName() {
        return _apHostName;
    }

   
    public ABStats getAbStats() {
        return abStats;
    }

    public VLCStats getVlcStats() {
        return vlcStats;
    }

    public NetRateStats getNetRateStats() {
        return netRateStats;
    }
        
    
}
