package Clients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import Controller.Configuration;
import Controller.Controller;
import Enumerators.EGeneratorType;
import Enumerators.ESlotDurationMetric;
import Statistics.WebRequestStatsSlot;
import Utilities.Utilities;

public class ClientsSimulator {

	ClientsConfiguration clients_config;
	Configuration config;
	int providers_number;
	int clients_number;
	int services_number;
	int[][][][][] running_allocations; // [s][n][p][v][s]
	Client[][][] clients; // [p][s][c]
	Timer[][][] clientsTimer; // [p][s][c]
	WebRequestStatsSlot[] _webRequestStatsSlot;
	Controller controller;

	public ClientsSimulator(Configuration config, Controller controller) {
		this.config = config;
		this.controller = controller;
		this.clients_config = new ClientsConfiguration(config);
		this.providers_number = config.getProviders_number();
		this.clients_number = clients_config.getClients_number();
		this.services_number = config.getServices_number();
		this.clients = new Client[providers_number][services_number][clients_number];
		this.clientsTimer = new Timer[providers_number][services_number][clients_number];

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					clients[p][s][c] = new Client(p, s, c, config, clients_config);
					clientsTimer[p][s][c]=new Timer();
				}
			}
		}
	}

	public void startClientsRequests() {

		System.out.println("********** Clients Requests Loader ****************");

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					clientsTimer[p][s][c].schedule(new ExecuteClientRequest(controller, config, clients_config,
							clientsTimer, clients, p, s, c), 100); 
					// 100)
				}
			}
			System.out.println("****** All Clients Request Generators Loaded ******");
			System.out.println();
		}
	}
}

class ExecuteClientRequest extends TimerTask {

	int provider_id;
	int service_id;
	int client_id;
	Client[][][] clients;
	Configuration config;
	ClientsConfiguration clients_config;
	Timer[][][] clientsTimer;
	Controller controller;
	FakeServers fake_servers;
	int requests[][];

	public ExecuteClientRequest(Controller controller, Configuration config, ClientsConfiguration clients_config,
			Timer[][][] clientsTimer, Client[][][] clients, int providerID, int serviceID, int clientID) {
		this.provider_id = providerID;
		this.service_id = serviceID;
		this.client_id = clientID;
		this.clients = clients;
		this.clientsTimer = clientsTimer;
		this.config = config;
		this.clients_config = clients_config;
		this.controller = controller;
		this.fake_servers = new FakeServers(config, clients_config);
		requests = new int[clients_config.getClients_number()][config.getServices_number()];
	}

	@Override
	public void run() {

		int vms_number = 0;
		double response_time = 0;
		int[][][][] running_allocations = controller.getRunning_allocations();
		requests[service_id][client_id]++;
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

		sendClientStatsToDB(controller.getSlot(), provider_id, service_id, client_id, requests[service_id][client_id],
				response_time);

		int duration = config.getSlotDuration();

		if (config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString())) {
			duration = 1000 * duration;
		} else if (config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString())) {
			duration = 60 * duration * 1000;
		} else if (config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString())) {
			duration = 3600 * duration * 1000;
		}

		double x = duration * calculateWebRequestInterarrivalInterval(provider_id, service_id, client_id);
		long delay = (long) x;

		clientsTimer[provider_id][service_id][client_id].schedule(new ExecuteClientRequest(controller, config,
				clients_config, clientsTimer, clients, provider_id, service_id, client_id), delay);

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

	
	public void sendClientStatsToDB(int slot, int provider_id, int service_id, int client_id, int request,
			double response_time) {

		String sql = "INSERT INTO STATS_CLIENTS(slot,provider_id,service_id,client_id,request,response_time) VALUES(?,?,?,?,?,?)";

		try {
			Connection conn =controller.getConn(); 

			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, slot);
			pstmt.setInt(2, provider_id);
			pstmt.setInt(3, service_id);
			pstmt.setInt(4, client_id);
			pstmt.setInt(5, request);
			pstmt.setDouble(6, response_time);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

}
