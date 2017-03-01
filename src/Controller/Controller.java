/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Cplex.CplexResponse;
import Enumerators.ESlotDurationMetric;
import Utilities.WebUtilities;
import Statistics.WebRequestStatsSlot;
import Cplex.Scheduler;
import Cplex.SchedulerData;
import Enumerators.EAlgorithms;
import Enumerators.EStatsUpdateMethod;
import Enumerators.UpdateType;
import Utilities.Utilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostas
 */
public class Controller {

	Configuration _config;
	Slot[] _slots;
	Host[] _hosts;
	Provider[] _providers;

	WebUtilities _webUtilities;

	int _currentInstance = 0;
	int vmIDs = 0;

	SchedulerData _cplexData;
	Scheduler scheduler;
	GenericScheduler generic_scheduler;
	WebRequestStatsSlot[][][] _webRequestSlotStats;


	int[][] _webRequestPattern; // [p][s]: p=provider, s=service

	int hosts_number; 
	int providers_number; 
	int vm_types_number; 
	int services_number; 
	int slots_number;

	Timer stats_timer;

	int[][][] total_requests; 

	Controller(Configuration config, Host[] hosts, Slot[] slots, Provider[] _provider) {

		this._config = config;
		this._slots = slots;
		this._hosts = hosts;

		this.hosts_number = _config.getHosts_number();
		this.providers_number = _config.getProviders_number();
		this.vm_types_number = _config.getVm_types_number();
		this.services_number = _config.getServices_number();
		this.slots_number = _config.getSlotsNumber();
		this._webUtilities = new WebUtilities(config);
		this._cplexData = new SchedulerData(config);
		this._providers = _provider;

		this.generic_scheduler=new GenericScheduler(config);
		initializeController();
		_cplexData.initializeWebRequestMatrix(_webRequestPattern);

	}

	private void initializeController() {

		this._webRequestSlotStats = new WebRequestStatsSlot[_config.getSlotsNumber()][providers_number][services_number];
		this._webRequestPattern = new int[providers_number][services_number];

		for (int slot = 0; slot < slots_number; slot++) {
			for (int p = 0; p < this.providers_number; p++) {
				for (int s = 0; s < this.services_number; s++) {
					_webRequestSlotStats[slot][p][s] = new WebRequestStatsSlot();
				}
			}
		}

		for (int p = 0; p < this.providers_number; p++) {
			for (int s = 0; s < this.services_number; s++) {
				_webRequestPattern[p][s] = (int) _config.getArrivals_generator()[p][s].get("estimated_requests");
			}
		}
		total_requests= new int[providers_number][vm_types_number][services_number];
		for (int p = 0; p < this.providers_number; p++) {
			for (int v = 0; v < this.vm_types_number; v++) {
				for (int s = 0; s < this.services_number; s++) {
					total_requests[p][v][s]=0;
				}
			}
		}
	}

	@SuppressWarnings("unused")
	void Run(int slot) throws IOException {

		System.out.println("******* Slot:" + slot + " Controller Run *******");
		updateServiceRequestPattern(slot);

		scheduler = new Scheduler(_config);

		//		if (false)
		//			startNodesStatsTimer(slot); // for Statistics updates

		try {

			// ----------- Load VM Request Lists
			for (int p = 0; p < this.providers_number; p++) {
				for (ServiceRequest serviceRequest : _slots[slot].getServiceRequests2Activate()[p]) {
					addVmRequestsPerService(serviceRequest, slot);
				}
			}
			int[][][] vmRequestMatrix = loadVMRequestMatrix(slot);
			Utilities.updateRequestStats2Db(slot,_config,vmRequestMatrix,total_requests);

			System.out.println("REQUESTS Matrix:"+Arrays.deepToString(vmRequestMatrix));

			// Load VM Deactivation Matrix
			int[][][][] vms2DeleteMatrix=prepareVmDeleteMatrix(slot);
			System.out.println("DELETE Matrix:"+Arrays.deepToString(vms2DeleteMatrix));
			reduceRunningAllocation(vms2DeleteMatrix);
			destroyServices(slot);


			// Update WebRequest Pattern
			// int[][] requestPattern=Utilities.findRequestPattern(_config);

			// Update CPLEX data Parameters
			_cplexData.updateParameters(_webRequestPattern, vmRequestMatrix, vms2DeleteMatrix);

			// Run CPLEX
			int[][][][] activationMatrix = new int[hosts_number][providers_number][vm_types_number][services_number];

			if (_slots[slot].getServiceRequests2Activate().length > 0) {

				if ((_config.getAlgorithm()).equals(EAlgorithms.FirstFit.toString()))
					activationMatrix = generic_scheduler.FirstFit(vmRequestMatrix);

				else if ((_config.getAlgorithm()).equals(EAlgorithms.Lyapunov.toString()))
					activationMatrix = scheduler.RunLyapunov(_cplexData);
				else
					System.out.print("No scheduling algorithm is defined");

				Utilities.updateActivationStats(slot,_config,activationMatrix);


			}

			System.out.println("ACTIVATION Matrix:"+Arrays.deepToString(activationMatrix));


			scheduler.updateData(_cplexData, activationMatrix);
			CplexResponse cplexResponse = updatePenaltyAndUtility(_cplexData, activationMatrix);

			// ----------- Update Statistics Object
			double net_benefit = cplexResponse.getNetBenefit();
			System.out.println("NET_BENEFIT: "+net_benefit);

			// ----------- Create VMs Actual or Objects)
		
			createAllServices(slot,activationMatrix);

		} catch (Exception ex) {
			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public void updateServiceRequestPattern(int slot) {

		int slots_window = _config.getSlots_window();
		int index = 0;
		int requestsMade = 0;

		try {

			if (slot > 0) {
				if (EStatsUpdateMethod.simple_moving_average.toString().equals(_config.getWeb_stats_update_method())) {

					index = _slots.length - slots_window;

					if (index - 1 >= 0)
						for (int p = 0; p < this.providers_number; p++) {
							for (int s = 0; s < this.services_number; s++) {
								requestsMade = 0;

								for (int i = index - 1; i <= slot; i++) {
									requestsMade += Utilities.getRequestsMadefromDB(slot, p, s);
								}
								_webRequestPattern[p][s] = requestsMade / slots_window;
							}

						}

				} else if (EStatsUpdateMethod.cumulative_moving_average.toString()
						.equals(_config.getWeb_stats_update_method())) {

					for (int p = 0; p < this.providers_number; p++) {
						for (int s = 0; s < this.services_number; s++) {

							requestsMade = 0;
							for (int i = 0; i <= slot; i++) {
								requestsMade += Utilities.getRequestsMadefromDB(slot, p, s);
							}
							_webRequestPattern[p][s] = requestsMade / slot;
						}

					}

				} else if (EStatsUpdateMethod.weighted_moving_average.toString()
						.equals(_config.getWeb_stats_update_method())) {

				} else if (EStatsUpdateMethod.exponential_moving_average.toString()
						.equals(_config.getWeb_stats_update_method())) {

				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	//	class StatisticsTimer extends TimerTask {
	//
	//		int slot;
	//		int numberOfMachineStatsPerSlot = _config.getNumberOfMachineStatsPerSlot();
	//
	//		StatisticsTimer(int slot) {
	//			this.slot = slot;
	//		}
	//
	//		public void run() {
	//
	//			if (_currentInstance < numberOfMachineStatsPerSlot) {
	//
	//				//		Utilities.updateSimulatorStatistics(_config, slot, _currentInstance);
	//
	//				_currentInstance++;
	//
	//			} else {
	//				stats_timer.cancel();
	//			}
	//
	//		}
	//
	//	}

	//	private void startNodesStatsTimer(int slot) {
	//
	//		int statsUpdateInterval = _config.getSlotDuration() / _config.getNumberOfMachineStatsPerSlot();
	//
	//		stats_timer = new Timer();
	//		_currentInstance = 0;
	//
	//		if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.milliseconds.toString()))
	//			stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, statsUpdateInterval);
	//		else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString()))
	//			stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, statsUpdateInterval * 1000);
	//		else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString()))
	//			stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, 60 * statsUpdateInterval * 1000);
	//		else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString()))
	//			stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, 3600 * statsUpdateInterval * 1000);
	//	}

	private void addVmRequestsPerService(ServiceRequest _serviceRequest, int slot) {

		int slot2AddVM = _serviceRequest.getSlotStart();
		int serviceID = _serviceRequest.getServiceID();

		if (slot != slot2AddVM)
			System.out.println("Error in slot handling Service Request");

		int providerID = _serviceRequest.getProviderID();

		// Solves the VM mapping problem
		int[] _vms = _cplexData.f(providerID, serviceID);

		for (int v = 0; v < _vms.length; v++) {
			_serviceRequest.getVms_requested()[v] = _vms[v];
		}


	}

	@SuppressWarnings("unused")
	private void createAllServices(int slot,int[][][][] activation_matrix) {
		LoadService load_service_object;
		Thread thread;
		int vms_number = 0;

		if(false){
			try {
				load_service_object = new LoadService(_config,slot,activation_matrix);
				thread = new Thread(load_service_object);
				thread.start();
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unused")
	private void destroyServices(int slot) throws InterruptedException {

	
		if(false){
			DestroyService deleter = new DestroyService(_slots[slot]);
			Thread thread = new Thread(deleter);
			thread.start();
			Thread.sleep(5);
		}
	}



	private CplexResponse updatePenaltyAndUtility(SchedulerData data, int[][][][] activationMatrix) {

		double netBenefit = 0;
		double penalty = 0;
		double utility = 0;

		for (int i = 0; i < data.N; i++)
			for (int j = 0; j < data.P; j++)
				for (int s = 0; s < data.S; s++)
					for (int v = 0; v < data.V; v++)
						utility += activationMatrix[i][j][v][s] * data.w[v];

		penalty = 0; // Cost

		for (int p = 0; p < data.P; p++) {

			for (int s = 0; s < data.S; s++) {
				double temp = 0;
				for (int v = 0; v < data.V; v++)
					for (int i = 0; i < data.N; i++)
						temp += data.n[i][p][v][s] * data.ksi(s, p, v);

				penalty += Math.max((data.r[p][s] - temp), 0) * data.pen[p][s];
			}
		}

		netBenefit = utility - penalty;

		System.out.println();

		
		CplexResponse response = new CplexResponse(activationMatrix, netBenefit, utility, penalty);

		return response;
	}

	private int[][][] loadVMRequestMatrix(int slot) {

		int[][][] vmRequestMatrix = new int[this.providers_number][vm_types_number][services_number];

		int s = -1;
		int vm_requests;
		for (int p = 0; p < this.providers_number; p++) {

			List<ServiceRequest> listOfRequestedServices = _slots[slot].getServiceRequests2Activate()[p];

			for (ServiceRequest nextRequest : listOfRequestedServices) {
				s = nextRequest.getServiceID();
				for (int v = 0; v < vm_types_number; v++) {
					vm_requests=nextRequest.getVms_requested()[v];
					vmRequestMatrix[p][v][s] = vm_requests;
					total_requests[p][v][s] += vm_requests;
				}
			}
		}
		return vmRequestMatrix;
	}

	private int[][][][] prepareVmDeleteMatrix(int slot) {

		int[][][][] vms2DeleteMatrix = new int[hosts_number][providers_number][vm_types_number][services_number];

		for (int i = 0; i < hosts_number; i++)
			for (int p = 0; p < providers_number; p++)
				for (int v = 0; v < vm_types_number; v++)
					for (int s = 0; s < services_number; s++) {
						vms2DeleteMatrix[i][p][v][s] = 0;
					}

		int s = -1;

		for (int p = 0; p < providers_number; p++) {
			for (ServiceRequest request : _slots[slot].getServiceRequests2Remove()[p]) {

				for (int n = 0; n < hosts_number; n++) {
					for (int v = 0; v < vm_types_number; v++) {
						s = request.getServiceID();
						vms2DeleteMatrix[n][p][v][s] = generic_scheduler.getRunning_allocations()[n][p][v][s];
					}
				}
			}
		}

		return vms2DeleteMatrix;
	}


	private void reduceRunningAllocation(int[][][][] vmsMatrix ){

		int index=0;
		for (int n = 0; n < hosts_number; n++)
			for (int p = 0; p < providers_number; p++)
				for (int v = 0; v < vm_types_number; v++)
					for (int s = 0; s < services_number; s++) {
						index=0;
						while(index<vmsMatrix[n][p][v][s]){
							generic_scheduler.getRunning_allocations()[n][p][v][s]--;
							index++;
						}
					}
	}	

	




	class DestroyService implements Runnable {

		private String threadName;
		private Boolean deleted = false;
		Slot slot;

		DestroyService(Slot slot ) {
			this.slot=slot;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {

			try {

				String service_name="";
				for (int p = 0; p < providers_number; p++) {

					for (ServiceRequest request : this.slot.getServiceRequests2Remove()[p]) {
						service_name=Utilities.getServiceName(_config,request.slotStart, p, request.serviceID);

						Hashtable parameters=new Hashtable();
						parameters.put("service_name",service_name);

						deleted = _webUtilities.destroyService(parameters);
						Thread.sleep(0);

					}
				}
			} catch (Exception e) {
				System.out.println("Thread " + threadName + " interrupted.");
			}

			System.out.println("Delete Service Thread " + threadName + " finished.");

		}

		public boolean isDeleted() {
			return deleted;
		}

	}

	public class LoadService implements Runnable {

		String thread_name;

		int[][][][] activation_matrix;
		int slot;
		Configuration config;

		LoadService(Configuration config,int slot,int[][][][] activation_matrix) {

			this.activation_matrix=activation_matrix;
			this.slot=slot;
			this.config=config;


		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			String vm_type_name="";
			String vm_name="";
			String vm_series="";
			String service_name;
			String charm_name="";
			int vms_number;
			List<String> vm_names_list;

			try {

				System.out.println("Load VM Thread: " + thread_name + " started");
				Hashtable parameters;
				// Deploy VMs
				for (int n = 0; n < hosts_number; n++) {
					for (int p = 0; p < providers_number; p++) {
						for (int v = 0; v < vm_types_number; v++) {
							for (int s = 0; s < services_number; s++) {

								service_name=Utilities.buildServiceName(config,slot, p, s);
								charm_name=config.getService_charm()[s];
								vm_type_name=Utilities.getVMTypeName(v);
								vm_name= slot+"_h"+n+"_p"+p+"_v"+v+"_s"+s+"_";
								vm_series=config.getVm_series();
								thread_name = "thread_"+vm_name;

								vms_number = this.activation_matrix[n][p][v][s];
								vm_names_list=new ArrayList<String>();

								while (vms_number > 0) {

									for (int i = 0; i < vms_number; i++) {

										vm_name+=i;
										vm_names_list.add(vm_name);

										parameters=new Hashtable();
										parameters.put("vm_name",vm_name);
										parameters.put("vm_series",vm_series);
										parameters.put("vm_type",vm_type_name);

										_webUtilities.createVM(parameters);
										//	System.out.println("vm_created:" + vm_created );	

									}

									// Deploy Service in VM 0
									for (int i = 0; i < vm_names_list.size(); i++) {
										parameters=new Hashtable();
										parameters.put("service_name",service_name);
										parameters.put("vm_name",vm_names_list.get(i));
										parameters.put("charm_name",charm_name);

										if(i==0) // Deploy in the first VM
											_webUtilities.deployService(parameters);
										else // Add units for the rest
											_webUtilities.scaleService(parameters);
									}
									Thread.sleep(0);
								}
								// Thread.sleep(5000);
								vms_number--;
							}
						}
					}
				}


			} catch (Exception e) {
				System.out.println("Thread " + thread_name + " interrupted.");
			}

			return;
		}


		public SchedulerData getCplexData() {
			return _cplexData;
		}

	}
}