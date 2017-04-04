package LocalClients;

import Controller.Configuration;
import Enumerators.EGeneratorType;
import Utilities.Utilities;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

public class FakeServers {

	Exponential[] _serviceEdgeExpGenerator;
	Exponential[] _serviceCloudExpGenerator;
	Pareto[] _serviceEdgeParetoGenerator;
	Pareto[] _serviceCloudParetoGenerator;

	int[] _service_edge_min;
	int[] _service_cloud_min;
	int[] _service_edge_max;
	int[] _service_cloud_max;

	Configuration config;
	ClientsConfiguration clients_config;

	public FakeServers(Configuration config, ClientsConfiguration clients_config) {
		this.config = config;
		this.clients_config = clients_config;
		initializeCloudServersGenerators();
		initializeEdgeServersGenerators();
	}

	private void initializeEdgeServersGenerators() {
		int services_number = config.getServices_number();

		_serviceEdgeExpGenerator = new Exponential[services_number];
		_serviceEdgeParetoGenerator = new Pareto[services_number];
		_service_edge_min = new int[services_number];
		_service_edge_max = new int[services_number];

		String type = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			type = (String) clients_config.getService_time_edge()[s].get("service_time_edge_type");

			if (type.equals("Exponential")) {

				lamda = (double) clients_config.getService_time_edge()[s].get("service_time_edge_lamda");
				_serviceEdgeExpGenerator[s] = new Exponential(lamda);

			} else if (type.equals("Pareto")) {

				location = (double) clients_config.getService_time_edge()[s].get("service_time_edge_location");
				shape = (double) clients_config.getService_time_edge()[s].get("service_time_edge_shape");
				_serviceEdgeParetoGenerator[s] = new Pareto(location, shape);
			} else if (type.equals("Random")) {
				_service_edge_min[s] = (int) clients_config.getService_time_edge()[s].get("service_time_edge_min");
				_service_edge_max[s] = (int) clients_config.getService_time_edge()[s].get("service_time_edge_max");

			}

		}

	}

	private void initializeCloudServersGenerators() {
		int services_number = config.getServices_number();

		_serviceCloudExpGenerator = new Exponential[services_number];
		_serviceCloudParetoGenerator = new Pareto[services_number];
		_service_cloud_min = new int[services_number];
		_service_cloud_max = new int[services_number];

		String type = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			type = (String) clients_config.getService_time_cloud()[s].get("service_time_cloud_type");

			if (type.equals("Exponential")) {

				lamda = (double) clients_config.getService_time_cloud()[s].get("service_time_cloud_lamda");
				_serviceCloudExpGenerator[s] = new Exponential(lamda);

			} else if (type.equals("Pareto")) {

				location = (double) clients_config.getService_time_cloud()[s].get("service_time_cloud_location");
				shape = (double) clients_config.getService_time_cloud()[s].get("service_time_cloud_shape");
				_serviceCloudParetoGenerator[s] = new Pareto(location, shape);
			} else if (type.equals("Random")) {
				_service_cloud_min[s] = (int) clients_config.getService_time_cloud()[s].get("service_time_cloud_min");
				_service_cloud_max[s] = (int) clients_config.getService_time_cloud()[s].get("service_time_cloud_max");

			}

		}
	}
	
	public double edgeServerResponseTime(int service_id) {

		double response_time= 0.000001;
		int min = 0;
		int max = 0;
		
		String type = (String) clients_config.getService_time_edge()[service_id].get("service_time_edge_type");

		switch (EGeneratorType.valueOf(type)) {
		case Exponential:
			response_time  = _serviceEdgeExpGenerator[service_id].random();
			break;

		case Pareto:
			response_time  = _serviceEdgeParetoGenerator[service_id].random();
			break;
		case Random:
			min = _service_edge_min[service_id] ;
			max = _service_edge_min[service_id] ;
			response_time  = (double)Utilities.randInt(min, max);
			break;
		default:
			break;
		}
		
		return response_time;

	}

	public double cloudServerResponseTime(int service_id) {

		double response_time= 0.000001;
		int min = 0;
		int max = 0;
		
		String type = (String) clients_config.getService_time_cloud()[service_id].get("service_time_cloud_type");

		switch (EGeneratorType.valueOf(type)) {
		case Exponential:
			response_time  = _serviceCloudExpGenerator[service_id].random();
			break;

		case Pareto:
			response_time  = _serviceCloudParetoGenerator[service_id].random();
			break;
		case Random:
			min = _service_cloud_min[service_id] ;
			max = _service_cloud_min[service_id] ;
			response_time  = (double)Utilities.randInt(min, max);
			break;
		default:
			break;
		}
		
		return response_time;

	}

}
