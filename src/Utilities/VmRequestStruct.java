/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utilities;

/**
 *
 * @author kostas
 */
public class VmRequestStruct {
    
    int providerID;
    int vm;
    int service;

    public VmRequestStruct(int providerID, int vm, int service) {
        this.providerID = providerID;
        this.vm = vm;
        this.service = service;
    }

    
    public int getProviderID() {
        return providerID;
    }

     public int getVm() {
        return vm;
    }

 
    public int getService() {
        return service;
    }


    
    
}
