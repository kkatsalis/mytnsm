/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author kostas
 */
public class ServiceRequestRates {
    
    int providerID;
    int serviceID;
    int numberOfRequests;
    String serviceName;
    
    HashMap _lifeTimeConfig;
    HashMap _requestRateConfig;
    HashMap _webRequestsArrivalRateConfig;
    HashMap _localServiceRateConfig;
    HashMap _cloudServiceRateConfig;

    public ServiceRequestRates(int providerID, int serviceID,int numberOfRequests,String serviceName) {
        
        this.providerID=providerID;
        this.serviceID=serviceID;
        this.numberOfRequests=numberOfRequests;
        this.serviceName=serviceName;
        
        this._lifeTimeConfig=new HashMap();
        this._requestRateConfig=new HashMap();
        this._webRequestsArrivalRateConfig=new HashMap();
        this._localServiceRateConfig=new HashMap();
        this._cloudServiceRateConfig=new HashMap();
                   
    }

    public HashMap getWebRequestsArrivalRateConfig() {
        return _webRequestsArrivalRateConfig;
    }

    
    public HashMap getLocalServiceTimeConfig() {
        return _localServiceRateConfig;
    }

    public HashMap getCloudServiceTimeConfig() {
        return _cloudServiceRateConfig;
    }

    
    public HashMap getLifeTimeConfig() {
        return _lifeTimeConfig;
    }

    public HashMap getRequestRateConfig() {
        return _requestRateConfig;
    }

    
    
    public String getServiceName() {
        return serviceName;
    }

    public int getProviderID() {
        return providerID;
    }

    public void setProviderID(int providerID) {
        this.providerID = providerID;
    }

    public int getServiceID() {
        return serviceID;
    }

    public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    public void setNumberOfRequests(int numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

   
    
    
}
