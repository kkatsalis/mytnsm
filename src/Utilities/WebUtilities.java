/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Statistics.ABStats;
import Statistics.NetRateStats;
import Statistics.VMStats;
import com.google.gson.JsonArray;

import Controller.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
/**
 *
 * @author kostas
 */
public class WebUtilities {
    
    Configuration _config;
    
    public WebUtilities(Configuration config){
    
        this._config=config;
            
    }
    
    public boolean createVM(Hashtable parameters) throws IOException{
    
       //http://nitlab3.inf.uth.gr:4100/vm-create/server-john/precise/small/192.168.100.10/255.255.254.0/192.168.100.1/node
        
      
         
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/vm-create/";
        boolean methodResponse=false;
        
        String vmName=String.valueOf(parameters.get("vmName"));
        String OS=String.valueOf(parameters.get("OS")); 
        String vmType=String.valueOf(parameters.get("vmType")); 
        String interIP=String.valueOf(parameters.get("interIP")); 
        String interMask=String.valueOf(parameters.get("interMask")); 
        String interDefaultGateway=String.valueOf(parameters.get("interDefaultGateway")); 
        String hostName=String.valueOf(parameters.get("hostName"));
                
        uri+=vmName+"/";
        uri+=OS+"/";
        uri+=vmType+"/";
        uri+=interIP+"/";
        uri+=interMask+"/";
        uri+=interDefaultGateway+"/";   
        uri+=hostName;
        

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        
        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            System.out.println("CreateVM called for:"+vmName);
            System.out.println("Response Status:"+response.getStatusLine().toString());

            if(response.getStatusLine().toString().contains("200"))
                methodResponse=true;
        }

       
        
        return methodResponse;
    }
    
    public boolean startVM(String vmName, String hostName) throws IOException{
    
        //http://nitlab3.inf.uth.gr:4100/vm-start/server-john
        
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/vm-start/";
        String methodResponse="";
        
        uri+=vmName;
        uri+="/"+hostName;
        
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);

        try {
              System.out.println("**** VM:"+vmName+" started");
            if(response.getStatusLine().toString().contains("200"))
                return true;

        } 
        finally {
            response.close();
        }
        
        return false;
    }
    
    public String deleteVM(String vmName,String hostName) throws IOException{
      
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/vm-destroy/";
        String methodResponse="";
        
        uri+=vmName;
        uri+="/"+hostName;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        
        CloseableHttpResponse response = httpclient.execute(httpget);

        try {
            System.out.println("****** VM:"+vmName+" deleted");
            System.out.println(response.getStatusLine().toString());

        } 
        finally {
            response.close();
        }
        
        return methodResponse;
    }
    
     public boolean checkVMListOnHost(String hostName, String vmName) throws IOException{
    
         
            String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/virsh_list_all/";
            uri+=hostName;
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(httpget);
            
            String json="";
            String output;
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            
            while ((output = br.readLine()) != null) {
                if(output.contains(vmName)&output.contains("shut off"))
                return true;
            }
            
            return false;
        
    }
    
   
    public List<VMStats> retrieveStatsVMsPerHost(String hostName, int slot,int instance) throws IOException{
    
        List<VMStats> vmStatsList=new ArrayList<>();
     
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4000/vm/";
        uri+=hostName;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);

        try {

            JSONObject body=new JSONObject(response);

            JSONArray vmStatsArray=body.getJSONArray("VMs");
            int vap_number=vmStatsArray.length();

            JSONObject vm ;

            for (int i = 0; i < vap_number; ++i) {
                vm = vmStatsArray.getJSONObject(i);

                vmStatsList.add(new VMStats());

                vmStatsList.get(i).setSlot(String.valueOf(slot));
                vmStatsList.get(i).setInstance(String.valueOf(instance));
                vmStatsList.get(i).setHostName(hostName);
                vmStatsList.get(i).setDomain_ID(vm.getString("Domain_ID"));
                vmStatsList.get(i).setDomain_name(vm.getString("Domain_name"));
                vmStatsList.get(i).setCPU_ns(vm.getString("CPU_ns"));
                vmStatsList.get(i).setCPU_percentage(vm.getString("CPU_percentage"));
                vmStatsList.get(i).setMem_bytes(vm.getString("Mem_bytes"));  
                vmStatsList.get(i).setMem_percentage(vm.getString("Mem_percentage")); 
                vmStatsList.get(i).setBlock_RDRQ(vm.getString("Block_RDRQ"));
                vmStatsList.get(i).setBlock_WRRQ(vm.getString("Block_WRRQ"));
                vmStatsList.get(i).setNet_RXBY(vm.getString("Net_RXBY"));
                vmStatsList.get(i).setNet_TXBY(vm.getString("Net_TXBY"));
                vmStatsList.get(i).getNetRates().setKbps_in(vm.getDouble("Kbps in"));
                vmStatsList.get(i).getNetRates().setKbps_out(vm.getDouble("Kbps out"));
                vmStatsList.get(i).getNetRates().setInterface(vm.getString("interface"));
                vmStatsList.get(i).getNetRates().setTimeStamp(vm.getString("timestamp"));
                
            }
        } 
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            response.close();
        }
    
        return vmStatsList;
    }
    
    public Hashtable retrieveStatsHost(String hostName, int slot,int instance) throws IOException, JSONException{
    
        List<NetRateStats> netRates=new ArrayList<>();
        Hashtable parameters=new Hashtable();
        
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/host/";
        uri+=hostName;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);
        
        
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
  
        try {

            String json="";
            String output;
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            
             while ((output = br.readLine()) != null) {
               json+=output;
            }
             
            JSONObject body=new JSONObject(json);
                                 
            JSONArray interfacesArray=body.getJSONArray("net rate");
           
            int interfacesNumber=interfacesArray.length();
             
            JSONObject interfaceStats ;
            
            for (int i = 0; i < interfacesNumber; ++i) {
                interfaceStats = interfacesArray.getJSONObject(i);
            
                netRates.add(new NetRateStats());
           
                netRates.get(i).setInterface(interfaceStats.getString("interface"));
                netRates.get(i).setTimeStamp(interfaceStats.getString("timestamp"));
                netRates.get(i).setKbps_in(interfaceStats.getDouble("Kbps in"));
                netRates.get(i).setKbps_out(interfaceStats.getDouble("Kbps out"));
            }
            
            
            parameters.put("Slot",slot);
            parameters.put("Measurement",instance);
            parameters.put("Hostname", hostName);
            parameters.put("Time",body.getString("Time") );
            parameters.put("Arch",body.getString("Arch") );
            parameters.put("Physical_CPUs",body.getString("Physical CPUs") );
            parameters.put("Count",body.getString("Count") );
            parameters.put("Running",body.getString("Running") );
            parameters.put("Blocked",body.getString("Blocked") );
            parameters.put("Paused",body.getString("Paused") );
            parameters.put("Shutdown",body.getString("Shutdown") );
            parameters.put("Shutoff",body.getString("Shutoff") );
            parameters.put("Crashed",body.getString("Crashed") );
            parameters.put("Active",body.getString("Active") );
            parameters.put("Inactive",body.getString("Inactive") );
            parameters.put("CPU_percentage",body.getString("%CPU") );
            parameters.put("Total_hardware_memory_KB",body.getString("Total hardware memory (KB)") );
            parameters.put("Total_memory_KB",body.getString("Total memory (KB)") );
            parameters.put("Total_guest_memory_KB",body.getString("Total guest memory (KB)") );
            parameters.put("netRates",netRates);
            
            return parameters;
        } 
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            response.close();
        }
        
       return null;
     
    }
    
    public ABStats retrieveStatsABPerClient(String clientName,String vmIP) throws IOException, JSONException{
    
        ABStats abStats=new ABStats();
        //http://nitlab3.inf.uth.gr:4100/ab/1000/10/node080/?url=<vm_ip>/
        
        String uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/ab/";
        uri+=_config.getAbRequestsNumber();
        uri+="/"+_config.getAbBatchRequestsNumber();
        uri+="/"+clientName;
        uri+="/?url=+"+vmIP+"+/";
        
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);
        
        
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());
  
        try {

            String json="";
            String output;
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            
             while ((output = br.readLine()) != null) {
               json+=output;
            }
             
            JSONObject body=new JSONObject(json);
             
            abStats.setClientName(clientName);
            abStats.setServer_Software(body.getString("Server Software"));
            abStats.setServer_Hostname( body.getString("Server Hostname"));
            abStats.setServer_Port(body.getString("Server Port"));
            abStats.setDocument_Path(body.getString("Document Path"));
            abStats.setDocument_Length_bytes(body.getString("Document Length-bytes"));
            abStats.setConcurrency_Level(body.getString("Concurrency Level"));
            abStats.setTime_taken_for_tests_seconds(body.getString("Time taken for tests-seconds"));
            abStats.setComplete_requests(body.getString("Complete requests"));
            abStats.setNon_2xx_responses(body.getString("Non-2xx responses"));
            abStats.setKeep_Alive_requests(body.getString("Keep-Alive requests"));
            abStats.setTotal_transferred_bytes(body.getString("Total transferred-bytes"));
            abStats.setHTML_transferred_bytes(body.getString("HTML transferred-bytes"));
            abStats.setRequests_per_second_mean(body.getString("Requests per second-[#/sec] (mean)"));
            abStats.setTime_per_request_mean(body.getString("Time per request-[ms] (mean)"));
            abStats.setTime_per_request_mean_across_all_concurrent_requests(body.getString("Time per request-[ms] (mean, across all concurrent requests)"));
            abStats.setTransfer_rate_received(body.getString("Transfer rate-[Kbytes/sec] received"));
   
            JSONObject Failed_requests_body = body.getJSONObject("Failed requests");
            
           abStats.setFailed_requests_number(Failed_requests_body.getString("number"));
           abStats.setFailed_requests_Connect(Failed_requests_body.getString("Connect"));
           abStats.setFailed_requests_Receive(Failed_requests_body.getString("Receive"));
           abStats.setFailed_requests_Length(Failed_requests_body.getString("Length"));
           abStats.setFailed_requests_Exceptions(Failed_requests_body.getString("Exceptions"));

            JSONObject Connection_Times_body = body.getJSONObject("Connection Times (ms)");

            JSONObject Connect_body = Connection_Times_body.getJSONObject("Connect");
            
            abStats.setConnection_Times_Connect_max(Connect_body.getString("max"));
            abStats.setConnection_Times_Connect_min(Connect_body.getString("min"));
            abStats.setConnection_Times_Connect_mean(Connect_body.getString("mean"));
            abStats.setConnection_Times_Connect_sd(Connect_body.getString("sd"));
            abStats.setConnection_Times_Connect_median(Connect_body.getString("median"));
    
            JSONObject Processing_body = Connection_Times_body.getJSONObject("Processing");
            
            abStats.setConnection_Times_Processing_max(Processing_body.getString("max"));
            abStats.setConnection_Times_Processing_min(Processing_body.getString("min"));
            abStats.setConnection_Times_Processing_mean(Processing_body.getString("mean"));
            abStats.setConnection_Times_Processing_sd(Processing_body.getString("sd"));
            abStats.setConnection_Times_Processing_median(Processing_body.getString("median"));
        
            JSONObject Waiting_body = Connection_Times_body.getJSONObject("Waiting");
            
            abStats.setConnection_Times_Waiting_max(Waiting_body.getString("max"));
            abStats.setConnection_Times_Waiting_min(Waiting_body.getString("min"));
            abStats.setConnection_Times_Waiting_mean(Waiting_body.getString("mean"));
            abStats.setConnection_Times_Waiting_sd(Waiting_body.getString("sd"));
            abStats.setConnection_Times_Waiting_median(Waiting_body.getString("median"));
       
            JSONObject Total_body = Connection_Times_body.getJSONObject("Total");
            
            abStats.setConnection_Times_Total_max(Total_body.getString("max"));
            abStats.setConnection_Times_Total_min(Total_body.getString("min"));
            abStats.setConnection_Times_Total_mean(Total_body.getString("mean"));
            abStats.setConnection_Times_Total_sd(Total_body.getString("sd"));
            abStats.setConnection_Times_Total_median(Total_body.getString("median"));
       
            JSONObject percentages_body=body.getJSONObject("Percentage of the requests served within a certain time (ms)");
            abStats.setPercentage_50(percentages_body.getString("50"));
            abStats.setPercentage_66(percentages_body.getString("66"));
            abStats.setPercentage_75(percentages_body.getString("75"));
            abStats.setPercentage_80(percentages_body.getString("80"));
            abStats.setPercentage_90(percentages_body.getString("90"));
            abStats.setPercentage_95(percentages_body.getString("95"));
            abStats.setPercentage_98(percentages_body.getString("98"));
            abStats.setPercentage_99(percentages_body.getString("99"));
            abStats.setPercentage_100(percentages_body.getString("100"));
                    
            return abStats;
        } 
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            response.close();
        }
        return null;
     
    
    }
    
    
    

   
    
    
    
}
