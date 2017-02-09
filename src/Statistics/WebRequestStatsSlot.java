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
public class WebRequestStatsSlot {
 
    int slot;
    
    double responseTime[][]; // responseTime[p][s] p:provider, s:service
    int numberOfRequests[][]; // numberOfRequests[p][s] p:provider, s:service
   
    
    public WebRequestStatsSlot(int slot,int pi, int si) {
        
        this.slot = slot;
        
        responseTime=new double[pi][si];
        numberOfRequests=new int[pi][si];
        
        for (int p = 0; p < pi; p++) {
            for (int s= 0; s < si; s++) {
                responseTime[p][s]=0;
                numberOfRequests[p][s]=0;
            }
        }
    }

    public int getSlot() {
        return slot;
    }

    public double[][] getResponseTime() {
        return responseTime;
    }

    public int[][] getNumberOfRequests() {
        return numberOfRequests;
    }
    
    


    
    
    
}
