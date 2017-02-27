/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;


import Controller.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean createVM(Hashtable parameters) throws IOException {

		String uri = "http://localhost:5004/jox/slice/stack/machines?slice=default-slice&stack=default-stack";

		String vm_name = String.valueOf(parameters.get("vm_name"));
		String vm_series = String.valueOf(parameters.get("vm_series"));
		String vm_type = String.valueOf(parameters.get("vm_type"));
		
		String json="{ \"machines-list\":[{"
				+ "\"machine-type\": \"kvm\","
				+ "\"machine-template\": \""+vm_type+"\","
				+ "\"stack-machine-name\": \""+vm_name+"\","
				+ "\"series\": \""+vm_series+"\","
				+ "\"parent-machine-name\": \"\","
				+ "\"constraints\": {}}]}";
		
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost(uri);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");


			System.out.println("ante na doume");


		} catch (IOException ex) {
			return false;
		}
		
		return true;
		
	}
	

	public Boolean destroyService(Hashtable parameters) throws IOException {

		String uri = "http://localhost:5004/jox/slice/stack/machines?slice=default-slice&stack=default-stack";

		String service_name = String.valueOf(parameters.get("vm_name"));
		
		
		String json="{ \"destroy-services-list\": [{\"stack-service-name\": \""+service_name+"\"}";
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost(uri);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");

			System.out.println("ante na doume");


		} catch (IOException ex) {
			return false;
		}
		
		return true;
	
	}

	
	
	
	


}