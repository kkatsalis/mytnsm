/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;


import Controller.Configuration;

import java.io.IOException;
import java.util.Hashtable;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.net.URI;

/**
 *
 * @author kostas
 */
public class WebUtilities {

	Configuration _config;

	public WebUtilities(Configuration config) {

		this._config = config;

	}

	public boolean createVM(Hashtable<?, ?> parameters) throws IOException {

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


			System.out.println("VM created:" + vm_name);


		} catch (IOException ex) {
			System.out.print(ex.getCause());
			return false;
		}
		
		return true;
		
	}
	

	@SuppressWarnings("unused")
	public Boolean destroyService(Hashtable<?, ?> parameters) throws IOException {

		String uri = "http://localhost:5004/jox/slice/stack/services?slice=default-slice&stack=default-stack";

		String service_name = String.valueOf(parameters.get("service_name"));
		
		
		String json="{ \"destroy-services-list\": [{\"stack-service-name\": \""+service_name+"\"}]}";
		System.out.println(json);	
		try{
			
			 CloseableHttpClient httpclient = HttpClients.createDefault();
		     HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(uri);
			
			StringEntity params = new StringEntity(json);
			httpDelete.setEntity(params);  
			Header requestHeaders[] = httpDelete.getAllHeaders();
	        CloseableHttpResponse response = httpclient.execute(httpDelete);
			json = EntityUtils.toString(response.getEntity(), "UTF-8");

			System.out.println("service destroyed"+service_name);


		} catch (IOException ex) {
			System.out.print(ex.getCause());
			return false;
		}
		
		return true;
	
	}

	public Boolean deployService(Hashtable<?, ?> parameters) throws IOException {

		String uri = "http://localhost:5004/jox/slice/stack/services?slice=default-slice&stack=default-stack";

		String service_name = String.valueOf(parameters.get("service_name"));
		String charm_name = String.valueOf(parameters.get("charm_name"));
		String vm_name = String.valueOf(parameters.get("vm_name"));
		
		
		String json="{\"services-list\": [{\"stack-service-name\": \""+service_name+"\","
				+ "\"description\": \"add description\",\"charm\": \""+charm_name+"\","
				+ "\"max-units\": 1,\"kvm-machine-to-deploy\": \""+vm_name+"\",\"lxc-machine-to-deploy\": \"\","
			    + "\"units-to-deploy\": 1,\"config-to-deploy\":{},\"kvm-machines-units\":[]}]}";
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost request = new HttpPost(uri);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");

			System.out.println("service deployed:"+service_name);


		} catch (IOException ex) {
			return false;
		}
		
		return true;
	
	}
	
	public Boolean scaleService(Hashtable<?, ?> parameters) throws IOException {

		String uri = "http://localhost:5004/jox/slice/stack/services?slice=default-slice&stack=default-stack";

		String service_name = String.valueOf(parameters.get("service_name"));
		String vm_name = String.valueOf(parameters.get("vm_name"));
		
		
		String json="{\"update-services-list\": [{\"stack-service-name\": \""+service_name+"\","
				+ "\"kvm-machines-units\":[{\"stack-machine-name\": \""+vm_name+"\","
						+ "\"add-units\": 1,\"remove-units\": 0}]}]}";
		
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPut request = new HttpPut(uri);
			StringEntity params = new StringEntity(json);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");

			System.out.println("service scaled:"+service_name);

		} catch (IOException ex) {
			return false;
		}
		
		return true;
	
	}
	
	
	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
	    public static final String METHOD_NAME = "DELETE";
	 
	    public String getMethod() {
	        return METHOD_NAME;
	    }
	 
	    public HttpDeleteWithBody(final String uri) {
	        super();
	        setURI(URI.create(uri));
	    }
	 
	    public HttpDeleteWithBody(final URI uri) {
	        super();
	        setURI(uri);
	    }
	 
	    public HttpDeleteWithBody() {
	        super();
	    }
	}
	


}