package Clients;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import Controller.Configuration;
import Enumerators.EGeneratorType;

@SuppressWarnings("rawtypes")
public class ClientsConfiguration {

	Hashtable[][][] arrivals;
	Hashtable[] service_time_edge; // [s:service]
	Hashtable[] service_time_cloud; // [s:service]

	int clients_number;
	int providers_number;
	int services_number;

	String filename;
	Configuration config;
	
	public ClientsConfiguration(Configuration config) {
		this.config=config;
		filename = "client.properties";
		this.loadParameters();
		this.arrivalsConfig();
		this.serviceEdgeConfig();
		this.serviceCloudConfig();
		
	}

	private void loadParameters() {

		Properties property = new Properties();
		InputStream input = null;
		
		input = ClientsConfiguration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(ClientsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
		}

		providers_number = config.getProviders_number();
		services_number = config.getServices_number();
		clients_number = Integer.valueOf(property.getProperty("clients_number"));

		service_time_cloud = new Hashtable[services_number];
		service_time_edge = new Hashtable[services_number];
		arrivals = new Hashtable[providers_number][services_number][clients_number];

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void arrivalsConfig() {

		Properties property = new Properties();
		InputStream input = null;

		input = ClientsConfiguration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(ClientsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
		}

		String parameter = "";
		String rate_type = "";
		double double_value = -1;
		int int_value = -1;

		for (int p = 0; p < providers_number; p++) {
			for (int s = 0; s < services_number; s++) {
				for (int c = 0; c < clients_number; c++) {
					arrivals[p][s][c] = new Hashtable();

					// arrivals type
					parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_type";
					rate_type = String.valueOf(property.getProperty(parameter));
					arrivals[p][s][c].put("arrivals_type", rate_type);

					// Exponential
					if (rate_type.equals(EGeneratorType.Exponential.toString())) {

						parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_lamda";
						double_value = Double.valueOf(property.getProperty(parameter));
						double_value = (double) 1 / double_value;
						arrivals[p][s][c].put("arrivals_lamda", double_value);

					} else if (rate_type.equals(EGeneratorType.Pareto.toString())) {

						parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_location";
						double_value = Double.valueOf(property.getProperty(parameter));
						arrivals[p][s][c].put("arrivals_location", double_value);

						parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_shape";
						double_value = Double.valueOf(property.getProperty(parameter));
						arrivals[p][s][c].put("arrivals_location", double_value);

					} else if (rate_type.equals(EGeneratorType.Random.toString())) {

						parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_min";
						int_value = Integer.valueOf(property.getProperty(parameter));
						arrivals[p][s][c].put("arrivals_min", int_value);

						parameter = "provider" + p + "_service" + s + "_client" + c + "_arrivals_max";
						int_value = Integer.valueOf(property.getProperty(parameter));
						arrivals[p][s][c].put("arrivals_max", int_value);
					}

				}

			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void serviceEdgeConfig() {

		Properties property = new Properties();
		InputStream input = null;

		input = ClientsConfiguration.class.getClassLoader().getResourceAsStream(filename);

		try {
			// load a properties file
			property.load(input);
		} catch (IOException ex) {
			Logger.getLogger(ClientsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
		}

		String parameter = "";
		String rate_type = "";
		double double_value = -1;
		int int_value = -1;

		for (int s = 0; s < services_number; s++) {

			service_time_edge[s] = new Hashtable();

			parameter = "service" + s + "_service_time_edge_type";
			rate_type = String.valueOf(property.getProperty(parameter));
			service_time_edge[s].put("service_time_edge_type", rate_type);

			// Exponential
			if (rate_type.equals(EGeneratorType.Exponential.toString())) {

				parameter = "service" + s + "_service_time_edge_lamda";
				double_value = Double.valueOf(property.getProperty(parameter));
				service_time_edge[s].put("service_time_edge_lamda", double_value);

			} else if (rate_type.equals(EGeneratorType.Pareto.toString())) {

				parameter = "service" + s + "_service_time_edge_location";
				double_value = Double.valueOf(property.getProperty(parameter));
				service_time_edge[s].put("service_time_edge_location", double_value);

				parameter = "service" + s + "_service_time_edge_shape";
				double_value = Double.valueOf(property.getProperty(parameter));
				service_time_edge[s].put("service_time_edge_shape", double_value);

			} else if (rate_type.equals(EGeneratorType.Random.toString())) {

				parameter = "_service" + s + "_service_time_edge_min";
				int_value = Integer.valueOf(property.getProperty(parameter));
				service_time_edge[s].put("service_time_edge_min", int_value);

				parameter = "service" + s + "_service_time_edge_max";
				int_value = Integer.valueOf(property.getProperty(parameter));
				service_time_edge[s].put("service_time_edge_max", int_value);
			}

		}

	}


@SuppressWarnings({ "rawtypes", "unchecked" })
private void serviceCloudConfig() {

	Properties property = new Properties();
	InputStream input = null;

	input = ClientsConfiguration.class.getClassLoader().getResourceAsStream(filename);

	try {
		// load a properties file
		property.load(input);
	} catch (IOException ex) {
		Logger.getLogger(ClientsConfiguration.class.getName()).log(Level.SEVERE, null, ex);
	}

	String parameter = "";
	String rate_type = "";
	double double_value = -1;
	int int_value = -1;

	for (int s = 0; s < services_number; s++) {

		service_time_cloud[s] = new Hashtable();

		parameter = "service" + s + "_service_time_cloud_type";
		rate_type = String.valueOf(property.getProperty(parameter));
		service_time_cloud[s].put("service_time_cloud_type", rate_type);

		// Exponential
		if (rate_type.equals(EGeneratorType.Exponential.toString())) {

			parameter = "service" + s + "_service_time_cloud_lamda";
			double_value = Double.valueOf(property.getProperty(parameter));
			service_time_cloud[s].put("service_time_cloud_lamda", double_value);

		} else if (rate_type.equals(EGeneratorType.Pareto.toString())) {

			parameter = "service" + s + "_service_time_cloud_location";
			double_value = Double.valueOf(property.getProperty(parameter));
			service_time_cloud[s].put("service_time_cloud_location", double_value);

			parameter = "service" + s + "_service_time_cloud_shape";
			double_value = Double.valueOf(property.getProperty(parameter));
			service_time_cloud[s].put("service_time_cloud_shape", double_value);

		} else if (rate_type.equals(EGeneratorType.Random.toString())) {

			parameter = "service" + s + "_service_time_cloud_min";
			int_value = Integer.valueOf(property.getProperty(parameter));
			service_time_cloud[s].put("service_time_local_min", int_value);

			parameter = "service" + s + "_service_time_cloud_max";
			int_value = Integer.valueOf(property.getProperty(parameter));
			service_time_cloud[s].put("service_time_cloud_max", int_value);
		}

	}

}



public Hashtable[][][] getArrivals() {
	return arrivals;
}

public Hashtable[] getService_time_edge() {
	return service_time_edge;
}

public Hashtable[] getService_time_cloud() {
	return service_time_cloud;
}

public int getClients_number() {
	return clients_number;
}

public int getProviders_number() {
	return providers_number;
}

public int getServices_number() {
	return services_number;
}

}
