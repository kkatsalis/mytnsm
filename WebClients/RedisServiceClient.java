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

public class RedisServiceClient implements Runnable {

	Connection conn = null;
	int provider;
	int client_id;
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
				" SERVICEURL, "+
				" requests, "+
				" concurrency, "+ 
				" request_rate, "+
				" set_completed, " +
				" set_percentiles, " +
				" set_latencies, "+
				" get_completed, "+
				" get_percentiles, "+
				" get_latencies) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
			pstmt.setFloat(8, data.set_completed);
			pstmt.setString(9, data.set_percentiles);
			pstmt.setString(10, data.set_latencies);
			pstmt.setFloat(11, data.get_completed);
			pstmt.setString(12, data.get_percentiles);
			pstmt.setString(13, data.get_latencies);
			
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
		
		//System.out.println("Mpika parsed");
		while ((line=rd.readLine()) != null)
		{	
			//System.out.println("mpika while");
			line = line.trim();
			//System.out.println(line);
			if (line.equals(""))
				continue;
			else if (line.equals("====== SET ======")) {
				//System.out.println("SET");
				category = "SET";
				data.set_percentiles = "";
				data.set_latencies = "";
			} else if (line.equals("====== GET ======")) {
				//System.out.println("GET");
				category = "GET";
				data.get_percentiles = "";
				data.get_latencies = "";
			} else if (line.startsWith("======")) 
				category = "OTHER";
			else if (category.equals("SET")) {
				strtoken = new StringTokenizer(line);
				try {
					String first = strtoken.nextToken();
					String second = strtoken.nextToken();
					
					if (second.equals("requests"))
					{
						if (!strtoken.nextToken().equals("completed"))
							continue;
						//System.out.println("set requests");
						data.set_completed = Integer.parseInt(first);
					} else if (second.equals("parallel")) {
						//System.out.println("set concurrency");
						data.concurrency = Integer.parseInt(first);
					} else if (second.equals("bytes") || second.equals("alive"))
						continue;
					else if (second.equals("<=")) {
						//System.out.println("Percentiles set");
						StringTokenizer tmpToken = new StringTokenizer(first,"%");
						data.set_percentiles += tmpToken.nextToken()+",";
						data.set_latencies += strtoken.nextToken()+",";
					} 
				} catch(NoSuchElementException e)
				{
					System.out.println("Malformed redis-benchmark output");
				}
			} else if (category.equals("GET")) {
				strtoken = new StringTokenizer(line);
				try {
					String first = strtoken.nextToken();
					String second = strtoken.nextToken();

					if (second.equals("requests"))
					{
						if (!strtoken.nextToken().equals("completed"))
							continue;
						data.get_completed = Integer.parseInt(first);
					} else if (second.equals("parallel")) 
						data.concurrency = Integer.parseInt(first);
					else if (second.equals("bytes") || second.equals("alive"))
						continue;
					else if (second.equals("<=")) {
						StringTokenizer tmpToken = new StringTokenizer(first,"%");
						
						data.get_percentiles += tmpToken.nextToken()+",";
						data.get_latencies += strtoken.nextToken()+",";
					} 
				} catch(NoSuchElementException e)
				{
					System.out.println("Malformed redis-benchmark output");
				}
			}
		}
		Date now = new Date();
		data.ts = new Timestamp(now.getTime());
 		return data;
	}

	public RedisServiceClient(int provider, int client_id, float request_rate, int requests, int concurrency, String ip)
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
		pb.directory(new File("."));
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//System.out.println("EXECUTED REDIS");
			RedisData data = parseRED(p.getInputStream());
			//System.out.println("PARSED DATA");
			data.provider = provider;
			//data.client_id = client_id+"_"+UUID.randomUUID();
			data.client_id = client_id;
			data.url = ip;
			data.requests = requests;
			data.request_rate = request_rate;
			insertRED(data);
			//System.out.println("STORED DATA");
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
                " PRIMARY KEY ( TS,PROVIDER,CLIENT_ID,SERVICEURL ))"; 
	
		float request_rate = 300; // Poisson mean request rate in requests per second
		
		int concurrent = 300;
		
		RedisServiceClient red = new RedisServiceClient(1, 1, request_rate, concurrent, concurrent, "10.95.196.143");
		red.createTable(redTable);
		new Thread(red).start();

	}
}

class RedisData {
	Timestamp ts;
	int client_id;
	int provider;
	String url;
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
