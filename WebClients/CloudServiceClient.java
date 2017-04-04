import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

		long slotDuration = 0;
		int initialRate = 0; // Poisson mean request rate in requests per second
		float concurrentRatio = 1;
		int multiplier = 1;
		int startFlushSlot = -1;
		int stopFlushSlot = -1;
		int stopSlot = 0;
		int providersNum = 1;
		String jujuURL = "";

		String propFileName = "config.properties";

		// Read config properties

		try {

			InputStream iStream = null;
			Properties prop = new Properties();
			iStream = new FileInputStream(propFileName);

			if (iStream != null) 
				prop.load(iStream);
			
			providersNum = Integer.parseInt(prop.getProperty("providers"));
			slotDuration = Long.parseLong(prop.getProperty("slotDuration"));
			initialRate = Integer.parseInt(prop.getProperty("initialRate"));
			concurrentRatio = Float.parseFloat(prop.getProperty("concurrentRatio"));
			multiplier = Integer.parseInt(prop.getProperty("multiplier"));
			startFlushSlot = Integer.parseInt(prop.getProperty("startFlushSlot"));
			stopFlushSlot = Integer.parseInt(prop.getProperty("stopFlushSlot"));
			stopSlot = Integer.parseInt(prop.getProperty("stopSlot"));
			jujuURL = prop.getProperty("jujuURL");

		} catch(IOException e){
			System.out.println("property file '" + propFileName + "' not found in the classpath");
		}

		// The aggregate request rate has to be shared among edge IPs, by starting one client thread for each separate IP/URL
		// The data per client have to be stored also per IP, so the IP has to be put in the serviceStats tables.
		// The getRequests() and getResponseTime() have to be adjusted accordingly
		// For each Thread I need a setRate method for dynamically adjusting the send rate


		////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////// AB ////////////////////////////////////////////////////////

		String abTable = "CREATE TABLE ABSTATS " +
				"(ts TIMESTAMP not NULL, " +
				" CLIENT_ID INTEGER not NULL, " + 
				" PROVIDER INTEGER not NULL, " +
				" SERVICEURL VARCHAR(200) not NULL, "+
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
				" PRIMARY KEY ( TS,PROVIDER,CLIENT_ID, SERVICEURL ))"; 


		////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////// REDIS ////////////////////////////////////////////////////////

		String redTable = "CREATE TABLE REDISSTATS " +  
				"(ts TIMESTAMP not NULL, " +
				" CLIENT_ID INTEGER not NULL, " + 
				" PROVIDER INTEGER not NULL, " +
				" SERVICEURL VARCHAR(200) not NULL, "+
				" requests INTEGER not NULL, "+
				" concurrency INTEGER not NULL, "+ 
				" request_rate FLOAT not NULL, "+
				" set_completed INTEGER not NULL, " +
				" set_percentiles VARCHAR(120) not NULL, "+
				" set_latencies VARCHAR(120) not NULL, "+
				" get_completed INTEGER not NULL, " +
				" get_percentiles VARCHAR(120) not NULL, "+
				" get_latencies VARCHAR(120) not NULL, "+
				" PRIMARY KEY ( TS,PROVIDER,CLIENT_ID, SERVICEURL ))"; 



		int rate = (int)initialRate;
		int requests = (int)(concurrentRatio*initialRate);
		int concurrent = (int)(concurrentRatio*initialRate);

		int slot = 0;

		while (slot <= stopSlot) {
			System.out.println("--------------------------------- TIME SLOT "+slot+" -----------------------------");
			if (slot == startFlushSlot){
				rate = (int)multiplier*initialRate;
				requests = (int)(multiplier*concurrentRatio*initialRate);
				concurrent = (int)(multiplier*concurrentRatio*initialRate);
			} else if (slot == stopFlushSlot) {
				multiplier = 1;
				rate = (int)multiplier*initialRate;
				requests = (int)(multiplier*concurrentRatio*initialRate);
				concurrent = (int)(multiplier*concurrentRatio*initialRate);
			}
			
			ABServiceClient[] abs = null;
			RedisServiceClient[] redises = null;
			String[]  IPs = null;
			for (int j=0;j<providersNum;j++) {
				IPs = getServiceIPs("apachep"+j, jujuURL);
				abs = new ABServiceClient[IPs.length];
				for (int i=0;i<IPs.length;i++) 
					abs[i] = new ABServiceClient(1, 1, rate/IPs.length, requests/IPs.length, concurrent/IPs.length, IPs[i]);

				IPs = getServiceIPs("redisp"+j, jujuURL);
				redises = new RedisServiceClient[IPs.length];
				for (int i=0;i<IPs.length;i++) 
					redises[i] = new RedisServiceClient(1, 1, rate/IPs.length, requests/IPs.length, concurrent/IPs.length, IPs[i]);
			}
			
			
			if (slot == 0) {
				abs[0].createTable(abTable);
				redises[0].createTable(redTable);
			}
			
			for (int j=0;j<providersNum;j++) {
				new ClientThread(rate/IPs.length, concurrent/IPs.length, slotDuration-1000, abs[j]).start();
				new ClientThread(rate/IPs.length, concurrent/IPs.length, slotDuration-1000, redises[j]).start();
			}
			 
			// Currently we have only 2 services deployed at 2 IPs, the total requests have to be split to the total number of deployed VMs+1 per service
//			ABServiceClient ab = new ABServiceClient(1, 1, rate, requests, concurrent, "http://10.95.196.78:80/");
//		RedisServiceClient red = new RedisServiceClient(1, 1, rate, requests, concurrent, "10.95.196.143");

//			if (slot == 0)
//			{
//				ab.createTable(abTable);
//				red.createTable(redTable);
//			}
//			ClientThread abthread = new ClientThread(rate, concurrent, slotDuration-1000, ab);
//			ClientThread redthread = new ClientThread(rate, concurrent, slotDuration-1000, red);
//			abthread.start();
//			redthread.start();

			try {
				Thread.sleep(slotDuration);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			slot++;
		}


	}



	static String[] getServiceIPs(String serviceName, String serviceURL) {
		String[] IPs = null;

		try{

			HttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet(serviceURL+serviceName);
			HttpResponse response = client.execute(request);

			String responseString = new BasicResponseHandler().handleResponse(response);
			//String responseString="{\"public-address\":\"87.42.3.23\",\"edge-ips\":[\"A\",\"B\"]}";
			JSONObject obj = new JSONObject(responseString);
			String cloudIP = obj.getString("remote-ip");
			JSONArray arr = obj.getJSONArray("local-ips");
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
		int iterations=0;

		while (time<endTime) { 
			try {
				new Thread(serviceThread).start();

				total_requests += concurrent_requests;
				interarrival = Math.round(-1000*(Math.log(Math.random())/Math.log(Math.E))/(request_rate/concurrent_requests));
				//System.out.println("SLEEP TIME: "+interarrival);

				Thread.sleep(interarrival);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			iterations++;
			time = System.currentTimeMillis();
		}

		System.out.println("MEAN REQUEST RATE : "+(total_requests/iterations));
	}
}

