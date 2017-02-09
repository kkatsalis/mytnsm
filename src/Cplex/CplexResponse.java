/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cplex;


public class CplexResponse {
    
    int[][][][] activationMatrix;
    double netBenefit;
    double utility;
	double cost;

    public CplexResponse(int[][][][] activationMatrix, double netBenefit, double utility, double cost) {
        this.activationMatrix = activationMatrix;
        this.netBenefit = netBenefit;
        this.utility = utility;
        this.cost = cost;
    }

    public int[][][][] getActivationMatrix() {
        return activationMatrix;
    }

    public double getNetBenefit() {
        return netBenefit;
    }
    
     public double getUtility() {
		return utility;
	}

	public double getCost() {
		return cost;
    }
}

