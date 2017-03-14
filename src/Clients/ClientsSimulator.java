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
import Controller.SlotChangedListener;
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
	ClientRequests[][][] clients_thread; // [p][s][c]
	WebRequestStatsSlot[] _webRequestStatsSlot;
	Controller controller;
	int requests[][][];
	List<SlotChangedListener> slot_changed_listeners;
	
	public ClientsSimulator(Configuration config, Controller controller) {
		this.config = config;
		this.controller = controller;
		this.clients_config = new ClientsConfiguration(config);
		this.providers_number = config.getProviders_number();
		this.clients_number = clients_config.getClients_number();
		this.services_number = config.getServices_number();
		this.clients = new Client[providers_number][services_number][clients_number];
		this.clients_thread = new ClientRequests[providers_number][services_number][clients_number];
		this.requests = new int[config.getProviders_number()][config.getServices_number()][clients_config.getClients_number()];
		slot_changed_listeners=controller.getSlot_changed_listeners();
	
		
		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					clients[p][s][c] = new Client(p, s, c, config, clients_config);
					clients_thread[p][s][c]=new ClientRequests(controller, config, clients_config,
							clients, p, s, c,requests);
					slot_changed_listeners.add(clients_thread[p][s][c]);
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

class ClientRequests extends Thread implements SlotChangedListener{

	int provider_id;
	int service_id;
	int client_id;
	Client[][][] clients;
	Configuration config;
	ClientsConfiguration clients_config;
	Controller controller;
	FakeServers fake_servers;
	int requests[][][];
	int running_slot;
	List<Double>[] response_times;
	double average=0;
	
	@SuppressWarnings("unchecked")
	public ClientRequests(Controller controller, Configuration config, ClientsConfiguration clients_config,
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
		this.running_slot=0;
		this.response_times=new ArrayList[config.getSlots()];
		for (int i = 0; i < config.getSlots(); i++) {
			response_times[i]=new ArrayList<Double>();
		}
	}
	
	@Override
    public void slotChanged(int new_slot) {
        running_slot=new_slot;
        
    	average= calculateAverage(response_times[running_slot-1]);
		sendClientStatsToDB(controller, running_slot-1, provider_id, service_id, client_id, requests[provider_id][service_id][client_id],
		average);
		
		response_times[running_slot-1].clear();

    }

	@SuppressWarnings("static-access")
	@Override
	public void run() {

		int vms_number = 0;
		double response_time = 0;
		
		while(running_slot<config.getSlots()){
			
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

			
			response_times[running_slot].add(response_time);
			
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


	public void sendClientStatsToDB(Controller  controller,int slot, int provider_id, int service_id, int client_id, int request_index,
			double response_time) {

		String sql = "INSERT INTO CLIENTS(sim_id,run_id,slot,provider_id,service_id,client_id,request_index,response_time) VALUES(?,?,?,?,?,?,?,?)";
		int sim_id=config.getSimulationID();
		int run_id=config.getRunID();

		
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

