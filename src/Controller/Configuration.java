package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Enumerators.EGeneratorType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
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
	String vm_os;
	String[] vm_type_name;
	int[] vm_cpu_cores; // One per VM Type 0:small, 1:medium, 2:large
	int[] vm_cpu_power; // One per VM Type 0:small, 1:medium, 2:large
	int[] vm_memory; // One per VM Type
	int[] vm_storage; // One per VM Type
	int[] vm_bandwidth; // One per VM Type

	// ========= Local Cloud VMs ==========

	int providers_number;
	int services_number; // Each Service Corresponds to one VNF

	// ========= CPLEX ==========

	double omega;
	double priceBase;
	double[] phiWeight;

	double[][] penalty; // [p][s] provider p, service s
	double[][] xi; // [v][s] vm v, service s

	// ========= PROVIDERS ==========
	Hashtable[][] arrivals_generator;
	Hashtable[][] lifetime_generator;

	public Configuration() {

		this.loadSimulationProperties();
		this.loadEnvironmentProperties();
		this.serviceTrafficGenerators();
		this.serviceLifetimeGenerators();

	}

	@SuppressWarnings("unchecked")
	private void serviceTrafficGenerators() {
		
		arrivals_generator = new Hashtable[providers_number][services_number];

		Properties property = new Properties();
		InputStream input = null;
		String filename = "simulation.properties";

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
				arrivals_generator[p][s]=new Hashtable();

				// Estimated requests
				parameter = "provider" + p + "_service" + s + "_estimatedRequets";
				int_value = Integer.valueOf((String) property.getProperty(parameter));
				arrivals_generator[p][s].put("estimated_requests", int_value);

				//arrivals type
				parameter = "provider" + p + "_service" + s + "_arrivals_type";
				arrival_rate_type = String.valueOf((String) property.getProperty(parameter));
				arrivals_generator[p][s].put("arrival_type", int_value);
				
				// Exponential
				if (EGeneratorType.Exponential.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_lamda";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					double_value = (double) 1 / double_value;
					arrivals_generator[p][s].put("arrivals_lamda", double_value);
				
				} else if (EGeneratorType.Pareto.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_location";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_location", double_value);

					parameter = "provider" + p + "_service" + s + "_arrivals_shape";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_location", double_value);
				

				} else if (EGeneratorType.Random.toString().equals(arrival_rate_type)) {

					parameter = "provider" + p + "_service" + s + "_arrivals_min";
					int_value = Integer.valueOf((String) property.getProperty(parameter));
					arrivals_generator[p][s].put("arrivals_min", int_value);
					
					parameter = "provider" + p + "_service" + s + "_arrivals_max";
					int_value = Integer.valueOf((String) property.getProperty(parameter));
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
		String filename = "simulation.properties";

		input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}

		String parameter = "";
		String ifetime_type = "";
		double double_value = -1;
		int int_value = -1;
		

		for (int p = 0; p < providers_number; p++) {
					
			for (int s = 0; s < services_number; s++) {
				lifetime_generator [p][s]=new Hashtable();


				//arrivals type
				parameter = "provider" + p + "_service" + s + "_lifetime_type";
				ifetime_type = String.valueOf((String) property.getProperty(parameter));
				lifetime_generator [p][s].put("lifetime_type", int_value);
				
				// Exponential
				if (EGeneratorType.Exponential.toString().equals(ifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_lamda";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					double_value = (double) 1 / double_value;
					lifetime_generator [p][s].put("lifetime_lamda", double_value);
				
				} else if (EGeneratorType.Pareto.toString().equals(ifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_location";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_location", double_value);

					parameter = "provider" + p + "_service" + s + "_lifetime_shape";
					double_value = Double.valueOf((String) property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_shape", double_value);
				

				} else if (EGeneratorType.Random.toString().equals(ifetime_type)) {

					parameter = "provider" + p + "_service" + s + "_lifetime_min";
					int_value = Integer.valueOf((String) property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_min", int_value);
					
					parameter = "provider" + p + "_service" + s + "_lifetime_max";
					int_value= Integer.valueOf((String) property.getProperty(parameter));
					lifetime_generator [p][s].put("lifetime_max", int_value);
				}

			}
		}
		
		

	}

	private void loadEnvironmentProperties() {

		Properties property = new Properties();
		InputStream input = null;
		String filename = "env.properties";
		String parameter = "";
		input = Configuration.class.getClassLoader().getResourceAsStream(filename);

		try {
			property.load(input); // load a properties file

			providers_number = Integer.valueOf(property.getProperty("providers_number"));
			services_number = Integer.valueOf(property.getProperty("services_number"));

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

		} catch (IOException ex) {
			Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@SuppressWarnings("unchecked")
	private void loadSimulationProperties() {

		Properties property = new Properties();
		String parameter = "";
		String svalue = "";

		
		try {
			String filename = "simulation.properties";
			InputStream input = Configuration.class.getClassLoader().getResourceAsStream(filename);

			// load a properties file
			property.load(input);

			simulationID = Integer.valueOf(property.getProperty("simulationID"));
			runID = Integer.valueOf(property.getProperty("runID"));
			algorithm = String.valueOf(property.getProperty("algorithm"));

			slots = Integer.valueOf(property.getProperty("slots"));
			slotDuration = Integer.valueOf(property.getProperty("slotDuration"));
			slotDurationMetric = String.valueOf(property.getProperty("slotDurationMetric"));
			number_of_machine_stats_per_slot = Integer.valueOf(property.getProperty("number_of_machine_stats_per_slot"));
			web_stats_update_method=String.valueOf(property.getProperty("web_stats_update_method"));
			slots_window=Integer.valueOf(property.getProperty("window"));
			// Remote Cloud Machines
			remote_machines_number = Integer.valueOf(property.getProperty("remote_machines_number"));
			remote_machine_config = new Hashtable[remote_machines_number];

			for (int i = 0; i < remote_machines_number; i++) {

				parameter = "remote_machine_ip_" + String.valueOf(i);
				svalue = String.valueOf((String) property.getProperty(parameter));
				remote_machine_config[i].put("ip", svalue);
			}
			
			resources_number=Integer.valueOf((String) property.getProperty("resources_number"));
			
			// Local Cloud Hosts
			hosts_number = Integer.valueOf(property.getProperty("hosts_number"));
			host_machine_config = new Hashtable[hosts_number];
			for (int i = 0; i < hosts_number; i++) {
				
				parameter = "host_"+ String.valueOf(i)+"_ip";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("ip", svalue);
				
				parameter = "host_"+ String.valueOf(i)+"_cpu_cores";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("cpu_cores", svalue);
				
				parameter = "host_"+ String.valueOf(i)+"_cpu_power";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("cpu_power", svalue);
				
				parameter = "host_"+ String.valueOf(i)+"_memory";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("memory", svalue);
				
				parameter = "host_"+ String.valueOf(i)+"_storage";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("storage", svalue);
				
				parameter = "host_"+ String.valueOf(i)+"_bandwidth";
				svalue = String.valueOf((String) property.getProperty(parameter));
				host_machine_config[i].put("bandwidth", svalue);
				
			}

			

			// Local Cloud VMs
			vm_types_number = Integer.valueOf(property.getProperty("vm_types_number"));
			vm_os= String.valueOf(property.getProperty("vm_os"));
			
			this.vm_type_name = new String[vm_types_number];

			// VM_Types
			for (int i = 0; i < vm_types_number; i++) {
				parameter = "vm_name_type_" + i;
				svalue = String.valueOf((String) property.getProperty(parameter));
				vm_type_name[i] = svalue;
			}

			vm_cpu_cores[0] = Integer.valueOf((String) property.getProperty("small_vm_cpu_cores"));
			vm_cpu_cores[1] = Integer.valueOf((String) property.getProperty("medium_vm_cpu_cores"));
			vm_cpu_cores[2] = Integer.valueOf((String) property.getProperty("large_vm_cpu_cores"));

			vm_cpu_power[0] = Integer.valueOf((String) property.getProperty("small_vm_cpu_power"));
			vm_cpu_power[1] = Integer.valueOf((String) property.getProperty("medium_vm_cpu_power"));
			vm_cpu_power[2] = Integer.valueOf((String) property.getProperty("large_vm_cpu_power"));

			
			vm_memory[0] = Integer.valueOf((String) property.getProperty("small_vm_memory"));
			vm_memory[1] = Integer.valueOf((String) property.getProperty("medium_vm_memory"));
			vm_memory[2] = Integer.valueOf((String) property.getProperty("large_vm_memory"));

			vm_storage[0] = Integer.valueOf((String) property.getProperty("small_vm_storage"));
			vm_storage[1] = Integer.valueOf((String) property.getProperty("medium_vm_storage"));
			vm_storage[2] = Integer.valueOf((String) property.getProperty("large_vm_storage"));

			vm_bandwidth[0] = Integer.valueOf((String) property.getProperty("small_vm_bandwidth"));
			vm_bandwidth[1] = Integer.valueOf((String) property.getProperty("medium_vm_bandwidth"));
			vm_bandwidth[2] = Integer.valueOf((String) property.getProperty("large_vm_bandwidth"));

		} catch (Exception e) {
			System.out.println(e.toString());
		}

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

	public Hashtable[] getRemote_machine_config() {
		return remote_machine_config;
	}

	public int getHosts_number() {
		return hosts_number;
	}

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

	public Hashtable[][] getArrivals_generator() {
		return arrivals_generator;
	}

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

	public String getVm_os() {
		return vm_os;
	}

	public int getResources_number() {
		return resources_number;
	}
	
	
	
	

}
