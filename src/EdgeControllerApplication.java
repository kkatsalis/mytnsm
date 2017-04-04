/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Controller.Configuration;
import Controller.Simulator;
import LocalClients.ClientsSimulator;

/**
 *
 * @author kostas
 */
public class EdgeControllerApplication {

	
	public static void main(String[] args) {

		Simulator simulator;
		int simulationID;
		int runID;
		String algorithm;

		Configuration config= new Configuration();

		if(args.length>0){

			algorithm=args[0].toString();
			simulationID=Integer.valueOf(args[1].toString());
			runID=Integer.valueOf(args[2].toString());
		
			config.setSimulationID(simulationID);
			config.setAlgorithm(algorithm);
			config.setRunID(runID);
		}			
		
		simulator=new Simulator(config);
		simulator.StartExperiment();
	
		if(simulator.get_config().getSimulation_mode()){
			ClientsSimulator clients_simulator=new ClientsSimulator(simulator.get_config(),simulator.get_controller());
		
			System.out.println("Initialization: Web Clients Object Ready");
			clients_simulator.start();
		}
		
	}


}
