/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.List;

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

		if(args.length>0){

			algorithm=args[0].toString();
			simulationID=Integer.valueOf(args[1].toString());
			runID=Integer.valueOf(args[2].toString());

			simulationID=1;
			runID=1;

			simulator=new Simulator(algorithm,simulationID,runID);
			simulator.StartExperiment();

		}else{
			simulationID=1;
			runID=1;
			algorithm= EAlgorithms.Lyapunov.toString();   
			simulator=new Simulator(algorithm,simulationID,runID);
			simulator.StartExperiment();
		}

	}


}
