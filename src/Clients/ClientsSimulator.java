package Clients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Controller.Configuration;
import Controller.Controller;
import Enumerators.EGeneratorType;
import Enumerators.ESlotDurationMetric;
import Statistics.WebRequestStatsSlot;
import Utilities.Utilities;

public class ClientsSimulator extends Thread{

	ClientsConfiguration clients_config;
	Configuration config;
	int providers_number;
	int clients_number;
	int services_number;
	int[][][][][] running_allocations; // [s][n][p][v][s]
	Client[][][] clients; // [p][s][c]
	ClientRequest[][][] clients_thread; // [p][s][c]
	WebRequestStatsSlot[] _webRequestStatsSlot;
	Controller controller;
	int requests[][][];
	
	public ClientsSimulator(Configuration config, Controller controller) {
		this.config = config;
		this.controller = controller;
		this.clients_config = new ClientsConfiguration(config);
		this.providers_number = config.getProviders_number();
		this.clients_number = clients_config.getClients_number();
		this.services_number = config.getServices_number();
		this.clients = new Client[providers_number][services_number][clients_number];
		this.clients_thread = new ClientRequest[providers_number][services_number][clients_number];
		requests = new int[config.getProviders_number()][config.getServices_number()][clients_config.getClients_number()];

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					clients[p][s][c] = new Client(p, s, c, config, clients_config);
					clients_thread[p][s][c]=new ClientRequest(controller, config, clients_config,
							clients, p, s, c,requests);
				}
			}
		}
	}

	public void run() {

		System.out.println("********** Clients Requests Loader ****************");

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					clients_thread[p][s][c].start();

				}
			}
		}
		System.out.println("****** All Clients Request Generators Loaded ******");
		System.out.println();
	}
}

class ClientRequest extends Thread {

	int provider_id;
	int service_id;
	int client_id;
	Client[][][] clients;
	Configuration config;
	ClientsConfiguration clients_config;
	Controller controller;
	FakeServers fake_servers;
	int requests[][][];
	
	public ClientRequest(Controller controller, Configuration config, ClientsConfiguration clients_config,
			Client[][][] clients, int providerID, int serviceID, int clientID,int requests[][][]) {
		this.requests=requests;
		this.provider_id = providerID;
		this.service_id = serviceID;
		this.client_id = clientID;
		this.clients = clients;
		this.config = config;
		this.clients_config = clients_config;
		this.controller = controller;
		this.fake_servers = new FakeServers(config, clients_config);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {


		int vms_number = 0;
		double response_time = 0;
		int client_slot=0;
		List<Double> response_times=new ArrayList<Double>();
		double average=0;
		while(controller.getSlot()<config.getSlots()){
			
			if( client_slot<controller.getSlot()){
				client_slot=controller.getSlot();
				
				average= calculateAverage(response_times);
				sendClientStatsToDB(controller, provider_id, service_id, client_id, requests[provider_id][service_id][client_id],
				average);
				
				response_times.clear();
				
			}
			
			int[][][][] running_allocations = controller.getRunning_allocations();
			
			requests[provider_id][service_id][client_id]++;
			
			for (int n = 0; n < config.getHosts_number(); n++) {
				for (int v = 0; v < config.getVm_types_number(); v++) {
					vms_number += running_allocations[n][provider_id][v][service_id];
				}
			}

			if (vms_number > 0) {
				response_time = fake_servers.edgeServerResponseTime(service_id);
				response_time = response_time / vms_number; // assuming load
				// balancing and better
				// performance
			} else
				response_time = fake_servers.cloudServerResponseTime(service_id);

//			if(provider_id==1)
//				System.out.println(response_time);
			
			response_times.add(response_time);
			
			int duration = config.getSlotDuration();

			if (config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString())) {
				duration = 1000 * duration;
			} else if (config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString())) {
				duration = 60 * duration * 1000;
			} else if (config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString())) {
				duration = 3600 * duration * 1000;
			}

			double interarrivaltime=calculateWebRequestInterarrivalInterval(provider_id, service_id, client_id);
			double x = duration * interarrivaltime;
			long delay = (long) x;
			
			if(delay==0)
				delay=1;
			
			try {
				currentThread().sleep(delay);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		

	}

	private double calculateWebRequestInterarrivalInterval(int provider_id, int service_id, int client_id) {

		double interArrivalTime = 0.000001;
		int min = 0;
		int max = 0;

		String arrivalsType = (String) clients_config.getArrivals()[provider_id][service_id][client_id]
				.get("arrivals_type");

		switch (EGeneratorType.valueOf(arrivalsType)) {

		case Exponential:
			interArrivalTime = clients[provider_id][service_id][client_id].get_arrivalExpGenerator().random();
			break;

		case Pareto:
			interArrivalTime = clients[provider_id][service_id][client_id].get__arrivalParetoGenerator().random();
			break;

		case Random:
			min = clients[provider_id][service_id][client_id].get_arrivals_min();
			max = clients[provider_id][service_id][client_id].get_arrivals_max();
			interArrivalTime = (double) Utilities.randInt(min, max);
			break;

		default:
			break;
		}

		return interArrivalTime;

	}


	public void sendClientStatsToDB(Controller  controller, int provider_id, int service_id, int client_id, int request_index,
			double response_time) {

		String sql = "INSERT INTO CLIENTS(sim_id,run_id,slot,provider_id,service_id,client_id,request_index,response_time) VALUES(?,?,?,?,?,?,?,?)";
		int sim_id=config.getSimulationID();
		int run_id=config.getRunID();
		int slot=controller.getSlot();
		
		try {
			
			PreparedStatement pstmt = controller.getConn().prepareStatement(sql);

			pstmt.setInt(1, sim_id);
			pstmt.setInt(2, run_id);
			pstmt.setInt(3, slot);
			pstmt.setInt(4, provider_id);
			pstmt.setInt(5, service_id);
			pstmt.setInt(6, client_id);
			pstmt.setInt(7, request_index);
			pstmt.setDouble(8, response_time);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private double calculateAverage(List <Double> marks) {
		  Double sum = 0.0;
		  if(!marks.isEmpty()) {
		    for (Double mark : marks) {
		        sum += mark;
		    }
		    return sum.doubleValue() / marks.size();
		  }
		  return sum;
		}
	
}

