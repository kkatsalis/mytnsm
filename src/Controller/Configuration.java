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
    String algorithm;
    int slots;
    int slotDuration;
    String slotDurationMetric;

    //======= Statistics ==========

    int numberOfMachineStatsPerSlot;
    
    //========= Remote Cloud Machines==========
    int remote_machines_number;
    @SuppressWarnings("rawtypes")
	Hashtable[]  remote_machine_config; // host name, ip 
    
    
    
    //========= Local Cloud Hosts ==========
    // Used for real deployment
    int hosts_number;
    @SuppressWarnings("rawtypes")
	Hashtable[]  host_machine_config; // host name, ip 
    
    // Used for simulations
    int host_cpu;
    int host_memory;
    int host_storage;
    int host_bandwidth;
    
    //========= Local Cloud VMs ==========
    
    int vm_types_number;
    String[] vm_type_name; 
    int[] vm_cpu; //One per VM Type 0:small, 1:medium, 2:large
    int[] vm_memory; //One per VM Type
    int[] vm_storage; //One per VM Type
    int[] vm_bandwidth; //One per VM Type


    //========= Local Cloud VMs ==========
    
    int providers_number;
    int services_number; // Each Service Corresponds to one VNF
    
    
    //========= CPLEX ==========	

    double omega;
    double priceBase;
    double[] phiWeight;

    double[][] penalty; //[p][s] provider p, service s
    double[][] xi; //[v][s] vm v, service s
    
    

    public Configuration() {

        this.loadSimulationProperties();
        this.loadEnvironmentProperties();
    
    }

    private void loadEnvironmentProperties() {
    	 
          Properties property = new Properties();
          InputStream input = null;
          String filename = "env.properties";
          String parameter = "";
          input = Configuration.class.getClassLoader().getResourceAsStream(filename);

          try {
              property.load(input); // load a properties file
              
              providers_number=Integer.valueOf(property.getProperty("providers_number"));
              services_number=Integer.valueOf(property.getProperty("services_number"));
              
              //------- CPLEX Configuration
              
              omega = Double.valueOf(property.getProperty("omega"));
              priceBase = Double.valueOf(property.getProperty("priceBase"));
              phiWeight = new double[providers_number];
              penalty=new double[providers_number][services_number];
              xi=new double[vm_types_number][services_number];
              
              
              for (int p = 0; p < providers_number; p++) {
                  parameter = "phiWeight_" + p;
                  phiWeight[p] = Double.valueOf((String) property.getProperty(parameter));
              }
              
              for (int p = 0; p < providers_number; p++) {
                  for (int s = 0; s < services_number; s++) {
                      parameter = "penalty_p" + p + "_s" + s;
                      penalty[p][s] = Double.valueOf((String) property.getProperty(parameter));
                  }

              }
              
              for (int v = 0; v < vm_types_number; v++) {
                  for (int s = 0; s < services_number; s++) {
                      parameter = "xi_v" + v + "_s" + s;
                      xi[v][s] = Double.valueOf((String) property.getProperty(parameter));
                  }

              }
              
          } catch (IOException ex) {
              Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
          }
    	
    	
	}

   
    @SuppressWarnings("unchecked")
	private void loadSimulationProperties() {

        Properties property = new Properties();
        String parameter = "";
        String svalue="";
        
        try {
            String filename = "simulation.properties";
            InputStream input = Configuration.class.getClassLoader().getResourceAsStream(filename);

            // load a properties file
            property.load(input);

            simulationID = Integer.valueOf(property.getProperty("simulationID"));
            runID = Integer.valueOf(property.getProperty("runID"));
            algorithm = String.valueOf(property.getProperty("algorithm"));
            
            
            slots = Integer.valueOf(property.getProperty("slots"));
            slotDuration = Integer.valueOf(property.getProperty("slotDuration"));
            slotDurationMetric = String.valueOf(property.getProperty("slotDurationMetric"));
            numberOfMachineStatsPerSlot = Integer.valueOf(property.getProperty("numberOfMachineStatsPerSlot"));
          
          
            
            //	Remote Cloud Machines
            remote_machines_number= Integer.valueOf(property.getProperty("remote_machines_number"));
            remote_machine_config=new Hashtable[remote_machines_number];
            
            for (int i = 0; i < remote_machines_number; i++) {
            	
            	parameter = "remote_machine_ip_"+String.valueOf(i);
            	svalue=String.valueOf((String) property.getProperty(parameter));
            	remote_machine_config[i].put("remote_machine_ip", svalue);
			}
            
            //	Local Cloud Hosts
            hosts_number= Integer.valueOf(property.getProperty("hosts_number"));
            host_machine_config=new Hashtable[hosts_number];
            for (int i = 0; i < hosts_number; i++) {
            	parameter = "host_machine_ip_"+String.valueOf(i);
            	svalue=String.valueOf((String) property.getProperty(parameter));
            	host_machine_config[i].put("host_machine_ip", svalue);
			}
            
            host_cpu= Integer.valueOf(property.getProperty("host_cpu"));
            host_memory= Integer.valueOf(property.getProperty("host_memory"));
            host_storage= Integer.valueOf(property.getProperty("host_storage"));
            host_bandwidth= Integer.valueOf(property.getProperty("host_bandwidth"));
            
            //	Local Cloud VMs
            vm_types_number = Integer.valueOf(property.getProperty("vm_types_number"));

            this.vm_type_name=new String[vm_types_number];
            
            // VM_Types
            for (int i = 0; i < vm_types_number; i++) {
                parameter = "vm_name_type_" + i;
                svalue = String.valueOf((String) property.getProperty(parameter));
                vm_type_name[i]=svalue;
            }
            
            vm_cpu[0] = Integer.valueOf((String) property.getProperty("small_vm_cpu"));
            vm_cpu[1] = Integer.valueOf((String) property.getProperty("medium_vm_cpu"));
            vm_cpu[2] = Integer.valueOf((String) property.getProperty("large_vm_cpu"));

            vm_memory[0] = Integer.valueOf((String) property.getProperty("small_vm_memory"));
            vm_memory[1] = Integer.valueOf((String) property.getProperty("medium_vm_memory"));
            vm_memory[2] = Integer.valueOf((String) property.getProperty("large_vm_memory"));

            vm_storage[0] = Integer.valueOf((String) property.getProperty("small_vm_storage"));
            vm_storage[1] = Integer.valueOf((String) property.getProperty("medium_vm_storage"));
            vm_storage[2] = Integer.valueOf((String) property.getProperty("large_vm_storage"));

            vm_bandwidth[0] = Integer.valueOf((String) property.getProperty("small_vm_bandwidth"));
            vm_bandwidth[1] = Integer.valueOf((String) property.getProperty("medium_vm_bandwidth"));
            vm_bandwidth[2] = Integer.valueOf((String) property.getProperty("large_vm_bandwidth"));

           
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

  


   




    
    
    
    
}
