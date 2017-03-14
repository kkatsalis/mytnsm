/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Controller.Configuration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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


	public static Connection connect(Configuration config) {
		Connection conn = null;
		try {
			// db parameters
			String url = "jdbc:sqlite:";

			url+="Sim"+config.getSimulationID()+"_"+config.getAlgorithm();			
			// create a connection to the database
			conn = DriverManager.getConnection(url);

			//			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
		return conn;
	}

	public static void updateActivationStats(Connection conn,int slot, Configuration config,int[][][][] activation_matrix, int[][][] total_vms_allocated, double benefit) {

		String sql = "INSERT INTO ACTIVATION(sim_id, run_id,slot,algorithm,host_id,provider_id,vm_type_id,service_id,vms_allocated,total_vms_allocated,benefit) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		String algorithm=config.getAlgorithm();
		int sim_id=config.getSimulationID();
		int run_id=config.getRunID();
		int hosts_number = config.getHosts_number();
		int providers_number = config.getProviders_number();
		int vm_types_number = config.getVm_types_number();
		int services_number = config.getServices_number();
		int vms_number=0;
		int total_vms_number=0;

		for (int n = 0; n < hosts_number; n++) {
			for (int p = 0; p < providers_number; p++) {
				for (int v = 0; v < vm_types_number; v++) {
					for (int s = 0; s < services_number; s++) {
						vms_number = activation_matrix[n][p][v][s];
						total_vms_number=total_vms_allocated[p][v][s];

						try{
							PreparedStatement pstmt = conn.prepareStatement(sql); 
							pstmt.setInt(1, sim_id);
							pstmt.setInt(2, run_id);
							pstmt.setInt(3, slot);
							pstmt.setString(4, algorithm);
							pstmt.setInt(5, n);
							pstmt.setInt(6, p);
							pstmt.setInt(7, v);
							pstmt.setInt(8, s);
							pstmt.setInt(9, vms_number);
							pstmt.setDouble(10,total_vms_number);
							pstmt.setDouble(11, benefit);


							pstmt.executeUpdate();
						} catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}
	}

	public static void updateVmRequestStats2Db(Connection conn,int slot, Configuration config,int[][][] vmRequestMatrix,int[][][] total_requests) {	

		String sql = "INSERT INTO VMS_REQUESTED(sim_id,run_id,slot, provider_id,vm_type_id,service_id,vms_requested,total_vms_requested) VALUES(?,?,?,?,?,?,?,?)";
		int sim_id=config.getSimulationID();
		int run_id=config.getRunID();
		int providers_number = config.getProviders_number();
		int vm_types_number = config.getVm_types_number();
		int services_number = config.getServices_number();
		int vms=0;
		int total_vms=0;
		for (int p = 0; p < providers_number; p++) {
			for (int v = 0; v < vm_types_number; v++) {
				for (int s = 0; s < services_number; s++) {
					vms=vmRequestMatrix[p][v][s];
					total_vms=total_requests[p][v][s];
					try {
						PreparedStatement pstmt = conn.prepareStatement(sql);
						pstmt.setInt(1, sim_id);
						pstmt.setInt(2, run_id);
						pstmt.setInt(3, slot);
						pstmt.setInt(4, p);
						pstmt.setInt(5, v);
						pstmt.setInt(6, s);
						pstmt.setInt(7, vms);
						pstmt.setInt(8, total_vms);
						pstmt.executeUpdate();
					} catch (SQLException e) {
						System.out.println(e.getMessage());
					}
				}
			}	
		}

	}

	public static String getVMTypeName(int vm_id){

		String vm_type_name="";

		switch (vm_id) {
		case 0:
			vm_type_name="kvm-small";
			break;
		case 1:
			vm_type_name="kvm-medium";
			break;
		case 2:
			vm_type_name="kvm-large";
			break;

		default:
			break;
		}

		return vm_type_name;

	}

	@SuppressWarnings("rawtypes")
	public static int[] getHostMaxCapacity(Configuration config, int host_id){

		int resources_number=config.getResources_number();
		int[] host_max_capacity=new int[resources_number];
		Hashtable host_config=config.getHost_machine_config()[host_id];

		for (int r = 0; r < resources_number; r++) {

			switch (r) {
			case 0:
				host_max_capacity[r]=Integer.valueOf((String) host_config.get("cpu_cores"));
				break;
			case 1:
				host_max_capacity[r]=Integer.valueOf((String) host_config.get("cpu_power"));
				break;
			case 2:
				host_max_capacity[r]=Integer.valueOf((String) host_config.get("memory"));
				break;
			case 3:
				host_max_capacity[r]=Integer.valueOf((String) host_config.get("storage"));
				break;
			case 4:
				host_max_capacity[r]=Integer.valueOf((String) host_config.get("bandwidth"));
				break;

			default:
				break;
			}
		}
		return  host_max_capacity;

	}

	public static int getVmResourceCost(Configuration config, int vm_type, int resource_id){

		int resource_cost=0;

		switch (resource_id) {
		case 0:
			resource_cost=config.getVm_cpu_cores()[vm_type];
			break;
		case 1:
			resource_cost=config.getVm_cpu_power()[vm_type];
			break;
		case 2:
			resource_cost=config.getVm_memory()[vm_type];
			break;
		case 3:
			resource_cost=config.getVm_storage()[vm_type];
			break;
		case 4:
			resource_cost=config.getVm_bandwidth()[vm_type];
			break;

		default:
			break;
		}

		return resource_cost;

	}

	public static String buildServiceName(Configuration config, int provider_id, int service_id) {

		String alias=config.getService_alias()[service_id];
		String service_name=alias+"p"+provider_id;

		return service_name;
	}

	public static String getServiceName(Configuration config,int provider_id, int service_id) {

		String alias=config.getService_alias()[service_id];
		String service_name=alias+"p"+provider_id;

		return service_name;
	}

	static ResultSet executeQR(String query, Connection conn){
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch(SQLException e) {
			System.err.println("Query "+query+" failed");
		}
		return rs;
	}

	public static int getRequestsMadefromDB(Connection conn,int slot, int p, int s	)
	{

		int requests = 0;
		System.out.println("update request pattern called- slot:"+slot+"-p:"+p+"-s:"+s);

		String qr = "SELECT request_index FROM CLIENTS WHERE PROVIDER_ID="+p
				+" AND service_id="+s+" AND Slot="+slot;
		
		try {				
			ResultSet rs = executeQR(qr,conn);
			 
			while (rs.next()) {
				requests=Integer.valueOf(rs.getString("request_index"));
				System.out.println("slot"+slot+"_p"+p+"total-req: "+requests);
			}
			

		}catch(NoSuchElementException e) {
			e.printStackTrace();

		} catch(SQLException e) {
			System.err.println("Query failed");
		}
		return requests;

	}
}



