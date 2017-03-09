import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.NoSuchElementException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;

public class CloudServiceClient {
	public static void main(String[] args){
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

