package Controller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import Cplex.SchedulerData;
import Enumerators.UpdateType;
import Utilities.Utilities;

public class GenericScheduler {

	Configuration config;

	int hosts_number;
	int providers_number;
	int vm_types_number;
	int services_number;
	int resources_number;
	int[][] reserved_resources; //[n][R]
	Controller controller;
	
	
	public GenericScheduler(Configuration confg, Controller controller) {
		super();
		this.config = confg;
		this.controller=controller;
		hosts_number=confg.getHosts_number();
		providers_number=confg.getProviders_number();
		vm_types_number=confg.getVm_types_number();
		services_number=confg.getServices_number();
		resources_number=config.getResources_number();

	
	}

	//	 vmRequestMatrix[p][v][s] 
	public int[][][][] FirstFit(int[][][] vmRequestsMatrix) {

		int[][][][] activationMatrix = new int[hosts_number][providers_number][vm_types_number][services_number];

		int vms_requested = 0;
		boolean checkIfFits = false;
		int vms_examined;

		for (int p = 0; p < providers_number; p++) {
			for (int v = 0; v < vm_types_number; v++) {
				for (int s = 0; s < services_number; s++) {
					vms_requested= vmRequestsMatrix[p][v][s];

					vms_examined = 0;

					while (vms_examined < vms_requested) {

						for (int n = 0; n < hosts_number; n++) {
							checkIfFits = checkIftheVMFits(n,v);

							if (checkIfFits) {
								activationMatrix[n][p][v][s]++;
								controller.getRunning_allocations()[n][p][v][s]++;
							
								break;
							}
						}
						vms_examined++;

					}

				}

			}

		}

		return activationMatrix;

	}

		





	@SuppressWarnings("rawtypes")
	private boolean checkIftheVMFits(int n,int v) {


		int[] resource_cost = new int[resources_number];
		int[][] reserved_resources=new int[hosts_number][resources_number];
		
	
		
		boolean fits=true;
		int[] host_max_capacity= Utilities.getHostMaxCapacity(config, n);
		int load;

		int vms_number=0;
		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				vms_number=controller.getRunning_allocations()[n][p][v][s];
				for (int r = 0; r < resources_number; r++) {
					reserved_resources[n][r]+=vms_number*Utilities.getVmResourceCost(config, v, r);
				}
			}
		}
		
		for (int r = 0; r < resources_number; r++) {

			load=reserved_resources[n][r]+resource_cost[r];

			if(load>host_max_capacity[r]){
				fits=false;
				System.out.println("Resource capacity violation: "+r);
			}
		}

		return fits;

	}

	





}
