package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Enumerators.EGeneratorType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostas
 */
public class Configuration {

    Configuration _config = this;

    int simulationID;
    int runID;
    int numberOfSlots;

    int hostsNumber;
    int providersNumber;
    int servicesNumber; // Each Service Corresponds to one VNF


    String algorithm;
    int slotDuration;
    String slotDurationMetric;


    
    //========= ClOUD ==========
    Hashtable[]  host_machine_config; // host name, ip 
    
    double cpu_host;  		// for simulations only
    double memory_host; 	// for simulations only
    double storage_host; 	// for simulations only
    double bandwidth_host;	// for simulations only
    
    // VM specification
    int vmTypesNumber;
    String[] vm_type_name; 
    double[] vm_cpu; //One per VM Type 0:small, 1:medium, 2:large
    double[] vm_memory; //One per VM Type
    double[] vm_storage; //One per VM Type
    double[] vm_bandwidth; //One per VM Type

    // Host specification
    String remote_machine_ip; //when not local every service is deployed there
    

    
    
    //========= CPLEX ==========	
    double[] phiWeight;
    double priceBase;
    double[][] penalty; //[p][s] provider p, service s
    double[][] xi; //[v][s] vm v, service s
    
    //========= STATS ==========
    double omega;
    int numberOfMachineStatsPerSlot;
    String statsUpdateMethod;


    public Configuration() {

        // simulation parameters
        this.loadSimProperties();
        
    	this.addHostNodes();
        this.addMachinesConfig();

        this.loadResources();
    	
    	// Provider parameters
    	
    	
        // CPLEX parameters
        this.loadCplexParameters();
        this.loadXi();
        
        
        this.loadExternalCloudParameters();

    }

    @SuppressWarnings("unchecked")
	private void addHostNodes() {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";
        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        hostsNumber = Integer.valueOf(property.getProperty("hostsNumber"));
        host_machine_config=new Hashtable[hostsNumber];
        
        String parameter = "";
        String hostName = "";
        String host_ip="";
        
        // InterArrival Time
        for (int i = 0; i < hostsNumber; i++) {
        	host_machine_config[i].put("id", i);
        	parameter = "host_" + i;
            hostName = String.valueOf((String) property.getProperty(parameter));
            host_machine_config[i].put("name", hostName);
            parameter = "host_ip_" + i;
            host_ip = String.valueOf((String) property.getProperty(parameter));
            host_machine_config[i].put("ip", host_ip);
        }

    }


    private void addMachinesConfig() {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        vmTypesNumber = Integer.valueOf(property.getProperty("vmTypesNumber"));

        this.vm_type_name=new String[vmTypesNumber];
        String parameter = "";
        String vmTypeName = "";

        // VM_Types
        for (int i = 0; i < vmTypesNumber; i++) {
            parameter = "vm_type_" + i;
            vmTypeName = String.valueOf((String) property.getProperty(parameter));
            vm_type_name[i]=vmTypeName;
        }
        
        
        //HOST Resources
        parameter = "cpu_host";
        cpu_host = Double.valueOf((String) property.getProperty(parameter));
        parameter = "memory_host";
        memory_host = Double.valueOf((String) property.getProperty(parameter));
        parameter = "storage_host";
        storage_host = Double.valueOf((String) property.getProperty(parameter));
        parameter = "bandwidth_host";
        bandwidth_host = Double.valueOf((String) property.getProperty(parameter));

        // VM CPU
        parameter = "cpu_small_vm";
        vm_cpu[0] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "cpu_medium_vm";
        vm_cpu[1] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "cpu_large_vm";
        vm_cpu[2] = Double.valueOf((String) property.getProperty(parameter));

        // VM MEMORY
        parameter = "memory_small_vm";
        vm_memory[0] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "memory_medium_vm";
        vm_memory[1] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "memory_large_vm";
        vm_memory[2] = Double.valueOf((String) property.getProperty(parameter));

        // VM STORAGE 
        parameter = "storage_small_vm";
        vm_storage[0] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "storage_medium_vm";
        vm_storage[1] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "storage_large_vm";
        vm_storage[2] = Double.valueOf((String) property.getProperty(parameter));

        // VM BANDWIDTH
        parameter = "bandwidth_small_vm";
        vm_bandwidth[0] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "bandwidth_medium_vm";
        vm_bandwidth[1] = Double.valueOf((String) property.getProperty(parameter));
        parameter = "bandwidth_large_vm";
        vm_bandwidth[2] = Double.valueOf((String) property.getProperty(parameter));

    }

 
   
    private void loadSimProperties() {

        Properties property = new Properties();

        try {
            String filename = "simulation.properties";
            InputStream input = Configuration.class.getClassLoader().getResourceAsStream(filename);

            // load a properties file
            property.load(input);

            simulationID = Integer.valueOf(property.getProperty("simulationID"));
            runID = Integer.valueOf(property.getProperty("runID"));
            algorithm = String.valueOf(property.getProperty("algorithm"));
            
            
            providersNumber = Integer.valueOf(property.getProperty("providers"));
            numberOfSlots = Integer.valueOf(property.getProperty("slots"));
          
            
            slotDuration = Integer.valueOf(property.getProperty("slotDuration"));
            slotDurationMetric = String.valueOf(property.getProperty("slotDurationMetric"));
            numberOfMachineStatsPerSlot = Integer.valueOf(property.getProperty("numberOfMachineStatsPerSlot"));
           
            omega = Double.valueOf(property.getProperty("omega"));
            cloudVM_number = Integer.valueOf(property.getProperty("cloudVM_number"));
            servicesNumber = Integer.valueOf(property.getProperty("servicesNumber"));
            statsUpdateMethod = String.valueOf(property.getProperty("stats_updateMethod"));
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private void loadCplexParameters() {

        phiWeight = new double[providersNumber];

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";
        String parameter = "";
        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            property.load(input); // load a properties file
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        priceBase = Double.valueOf(property.getProperty("priceBase"));
        
        for (int i = 0; i < providersNumber; i++) {
            parameter = "phiWeight_" + i;
            phiWeight[i] = Double.valueOf((String) property.getProperty(parameter));
        }
        
        for (int i = 0; i < providersNumber; i++) {
            for (int j = 0; j < servicesNumber; j++) {
                parameter = "penalty_p" + i + "_s" + j;
                penalty[i][j] = Double.valueOf((String) property.getProperty(parameter));
            }

        }

    }

    public int getVnfNumber(int provideID){
    	 Properties property = new Properties();
         InputStream input = null;
         String filename = "provider.properties";
         String parameter = "";
         input = Configuration.class.getClassLoader().getResourceAsStream(filename);

         int vnfNumber=0;
         try {
             // load a properties file
             property.load(input);
             parameter = "provider" + provideID + "_servicesNumber";
             vnfNumber=Integer.valueOf((String) property.getProperty(parameter));
         } catch (IOException ex) {
             Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
         }
    
    	return vnfNumber;
    }
    
    
   



    private void loadExternalCloudParameters() {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        String parameter = "";
        String ip = "";

        // AB Service
        for (int i = 0; i < _config.cloudVM_number; i++) {
            parameter = "cloudVM_" + i;
            ip = String.valueOf((String) property.getProperty(parameter));
            cloudVM_IPs.add(ip);

        }

    }

    public int getProvidersNumber() {
        return providersNumber;
    }

    public int getSimulationID() {
        return simulationID;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public String getNitosServer() {
        return nitosServer;
    }

    public int getSlotDuration() {
        return slotDuration;
    }

    public String getSlotDurationMetric() {
        return slotDurationMetric;
    }

    public int getNumberOfMachineStatsPerSlot() {
        return numberOfMachineStatsPerSlot;
    }

    public int getHostsNumber() {
        return hostsNumber;
    }

    public void setHostsNumber(int hostsNumber) {
        this.hostsNumber = hostsNumber;
    }

    public int getClientsNumber() {
        return clientsNumber;
    }

    public void setClientsNumber(int clientsNumber) {
        this.clientsNumber = clientsNumber;
    }

    public int getServicesNumber() {
        return servicesNumber;
    }

    public int getVmTypesNumber() {
        return vmTypesNumber;
    }

    public List<String> getClientNames() {
        return clientNames;
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public List<String> getVmTypesNames() {
        return vmTypesNames;
    }

    public int getMachineResourcesNumber() {
        return machineResourcesNumber;
    }

    public double[] getPhiWeight() {
        return phiWeight;
    }

    public double[] getCpu_VM() {
        return vm_cpu;
    }

    public double[] getMemory_VM() {
        return vm_memory;
    }

    public double[] getStorage_VM() {
        return vm_storage;
    }

    public double[] getBandwidth_VM() {
        return vm_bandwidth;
    }

    public double getCpu_host() {
        return cpu_host;
    }

    public void setCpu_host(double cpu_host) {
        this.cpu_host = cpu_host;
    }

    public double getMemory_host() {
        return memory_host;
    }

    public void setMemory_host(double memory_host) {
        this.memory_host = memory_host;
    }

    public double getStorage_host() {
        return storage_host;
    }

    public void setStorage_host(double storage_host) {
        this.storage_host = storage_host;
    }

    public double getBandwidth_host() {
        return bandwidth_host;
    }

    public void setBandwidth_host(double bandwidth_host) {
        this.bandwidth_host = bandwidth_host;
    }

    public double[][] getPenalty() {
        return penalty;
    }

    public int getAbRequestsNumber() {
        return abRequestsNumber;
    }

    public int getAbBatchRequestsNumber() {
        return abBatchRequestsNumber;
    }

    public HashMap getAssociatedAPsPerClient() {
        return associatedAPsPerClient;
    }

    public double getOmega() {
        return omega;
    }

    public int getCloudVM_AB_number() {
        return cloudVM_number;
    }

    public List<String> getCloudVM_IPs() {
        return cloudVM_IPs;
    }

    public String getStatsUpdateMethod() {
        return statsUpdateMethod;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public double getPriceBase() {
        return priceBase;
    }

    private void loadXi() {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        xi = new double[providersNumber][vmTypesNumber][servicesNumber];

        try {
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        String parameter = "";

        for (int p = 0; p < providersNumber; p++) {
            for (int v = 0; v < vmTypesNumber; v++) {
                for (int s = 0; s < servicesNumber; s++) {
                    parameter = "xi_p" + p + "_v" + v + "_s" + s;

                    xi[p][v][s] = Double.valueOf((String) property.getProperty(parameter));
                }
            }

        }

    }

    public double[][][] getXi() {
        return xi;
    }

    public int getRunID() {
        return runID;
    }

    public int getSkipWebClientStats() {
        return skipWebClientStats;
    }

    
    
    
    
}
