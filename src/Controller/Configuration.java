package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Enumerators.EGeneratorType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostas
 */
public class Configuration {

	Configuration _config = this;

	int simulationID;
	int runID;
	String algorithm;
	int slots;
	int slotDuration;
	String slotDurationMetric;
	Boolean simulation_mode;

	// ======= Statistics ==========

	int number_of_machine_stats_per_slot;
	String web_stats_update_method; //cumulative_moving_average or simple_moving_average or weighted_moving_average or exponential_moving_average
	int slots_window;

	// ========= Remote Cloud Machines==========
	int remote_machines_number;
	@SuppressWarnings("rawtypes")
	Hashtable[] remote_machine_config; // host name, ip

	int resources_number;
	// ========= Local Cloud Hosts ==========
	// Used for real deployment
	int hosts_number;
	@SuppressWarnings("rawtypes")
	Hashtable[] host_machine_config; // host name, ip


	// ========= Local Cloud VMs ==========

	int vm_types_number;
	String vm_series;
	String[] vm_type_name;
	int[] vm_cpu_cores; // One per VM Type 0:small, 1:medium, 2:large
	int[] vm_cpu_power; // One per VM Type 0:small, 1:medium, 2:large
	int[] vm_memory; // One per VM Type
	int[] vm_storage; // One per VM Type
	int[] vm_bandwidth; // One per VM Type

	// ========= Local Cloud VMs ==========

	int providers_number;
	int services_number; // Each Service Corresponds to one VNF
	String[] service_alias;
	String[] service_charm;

	// ========= CPLEX ==========

	double omega;
	double priceBase;
	double[] phiWeight;

	double[][] penalty; // [p][s] provider p, service s
	double[][] xi; // [v][s] vm v, service s

	// ========= PROVIDERS ==========
	@SuppressWarnings("rawtypes")
	Hashtable[][] arrivals_generator;
	@SuppressWarnings("rawtypes")
	Hashtable[][] lifetime_generator;

	public Configuration() {

		this.loadSimulationProperties();
		this.loadProperties();
		this.serviceTrafficGenerators();
		this.serviceLifetimeGenerators();

	}

	@SuppressWarnings("unchecked")
	private void serviceTrafficGenerators() {

		arrivals_generator = new Hashtable[providers_number][services_number];

		Properties property = new Properties();
		InputStream input = null;
		String filename = "traffic.properties";

		input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}

		String parameter = "";
		String arrival_rate_type = "";
		double double_value = -1;
		int int_value = -1;


		for (int p = 0; p < providers_number; p++) {

			for (int s = 0; s < services_number; s++) {
				arrivals_generator[p][s]=new Hashtable<Object, Object>();

				// Estimated requests
				parameter = "provider" + p + "_service" + s + "_estimatedRequests";
				int_value = Integer.valueOf(property.getProperty(parameter));
				arrivals_generator[p][s].put("estimated_requests", int_value);

				//arrivals type
				parameter = "provider" + p + "_service" + s + "_arrivals_type";
				arrival_rate_type = String.valueOf(property.getProperty(parameter));
				arrivals_generator[p][s].put("arrivals_type", arrival_rate_type);

				// Exponential
				if (EGeneratorType.Poisson.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_lamda";
					double_value = Double.valueOf( property.getProperty(parameter));
					double_value = (double) 1 / double_value;
					arrivals_generator[p][s].put("arrivals_lamda", double_value);

				} else if (EGeneratorType.Pareto.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_location";
					double_value = Double.valueOf(property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_location", double_value);

					parameter = "provider" + p + "_service" + s + "_arrivals_shape";
					double_value = Double.valueOf( property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_shape", double_value);


				} else if (EGeneratorType.Random.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_min";
					int_value = Integer.valueOf(property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_min", int_value);

					parameter = "provider" + p + "_service" + s + "_arrivals_max";
					int_value = Integer.valueOf( property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_max", int_value);
				}

			}
		}

	}

	@SuppressWarnings("unchecked")
	private void serviceLifetimeGenerators() {

		lifetime_generator = new Hashtable[providers_number][services_number];

		Properties property = new Properties();
		InputStream input = null;
		String filename = "traffic.properties";

		input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}

		String parameter = "";
		String lifetime_type = "";
		double double_value = -1;
		int int_value = -1;


		for (int p = 0; p < providers_number; p++) {

			for (int s = 0; s < services_number; s++) {
				lifetime_generator [p][s]=new Hashtable<Object, Object>();


				//arrivals type
				parameter = "provider" + p + "_service" + s + "_lifetime_type";
				lifetime_type = String.valueOf(property.getProperty(parameter));
				lifetime_generator [p][s].put("lifetime_type", lifetime_type);

				// Exponential
				if (EGeneratorType.Exponential.toString().equals(lifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_lamda";
					double_value = Double.valueOf( property.getProperty(parameter));
					double_value = (double) 1 / double_value;
					lifetime_generator [p][s].put("lifetime_lamda", double_value);

				} else if (EGeneratorType.Pareto.toString().equals(lifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_location";
					double_value = Double.valueOf( property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_location", double_value);

					parameter = "provider" + p + "_service" + s + "_lifetime_shape";
					double_value = Double.valueOf(property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_shape", double_value);


				} else if (EGeneratorType.Random.toString().equals(lifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_min";
					int_value = Integer.valueOf(property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_min", int_value);

					parameter = "provider" + p + "_service" + s + "_lifetime_max";
					int_value= Integer.valueOf(property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_max", int_value);
				}

			}
		}



	}

	private void loadProperties() {

		Properties property = new Properties();
		InputStream input = null;
		String filename = "traffic.properties";
		String parameter = "";
		input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		try {
			property.load(input); // load a properties file
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}
		providers_number = Integer.valueOf(property.getProperty("providers_number"));
		services_number = Integer.valueOf(property.getProperty("services_number"));

		service_alias=new String[services_number];
		service_charm=new String[services_number];
		// ------- CPLEX Configuration

		omega = Double.valueOf(property.getProperty("omega"));
		priceBase = Double.valueOf(property.getProperty("priceBase"));
		phiWeight = new double[providers_number];
		penalty = new double[providers_number][services_number];
		xi = new double[vm_types_number][services_number];

		for (int p = 0; p < providers_number; p++) {
			parameter = "phiWeight_" + p;
			phiWeight[p] = Double.valueOf((String) property.getProperty(parameter));
		}

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				parameter = "penalty_p" + p + "_s" + s;
				penalty[p][s] = Double.valueOf((String) property.getProperty(parameter));
			}

		}

		for (int v = 0; v < vm_types_number; v++) {
			for (int s = 0; s < services_number; s++) {
				parameter = "xi_v" + v + "_s" + s;
				xi[v][s] = Double.valueOf((String) property.getProperty(parameter));
			}

		}

		for (int s = 0; s < services_number; s++) {
			parameter="service_alias_"+s;
			service_alias[s]=String.valueOf((String) property.getProperty(parameter));

			parameter="service_charm_"+s;
			service_charm[s]=String.valueOf((String) property.getProperty(parameter));
		}

	}

	@SuppressWarnings({ "unchecked" })
	private void loadSimulationProperties() {

		Properties property = new Properties();
		String parameter = "";
		String svalue = "";


		String filename = "simulation.properties";
		InputStream input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		// load a properties file
		try {
			property.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		simulationID = Integer.valueOf(property.getProperty("simulationID"));
		runID = Integer.valueOf(property.getProperty("runID"));
		algorithm = String.valueOf(property.getProperty("algorithm"));
		simulation_mode= Boolean.valueOf(property.getProperty("simulation_mode"));
		slots = Integer.valueOf(property.getProperty("slots"));
		slotDuration = Integer.valueOf(property.getProperty("slotDuration"));
		slotDurationMetric = String.valueOf(property.getProperty("slotDurationMetric"));
		number_of_machine_stats_per_slot = Integer.valueOf(property.getProperty("number_of_machine_stats_per_slot"));
		web_stats_update_method=String.valueOf(property.getProperty("web_stats_update_method"));
		slots_window=Integer.valueOf(property.getProperty("slots_window"));
		// Remote Cloud Machines
		remote_machines_number = Integer.valueOf(property.getProperty("remote_machines_number"));
		remote_machine_config = new Hashtable[remote_machines_number];

		for (int i = 0; i < remote_machines_number; i++) {
			remote_machine_config[i]=new Hashtable<Object, Object>();
			parameter = "remote_machine_ip_" + String.valueOf(i);
			svalue = String.valueOf((String) property.getProperty(parameter));
			remote_machine_config[i].put("ip", svalue);
		}

		resources_number=Integer.valueOf(property.getProperty("resources_number"));

		// Local Cloud Hosts
		hosts_number = Integer.valueOf(property.getProperty("hosts_number"));
		host_machine_config = new Hashtable[hosts_number];
		
		for (int i = 0; i < hosts_number; i++) {
			host_machine_config[i]=new Hashtable<Object, Object>();
			
			parameter = "host_"+ String.valueOf(i)+"_ip";
			svalue = String.valueOf( property.getProperty(parameter));
			host_machine_config[i].put("ip", svalue);

			parameter = "host_"+ String.valueOf(i)+"_cpu_cores";
			svalue = String.valueOf(property.getProperty(parameter));
			host_machine_config[i].put("cpu_cores", svalue);

			parameter = "host_"+ String.valueOf(i)+"_cpu_power";
			svalue = String.valueOf(property.getProperty(parameter));
			host_machine_config[i].put("cpu_power", svalue);

			parameter = "host_"+ String.valueOf(i)+"_memory";
			svalue = String.valueOf(property.getProperty(parameter));
			host_machine_config[i].put("memory", svalue);

			parameter = "host_"+ String.valueOf(i)+"_storage";
			svalue = String.valueOf(property.getProperty(parameter));
			host_machine_config[i].put("storage", svalue);

			parameter = "host_"+ String.valueOf(i)+"_bandwidth";
			svalue = String.valueOf(property.getProperty(parameter));
			host_machine_config[i].put("bandwidth", svalue);

		}



		// Local Cloud VMs
		vm_types_number = Integer.valueOf(property.getProperty("vm_types_number"));
		vm_series= String.valueOf(property.getProperty("vm_series"));

		this.vm_type_name = new String[vm_types_number];

		// VM_Types
		for (int i = 0; i < vm_types_number; i++) {
			parameter = "vm_name_type_" + i;
			svalue = String.valueOf((String) property.getProperty(parameter));
			vm_type_name[i] = svalue;
		}

		vm_cpu_cores=new int[vm_types_number];
		vm_cpu_cores[0] = Integer.valueOf(property.getProperty("small_vm_cpu_cores"));
		vm_cpu_cores[1] = Integer.valueOf(property.getProperty("medium_vm_cpu_cores"));
		vm_cpu_cores[2] = Integer.valueOf(property.getProperty("large_vm_cpu_cores"));
		
		vm_cpu_power=new int[vm_types_number];
		vm_cpu_power[0] = Integer.valueOf(property.getProperty("small_vm_cpu_power"));
		vm_cpu_power[1] = Integer.valueOf(property.getProperty("medium_vm_cpu_power"));
		vm_cpu_power[2] = Integer.valueOf(property.getProperty("large_vm_cpu_power"));

		vm_memory=new int[vm_types_number];
		vm_memory[0] = Integer.valueOf(property.getProperty("small_vm_memory"));
		vm_memory[1] = Integer.valueOf(property.getProperty("medium_vm_memory"));
		vm_memory[2] = Integer.valueOf(property.getProperty("large_vm_memory"));
		
		vm_storage=new int[vm_types_number];
		vm_storage[0] = Integer.valueOf(property.getProperty("small_vm_storage"));
		vm_storage[1] = Integer.valueOf(property.getProperty("medium_vm_storage"));
		vm_storage[2] = Integer.valueOf(property.getProperty("large_vm_storage"));
		
		vm_bandwidth=new int[vm_types_number];
		vm_bandwidth[0] = Integer.valueOf(property.getProperty("small_vm_bandwidth"));
		vm_bandwidth[1] = Integer.valueOf(property.getProperty("medium_vm_bandwidth"));
		vm_bandwidth[2] = Integer.valueOf(property.getProperty("large_vm_bandwidth"));



	}

	public int getSimulationID() {
		return simulationID;
	}

	public int getRunID() {
		return runID;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getSlotsNumber() {
		return slots;
	}

	public int getSlotDuration() {
		return slotDuration;
	}

	public String getSlotDurationMetric() {
		return slotDurationMetric;
	}

	public int getNumberOfMachineStatsPerSlot() {
		return number_of_machine_stats_per_slot;
	}

	public int getRemote_machines_number() {
		return remote_machines_number;
	}

	@SuppressWarnings("rawtypes")
	public Hashtable[] getRemote_machine_config() {
		return remote_machine_config;
	}

	public int getHosts_number() {
		return hosts_number;
	}

	@SuppressWarnings("rawtypes")
	public Hashtable[] getHost_machine_config() {
		return host_machine_config;
	}


	public int getVm_types_number() {
		return vm_types_number;
	}

	public String[] getVm_type_name() {
		return vm_type_name;
	}


	public int[] getVm_cpu_cores() {
		return vm_cpu_cores;
	}

	public int[] getVm_cpu_power() {
		return vm_cpu_power;
	}

	public int[] getVm_memory() {
		return vm_memory;
	}

	public int[] getVm_storage() {
		return vm_storage;
	}

	public int[] getVm_bandwidth() {
		return vm_bandwidth;
	}

	public int getProviders_number() {
		return providers_number;
	}

	public int getServices_number() {
		return services_number;
	}

	public double getOmega() {
		return omega;
	}

	public double getPriceBase() {
		return priceBase;
	}

	public double[] getPhiWeight() {
		return phiWeight;
	}

	public double[][] getPenalty() {
		return penalty;
	}

	public double[][] getXi() {
		return xi;
	}
	@SuppressWarnings("rawtypes")
	public Hashtable[][] getArrivals_generator() {
		return arrivals_generator;
	}

	@SuppressWarnings("rawtypes")
	public Hashtable[][] getLifetime_generator() {
		return lifetime_generator;
	}

	public Configuration get_config() {
		return _config;
	}

	public int getSlots() {
		return slots;
	}

	public int getNumber_of_machine_stats_per_slot() {
		return number_of_machine_stats_per_slot;
	}

	public String getWeb_stats_update_method() {
		return web_stats_update_method;
	}

	public int getSlots_window() {
		return slots_window;
	}

	public String getVm_series() {
		return vm_series;
	}

	public int getResources_number() {
		return resources_number;
	}


	public String[] getService_alias() {
		return service_alias;
	}

	public String[] getService_charm() {
		return service_charm;
	}

	public Boolean getSimulation_mode() {
		return simulation_mode;
	}

	public void setSimulationID(int simulationID) {
		this.simulationID = simulationID;
	}

	public void setRunID(int runID) {
		this.runID = runID;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}






}
