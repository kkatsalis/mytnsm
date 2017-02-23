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

	public WebUtilities(Configuration config) {

		this._config = config;

	}

	public String createVM(Hashtable parameters) throws IOException {

		// http://nitlab3.inf.uth.gr:4100/vm-create/server-john/precise/small/192.168.100.10/255.255.254.0/192.168.100.1/node

		String uri = "http://" + _config.getNitosServer() + ".inf.uth.gr:4100/vm-create/";
		boolean methodResponse = false;

		String vm_name = String.valueOf(parameters.get("vmName"));
		String OS = String.valueOf(parameters.get("OS"));
		String vmType = String.valueOf(parameters.get("vmType"));
		String interIP = String.valueOf(parameters.get("interIP"));
		String interMask = String.valueOf(parameters.get("interMask"));
		String interDefaultGateway = String.valueOf(parameters.get("interDefaultGateway"));
		String hostName = String.valueOf(parameters.get("hostName"));

		uri += vm_name + "/";
		uri += OS + "/";
	

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);

		try (CloseableHttpResponse response = httpclient.execute(httpget)) {
			System.out.println("CreateVM called");
			System.out.println("Response Status:" + response.getStatusLine().toString());

			if (response.getStatusLine().toString().contains("200"))
				methodResponse = true;
		}

		return vm_name;
	}
	

	public Boolean deleteVM(int host_id, String vm_ip) throws IOException {

		String uri = "http://" + _config.getNitosServer() + ".inf.uth.gr:4100/vm-destroy/";
		// String
		// uri="http://"+_config.getNitosServer()+".inf.uth.gr:4100/vm-destroy/";

		String methodResponse = "";

		uri += vmName;
		uri += "/" + hostName;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);

		CloseableHttpResponse response = httpclient.execute(httpget);

		try {
			System.out.println("****** VM:" + vmName + " deleted");
			System.out.println(response.getStatusLine().toString());

		} finally {
			response.close();
		}

		return true;
	}

	public boolean checkVMListOnHost(String hostName, String vmName) throws IOException {

		String uri = "http://" + _config.getNitosServer() + ".inf.uth.gr:4100/virsh_list_all/";
		uri += hostName;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);
		CloseableHttpResponse response = httpclient.execute(httpget);

		String json = "";
		String output;
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

		while ((output = br.readLine()) != null) {
			if (output.contains(vmName) & output.contains("shut off"))
				return true;
		}

		return false;

	}

	
	@SuppressWarnings("unchecked")
	public Hashtable retrieveHostStats(String host_identifier,int slot,int instance) {
		// List<NetRateStats> netRates=new ArrayList<>();

		Hashtable parameters=new Hashtable();

		String uri=host_identifier;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);
		CloseableHttpResponse response;

		try {
			response = httpclient.execute(httpget);


			System.out.println(response.getProtocolVersion());
			System.out.println(response.getStatusLine().getStatusCode());
			System.out.println(response.getStatusLine().getReasonPhrase());
			System.out.println(response.getStatusLine().toString());



			String json="";
			String output;
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			while ((output = br.readLine()) != null) {
				json+=output;
			}	

			JSONObject body=new JSONObject(json);

			parameters.put("slot",slot);
			parameters.put("measurement",instance);
			parameters.put("host_identifier", host_identifier);
			parameters.put("time",body.getString("Time") );
			parameters.put("arch",body.getString("Arch") );
			parameters.put("physical_CPUs",body.getString("Physical CPUs") );
			parameters.put("count",body.getString("Count") );
			parameters.put("running",body.getString("Running") );
			parameters.put("blocked",body.getString("Blocked") );
			parameters.put("paused",body.getString("Paused") );
			parameters.put("shutdown",body.getString("Shutdown") );
			parameters.put("shutoff",body.getString("Shutoff") );
			parameters.put("crashed",body.getString("Crashed") );
			parameters.put("active",body.getString("Active") );
			parameters.put("inactive",body.getString("Inactive") );
			parameters.put("cpu_percentage",body.getString("%CPU") );
			parameters.put("total_hardware_memory_KB",body.getString("Total hardware memory (KB)") );
			parameters.put("total_memory_KB",body.getString("Total memory (KB)") );
			parameters.put("total_guest_memory_KB",body.getString("Total guest memory (KB)") );

		}
		catch (IOException e1) {
			e1.printStackTrace();
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return parameters;

	}

	@SuppressWarnings("unchecked")
	public Hashtable retrieveVMStats(String vm_identifier,int slot,int instance) {
		// List<NetRateStats> netRates=new ArrayList<>();

		Hashtable parameters=new Hashtable();

		String uri=vm_identifier;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(uri);
		CloseableHttpResponse response;

		try {
			response = httpclient.execute(httpget);


			System.out.println(response.getProtocolVersion());
			System.out.println(response.getStatusLine().getStatusCode());
			System.out.println(response.getStatusLine().getReasonPhrase());
			System.out.println(response.getStatusLine().toString());



			String json="";
			String output;
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			while ((output = br.readLine()) != null) {
				json+=output;
			}	

			JSONObject body=new JSONObject(json);

			parameters.put("slot",slot);
			parameters.put("measurement",instance);
			parameters.put("host_identifier", vm_identifier);
			parameters.put("time",body.getString("Time") );


		}
		catch (IOException e1) {
			e1.printStackTrace();
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return parameters;

	}


}