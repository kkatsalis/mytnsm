/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cplex;

import Utilities.Utilities;
import ilog.concert.*;
import ilog.cplex.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import Controller.Configuration;

@SuppressWarnings("unused")
public class Scheduler {

    Configuration config;

    public Scheduler(Configuration config) {

        this.config = config;

    }

    static void buildModelByRow(IloCplex model,
            SchedulerData data,
            IloNumVar[][][][] a,
            IloNumVarType type) throws IloException {

        for (int i = 0; i < data.N; i++) {
            for (int j = 0; j < data.P; j++) {
                for (int v = 0; v < data.V; v++) {
                    for (int s = 0; s < data.S; s++) {
                        a[i][j][v][s] = model.numVar(0, data.A[j][v][s], type);
                    }
                }
            }
        }

        // build y[i]s
        IloNumExpr y_sum = model.numExpr();
        IloNumExpr[][] y = new IloNumExpr[data.N][data.R];
        double[][] Q = new double[data.N][data.R];

        for (int i = 0; i < data.N; i++) {
            IloNumExpr ksum = model.numExpr();
            for (int k = 0; k < data.R; k++) {
                IloNumExpr ssum = model.numExpr();

                for (int s = 0; s < data.S; s++) {
                    IloNumExpr jsum = model.numExpr();
                    for (int j = 0; j < data.P; j++) {
                        IloNumExpr vsum = model.numExpr();
                        for (int v = 0; v < data.V; v++) {
                            IloNumExpr expr = model.numExpr();
                            expr = model.sum(expr, a[i][j][v][s]);
                            expr = model.sum(expr, data.n[i][j][v][s]);
                            expr = model.diff(expr, data.D[i][j][v][s]);
                            expr = model.prod(expr, data.m[v][k]);
                            vsum = model.sum(vsum, expr);
                        }
                        jsum = model.sum(jsum, vsum);
                    }
                    ssum = model.sum(ssum, jsum);
                }
                y[i][k] = model.diff(ssum, data.p[i][k]);
                Q[i][k] = Math.max(data.PREV_Y[i][k] + data.PREV_Q[i][k], 0);

                ksum = model.sum(ksum, model.prod(y[i][k], Q[i][k]));
            }
            y_sum = model.sum(y_sum, ksum);
        }

        // build pr penalty expression
        IloNumExpr pr_expr = model.numExpr();
        for (int j = 0; j < data.P; j++) {
            IloNumExpr ssum = model.numExpr();
            for (int s = 0; s < data.S; s++) {
                IloNumExpr vsum = model.numExpr();
                for (int v = 0; v < data.V; v++) {
                    IloNumExpr expr = model.numExpr();
                    for (int i = 0; i < data.N; i++) {
                        expr = model.sum(expr, a[i][j][v][s]);
                        expr = model.sum(expr, data.n[i][j][v][s] - data.D[i][j][v][s]);
                    }
                    expr = model.prod(expr, data.ksi(s, j, v));
                    vsum = model.sum(vsum, expr);
                }
                vsum = model.diff(data.r[j][s], vsum);
                vsum = model.prod(vsum, data.pen[j][s]);
                ssum = model.sum(ssum, vsum);
            }
            pr_expr = model.sum(pr_expr, ssum);
        }

        // build log approximation points
        double range = 100;  // Maximum total number of VMs at the mobile cloud for 1 provider ! 
        int precision = (int) (2 * range); // i.e., mulitplicity of break points, rrange/2

        double[] xpoints = new double[precision - 1];
        double[] ypoints = new double[precision - 1];

        for (int i = 0; i < xpoints.length; i++) {
            xpoints[i] = (i + 1) * range / precision;
            ypoints[i] = Math.log(xpoints[i]);
        }

        // build fr fairness expression
        IloNumExpr fr_expr = model.numExpr();

        for (int j = 0; j < data.P; j++) {
            IloNumExpr expr = model.numExpr();

            expr = model.sum(expr, -data.phi[j]);

            IloNumExpr isum = model.numExpr();
            for (int i = 0; i < data.N; i++) {
                IloNumExpr vsum = model.numExpr();
                for (int v = 0; v < data.V; v++) {
                    IloNumExpr ssum = model.numExpr();
                    for (int s = 0; s < data.S; s++) {

                        ssum = model.sum(expr, a[i][j][v][s]);
                        ssum = model.sum(expr, data.n[i][j][v][s] - data.D[i][j][v][s]);
                    }
                    vsum = model.sum(vsum, ssum);
                }
                isum = model.sum(isum, vsum);
            }

            expr = model.prod(expr, model.piecewiseLinear(isum, 2.0, xpoints, ypoints, 0.01));
            fr_expr = model.sum(fr_expr, expr);
        }

        // build utility expression
        IloNumExpr utility = model.numExpr();
        for (int i = 0; i < data.N; i++) {
            IloNumExpr jsum = model.numExpr();
            for (int j = 0; j < data.P; j++) {

                IloNumExpr vsum = model.numExpr();
                for (int v = 0; v < data.V; v++) {
                    IloNumExpr ssum = model.numExpr();
                    IloNumExpr expr;
                    for (int s = 0; s < data.S; s++) {
                        expr = model.numExpr();
                        expr = model.sum(expr, -data.w[v]);

                        ssum = model.prod(expr, a[i][j][v][s]);
                    }
                    vsum = model.sum(vsum, ssum);
                }
                jsum = model.sum(jsum, vsum);
            }
            utility = model.sum(utility, jsum);
        }

        // constraint for sum of a[][][][] variables
        for (int j = 0; j < data.P; j++) {
            for (int v = 0; v < data.V; v++) {
                for (int s = 0; s < data.S; s++) {
                    IloNumExpr isum = model.numExpr();
                    for (int i = 0; i < data.N; i++) {
                        isum = model.sum(isum, a[i][j][v][s]);
                    }

                    model.addLe(isum, data.A[j][v][s]);
                }
            }
        }

        // constraint for y[i][k]
        for (int i = 0; i < data.N; i++) {
            for (int k = 0; k < data.R; k++) {
                model.addLe(y[i][k], 0);
            }
        }

        //start of debugging
        //fr_expr = model.numExpr();
        //y_sum = model.numExpr();
        //end of debugging
        // minimization problem
        IloNumExpr problem = model.numExpr();

        // PENALTY AND FAIRNESS TAKE EQUAL IMPORTANCE
        //problem = model.sum(problem,model.sum(model.prod(model.sum(pr_expr, fr_expr), data.Omega), y_sum));
        // UTILITY AND PENALTY MINIMIZATION GETS MORE IMPORTANCE OVER FAIRNESS 
        problem = model.sum(problem, model.sum(model.sum(model.prod(model.sum(pr_expr, utility), data.Omega), fr_expr), y_sum));
        model.addMinimize(problem);
    }

    public int[][][][] RunLyapunov(SchedulerData data) throws IOException {

        BufferedWriter ios = null;
        BufferedWriter nos = null;

        int[][][][] activationMatrix = new int[data.N][data.P][data.V][data.S];

        try {

            // Build model
            IloCplex cplex = new IloCplex();
            IloNumVar[][][][] a = new IloNumVar[data.N][data.P][data.V][data.S];

            IloNumVarType varType = IloNumVarType.Int;

            ios = new BufferedWriter(new FileWriter("a_values", true));
            nos = new BufferedWriter(new FileWriter("n_values", true));

            //System.out.println(cplex.toString());
            // Solve model
            buildModelByRow(cplex, data, a, varType);

            // Solve model
            if (cplex.solve()) {
                System.out.println();
                System.out.println("Solution status = " + cplex.getStatus());
                System.out.println();
                System.out.println(" cost = " + cplex.getObjValue());

                for (int i = 0; i < data.N; i++) {
                    for (int j = 0; j < data.P; j++) {
                        for (int v = 0; v < data.V; v++) {
                            for (int s = 0; s < data.S; s++) {
                                System.out.println(" a[" + i + "],[" + j + "][" + v + "][" + s + "] = " + cplex.getValue(a[i][j][v][s]));
                                activationMatrix[i][j][v][s] = (int) Math.round(cplex.getValue(a[i][j][v][s]));
                                ios.write(activationMatrix[i][j][v][s] + " ");
                                ios.flush();
                            }
                        }
                    }
                }
                ios.write("\n");
                ios.flush();

               // updateData(data, activationMatrix);
                System.out.println();

                for (int i = 0; i < data.N; i++) {
                    for (int j = 0; j < data.P; j++) {
                        for (int v = 0; v < data.V; v++) {
                            for (int s = 0; s < data.S; s++) {
                                nos.write(data.n[i][j][v][s] + " ");
                                nos.flush();
                            }
                        }
                    }
                }
                nos.write("\n");
                nos.flush();

            } else {
                System.out.println("Solution NOT FOUND");
                System.out.println("Solution status = " + cplex.getStatus());
            }
            
            cplex.end();

        } catch (IloException ex) {
            System.out.println("Concert Error: " + ex);
        }

        System.out.println("Method Call: Cplex Run Called");

        return activationMatrix;
    }

    // data.n // current allocation n[host][j][vmtype][s]
    // data.m // m[vmtype][res]: amount of each res res for each VM vmtype vmtype
    // data.p // p[host][res]: capacity of each res res at each AP host
    // data.A // A[j][vmtype][s]: # of new requests for VMs of vmtype vmtype for service s of provider j  
   
    public void updateData(SchedulerData data, int[][][][] a) {
        double[][] y = new double[data.N][data.R];
        double[][] Q = new double[data.N][data.R];


        for (int i = 0; i < data.N; i++) {
            for (int k = 0; k < data.R; k++) {
                double ssum = 0;

                for (int s = 0; s < data.S; s++) {
                    double jsum = 0;
                    for (int j = 0; j < data.P; j++) {
                        double vsum = 0;
                        for (int v = 0; v < data.V; v++) {
                            double expr = 0;
                            expr = (a[i][j][v][s] + data.n[i][j][v][s] - data.D[i][j][v][s]) * data.m[v][k];
                            vsum += expr;
                        }
                        jsum += vsum;
                    }
                    ssum += jsum;
                }
                y[i][k] = ssum - data.p[i][k];
                Q[i][k] = Math.max(data.PREV_Y[i][k] + data.PREV_Q[i][k], 0);

//                System.out.println("y[" + i + "][" + k + "]=" + y[i][k]);
//                System.out.println("Q[" + i + "][" + k + "]=" + Q[i][k]);
            }
        }
        data.PREV_Q = Q;
        data.PREV_Y = y;

        for (int i = 0; i < data.N; i++) {
            for (int j = 0; j < data.P; j++) {
                for (int v = 0; v < data.V; v++) {
                    for (int s = 0; s < data.S; s++) {
                        data.n[i][j][v][s] = Math.max(a[i][j][v][s] + data.n[i][j][v][s] - data.D[i][j][v][s], 0);
                    }
                }
            }
        }

        
    }


    private boolean checkIftheVMFits(SchedulerData data, int[][] reservedResources, int v, int n) {

        boolean fits;

        boolean[] fitsArray = new boolean[data.R];
        int resourceCost = 0;

        for (int r = 0; r < data.R; r++) {

            fitsArray[r] = false;
            resourceCost = (int) data.m[v][r];

            if (reservedResources[n][r] + resourceCost <= data.p[n][r]) {
                fitsArray[r] = true;
            }
        }

        fits = true;
        for (int r = 0; r < data.R; r++) {
            if (fitsArray[r] == false) {
                fits = false;
            }
        }

        return fits;

    }

}
