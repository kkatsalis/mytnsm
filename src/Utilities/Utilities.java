/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Controller.Configuration;
import java.util.Hashtable;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

		int vm_cpu_power = config.getVm_cpu_power()[vm_type];
		int vm_cpu_cores= config.getVm_cpu_cores()[vm_type];
		int vm_memory = config.getVm_memory()[vm_type];
		int vm_storage = config.getVm_storage()[vm_type];
		int vm_bandwidth = config.getVm_bandwidth()[vm_type];

		parameters.put("host_ip", host_ip);
		parameters.put("vm_os", vm_os);
		parameters.put("vm_cpu_power", vm_cpu_power);
		parameters.put("vm_cpu_cores", vm_cpu_cores);
		parameters.put("vm_memory", vm_memory);
		parameters.put("vm_storage", vm_storage);
		parameters.put("vm_bandwidth", vm_bandwidth);

		return parameters;

	}

	public static Connection connect() {
		Connection conn = null;
		try {
			// db parameters
			String url = "jdbc:sqlite:activations_stats.db";
			// create a connection to the database
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
		return conn;
	}

	public static void updateActivationStats(int slot, Configuration config,int[][][][] activation_matrix) {

		String sql = "INSERT INTO ACTIVATION_STATS(slot,algorithm,host_id,provider_id,vm_type_id,service_id,vms_requested) VALUES(?,?,?,?,?,?,?)";
		String algorithm=config.getAlgorithm();
		int hosts_number = config.getHosts_number();
		int providers_number = config.getProviders_number();
		int vm_types_number = config.getVm_types_number();
		int services_number = config.getServices_number();
		int vms_number=0;

		for (int n = 0; n < hosts_number; n++) {
			for (int p = 0; p < providers_number; p++) {
				for (int v = 0; v < vm_types_number; v++) {
					for (int s = 0; s < services_number; s++) {
						vms_number = activation_matrix[n][p][v][s];

						try (Connection conn = connect();
								PreparedStatement pstmt = conn.prepareStatement(sql)) {
							pstmt.setInt(1, slot);
							pstmt.setString(2, algorithm);
							pstmt.setInt(3, n);
							pstmt.setInt(4, p);
							pstmt.setInt(5, v);
							pstmt.setInt(6, s);
							pstmt.setInt(7, vms_number);

							pstmt.executeUpdate();
						} catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}
	}

	public static void updateRequestStats(int slot, int p, int v,int s, int vms) {	

		String sql = "INSERT INTO VM_REQUESTS_STATS(slot, provider_id,vm_type_id,service_id,vms_requested) VALUES(?,?,?,?,?)";
				
		try (Connection conn = connect();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, slot);
			pstmt.setInt(4, p);
			pstmt.setInt(5, v);
			pstmt.setInt(6, s);
			pstmt.setInt(7, vms);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}



public static int getRequestsMadefromDB(int slot, int p, int s) {
	// TODO Auto-generated method stub
	return 0;
}




}
