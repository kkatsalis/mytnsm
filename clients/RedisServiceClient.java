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

public class RedisServiceClient implements Runnable {

	Connection conn = null;
	String provider;
	String client_id;
	float request_rate;
	int requests;
	int concurrency;
	String ip;

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

	public void insertRED(RedisData data) {
		String sql = "INSERT INTO REDISSTATS(ts, "+
				" client_id, "+
				" PROVIDER, " + 
				" requests, "+
				" concurrency, "+ 
				" request_rate, "+
				" set_completed, " +
				" set_percentiles, " +
				" set_latencies, "+
				" get_completed, "+
				" get_percentiles, "+
				" get_latencies) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			if (conn == null) connectDB();
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setTimestamp(1, data.ts);
			pstmt.setString(2, data.client_id);
			pstmt.setString(3, data.provider);
			pstmt.setInt(4, data.requests);
			pstmt.setInt(5, data.concurrency);
			pstmt.setFloat(6, data.request_rate);
			pstmt.setInt(7, data.set_completed);
			pstmt.setString(8, data.set_percentiles);
			pstmt.setString(9, data.set_latencies);
			pstmt.setInt(10, data.get_completed);
			pstmt.setString(11, data.get_percentiles);
			pstmt.setString(12, data.get_latencies);
			
			pstmt.executeUpdate();
			
			
		} catch (SQLException e) {
			System.out.println(e);
		}
	}




	RedisData parseRED(InputStream is) throws IOException, NoSuchElementException 
	{
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringTokenizer strtoken = null;
		RedisData data = new RedisData();
		String category = "";
		data.set_percentiles = "";
		data.get_percentiles = "";
		data.set_latencies = "";
		data.get_latencies = "";
		
		while ((line=rd.readLine().trim()) != null)
		{	
			if (line.equals(""))
				continue;
			else if (line.equals("====== SET ======")) {
				//System.out.println("Mpika set");
				category = "SET";
			} else if (line.equals("====== GET ======")) {
				category = "GET";
			} else if (category.equals("SET")) {
				//System.out.println("Mpika set");
				strtoken = new StringTokenizer(line);
				try {
					String first = strtoken.nextToken();
					String second = strtoken.nextToken();
					
					if (second.equals("requests"))
					{
						data.requests = Integer.parseInt(first);
						data.set_completed = Integer.parseInt(first);
					} else if (second.equals("parallel")) 
						data.concurrency = Integer.parseInt(first);
					else if (second.equals("bytes") || second.equals("alive"))
						continue;
					else if (second.equals("<=")) {
						StringTokenizer tmpToken = new StringTokenizer(first,"%");
						
						data.set_percentiles += tmpToken.nextToken()+",";
						data.set_latencies += strtoken.nextToken()+",";
					} 
				} catch(NoSuchElementException e)
				{
					System.out.println("Malformed redis-benchmark output");
				}
			} else if (category.equals("GET")) {
				//System.out.println("Mpika get");
				strtoken = new StringTokenizer(line);
				try {
					String first = strtoken.nextToken();
					String second = strtoken.nextToken();
					
					if (second.equals("requests"))
					{
						data.requests = Integer.parseInt(first);
						data.set_completed = Integer.parseInt(first);
					} else if (second.equals("parallel")) 
						data.concurrency = Integer.parseInt(first);
					else if (second.equals("bytes") || second.equals("alive"))
						continue;
					else if (second.equals("<=")) {
						StringTokenizer tmpToken = new StringTokenizer(first,"%");
						
						data.get_percentiles += tmpToken.nextToken()+",";
						data.get_latencies += strtoken.nextToken()+",";
					} else if (second.equals("requests")) {
						data.request_rate = Float.parseFloat(first);
					}
				} catch(NoSuchElementException e)
				{
					System.out.println("Malformed redis-benchmark output");
				}
			}
		}
 		return data;
	}

	public RedisServiceClient(String provider, String client_id, float request_rate, int requests, int concurrency, String ip)
	{
		this.provider = provider;
		this.client_id = client_id;
		this.request_rate = request_rate;
		this.requests = requests;
		this.concurrency = concurrency;
		this.ip = ip;
	}
		
	public void run() 
	{ 
		ProcessBuilder pb =

				new ProcessBuilder("redis-benchmark", "-c", ""+concurrency, "-n", ""+request_rate, "-h", ip);
		//		Map<String, String> env = pb.environment();
		//		env.put("VAR1", "myValue");
		//		env.remove("OTHERVAR");
		//		env.put("VAR2", env.get("VAR1") + "suffix");
		pb.directory(new File("."));
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			RedisData data = parseRED(p.getInputStream());
			data.provider = provider;
			data.client_id = client_id+"_"+UUID.randomUUID();
			data.requests = requests;
			data.request_rate = request_rate;
			insertRED(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		String redTable = "CREATE TABLE REDISSTATS " +
                "(ts TIMESTAMP not NULL, " +
                " CLIENT_ID VARCHAR(50) not NULL, " + 
                " PROVIDER VARCHAR(50) not NULL, " + 
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
	
		float request_rate = 300; // Poisson mean request rate in requests per second
		
		int concurrent = 300;
		
		RedisServiceClient red = new RedisServiceClient("vodafone", "1", request_rate, concurrent, concurrent, "http://10.95.196.78:80/");
		red.createTable(redTable);
		new Thread(red).start();

	}
}

class RedisData {
	Timestamp ts;
	String client_id;
	String provider;
	int requests;
	int concurrency;
	float request_rate;
	int set_completed;
	String set_percentiles;
	String set_latencies;
	int get_completed;
	String get_percentiles;
	String get_latencies;
}
