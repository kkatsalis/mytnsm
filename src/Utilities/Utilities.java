/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Controller.Configuration;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author kostas
 */
public class Utilities {

	public static int randInt(int min, int max) {

		Random rand = new Random();

		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Hashtable determineVMparameters(Configuration config, int host_identifier, int vm_type, int service_id) {

		Hashtable parameters = new Hashtable();

		String host_ip = (String) config.getHost_machine_config()[host_identifier].get("ip");
		String vm_os = config.getVm_os();

		int vm_cpu = config.getVm_cpu()[vm_type];
		int vm_memory = config.getVm_memory()[vm_type];
		int vm_storage = config.getVm_storage()[vm_type];
		int vm_bandwidth = config.getVm_bandwidth()[vm_type];

		parameters.put("host_ip", host_ip);
		parameters.put("vm_os", vm_os);
		parameters.put("vm_cpu", vm_cpu);
		parameters.put("vm_memory", vm_memory);
		parameters.put("vm_storage", vm_storage);
		parameters.put("vm_bandwidth", vm_bandwidth);

		return parameters;

	}

	public static int getRequestsMadefromDB(int slot, int p, int s) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("rawtypes")
	public static void updateAllHostStatisObjects(Configuration config, int slot, int measurement) {

		String host_identifier;
		Hashtable host_config;
		WebUtilities web_utilities = new WebUtilities(config);

		for (int i = 0; i < config.getHosts_number(); i++) {

			host_config = config.getHost_machine_config()[i];
			host_identifier = (String) host_config.get("ip");
			Hashtable parameters = web_utilities.retrieveHostStats(host_identifier, slot, measurement);

			// ----------------
			// @ToDo send to DB

		}

	}

	public static void updateVMsStatistics(Configuration _config, int slot, int _currentInstance) {
		// TODO Auto-generated method stub

	}

	public static void updateSimulatorStatistics(Configuration _config, int slot, int _currentInstance) {
		// TODO Auto-generated method stub

	}

	public static void checkVM(String vm_name) {
		// TODO Auto-generated method stub

	}

}
