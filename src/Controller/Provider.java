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

	int arrivals_min[];
	int arrivals_max[];
	int lifetime_min[];
	int lifetime_max[];

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

		String lifetimeType = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			lifetimeType = (String) _config.getLifetime_generator()[providerID][s].get("lifetime_type");

			if (lifetimeType.equals(EGeneratorType.Exponential.toString())) {

				lamda = Double.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_lamda"));
				_lifetimeExponentialGenerator[s] = new Exponential(lamda);

			} else if (lifetimeType.equals(EGeneratorType.Pareto.toString())) {

				location = Double
						.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_location"));
				shape = Double.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_shape"));
				_lifetimeParetoGenerator[s] = new Pareto(location, shape);

			}
			lifetime_min[s] = Integer.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_min"));
			lifetime_max[s] = Integer.valueOf((String) _config.getLifetime_generator()[providerID][s].get("lifetime_max"));

		}

	}

	private void initializeArrivalsGenerators() {

		int services_number = _config.getServices_number();
		// How to create requests per Service, one per provider
		_arrivalExpGenerator = new Exponential[services_number];
		__arrivalParetoGenerator = new Pareto[services_number];

		String arrivalsType = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < services_number; s++) {

			arrivalsType = (String) _config.getArrivals_generator()[providerID][s].get("arrivals_type");

			if (arrivalsType.equals(EGeneratorType.Exponential.toString())) {

				lamda = Double.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_lamda"));
				_lifetimeExponentialGenerator[s] = new Exponential(lamda);

			} else if (arrivalsType.equals(EGeneratorType.Pareto.toString())) {

				location = Double
						.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_location"));
				shape = Double.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_shape"));
				_lifetimeParetoGenerator[s] = new Pareto(location, shape);
			}
			arrivals_min[s] = Integer.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_min"));
			arrivals_max[s] = Integer.valueOf((String) _config.getArrivals_generator()[providerID][s].get("arrivals_max"));

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
		return arrivals_min;
	}

	public int[] getArrivals_max() {
		return arrivals_max;
	}

	public int[] getLifetime_min() {
		return lifetime_min;
	}

	public int[] getLifetime_max() {
		return lifetime_max;
	}

	public void setRequestsForService(List<ServiceRequest> requestsForService) {
		this.requestsForService = requestsForService;
	}
	
	

}
