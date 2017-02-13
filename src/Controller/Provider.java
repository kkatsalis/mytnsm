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
	int vnfNumber;
	Configuration _config;
	// How to create requests per VNF, one per provider [] []
	Exponential[] _rateExponentialGenerator;
	Pareto[] _rateParetoGenerator;

	// Lifetime of VNF one per provider
	Exponential[] _lifetimeExponentialGenerator;
	Pareto[] _lifetimeParetoGenerator;

	List<ServiceRequestRates> requestsForService;

	public Provider(int providerID, Configuration config) {
		this.providerID = providerID;
		this._config = config;
		this.vnfNumber = config.getVnfNumber(this.providerID);
		requestsForService = new ArrayList<ServiceRequestRates>();

		initializeVnfArrivalRateGenerators();
		initializeVnfLifeTimeGenerators();
	}

	public List<ServiceRequestRates> getRequestsForService() {
		return requestsForService;
	}

	private void initializeVnfLifeTimeGenerators() {

		// Lifetime of VM one per provider
		_lifetimeExponentialGenerator = new Exponential[_config.getServicesNumber()];
		_lifetimeParetoGenerator = new Pareto[_config.getServicesNumber()];

		String lifetimeType = "";
		String parameter = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < vnfNumber; s++) {
			parameter = "provider" + providerID + "_service" + s + "_lifetimeType";
			lifetimeType = String.valueOf(requestsForService.get(s).getLifeTimeConfig().get(parameter));

			if (lifetimeType.equals(EGeneratorType.Exponential.toString())) {

				parameter = "_service" + s + "_lifetime_lamda";
				lamda = Double.valueOf(String.valueOf(requestsForService.get(s).getLifeTimeConfig().get(parameter)));
				_lifetimeExponentialGenerator[s] = new Exponential(lamda);

			} else if (lifetimeType.equals(EGeneratorType.Pareto.toString())) {

				parameter = "provider" + providerID + "_service" + s + "_lifetime_location";
				location = Double.valueOf(String.valueOf(requestsForService.get(s).getLifeTimeConfig().get(parameter)));
				parameter = "provider" + providerID + "_service" + s + "_lifetime_shape";
				shape = Double.valueOf(String.valueOf(requestsForService.get(s).getLifeTimeConfig().get(parameter)));

				_lifetimeParetoGenerator[s] = new Pareto(location, shape); // Dummy
																			// object
			}

		}

	}

	private void initializeVnfArrivalRateGenerators() {

		// How to create requests per Service, one per provider
		_rateExponentialGenerator = new Exponential[_config.getServicesNumber()];
		_rateParetoGenerator = new Pareto[_config.getServicesNumber()];

		String rateType = "";
		String parameter = "";
		double lamda;
		double location;
		double shape;

		for (int s = 0; s < vnfNumber; s++) {
			parameter = "provider" + providerID + "_service" + s + "_RateType";
			rateType = String.valueOf(requestsForService.get(s).getRequestRateConfig().get(parameter));

			if (rateType.equals(EGeneratorType.Exponential.toString())) {

				parameter = "provider" + providerID + "_service" + s + "_rate_lamda";
				lamda = Double.valueOf(String.valueOf(requestsForService.get(s).getRequestRateConfig().get(parameter)));
				_rateExponentialGenerator[s] = new Exponential(lamda);

			} else if (rateType.equals(EGeneratorType.Pareto.toString())) {

				parameter = "provider" + providerID + "_service" + s + "_rate_location";
				location = Double
						.valueOf(String.valueOf(requestsForService.get(s).getRequestRateConfig().get(parameter)));
				parameter = "provider" + providerID + "_service" + s + "_rate_shape";
				shape = Double.valueOf(String.valueOf(requestsForService.get(s).getRequestRateConfig().get(parameter)));

				_rateParetoGenerator[s] = new Pareto(location, shape); // Dummy
																		// object
			}

		}

	}

}
