package LocalClients;
import Controller.Configuration;
import Enumerators.EGeneratorType;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

public class Client {


	Exponential _arrivalExpGenerator;
	Pareto __arrivalParetoGenerator;

	int _arrivals_min;
	int _arrivals_max;
	Configuration config;
	ClientsConfiguration clients_config;
	int client_id;
	int provider_id;
	int service_id;

	public Client(int provider_id,int service_id,int client_id, Configuration config,ClientsConfiguration clients_config) {
		this.client_id = client_id;
		this.provider_id=provider_id;
		this.service_id=service_id;
		this.config = config;
		this.clients_config=clients_config;

		initializeArrivalsGenerators();
	}


	private void initializeArrivalsGenerators() {


		String arrivalsType = "";
		double lamda;
		double location;
		double shape;

		arrivalsType = (String) clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_type");

		if (arrivalsType.equals(EGeneratorType.Poisson.toString())) {

			lamda = (double) clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_lamda");
			_arrivalExpGenerator = new Exponential(lamda);

		} else if (arrivalsType.equals(EGeneratorType.Pareto.toString())) {

			location = Double.valueOf((String) clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_location"));
			shape = Double.valueOf((String) clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_shape"));
			__arrivalParetoGenerator = new Pareto(location, shape);
			
		} else if (arrivalsType.equals(EGeneratorType.Random.toString())) {
			_arrivals_min = (int)clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_min");
			_arrivals_max = (int)clients_config.getArrivals()[provider_id][service_id][client_id].get("arrivals_max");
		}

	}


	public Exponential get_arrivalExpGenerator() {
		return _arrivalExpGenerator;
	}


	public Pareto get__arrivalParetoGenerator() {
		return __arrivalParetoGenerator;
	}


	public int get_arrivals_min() {
		return _arrivals_min;
	}


	public int get_arrivals_max() {
		return _arrivals_max;
	}



}