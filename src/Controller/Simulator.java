package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Cplex.SchedulerData;
import Enumerators.EGeneratorType;
import Enumerators.ESlotDurationMetric;
import Statistics.ABStats;
import Statistics.DBClass;
import Statistics.DBUtilities;
import Statistics.WebRequestStats;
import Statistics.WebRequestStatsSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

import Controller.*;
import Utilities.*;

/**
 *
 * @author kostas
 */
public class Simulator {

	int slot = 0;

	Configuration _config;
	int providers_number;
	int services_number;
	
	List<String> _hostNames;
	List<String> _clientNames;

	Host[] _hosts;
	WebClient[] _webClients;
	Slot[] _slots;
	Controller _controller;
	Random rand;

	long experimentStart;
	long experimentStop;

	Timer controllerTimer;
	WebUtilities _webUtility;
	Provider[] _provider;
	WebRequestStatsSlot[][][] _webRequestStatsSlot;

	public Simulator(String algorithm, int simulatorID, int runID) {

		this._config = new Configuration();
		this.providers_number=_config.getProviders_number();
		this.services_number =_config.getServices_number();
		
		this.controllerTimer = new Timer();

		this._webUtility = new WebUtilities(_config);

		System.out.println("********** System Initialization Phase ****************");

		initializeSimulationObjects(); // creates: Hosts, Clients, Slots
		initiliazeWebRequestStats();

		this._controller = new Controller(_hosts, _webClients, _config, _slots, _dbUtilities, _provider);

		addInitialServiceRequestEvents();

		System.out.println("********** End of System Initialization Phase **************");
		System.out.println();

	}

	private void initializeSimulationObjects() {

		Random rand = new Random();

		// ----------Initialize Slots 
		// in every sclot there is a list of events to occur
		_slots = new Slot[_config.getSlots()];
		for (int i = 0; i < _config.getSlots(); i++) {
			_slots[i] = new Slot(i, _config);

		}
		System.out.println("Initialization: Slot Objects Ready");

//		for (int i = 0; i < _slots.length; i++) {
//			System.out.println("---------------- SLOT " + i + " -----------------------------");
//			for (int p = 0; p < _config.providers_number; p++) {
//				for (ServiceRequest e : _slots[i].getServiceRequests2Activate()[p]) {
//					System.out.println("Create: " + e.getRequestId());
//				}
//				for (ServiceRequest e : _slots[i].getServiceRequests2Remove()[p]) {
//					System.out.println("Delete: " + e.getRequestId());
//				}
//				System.out.println("");
//			}
//		}

		// ---------- Initialize providers
		_provider = new Provider[_config.getProviders_number()];
		for (int i = 0; i < _config.getProviders_number(); i++) {
			_provider[i] = new Provider(i, _config);
		}
		System.out.println("Initialization: Provider Objects Ready");

		// ---------- Hosts
		this._hosts = new Host[_config.getHosts_number()];
		for (int i = 0; i < _hosts.length; i++) {
			_hosts[i] = new Host(i, _config);
		}
		System.out.println("Initialization: Host Objects Ready");

	}

	private void addInitialServiceRequestEvents() {

		int runningSlot = 0;
		
		
		// add one request for every service in slot 0

		for (int p = 0; providers_number < ; p++) {
			for (int s = 0; services_number < ; s++) {

				if (!_config.getArrivals_generator()[p][s].isEmpty())
					CreateNewServiceRequest(p, s, runningSlot, true);

				runningSlot = 0;

				while (runningSlot < _config.getSlots()) {
					runningSlot = CreateNewServiceRequest(p, s, runningSlot, false);
				}
			}
		}

		System.out.println("Initialization: Added initial requests pattern");

	}

	// Returns the new running slot (this can be also 0)
	private int CreateNewServiceRequest(int providerID, int serviceID, int currentSlot, boolean firstSlot) {
		int slot2AddService = 0;
		int slot2RemoveService = 0;

		int lifetime = calculateServiceLifeTime(providerID, serviceID);
		int slots_away;

		if (lifetime < 1)
			lifetime = 2;

		if (firstSlot)
			slots_away = 0;
		else
			slots_away = calculateSlotsAway(providerID, serviceID);

		slot2AddService = currentSlot + slots_away;
		slot2RemoveService = slot2AddService + lifetime;

		ServiceRequest newServiceRequest = new ServiceRequest(providerID, serviceID, lifetime);

		newServiceRequest.setSlotStart(slot2AddService);
		newServiceRequest.setSlotEnd(slot2RemoveService);

		_slots[slot2AddService].getServiceRequests2Activate()[providerID].add(newServiceRequest);

		if (slot2RemoveService < _config.getSlots()) {
			_slots[slot2RemoveService].getServiceRequests2Remove()[providerID].add(newServiceRequest);
		}

		return slot2AddService;

	}

	private int calculateSlotsAway(int providerID, int serviceID) {

		int interArrivalTime = -1;
		int min = 0;
		int max = 0;
		Double value;

		String arrivalsType = (String) _config.getArrivals_generator()[providerID][serviceID].get("arrivals_type");

		switch (EGeneratorType.valueOf(arrivalsType)) {

		case Exponential:
			value = _provider[providerID].get_arrivalsExpGenerator()[serviceID].random();
			interArrivalTime = value.intValue();
			break;

		case Pareto:
			value = _provider[providerID].get__arrivalsParetoGenerator()[serviceID].random();
			interArrivalTime = value.intValue();
			break;

		case Random:
			min = _provider[providerID].getArrivals_min()[serviceID];
			max = _provider[providerID].getArrivals_max()[serviceID];

			interArrivalTime = Utilities.randInt(min, max);
			break;

		default:
			break;
		}

		return interArrivalTime;

	}

	private int calculateServiceLifeTime(int providerID, int serviceID) {

		int lifetime = -1;
		int min = 0;
		int max = 0;
		Double value;

		String lifetime_type = (String) _config.getArrivals_generator()[providerID][serviceID].get("lifetime_type");

		switch (EGeneratorType.valueOf(lifetime_type)) {
		case Exponential:
			value = _provider[providerID].get_lifetimeExponentialGenerator()[serviceID].random();
			lifetime = value.intValue();
			break;

		case Pareto:
			value = _provider[providerID].get_lifetimeParetoGenerator()[serviceID].random();
			lifetime = value.intValue();
			break;

		case Random:

			min = _provider[providerID].getLifetime_min()[serviceID];
			max = _provider[providerID].getLifetime_max()[serviceID];
			lifetime = Utilities.randInt(min, max);
			break;

		default:
			break;
		}

		return lifetime;

	}

	@SuppressWarnings("static-access")
	public final void StartExperiment() {

		int duration = _config.getSlotDuration();
		experimentStart = System.currentTimeMillis();

		System.out.println("Simulator now starts! Time instant: " + experimentStart);

		if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.milliseconds.toString())) {
			controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, duration);
		} else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString())) {
			controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, duration * 1000);
		} else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString())) {
			controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, 60 * duration * 1000);
		} else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString())) {
			controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, 3600 * duration * 1000);
		}

	}

	private void initiliazeWebRequestStats() {

		this._webRequestStatsSlot = new WebRequestStatsSlot[_config.getSlots()][providers_number][services_number];

		for (int i = 0; i < _config.getSlots(); i++) {
			for (int p = 0; p < providers_number; p++) {
				for (int s = 0; s < services_number; s++) {
					_webRequestStatsSlot[i][p][s] = new WebRequestStatsSlot();
				}
			}
		}
	}

	// Algorithm: Choose a VM in the hosting Node else choose at Random
	private String chooseVMforService(int serviceID, int providerID, String webClient) {

		String vmIP = "";
		String hostApName = "";
		Random random = new Random();

		// Step 1: Find the hosting node
		for (int i = 0; i < _webClients.length; i++) {
			if (_webClients[i].getClientName().equals(webClient)) {
				hostApName = _webClients[i].getApName();
			}
		}

		// Step 2: Find all the VMs that can be used
		try {

			CopyOnWriteArrayList<VM> potentialVMs = new CopyOnWriteArrayList<>();

			for (Host _host : _hosts) {
				for (Iterator iterator = _host.getVMs().iterator(); iterator.hasNext();) {
					VM nextVM = (VM) iterator.next();

					if (nextVM.isActive() & nextVM.getProviderID() == providerID & nextVM.getServiceID() == serviceID) {
						potentialVMs.add(nextVM);
					}
				}
			}

			if (potentialVMs.size() > 0) {
				vmIP = potentialVMs.get(random.nextInt(potentialVMs.size())).getIp();
				return vmIP;

			} else if (potentialVMs.isEmpty()) {
				return _config.getCloudVM_IPs().get(random.nextInt(_config.getCloudVM_IPs().size()));
			}

			// //Step 3: Find the local VM
			// for (Iterator iterator = potentialVMs.iterator();
			// iterator.hasNext();) {
			// VM nextVM = (VM)iterator.next();
			//
			// if(hostApName.equals(nextVM.getHostname()))
			// vmIP=nextVM.getIp();
			// }
		} catch (Exception e) {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			System.out.println(e);
			System.exit(0);
		}

		return null;

	}

	class RunSlot extends TimerTask {

		public void run() {

			try {
				if (slot < _config.getSlots()) {

					_controller.updateServiceRequestPattern(_webRequestPattern, slot);
					_controller.Run(slot);

					_dbUtilities.updateWebClientStatistics2DBPerSlot(slot, _webRequestStatsSlot);
					slot++;

				} else {
					experimentStop = System.currentTimeMillis();
					controllerTimer.cancel();

					_db.getOmlclient().close();
					System.exit(0);
				}
			} catch (IOException ex) {
				_db.getOmlclient().close();
				Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

}
