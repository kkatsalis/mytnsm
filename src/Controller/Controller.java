/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Cplex.CplexResponse;
import Enumerators.EMachineTypes;
import Enumerators.ESlotDurationMetric;
import Statistics.DBClass;
import Statistics.DBUtilities;
import Utilities.WebUtilities;
import Statistics.VMStats;
import Statistics.WebRequestStatsSlot;
import Statistics.HostStats;
import Statistics.NetRateStats;
import Cplex.Scheduler;
import Cplex.SchedulerData;
import Enumerators.EAlgorithms;
import Enumerators.EStatsUpdateMethod;
import Statistics.SimulatorStats;
import Utilities.SimWebRequestUtilities;
import Utilities.StatsUtilities;
import Utilities.Utilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import org.json.JSONException;

/**
 *
 * @author kostas
 */
public class Controller {

	Configuration _config;
	Slot[] _slots;
	Host[] _hosts;
	Provider[] _providers;

	HostStats[] _hostStats;
	List<VMStats> _activeVMStats;

	WebUtilities _webUtilities;

	int _currentInstance = 0;
	int vmIDs = 0;

	SchedulerData _cplexData;
	Scheduler scheduler;

	WebRequestStatsSlot[][][] _webRequestSlotStats;

	int[][] total_services_requested; // [p][s]=[provider][service]
	int[][] total_services_satisfied; // [p][s]=[provider][service]
	int[][] total_vms_requested; // [p][v]=[provider][vm_type]
	int[][] total_vms_satisfied; // [p][v]=[provider][vm_type]
	int[][] total_vms_deleted; // [p][v]=[provider][vm_type]
	int[][] vms_requested; // [p][v]=[provider][vm_type]
	int[][] vms_satisfied; // [p][v]=[provider][vm_type]

	int[][][][] vms2DeleteMatrix;
	int[][] _webRequestPattern; // [p][s]: p=provider, s=service

	int hosts_number; // hosts number
	int providers_number; // providers number
	int vm_types_number; // vm types number
	int services_number; // services number;
	int slots_number;

	Timer stats_timer;
	StatsUtilities stats_utilities;


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

		this._activeVMStats = new ArrayList<>();
		this._cplexData = new SchedulerData(config);
		this._providers = _provider;

		this.stats_utilities=new StatsUtilities(config);

		initializeController();
		_cplexData.initializeWebRequestMatrix(_webRequestPattern);

	}

	private void initializeController() {

		this._hostStats = new HostStats[hosts_number];

		this._webRequestSlotStats = new WebRequestStatsSlot[_config
		                                                    .getSlotsNumber()][providers_number][services_number];
		for (int slot = 0; slot < slots_number; slot++) {
			for (int p = 0; p < this.providers_number; p++) {
				for (int s = 0; s < this.services_number; s++) {
					_webRequestSlotStats[slot][p][s] = new WebRequestStatsSlot();
				}
			}
		}

		for (int p = 0; p < this.providers_number; p++) {
			for (int s = 0; s < this.services_number; s++) {
				_webRequestPattern[p][s] = (Integer) _config.getArrivals_generator()[p][s].get("estimated_requests");
			}
		}

		for (int p = 0; p < this.providers_number; p++) {
			for (int s = 0; s < this.services_number; s++) {
				total_services_requested[p][s]=0;
				total_services_satisfied[p][s]=0;
			}
		}


		// Vm delete matrix
		vms2DeleteMatrix = new int[hosts_number][providers_number][vm_types_number][services_number];

		// VM Statistics Matrices
		total_vms_requested = new int[providers_number][vm_types_number];
		total_vms_satisfied = new int[providers_number][vm_types_number];
		total_vms_deleted = new int[providers_number][vm_types_number];

		vms_requested = new int[providers_number][vm_types_number];
		vms_satisfied = new int[providers_number][vm_types_number];

		for (int p = 0; p < providers_number; p++) {
			for (int v = 0; v < vm_types_number; v++) {
				total_vms_requested[p][v] = 0;
				total_vms_satisfied[p][v] = 0;
				total_vms_deleted[p][v] = 0;
				vms_requested[p][v] = 0;
				vms_satisfied[p][v] = 0;
			}
		}
	}

	void Run(int slot) throws IOException {

		System.out.println("******* -- Slot:" + slot + " Controller Run*******");
		updateServiceRequestPattern(slot);

		scheduler = new Scheduler(_config);

		if (false)
			startNodesStatsTimer(slot); // for Statistics updates

		try {

			// ----------- Load VM Request Lists
			for (int p = 0; p < this.providers_number; p++) {
				for (ServiceRequest serviceRequest : _slots[slot].getServiceRequests2Activate()[p]) {
					addVmRequestsPerService(serviceRequest, slot);
				}
			}
			int[][][] vmRequestMatrix = loadVMRequestMatrix(slot);
			// System.out.println(Arrays.deepToString(vmRequestMatrix));

			// Load VM Deactivation Matrix
			prepareVmDeleteMatrix(slot);
			deleteVMs(slot);

			// Update WebRequest Pattern
			// int[][] requestPattern=Utilities.findRequestPattern(_config);

			// Update Cplex data Parameters
			_cplexData.updateParameters(_webRequestPattern, vmRequestMatrix, vms2DeleteMatrix);

			// Run CPLEX
			int[][][][] activationMatrix = new int[_cplexData.N][_cplexData.P][_cplexData.V][_cplexData.S];

			if (_slots[slot].getServiceRequests2Activate().length > 0) {

				if ((_config.getAlgorithm()).equals(EAlgorithms.FF.toString()))
					activationMatrix = scheduler.RunFF(_cplexData);
				else if ((_config.getAlgorithm()).equals(EAlgorithms.FFRR.toString()))
					activationMatrix = scheduler.RunFFRR(_cplexData);
				else if ((_config.getAlgorithm()).equals(EAlgorithms.FFRandom.toString()))
					activationMatrix = scheduler.RunFF_Random(_cplexData);
				else if ((_config.getAlgorithm()).equals(EAlgorithms.Lyapunov.toString()))
					activationMatrix = scheduler.RunLyapunov(_cplexData);
				else
					System.out.print("No scheduling algorithm is defined");
			}

			scheduler.updateData(_cplexData, activationMatrix);
			CplexResponse cplexResponse = updatePenaltyAndUtility(_cplexData, activationMatrix);

			// ----------- Update Statistics Object
			double net_benefit = cplexResponse.getNetBenefit();
			updateDbStatisticsObject(vmRequestMatrix, activationMatrix, net_benefit, slot,_cplexData);

			// ----------- Create VMs Actual or Objects)
			createAllVms();

		} catch (Exception ex) {
			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	// n[i][j][v][s]: # of allocated VMs of v v for service s of provider j at
	// AP i

	private void createAllVms() {
		LoadVM loadObject;
		Thread thread;

		for (int i = 0; i < this.hosts_number; i++) {
			List<VMRequest> vm2CreatePerHost = activationMatrix2VMRequests(slot, i, activationMatrix);

			System.out.println("MESSAGE: Host:" + i + " Bring up: " + vm2CreatePerHost.size() + "VMs");

			for (Iterator iterator = vm2CreatePerHost.iterator(); iterator.hasNext();) {
				request = (VMRequest) iterator.next();

				loadObject = new LoadVM(slot, request, i, _hosts[i].getNodeName());
				thread = new Thread(loadObject);
				thread.start();
				Thread.sleep(0);
				// Thread.sleep(5000);

			}


		}

		private void deleteVMs(int slot) throws InterruptedException {

			for (int p = 0; p < providers_number; p++) {

				for (ServiceRequest request : _slots[slot].getServiceRequests2Remove()[p]) {

					for (int i = 0; i < hosts_number; i++) {
						for (String vm_ip : request.getVms_deployed_ips()[i]) {

							DeleteVM deleter=new DeleteVM(i,vm_ip); 
							Thread thread=new Thread(deleter); 
							thread.start(); 
							Thread.sleep(5000);	
						}
					}
				}
			}

			System.out.println("Method Call: Delete VMs Called");
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

			System.out.println(Arrays.deepToString(activationMatrix));

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

			System.out.println("**** utility: " + utility);
			CplexResponse response = new CplexResponse(activationMatrix, netBenefit, utility, penalty);

			return response;
		}

		private int[][][] loadVMRequestMatrix(int slot) {

			int[][][] vmRequestMatrix = new int[this.providers_number][vm_types_number][services_number];

			int s = -1;

			for (int p = 0; p < this.providers_number; p++) {

				List<ServiceRequest> listOfRequestedServices = _slots[slot].getServiceRequests2Activate()[p];

				for (ServiceRequest nextRequest : listOfRequestedServices) {
					s = nextRequest.getServiceID();
					for (int v = 0; v < nextRequest.getVms_requested().length; v++) {
						vmRequestMatrix[p][v][s] = nextRequest.getVms_requested()[v];
					}
				}
			}
			return vmRequestMatrix;
		}

		private void prepareVmDeleteMatrix(int slot) {

			for (int i = 0; i < hosts_number; i++)
				for (int p = 0; p < providers_number; p++)
					for (int v = 0; v < vm_types_number; v++)
						for (int s = 0; s < services_number; s++) {
							this.vms2DeleteMatrix[i][p][v][s] = 0;
						}

			int s = -1;

			for (int p = 0; p < providers_number; p++) {
				for (ServiceRequest request : _slots[slot].getServiceRequests2Remove()[p]) {

					for (int i = 0; i < hosts_number; i++) {
						for (int v = 0; v < vm_types_number; v++) {
							s = request.getServiceID();
							this.vms2DeleteMatrix[i][p][v][s] = request.getVms_deployed()[i][v];
						}
					}
				}
			}
		}

		private int[][][][] tempScheduler(double[][][] vmRequestMatrix) {
			// activationMatrix[i][j][v][s]: # of allocated VMs of v v for service s
			// of provider j at AP i
			// requestMatrix[v][s][p]

			int[][][][] activationMatrix = new int[hosts_number][providers_number][vm_types_number][services_number];

			for (int j = 0; j < this.providers_number; j++) {
				for (int v = 0; v < this.vm_types_number; v++) {
					for (int s = 0; s < this.services_number; s++) {
						activationMatrix[0][j][v][s] = (int) vmRequestMatrix[v][s][j];
					}
				}
			}
			return activationMatrix;
		}

		private void updateDbStatisticsObject(int[][][] vmRequestMatrix, int[][][][] activationMatrix, double netBenefit, int slot, SchedulerData data) {
			// tbd
		}

		class DeleteVM implements Runnable {

			private String threadName;
			private int host_id;
			private String vm_ip;
			private Boolean deleted = false;

			DeleteVM(int host_id, String vm_ip) {

				this.host_id = host_id;
				this.vm_ip = vm_ip;
			}

			public void run() {

				try {

					System.out.println("Delete Thread: " + threadName + " started");
					deleted = _webUtilities.deleteVM(this.host_id, this.vm_ip);
					Thread.sleep(0);

				} catch (Exception e) {
					System.out.println("Thread " + threadName + " interrupted.");
				}

				System.out.println("Delete Thread " + threadName + " finished.");

			}

			public boolean isDeleted() {
				return deleted;
			}

		}

		public class LoadVM implements Runnable {

			private Thread _thread;
			String threadName;
			String hostName;
			VMRequest request;

			public boolean loaded = false;
			int slot;
			int hostID;
			Hashtable hostVMpair;

			LoadVM(int slot, VMRequest request, int hostID, String hostName) {

				this.slot = slot;
				this.hostName = hostName;
				this.hostID = hostID;
				this.request = request;
				this.threadName = hostName + "-" + request.getVmID();
				this.hostVMpair = new Hashtable();

				System.out.println("Creating " + threadName);
			}

			public void run() {

				try {

					System.out.println("Load VM Thread: " + threadName + " started");

					/*
					 * Block To Activate in real system
					 * createVM(slot,request,hostName); startVM(request,hostName);
					 */
					createVMobject(slot, request, hostID, hostName);

					Thread.sleep(0);

				} catch (Exception e) {
					System.out.println("Thread " + threadName + " interrupted.");
				}

				System.out.println("Delete Thread " + threadName + " finished.");
				loaded = true;
			}

			private boolean createVM(int slot, VMRequest request, String nodeName) throws IOException {

				Hashtable vmParameters;

				boolean vmCreated = false;
				boolean vmCreateCommandSend = false;

				System.out.println("provider:" + request.providerID + " - activate: " + request.getVmID());

				// Step 1: Add VM on the Physical node
				vmParameters = Utilities.determineVMparameters(request, nodeName);
				vmCreateCommandSend = _webUtilities.createVM(vmParameters);

				return vmCreateCommandSend;

			}

			private void startVM(VMRequest request, String hostName) throws IOException {

				Boolean vmCreated = false;
				Hashtable vmParameters = Utilities.determineVMparameters(request, hostName);
				int counter = 0;

				while (!vmCreated & counter < 200) {

					vmCreated = _webUtilities.checkVMListOnHost(hostName, String.valueOf(vmParameters.get("vmName")));
					System.out.println("Bring VM up attempt:" + counter + "-requestID:" + request.getVmID());

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						// handle the exception...
						// For example consider calling
						// Thread.currentThread().interrupt(); here.
					}
					counter++;
				}

				if (counter == 200) {
					System.out.println("Failed to load requestID:" + request.getVmID());
				}
				String vmName = String.valueOf(vmParameters.get("vmName"));
				_webUtilities.startVM(vmName, hostName);

			}

			

		}

		
		
		class StatisticsTimer extends TimerTask {

			int slot;
			int numberOfMachineStatsPerSlot=_config.getNumberOfMachineStatsPerSlot();

			StatisticsTimer(int slot) {
				this.slot = slot;
			}

			public void run() {

				if (_currentInstance < numberOfMachineStatsPerSlot) {

					try {

						stats_utilities.updateAllHostStatisObjects(slot, _currentInstance);
						stats_utilities.updateSimulatorStatistics(slot, _currentInstance);
						stats_utilities.updateSimulatorStatistics(slot, _currentInstance);
						
						_currentInstance++;

					} catch (IOException ex) {
						Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
					} catch (JSONException ex) {
						Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
					}
				} else {
					stats_timer.cancel();
				}

			}

		}

		private void startNodesStatsTimer(int slot) {

			int statsUpdateInterval = _config.getSlotDuration() / _config.getNumberOfMachineStatsPerSlot();

			stats_timer = new Timer();
			_currentInstance = 0;

			if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.milliseconds.toString()))
				stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, statsUpdateInterval);
			else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString()))
				stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, statsUpdateInterval * 1000);
			else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString()))
				stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, 60 * statsUpdateInterval * 1000);
			else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString()))
				stats_timer.scheduleAtFixedRate(new StatisticsTimer(slot), 0, 3600 * statsUpdateInterval * 1000);
		}

		public SchedulerData getCplexData() {
			return _cplexData;
		}

		private void addVmRequestsPerService(ServiceRequest _serviceRequest, int slot) {

			int slot2AddVM = _serviceRequest.getSlotStart();
			int serviceID = _serviceRequest.getServiceID();

			if (slot != slot2AddVM)
				System.out.println("Error in slot handling Service Request");

			int providerID = _serviceRequest.getProviderID();

			// Solves the VM mapping problem
			int[] _vms = _cplexData.f(_cplexData, providerID, serviceID);

			for (int i = 0; i < _vms.length; i++) {
				_serviceRequest.getVms_requested()[i] = _vms[i];
			}

		}

		public void updateServiceRequestPattern(int slot) {

			int simpleMovingAverageParameter = 3;
			int index = 0;
			int requestsMade = 0;

			try {

				if (slot > 0) {
					if (EStatsUpdateMethod.simple_moving_average.toString().equals(_config.getStatsUpdateMethod())) {

						index = _slots.length - simpleMovingAverageParameter;

						if (index - 1 >= 0)
							for (int p = 0; p < this.providers_number; p++) {
								for (int s = 0; s < this.services_number; s++) {
									requestsMade = 0;

									for (int i = index - 1; i <= slot; i++) {
										requestsMade += _simulatorWebRequestPattern[i][p][s];
									}
									_webRequestPattern[p][s] = requestsMade / simpleMovingAverageParameter;
								}

							}

					} else if (EStatsUpdateMethod.cumulative_moving_average.toString()
							.equals(_config.getStatsUpdateMethod())) {

						for (int p = 0; p < this.providers_number; p++) {
							for (int s = 0; s < this.services_number; s++) {

								requestsMade = 0;
								for (int i = 0; i <= slot; i++) {
									requestsMade += _simulatorWebRequestPattern[i][p][s];
								}
								_webRequestPattern[p][s] = requestsMade / slot;
							}

						}

					} else if (EStatsUpdateMethod.weighted_moving_average.toString()
							.equals(_config.getStatsUpdateMethod())) {

					} else if (EStatsUpdateMethod.exponential_moving_average.toString()
							.equals(_config.getStatsUpdateMethod())) {

					}
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}

		public int findActiveVMs(int providerID, SchedulerData data) {

			int activeVMs = 0;

			for (int i = 0; i < data.N; i++) {
				for (int v = 0; v < data.V; v++) {
					for (int s = 0; s < data.S; s++) {

						activeVMs += data.n[i][providerID][v][s];
					}

				}
			}

			return activeVMs;
		}

	}
