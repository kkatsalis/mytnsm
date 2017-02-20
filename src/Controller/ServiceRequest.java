/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author kostas
 */
public class ServiceRequest {

	static int requestId = 0;

	int slotStart;
	int slotEnd;

	int providerID;
	int serviceID;
	int lifetime; // in Slots

	public ServiceRequest(int providerID, int serviceID, int lifetime) {

		this.providerID = providerID;
		this.lifetime = lifetime;
		this.serviceID = serviceID;

		requestId++;

	}

	public int getServiceID() {
		return serviceID;
	}

	public int getProviderID() {
		return providerID;
	}

	public int getSlotStart() {
		return slotStart;
	}

	public void setSlotStart(int slotStart) {
		this.slotStart = slotStart;
	}

	public int getSlotEnd() {
		return slotEnd;
	}

	public void setSlotEnd(int slotEnd) {
		this.slotEnd = slotEnd;
	}

	public int getLifetime() {
		return lifetime;
	}

	public int getRequestId() {
		return requestId;
	}


}
