/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

/**
 *
 * @author kostas
 */
public class SimulatorStats {
    
    int slot;
    double netBenefit;
    
    int[] vmsRequested; //per provider from beggining of time
    int[] vmsSatisfied; //per provider from beggining of time
    int[] vmsDeleted; //per provider from beggining of time
    int[] smallVmsRequested; //per provider from beggining of time
    int[] smallVmsSatisfied; //per provider from beggining of time
    int[] mediumVmsRequested; //per provider from beggining of time
    int[] mediumVmsSatisfied; //per provider from beggining of time
    int[] largeVmsRequested; //per provider from beggining of time
    int[] largeVmsSatisfied; //per provider from beggining of time
    int[] numberOfActiveVMs; //per provider from beggining of time
    
    int[] activeVMsSlot;
    int[] vmsRequestedSlot; //per provider
    int[] vmsSatisfiedSlot; //per provider
    int[] smallVmsRequestedSlot; //per provider
    int[] smallVmsSatisfiedSlot; //per provider
    int[] mediumVmsRequestedSlot; //per provider
    int[] mediumVmsSatisfiedSlot; //per provider
    int[] largeVmsRequestedSlot; //per provider
    int[] largeVmsSatisfiedSlot; //per provider
    int[] numberOfActiveVMsSlot; //per provider
    

    public SimulatorStats(int numberOfProviders){
    
        smallVmsRequestedSlot=new int[numberOfProviders]; //per provider
        smallVmsSatisfiedSlot=new int[numberOfProviders]; //per provider
        mediumVmsRequestedSlot=new int[numberOfProviders]; //per provider
        mediumVmsSatisfiedSlot=new int[numberOfProviders]; //per provider
        largeVmsRequestedSlot=new int[numberOfProviders]; //per provider
        largeVmsSatisfiedSlot=new int[numberOfProviders]; //per provider
        numberOfActiveVMsSlot=new int[numberOfProviders]; //per provider
        vmsRequestedSlot=new int[numberOfProviders]; //per provider
        vmsSatisfiedSlot=new int[numberOfProviders]; //per provider
        activeVMsSlot=new int[numberOfProviders]; //per provider
        
        smallVmsRequested=new int[numberOfProviders]; //per provider
        smallVmsSatisfied=new int[numberOfProviders]; //per provider
        mediumVmsRequested=new int[numberOfProviders]; //per provider
        mediumVmsSatisfied=new int[numberOfProviders]; //per provider
        largeVmsRequested=new int[numberOfProviders]; //per provider
        largeVmsSatisfied=new int[numberOfProviders]; //per provider
        numberOfActiveVMs=new int[numberOfProviders]; //per provider
        vmsRequested=new int[numberOfProviders]; //per provider
        vmsSatisfied=new int[numberOfProviders]; //per provider
        vmsDeleted=new int[numberOfProviders]; //per provider
    }

    public int[] getActiveVMsSlot() {
        return activeVMsSlot;
    }

    
    public int[] getVmsDeleted() {
        return vmsDeleted;
    }
    
    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

  

    public double getNetBenefit() {
        return netBenefit;
    }

    public void setNetBenefit(double netBenefit) {
        this.netBenefit = netBenefit;
    }

    public int[] getVmsRequested() {
        return vmsRequested;
    }

    public int[] getVmsSatisfied() {
        return vmsSatisfied;
    }

    public int[] getSmallVmsRequested() {
        return smallVmsRequested;
    }

    public int[] getSmallVmsSatisfied() {
        return smallVmsSatisfied;
    }

    public int[] getMediumVmsRequested() {
        return mediumVmsRequested;
    }

    public int[] getMediumVmsSatisfied() {
        return mediumVmsSatisfied;
    }

    public int[] getLargeVmsRequested() {
        return largeVmsRequested;
    }

    public int[] getLargeVmsSatisfied() {
        return largeVmsSatisfied;
    }

    public int[] getNumberOfActiveVMs() {
        return numberOfActiveVMs;
    }

    public int[] getVmsRequestedSlot() {
        return vmsRequestedSlot;
    }

    public int[] getVmsSatisfiedSlot() {
        return vmsSatisfiedSlot;
    }

    public int[] getSmallVmsRequestedSlot() {
        return smallVmsRequestedSlot;
    }

    public int[] getSmallVmsSatisfiedSlot() {
        return smallVmsSatisfiedSlot;
    }

    public int[] getMediumVmsRequestedSlot() {
        return mediumVmsRequestedSlot;
    }

    public int[] getMediumVmsSatisfiedSlot() {
        return mediumVmsSatisfiedSlot;
    }

    public int[] getLargeVmsRequestedSlot() {
        return largeVmsRequestedSlot;
    }

    public int[] getLargeVmsSatisfiedSlot() {
        return largeVmsSatisfiedSlot;
    }

    public int[] getNumberOfActiveVMsSlot() {
        return numberOfActiveVMsSlot;
    }

   


  
    
}
