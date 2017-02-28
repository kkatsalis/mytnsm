package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Cplex.SchedulerData;
import Enumerators.EGeneratorType;
import Enumerators.ESlotDurationMetric;
import Statistics.WebRequestStatsSlot;

import java.util.ArrayList;
import java.util.Hashtable;
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
	int hosts_number;
	int providers_number;
	int services_number;

	List<String> _hostNames;
	List<String> _clientNames;

	Host[] _hosts;
	Slot[] _slots;
	Controller _controller;
	Random rand;

	long experimentStart;
	long experimentStop;

	Timer controllerTimer;
	WebUtilities _webUtility;
	Provider[] _providers;

	List<String> _remote_machine_ips;

	public Simulator(String algorithm, int simulatorID, int runID) {

		this._config = new Configuration();
		this.hosts_number=_config.getHosts_number();
		this.providers_number = _config.getProviders_number();
		this.services_number = _config.getServices_number();

		this.controllerTimer = new Timer();

		this._webUtility = new WebUtilities(_config);

		System.out.println("********** System Initialization Phase ****************");

		initializeSimulationObjects(); // creates: Hosts, Clients, Slots

		this._controller = new Controller(_config, _hosts, _slots, _providers);

		addInitialServiceRequestEvents();

		System.out.println("********** End of System Initialization Phase **************");
		System.out.println();

	}

	private void initializeSimulationObjects() {

		Random rand = new Random();

		// ----------Initialize Slots
		// in every sclot there is a list of events to occur
		_slots = new Slot[_config.getSlotsNumber()];
		for (int i = 0; i < _config.getSlotsNumber(); i++) {
			_slots[i] = new Slot(i, _config);

		}
		System.out.println("Initialization: Slots Objects Ready");

		// for (int i = 0; i < _slots.length; i++) {
		// System.out.println("---------------- SLOT " + i + "
		// -----------------------------");
		// for (int p = 0; p < _config.providers_number; p++) {
		// for (ServiceRequest e : _slots[i].getServiceRequests2Activate()[p]) {
		// System.out.println("Create: " + e.getRequestId());
		// }
		// for (ServiceRequest e : _slots[i].getServiceRequests2Remove()[p]) {
		// System.out.println("Delete: " + e.getRequestId());
		// }
		// System.out.println("");
		// }
		// }

		// ---------- Initialize providers
		_providers = new Provider[providers_number];
		for (int p = 0; p < providers_number; p++) {
			_providers[p] = new Provider(p, _config);
		}
		System.out.println("Initialization: Providers Objects Ready");

		// ---------- Hosts
		this._hosts = new Host[hosts_number];
		for (int i = 0; i < _hosts.length; i++) {
			_hosts[i] = new Host(i, _config);
		}
		System.out.println("Initialization: Hosts Objects Ready");

		// Clone Remote Cloud information

		_remote_machine_ips = new ArrayList<>();
		String ip = "";
		for (int i = 0; i < _config.getRemote_machines_number(); i++) {
			ip = (String) _config.getRemote_machine_config()[i].get("ip");
			_remote_machine_ips.add(ip);
		}

	}

	private void addInitialServiceRequestEvents() {

		int runningSlot = 0;

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				if (!_config.getArrivals_generator()[p][s].isEmpty())
					CreateNewServiceRequest(p, s, runningSlot, true);

				runningSlot = 0;

				while (runningSlot < _config.getSlotsNumber()) {
					runningSlot = CreateNewServiceRequest(p, s, runningSlot, false);
				}
			}
		}
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

		ServiceRequest newServiceRequest = new ServiceRequest(_config,providerID, serviceID, lifetime);

		newServiceRequest.setSlotStart(slot2AddService);
		newServiceRequest.setSlotEnd(slot2RemoveService);

		if (slot2AddService < _config.getSlotsNumber())
			_slots[slot2AddService].getServiceRequests2Activate()[providerID].add(newServiceRequest);
		
		if (slot2RemoveService < _config.getSlotsNumber()) 
			_slots[slot2RemoveService].getServiceRequests2Remove()[providerID].add(newServiceRequest);
		

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
			value = _providers[providerID].get_arrivalsExpGenerator()[serviceID].random();
			interArrivalTime = value.intValue();
			break;

		case Pareto:
			value = _providers[providerID].get__arrivalsParetoGenerator()[serviceID].random();
			interArrivalTime = value.intValue();
			break;

		case Random:
			min = _providers[providerID].getArrivals_min()[serviceID];
			max = _providers[providerID].getArrivals_max()[serviceID];
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

		String lifetime_type = (String) _config.getLifetime_generator()[providerID][serviceID].get("lifetime_type");

		switch (EGeneratorType.valueOf(lifetime_type)) {
		case Exponential:
			value = _providers[providerID].get_lifetimeExponentialGenerator()[serviceID].random();
			lifetime = value.intValue();
			break;

		case Pareto:
			value = _providers[providerID].get_lifetimeParetoGenerator()[serviceID].random();
			lifetime = value.intValue();
			break;

		case Random:

			min = _providers[providerID].getLifetime_min()[serviceID];
			max = _providers[providerID].getLifetime_max()[serviceID];
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

	// Algorithm: Choose a VM in the hosting Node else choose at Random
	class RunSlot extends TimerTask {

		public void run() {

			try {
				if (slot < _config.getSlotsNumber()) {

					_controller.Run(slot);
					slot++;
				} else {
					experimentStop = System.currentTimeMillis();
					controllerTimer.cancel();
					System.exit(0);
				}
			} catch (IOException ex) {
				Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

}
