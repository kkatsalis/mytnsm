import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.json.*;

public class CloudServiceClient {
	public static void main(String[] args){
		/*
		String[] ips = getServiceIPs("","");
		
		for (String ip:ips)
		{
			System.out.println(ip);
		}
		*/
		
		// The aggregate request rate has to be shared among edge IPs, by starting one client thread for each separate IP/URL
		// The data per client have to be stored also per IP, so the IP has to be put in the serviceStats tables.
		// The getRequests() and getResponseTime() have to be adjusted accordingly
		// For each Thread I need a setRate method for dynamically adjusting the send rate
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////// AB ////////////////////////////////////////////////////////

		String abTable = "CREATE TABLE ABSTATS " +
				"(ts TIMESTAMP not NULL, " +
				" CLIENT_ID VARCHAR(50) not NULL, " + 
				" PROVIDER INTEGER not NULL, " + 
				" requests INTEGER not NULL, "+
				" concurrency INTEGER not NULL, "+ 
				" request_rate FLOAT not NULL, "+
				" completed INTEGER not NULL, " +
				" failed INTEGER not NULL, " +
				" rps FLOAT not NULL, "+
				" mtpr FLOAT not NULL, "+
				" mtprc FLOAT not NULL, "+
				" transfer_rate FLOAT not NULL, "+		
				" min_connect INTEGER not NULL, "+
				" mean_connect FLOAT not NULL, "+
				" sd_connect FLOAT not NULL, "+
				" median_connect INTEGER not NULL, "+
				" max_connect INTEGER not NULL, "+
				" fractions VARCHAR(120) not NULL, "+
				" latencies VARCHAR(120) not NULL, "+
				" PRIMARY KEY ( TS,PROVIDER,CLIENT_ID ))"; 


		
		float request_rate = 300; // Poisson mean request rate in requests per second
		int concurrent = 300;

		ABServiceClient ab = new ABServiceClient(1, "1", request_rate, concurrent, concurrent, "http://10.95.196.78:80/");
		ab.createTable(abTable);

		new ClientThread(request_rate, concurrent, 20000, ab).start();


		////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////// REDIS ////////////////////////////////////////////////////////

		String redTable = "CREATE TABLE REDISSTATS " +
				"(ts TIMESTAMP not NULL, " +
				" CLIENT_ID VARCHAR(50) not NULL, " + 
				" PROVIDER INTEGER not NULL, " + 
				" requests INTEGER not NULL, "+
				" concurrency INTEGER not NULL, "+ 
				" request_rate FLOAT not NULL, "+
				" set_completed INTEGER not NULL, " +
				" set_percentiles VARCHAR(120) not NULL, "+
				" set_latencies VARCHAR(120) not NULL, "+
				" get_completed INTEGER not NULL, " +
				" get_percentiles VARCHAR(120) not NULL, "+
				" get_latencies VARCHAR(120) not NULL, "+
				" PRIMARY KEY ( TS,PROVIDER,CLIENT_ID ))"; 

		request_rate = 300; // Poisson mean request rate in requests per second
		concurrent = 300;

		RedisServiceClient red = new RedisServiceClient(1, "1", request_rate, concurrent, concurrent, "10.95.196.143");
		red.createTable(redTable);
		new ClientThread(request_rate, concurrent, 20000, red).start();
		
		
	}



	static String[] getServiceIPs(String serviceName, String serviceURL) {
		String[] IPs = null;

		try{

			HttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet(serviceURL+"?name="+serviceName);
			HttpResponse response = client.execute(request);

			String responseString = new BasicResponseHandler().handleResponse(response);
			//String responseString="{\"public-address\":\"87.42.3.23\",\"edge-ips\":[\"A\",\"B\"]}";
			JSONObject obj = new JSONObject(responseString);
			String cloudIP = obj.getString("public-address");
			JSONArray arr = obj.getJSONArray("edge-ips");
			IPs = new String[arr.length()+1];
			IPs[0] = cloudIP;
			for (int i = 0; i < arr.length(); i++) 
				IPs[i+1] = arr.getString(i);
			
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return IPs;
	}

}

class ClientThread extends Thread
{
	float request_rate;
	int concurrent_requests;
	long duration;
	Runnable serviceThread;

	ClientThread(float request_rate, int concurrent, long duration, Runnable serviceThread)
	{
		this.request_rate = request_rate; // Poisson mean request rate in requests per second
		this.concurrent_requests = concurrent;
		this.duration = duration;
		this.serviceThread = serviceThread;
	}

	public void run() {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + duration;
		long interarrival = 0;
		long total_requests = 0;
		long time = System.currentTimeMillis();

		while (time<endTime) { 
			try {
				new Thread(serviceThread).start();

				total_requests += concurrent_requests;
				interarrival = Math.round(-1000*(Math.log(Math.random())/Math.log(Math.E))/(request_rate/concurrent_requests));
				System.out.println("SLEEP TIME: "+interarrival);

				Thread.sleep(interarrival);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			time = System.currentTimeMillis();
		}

		System.out.println("MEAN REQUEST RATE : "+(total_requests/20.0));
	}
}

