/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Controller.Configuration;
import Controller.Provider;
import Enumerators.EGeneratorType;
import java.util.ArrayList;
import java.util.List;

import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

/**
 *
 * @author kostas
 */
public class SimWebRequestUtilities {
    
    // How to create service Time per provider, per Service (Local)
    Exponential[][] _localServiceTimeExponentialGenerator;
    Pareto[][] _localServiceTimeParetoGenerator;
    // How to create service Time per provider, per Service (Cloud)
    Exponential[][] _cloudServiceTimeExponentialGenerator;
    Pareto[][] _cloudServiceTimeParetoGenerator;
    
    Provider[] _provider;
    Configuration _config;

    public SimWebRequestUtilities(Provider[] _provider, Configuration _config) {
        this._provider = _provider;
        this._config = _config;
        
        initializeLocalServiceTimeGenerators();
        initializeCloudServiceTimeGenerators();
    }
    
    private void initializeLocalServiceTimeGenerators(){
    
     // Lifetime of VM one per provider
     _localServiceTimeExponentialGenerator=new Exponential[_config.getProvidersNumber()][_config.getServicesNumber()]; 
     _localServiceTimeParetoGenerator=new Pareto[_config.getProvidersNumber()][_config.getServicesNumber()]; 
    
       String serviceTimeType="";
       String parameter="";
       double lamda;
       double location;
       double shape;
       
       int servicesNumber=0;
       
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            servicesNumber=_provider[i].getRequestsForService().size();
            
            for (int j = 0; j < servicesNumber; j++) {
                parameter="provider"+i+"_service"+j+"_localServiceRateType";
                serviceTimeType=String.valueOf(_provider[i].getRequestsForService().get(j).getLocalServiceTimeConfig().get(parameter));
                
                if(serviceTimeType.equals(EGeneratorType.Exponential.toString())){
                    
                    parameter="provider"+i+"_service"+j+"_localServiceRate_lamda";
                    lamda=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getLocalServiceTimeConfig().get(parameter)));
                    _localServiceTimeExponentialGenerator[i][j]=new Exponential(lamda);
                   
                }
                else if(serviceTimeType.equals(EGeneratorType.Pareto.toString())){
                    
                    parameter= "provider"+i+"_service"+j+"_localServiceRate_location";
                    location=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getLocalServiceTimeConfig().get(parameter)));
                    parameter= "provider"+i+"_service"+j+"_localServiceRate_shape";
                    shape=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getLocalServiceTimeConfig().get(parameter)));
                
                    _localServiceTimeParetoGenerator[i][j]=new Pareto(location, shape); //Dummy object
                }
              
            
        }


        }
    }
    
    private void initializeCloudServiceTimeGenerators(){
    
     // Lifetime of VM one per provider
     _cloudServiceTimeExponentialGenerator=new Exponential[_config.getProvidersNumber()][_config.getServicesNumber()]; 
     _cloudServiceTimeParetoGenerator=new Pareto[_config.getProvidersNumber()][_config.getServicesNumber()]; 
    
       String serviceTimeType="";
       String parameter="";
       double lamda;
       double location;
       double shape;
       
       int servicesNumber=0;
       
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            servicesNumber=_provider[i].getRequestsForService().size();
            
            for (int j = 0; j < servicesNumber; j++) {
                parameter="provider"+i+"_service"+j+"_cloudServiceRateType";
                serviceTimeType=String.valueOf(_provider[i].getRequestsForService().get(j).getCloudServiceTimeConfig().get(parameter));
                
                if(serviceTimeType.equals(EGeneratorType.Exponential.toString())){
                    
                    parameter="provider"+i+"_service"+j+"_cloudServiceRate_lamda";
                    lamda=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getCloudServiceTimeConfig().get(parameter)));
                    _cloudServiceTimeExponentialGenerator[i][j]=new Exponential(lamda);
                  
                }
                else if(serviceTimeType.equals(EGeneratorType.Pareto.toString())){
                  
                    
                    parameter= "provider"+i+"_service"+j+"_localServiceRate_location";
                    location=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getCloudServiceTimeConfig().get(parameter)));
                    parameter= "provider"+i+"_service"+j+"_localServiceRate_shape";
                    shape=Double.valueOf( String.valueOf(_provider[i].getRequestsForService().get(j).getCloudServiceTimeConfig().get(parameter)));
                
                    _cloudServiceTimeParetoGenerator[i][j]=new Pareto(location, shape); //Dummy object
                }
             
            
        }


        }
    }
    
    public double calculateLocalServiceTime(int providerID,int serviceID) {
     
       double serviceTime=-1;
       double min=0;
       double max=0;
       Double value;
       String parameter="provider"+providerID+"_service"+serviceID+"_localServiceRateType";
       
       String generatorType= String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLocalServiceTimeConfig().get(parameter));
        
        switch (EGeneratorType.valueOf(generatorType)){
            case Exponential:
                value=_localServiceTimeExponentialGenerator[providerID][serviceID].random();
                serviceTime = value;
                break;
            
            case Pareto:
                value=_localServiceTimeParetoGenerator[providerID][serviceID].random();
                serviceTime=value;
                                
                break;
            
            
            case Random: 
                
                parameter="provider"+providerID+"_service"+serviceID+"_localServiceRate_min";
                min=Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLocalServiceTimeConfig().get(parameter))); 
                
                parameter="provider"+providerID+"_service"+serviceID+"_localServiceRate_max";
                max=Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLocalServiceTimeConfig().get(parameter))); 
                
               serviceTime = Utilities.randInt((int)min, (int)max);
                break;
           
           
            default:            
                break;
        }
       
            return serviceTime;
        
    }
    
     public double calculateCloudServiceTime(int providerID,int serviceID) {
     
       double serviceTime=-1;
       double min=0;
       double max=0;
       Double value;
       String parameter="provider"+providerID+"_service"+serviceID+"_cloudServiceRateType";
       
       String generatorType= String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getCloudServiceTimeConfig().get(parameter));
        
        switch (EGeneratorType.valueOf(generatorType)){
            case Exponential:
                value=_cloudServiceTimeExponentialGenerator[providerID][serviceID].random();
                serviceTime = value;
                break;
            
            case Pareto:
                value=_cloudServiceTimeParetoGenerator[providerID][serviceID].random();
                serviceTime=value;
                                
                break;
            
            
            case Random: 
                
                parameter="provider"+providerID+"_service"+serviceID+"_cloudServiceRate_min";
                min=Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getCloudServiceTimeConfig().get(parameter))); 
                
                parameter="provider"+providerID+"_service"+serviceID+"_cloudServiceRate_max";
                max=Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getCloudServiceTimeConfig().get(parameter))); 
                
               serviceTime = Utilities.randInt((int)min, (int)max);
                break;
           
           
            default:            
                break;
        }
       
            return serviceTime;
        
    }
    
    
}
