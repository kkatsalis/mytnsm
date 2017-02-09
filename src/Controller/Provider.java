/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kostas
 */
public class Provider {

    int providerID;
    List<ServiceRequestRates> requestsForService;

    public Provider(int providerID) {
        this.providerID=providerID;
        requestsForService=new ArrayList<ServiceRequestRates>();
    }

    public List<ServiceRequestRates> getRequestsForService() {
        return requestsForService;
    }
    
    
    
}
