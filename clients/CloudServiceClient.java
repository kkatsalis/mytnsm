import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CloudServiceClient {

	public Connection connect() {
		Connection conn = null;
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

	public void insert(String name, double response_time) {
		String sql = "INSERT INTO SERVICE_STATS(id,response_time) VALUES(?,?)";

		try (Connection conn = connect();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setDouble(2, response_time);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}



/*
CREATE TABLE table_name
(
column_name1 data_type(size),
column_name2 data_type(size),
column_name3 data_type(size),
....
);
*/
	static ABdata parseAB(InputStream is) throws IOException, NoSuchElementException 
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
			
			if (prompt.equals("Complete requests")) {
				System.out.println("Mpika complete");
				data.completed = Integer.parseInt(strtoken.nextToken().trim());
			} else if (prompt.equals("Failed requests")) {
				System.out.println("Mpika failed");
				data.failed = Integer.parseInt(strtoken.nextToken().trim());
			} else if (prompt.equals("Requests per second")) {
				System.out.println("Mpika requests");
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				data.rps = Float.parseFloat(tmptoken.nextToken().trim());
			} else if (prompt.equals("Time per request")) {
				System.out.println("Mpika time");
				StringTokenizer tmptoken = new StringTokenizer(strtoken.nextToken().trim());
				if (first) {
					data.mtpr = Float.parseFloat(tmptoken.nextToken());
					first = false;
				} else {
					data.mtprc = Float.parseFloat(tmptoken.nextToken());
					first = true;
				}
			} else if (prompt.equals("Transfer rate")) {
				System.out.println("Mpika transfer");
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
				data.fractions = new int[9];
				data.latencies = new int[9];
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
 		return data;
	}

	public static void abBenchmark(final int requests, final int concurrency, final int buffer, final String url)
	{
		new Thread(new Runnable() {
			public void run() { 

				ProcessBuilder pb =
						new ProcessBuilder("ab", "-n", ""+requests,"-c", 
								""+concurrency, "-b", ""+buffer, url);
				pb.directory(new File("."));
				Process p = null;
				try {
					p = pb.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					parseAB(p.getInputStream());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch(NoSuchElementException e) {
					e.printStackTrace();
				}
			}}).start();
	}

	public static void redisBenchmark(String ip)
	{
		new Thread(new Runnable() {
			public void run() { 
				ProcessBuilder pb =

						new ProcessBuilder("redis-benchmark", "-h", ip);
				//		Map<String, String> env = pb.environment();
				//		env.put("VAR1", "myValue");
				//		env.remove("OTHERVAR");
				//		env.put("VAR2", env.get("VAR1") + "suffix");
				pb.directory(new File("."));
				File log = new File("log");
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(log));
				Process p = null;
				try {
					p = pb.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				assert pb.redirectInput() == Redirect.PIPE;
				assert pb.redirectOutput().file() == log;
				try {
					assert p.getInputStream().read() == -1;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}).start();
	}


	public static void main(String[] args){
		abBenchmark(50000,100, 500, "http://10.95.196.78:80/");
		redisBenchmark("10.95.196.143");

	}
}

class ABdata {
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
	int[] fractions;
	int[] latencies;
}
