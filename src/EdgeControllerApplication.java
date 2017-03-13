/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.List;

import Clients.ClientsSimulator;
import Controller.Simulator;
import Enumerators.EAlgorithms;

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

		simulator=new Simulator();

		if(args.length>0){

			algorithm=args[0].toString();
			simulationID=Integer.valueOf(args[1].toString());
			runID=Integer.valueOf(args[2].toString());
		
			simulator.get_config().setSimulationID(simulationID);
			simulator.get_config().setAlgorithm(algorithm);
			simulator.get_config().setRunID(runID);
		}			
		
		simulator.StartExperiment();
	
		if(simulator.get_config().getSimulation_mode()){
			ClientsSimulator clients_simulator=new ClientsSimulator(simulator.get_config(),simulator.get_controller());
		
			System.out.println("Initialization: Web Clients Object Ready");
			clients_simulator.start();
		}
		
	}


}
