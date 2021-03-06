import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;

public class ABServiceClient implements Runnable {

	Connection conn = null;
	int provider;
	int client_id;
	float request_rate;
	int requests;
	int concurrency;
	String url;

	public Connection connectDB() {
		try {
			// db parameters
			String url = "jdbc:sqlite:serviceStats.db";
			// create a connection to the database
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
		return conn;
	}
	
	public  void createTable(String sql)
	{
		
		try {
			if (conn == null)
				conn = connectDB();
			
			Statement stmt = conn.createStatement();
			
			stmt.executeUpdate(sql);
			System.out.println("Table created");
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void insertAB(ABdata data) {
		String sql = "INSERT INTO ABSTATS(ts, "+
				" client_id, "+
				" PROVIDER, " + 
				" SERVICEURL, "+
				" requests, "+
				" concurrency, "+ 
				" request_rate, "+
				" completed, " +
				" failed, " +
				" rps, "+
				" mtpr, "+
				" mtprc, "+
				" transfer_rate, "+		
				" min_connect, "+
				" mean_connect, "+
				" sd_connect, "+
				" median_connect, "+
				" max_connect, "+
				" fractions, "+
				" latencies) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			if (conn == null) connectDB();
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setTimestamp(1, data.ts);
			pstmt.setInt(2, data.client_id);
			pstmt.setInt(3, data.provider);
			pstmt.setString(4, data.url);
			pstmt.setInt(5, data.requests);
			pstmt.setInt(6, data.concurrency);
			pstmt.setFloat(7, data.request_rate);
			pstmt.setInt(8, data.completed);
			pstmt.setInt(9, data.failed);
			pstmt.setFloat(10, data.rps);
			pstmt.setFloat(11, data.mtpr);
			pstmt.setFloat(12, data.mtprc);
			pstmt.setFloat(13, data.transfer_rate);
			pstmt.setInt(14, data.min_connect);
			pstmt.setFloat(15, data.mean_connect);
			pstmt.setFloat(16, data.sd_connect);
			pstmt.setInt(17, data.median_connect);
			pstmt.setInt(18, data.max_connect);
			
			String tmp = "";
			if (data.fractions != null) {
				for (int i=0;i<data.fractions.length-1;i++)
					tmp += data.fractions[i] +",";
				tmp += data.fractions[data.fractions.length-1];
			} else 
				System.out.println("I did not get any response time percentiles !!!!");
			pstmt.setString(19, tmp);

			tmp = "";
			if (data.latencies != null) {
				for (int i=0;i<data.latencies.length-1;i++)
					tmp += data.latencies[i] +",";
				tmp += data.latencies[data.latencies.length-1];
			} else 
				System.out.println("I did not get any response time latencies !!!!");
			pstmt.setString(20, tmp);

			pstmt.executeUpdate();

			
		} catch (SQLException e) {
			System.out.println(e);
		}
	}




	ABdata parseAB(InputStream is) throws IOException, NoSuchElementException 
	{
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringTokenizer strtoken = null;
		ABdata data = new ABdata();
		boolean first = true;
		
		while ((line=rd.readLine()) != null)
		{
			strtoken = new StringTokenizer(line, ":");
			String prompt = null;
			if (strtoken.hasMoreTokens())
				prompt = strtoken.nextToken();
			else  {
				prompt = line;
			}
			
			if (prompt.equals("Concurrency Level")) {
				//System.out.println("Mpika concurrency");
				data.concurrency = Integer.parseInt(strtoken.nextToken().trim());
			} else if (prompt.equals("Complete requests")) {
				//System.out.println("Mpika complete");
				data.completed = Integer.parseInt(strtoken.nextToken().trim());
			} else if (prompt.equals("Failed requests")) {
				//System.out.println("Mpika failed");
				data.failed = Integer.parseInt(strtoken.nextToken().trim());
			} else if (prompt.equals("Requests per second")) {
				//System.out.println("Mpika requests");
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				data.rps = Float.parseFloat(tmptoken.nextToken().trim());
			} else if (prompt.equals("Time per request")) {
				//System.out.println("Mpika time");
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				if (first) {
					data.mtpr = Float.parseFloat(tmptoken.nextToken());
					first = false;
				} else {
					data.mtprc = Float.parseFloat(tmptoken.nextToken());
					first = true;
				}
			} else if (prompt.equals("Transfer rate")) {
				//System.out.println("Mpika transfer");
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				data.transfer_rate = Float.parseFloat(tmptoken.nextToken());
			}/* else if (prompt.equals("Connect")) {
				System.out.println("Mpika connect");
			} else if (prompt.equals("Processing")) {
				System.out.println("Mpika processing");
			} else if (prompt.equals("Waiting")) {
				System.out.println("Mpika wiating");
			} */else if (prompt.equals("Total")) {
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				data.min_connect = Integer.parseInt(tmptoken.nextToken());
				data.mean_connect = Float.parseFloat(tmptoken.nextToken());
				data.sd_connect = Float.parseFloat(tmptoken.nextToken());
				data.median_connect = Integer.parseInt(tmptoken.nextToken());
				data.max_connect =  Integer.parseInt(tmptoken.nextToken());
			} else if (prompt.equals("Percentage of the requests served within a certain time (ms)")) {
				data.fractions = new Integer[9];
				data.latencies = new Integer[9];
				int i = 0;
				while ((line = rd.readLine()) != null) {
					strtoken = new StringTokenizer(line.trim());
					StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken(), "%");
					data.fractions[i] = Integer.parseInt(tmptoken.nextToken());
					data.latencies[i] = Integer.parseInt(strtoken.nextToken());
					i++;
				}
			}
			strtoken = null;
		}
		
		Date now = new Date();
		data.ts = new Timestamp(now.getTime());
		
		//System.out.println("Current time is : "+data.ts);
 		return data;
	}

	public ABServiceClient(int provider, int client_id, float request_rate, int requests, int concurrency, String url)
	{
		this.provider = provider;
		this.client_id = client_id;
		this.request_rate = request_rate;
		this.requests = requests;
		this.concurrency = concurrency;
		this.url = "http://"+url+"/";
	}
		
	public void run() 
	{ 
		ProcessBuilder pb =
				new ProcessBuilder("ab", "-k", "-n", ""+requests,"-c", 
						""+concurrency, url);
		pb.directory(new File("."));
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ABdata data = parseAB(p.getInputStream());
			data.provider = provider;
			//data.client_id = client_id+"_"+UUID.randomUUID();
			data.client_id = client_id;
			data.url = url;
			data.requests = requests;
			data.request_rate = request_rate;
			insertAB(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
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
                " PRIMARY KEY ( TS,PROVIDER,CLIENT_ID,SERVICEURL ))"; 
		
		
		
		float request_rate = 300; // Poisson mean request rate in requests per second
		
		int concurrent = 300;
		
		ABServiceClient ab = new ABServiceClient(1, 1, request_rate, concurrent, concurrent, "http://10.95.196.78:80/");
		ab.createTable(abTable);

		new Thread(ab).start();
	}
}


class ABdata {
	Timestamp ts;
	int client_id;
	int provider;
	String url;
	int requests;
	int concurrency;
	float request_rate;
	int completed;
	int failed;
	float rps;
	float mtpr;
	float mtprc;
	float transfer_rate;		
	int min_connect;
	float mean_connect;
	float sd_connect;
	int median_connect;
	int max_connect;
	Integer[] fractions;
	Integer[] latencies;
}
