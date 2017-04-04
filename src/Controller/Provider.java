/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;
import java.util.List;

import Enumerators.EGeneratorType;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

/**
 *
 * @author kostas
 */
public class Provider {

	int providerID;
	Configuration _config;

	// How to create requests per VNF, one per provider [] []
	Exponential[] _arrivalExpGenerator;
	Pareto[] __arrivalParetoGenerator;

	// Lifetime of VNF one per provider
	Exponential[] _lifetimeExponentialGenerator;
	Pareto[] _lifetimeParetoGenerator;

	int _arrivals_min[];
	int _arrivals_max[];
	int _lifetime_min[];
	int _lifetime_max[];

	List<ServiceRequest> requestsForService;

	public Provider(int providerID, Configuration config) {
		this.providerID = providerID;
		this._config = config;
		requestsForService = new ArrayList<ServiceRequest>();

		initializeArrivalsGenerators();
		initializeLifeTimeGenerators();
	}

	public List<ServiceRequest> getRequestsForService() {
		return requestsForService;
	}

	private void initializeLifeTimeGenerators() {

		int services_number = _config.getServices_number();
		// Lifetime of VM one per provider
		_lifetimeExponentialGenerator = new Exponential[services_number];
		_lifetimeParetoGenerator = new Pareto[services_number];
		_lifetime_min=new int [services_number];
		_lifetime_max=new int [services_number];
		
		String lifetime_type = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			lifetime_type = (String) _config.getLifetime_generator()[providerID][s].get("lifetime_type");	

			if (lifetime_type.equals(EGeneratorType.Exponential.toString())) {

				lamda = Double.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_lamda"));
				_lifetimeExponentialGenerator[s] = new Exponential(lamda);

			} else if (lifetime_type.equals(EGeneratorType.Pareto.toString())) {

				location = Double
						.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_location"));
				shape = Double.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_shape"));
				_lifetimeParetoGenerator[s] = new Pareto(location, shape);

			} else if (lifetime_type.equals(EGeneratorType.Random.toString())) {
				_lifetime_min[s] = (int)_config.getLifetime_generator()[providerID][s].get("lifetime_min");
				_lifetime_max[s] = (int)_config.getLifetime_generator()[providerID][s].get("lifetime_max");
			}
		}

	}

	private void initializeArrivalsGenerators() {

		int services_number = _config.getServices_number();
		// How to create requests per Service, one per provider
		_arrivalExpGenerator = new Exponential[services_number];
		__arrivalParetoGenerator = new Pareto[services_number];
		_arrivals_min=new int[services_number];
		_arrivals_max=new int[services_number];

		String arrivalsType = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			arrivalsType = (String) _config.getArrivals_generator()[providerID][s].get("arrivals_type");

			if (arrivalsType.equals(EGeneratorType.Poisson.toString())) {

				lamda = (double) _config.getArrivals_generator()[providerID][s].get("arrivals_lamda");
				_arrivalExpGenerator[s] = new Exponential(lamda);

			} else if (arrivalsType.equals(EGeneratorType.Pareto.toString())) {

				location = Double.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_location"));
				shape = Double.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_shape"));
				__arrivalParetoGenerator[s] = new Pareto(location, shape);
			} else if (arrivalsType.equals(EGeneratorType.Random.toString())) {
				_arrivals_min[s] = (int)_config.getArrivals_generator()[providerID][s].get("arrivals_min");
				_arrivals_max[s] = (int)_config.getArrivals_generator()[providerID][s].get("arrivals_max");
			}
	}

}

public Exponential[] get_arrivalsExpGenerator() {
	return _arrivalExpGenerator;
}

public Pareto[] get__arrivalsParetoGenerator() {
	return __arrivalParetoGenerator;
}

public Exponential[] get_lifetimeExponentialGenerator() {
	return _lifetimeExponentialGenerator;
}

public Pareto[] get_lifetimeParetoGenerator() {
	return _lifetimeParetoGenerator;
}

public int[] getArrivals_min() {
	return _arrivals_min;
}

public int[] getArrivals_max() {
	return _arrivals_max;
}

public int[] getLifetime_min() {
	return _lifetime_min;
}

public int[] getLifetime_max() {
	return _lifetime_max;
}

public void setRequestsForService(List<ServiceRequest> requestsForService) {
	this.requestsForService = requestsForService;
}



}
