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
    double average_response_time; // responseTime[p][s] p:provider, s:service
    int numberOfRequests; // numberOfRequests[p][s] p:provider, s:service
   
    public int getSlot() {
        return slot;
    }

	public double getAverage_response_time() {
		return average_response_time;
	}

	public void setAverage_response_time(double average_response_time) {
		this.average_response_time = average_response_time;
	}

	public int getNumberOfRequests() {
		return numberOfRequests;
	}

	public void setNumberOfRequests(int numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

    
    
    
  
    
    


    
    
    
}
