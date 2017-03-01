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
public class ServiceRequest {

	static int requestId = 0;

	int slotStart;
	int slotEnd;

	int providerID;
	int serviceID;
	int lifetime; // in Slots

	int[] vms_requested; //[v] v=0: small,1 medium, 2 large
	String service_name;
	String charm= "";
	String kvm_machine_to_deploy= "";
	List<String[][]> units; 

	
	@SuppressWarnings("unchecked")
	public ServiceRequest(Configuration config, int providerID, int serviceID, int lifetime) {

		this.providerID = providerID;
		this.lifetime = lifetime;
		this.serviceID = serviceID;

		this.vms_requested=new int [config.getVm_types_number()];
		this.service_name="";
		this.units=new ArrayList<String[][]>();
		
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


	public int[] getVms_requested() {
		return vms_requested;
	}




	public String getService_name() {
		return service_name;
	}


	public String getCharm() {
		return charm;
	}


	public String getKvm_machine_to_deploy() {
		return kvm_machine_to_deploy;
	}


	public List<String[][]> getUnits() {
		return units;
	}

	







}
