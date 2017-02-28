/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package Cplex;

import java.util.Arrays;

import Controller.Configuration;

/**
 *
 * @author kostas
 */
public class SchedulerData {
    
    public int N; // # of APs
    public int P; // # of service providers
    public int S; // # of services
    public int V; // # of VM types
    public int[][] r; // requests per service provider and service
    public int R; // # of physical resources per physical machine
    public double[] phi; // fairness weight per provider
    public double[][] m; // m[vmtype][res]: amount of each res res for each VM vmtype vmtype
    public int[][] p; // p[host][res]: capacity of each res res at each AP host
    public double[][] pen; // pen[j][s] penalty for not satisfying locally a request for service s of provider j
    public int[][][] A; // A[j][vmtype][s]: # of new requests for VMs of vmtype vmtype for service s of provider j
    public int[][][][] D; // D[host][j][vmtype][s]: # of removed VMs of vmtype vmtype for service s of provider j from AP host
    public int[][][][] n; // n[host][j][vmtype][s]: # of allocated VMs of vmtype vmtype for service s of provider j at AP host
    
    public double[][] PREV_Q;
    public double[][] PREV_Y;
    public double Omega = 100;
    
    Configuration config;
    
    public double[] w; // rent for each virtual machine type
    
    
    public SchedulerData(Configuration config)
    {
        this.config=config;
        this.S = config.getServices_number();
        this.P = config.getProviders_number();
        this.V = config.getVm_types_number();
        this.N = config.getHosts_number();
        this.R = config.getResources_number();
        
         r = new int[P][S];
         w = new double[V];
        
        this.Omega=config.getOmega();
        
        initializeArrays();
    }
    
    public void updateParameters(int[][] r, int[][][] A, int[][][][] D){
        
        this.r = r;
        this.A = A;
        this.D = D;
        
        System.out.println("Method Call: Update Cplex Parameters Called");
        
    }
    
    public void initializeArrays(){
        
        double priceBase = config.getPriceBase();
        
        for (int i=0;i<V;i++){
            w[i] = (i+1)*priceBase;
           System.out.println(w[i]);
        
        }
        
        
        // 1 - Fairness Weight
        phi = new double[P]; // fairness weight per provider
        for (int j=0;j<P;j++)
            phi[j] = config.getPhiWeight()[j];
        
        // 2 - amount of each res res for each VM vmtype vmtype
        // R0:cpu, R1:memory, R3:storage,R4:bandwidth, v0:small,v1:medium,v2:large
        m = new double[V][R];
        for (int vmtype=0;vmtype<V;vmtype++)
            for (int res=0;res<R;res++){
                if(res==0)
                    m[vmtype][res] = config.getVm_cpu_cores()[vmtype];
                else if(res==1)
                    m[vmtype][res] = config.getVm_cpu_power()[vmtype];
                else if(res==2)
                    m[vmtype][res] = config.getVm_memory()[vmtype];
                else if(res==3)
                    m[vmtype][res] = config.getVm_storage()[vmtype];
                else if(res==4)
                    m[vmtype][res] = config.getVm_bandwidth()[vmtype];
                
            }
        // 3- p[host][res]: capacity of each res res at each host
        p = new int[N][R];
        
        for (int host=0;host<N;host++)
            for (int res=0;res<R;res++){
                
                if(res==0)
                    p[host][res] = Integer.valueOf((String) config.getHost_machine_config()[host].get("cpu_cores"));
                else if(res==1)
                    p[host][res] = Integer.valueOf((String) config.getHost_machine_config()[host].get("cpu_power"));
                else if(res==2)
                    p[host][res] = Integer.valueOf((String) config.getHost_machine_config()[host].get("memory"));
                else if(res==3)
                    p[host][res] = Integer.valueOf((String) config.getHost_machine_config()[host].get("storage"));
                else if(res==4)
                    p[host][res] = Integer.valueOf((String) config.getHost_machine_config()[host].get("bandwidth"));
                
            }
        
        // 4- pen[j][s] penalty for not satisfying locally a request for service s of provider j
        pen = new double[P][S];
        for (int j=0;j<P;j++)
            for (int s=0;s<S;s++)
                pen[j][s] = config.getPenalty()[j][s];
        
        // 5- Initialize Prev_Q and Prev_Y
        PREV_Q = new double[N][R];
        PREV_Y = new double[N][R];
        
        for (int i=0;i<N;i++)
            for (int k=0;k<R;k++)
            {
                PREV_Q[i][k] = 0;
                PREV_Y[i][k] = 0;
            }
        
        n=new int[N][P][V][S];
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < P; j++) {
                for (int v = 0; v < V; v++) {
                    for (int s = 0; s < S; s++) {
                        n[i][j][v][s]=0;
                    }
                    
                }
            }
        }
    }
    
    
    
    public double ksi(int s, int j, int v)
    {
        
        double xi=config.getXi()[v][s];
            
        return xi;
         
//        if (v == 0 && s == 0)
//            return 5000*(j+1);
//        else if (v == 0 && s == 1)
//            return 500*(j+1);
//        else if (v == 1 && s == 0)
//            return 10000*(j+1);
//        else if (v == 1 && s == 1)
//            return 1000*(j+1);
//        else if (v == 2 && s == 0)
//            return 20000*(j+1);
//        else if (v == 2 && s == 1)
//            return 5000*(j+1);
//        else
//            return 100*(v+1);
    }
    
    
    static void myBubbleSort(double [] array, int[] index)
    {
        boolean flag = false;
        
        while (!flag)
        {
            flag = true;
            for (int i=0;i<array.length-1;i++)
            {
                if (array[i] > array[i+1])
                {
                    //swap
                    double tmp = array[i];
                    array[i] = array[i+1];
                    array[i+1] = tmp;
                    
                    int tm = index[i];
                    index[i] = index[i+1];
                    index[i+1] = tm;
                    flag = false;
                }
            }
        }
    }
    
    public int[] f(int j,int s )
    {
        try {
            
            int W = -this.r[j][s];
            
            System.out.println("Requests: "+W);
            double v[] = new double[this.V];
            double w[] = new double[this.V];
            double weights[] = new double[this.V];
            int[] indexes = new int[this.V];
            int[] toReturn = new int[this.V];
            
            for (int i=0;i<this.w.length;i++)
            {
                v[i] = -this.w[i];
                w[i] = -ksi(s, j, i);
                weights[i] = v[i]/w[i];
                indexes[i] = i;
            }
            
            myBubbleSort(weights, indexes);
            
            
//		for (int i = 0; i < weights.length; i++) {
//			System.out.println("Weight: "+weights[i]+" for VM: "+indexes[i]);
//		}
            
            double sum = 0;
            int i = 0;
            for (; i < weights.length; i++) {
                while (true)
                {
                    if (sum+w[indexes[i]] >= W)
                    {
                        sum += w[indexes[i]];
                        toReturn[indexes[i]]++;
                    } else
                    {
                        break;
                    }
//				System.out.println("Sum: "+sum+" Currently increased index: "+indexes[i]);
                }
            }
            if (sum > W)
            {
                sum += w[indexes[i-1]];
                toReturn[indexes[i-1]]++;
            }
            return toReturn;
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        
        
        return null;
    }
    
    public void initializeWebRequestMatrix(int[][] _webRequestPattern) {
        
        for (int p = 0; p < P; p++) {
            for (int s = 0; s < S; s++) {
                r[p][s]=_webRequestPattern[p][s];
            }
            
        }
    }
    
}

