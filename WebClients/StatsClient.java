import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.sql.ResultSet;

public class StatsClient {
	Connection conn = null;

	void connectDB() {
		try {
			// db parameters
			String url = "jdbc:sqlite:serviceStats.db";
			// create a connection to the database
			this.conn = DriverManager.getConnection(url);

			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
	}
	
	void disConnectDB() {
		try {
			if (conn != null)
				conn.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
	}

	ResultSet executeQR(String query){
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch(SQLException e) {
			System.err.println("Query "+query+" failed");
		}
		return rs;
	}

	long getRequests(int serviceId, int provId, long start, long end)
	{
		String table = null;
		long reqs = 0;

		if (serviceId == 0)
			table = "ABSTATS";
		else if (serviceId == 1)
			table = "REDISSTATS";

		String qr = "SELECT REQUESTS FROM "+table+" WHERE PROVIDER="+provId+" AND TS>="+start+" AND TS<="+end;
		ResultSet rs = executeQR(qr);
		try {
			while (rs.next()) 
				reqs += rs.getInt("requests");

		} catch(SQLException e) {
			System.err.println("Query "+qr+" failed");
		}
		return reqs;	
	}
	
	int findRedis95PercPos(String str)
	{
		StringTokenizer strToken = new StringTokenizer(str,",");
		String s = null;
		float percentile = 0;
		int pos = 0;

		while(strToken.hasMoreElements())
		{
			s = strToken.nextToken();
			percentile = Float.parseFloat(s);
			if (percentile > 95)
				return pos;
			pos++;
		}
		return pos;
	}

	
	float[] getResponseTime(int serviceId, int provId, long start, long end)
	{
		float latency = 0;
		int rows = 0;
		Vector<Float> latencies = new Vector<Float>();
		
		
		if (serviceId == 0)
		{
			String qr = "SELECT LATENCIES FROM ABSTATS WHERE PROVIDER="+
					provId+
					" AND TS>="+start+
					" AND TS<="+end;
			ResultSet rs = executeQR(qr);
			try {				
				while (rs.next()) {
					StringTokenizer strToken = new StringTokenizer(rs.getString("LATENCIES"),",");
					String s = null;
					try {
						for (int i=0;i<=5;i++)
							s = strToken.nextToken();
						latency += Float.parseFloat(s);
						latencies.add(latency);
						rows++;
					} catch(NoSuchElementException e) {
						e.printStackTrace();
					}
				}

			} catch(SQLException e) {
				System.err.println("Query "+qr+" failed");
			}
		}
		else if (serviceId == 1)
		{
			String qr = "SELECT GET_PERCENTILES,GET_LATENCIES FROM REDISSTATS WHERE PROVIDER="+
					provId+
					" AND TS>="+start+
					" AND TS<="+end;
			ResultSet rs = executeQR(qr);
			try {
				while (rs.next()) {
					int pos = findRedis95PercPos(rs.getString("get_percentiles"));
					//System.out.println("Position: "+pos);
					StringTokenizer strToken = new StringTokenizer(rs.getString("get_latencies"),",");
					String s = null;
					try {
						for (int i=0;i<=pos;i++)
							s = strToken.nextToken();
						latency += Float.parseFloat(s);
						latencies.add(latency);
						rows++;
					} catch(NoSuchElementException e) {
						e.printStackTrace();
					}
				}

			} catch(SQLException e) {
				System.err.println("Query "+qr+" failed");
			}
		}
		
		float mean = latency/rows;
		float sum = 0;
		for (int i=0;i<latencies.size();i++)
			sum += Math.pow(latencies.get(i).floatValue()-mean, 2);
		
		float std = (float)(1.96*Math.sqrt(sum)/Math.sqrt(rows));
		float[] ret = new float[2];
		ret[0] = mean;
		ret[1] = std;
		
		return ret;	
	}
	
	public static void main(String[] args) {
		PrintStream ab = null;
		PrintStream red = null;
		try {
			ab = new PrintStream(new FileOutputStream("ab_response"));
			red = new PrintStream(new FileOutputStream("red_response"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StatsClient client = new StatsClient();
		int numSlots = 10;
		long slotDuration = 10000l;
		long time = 1489394133649l;
		long endTime = time + numSlots*slotDuration;
		int slotnum = 1;
		
		client.connectDB();
		while (time <= endTime)
		{
			System.out.println("AB Requests "+client.getRequests(0, 1,time, time+slotDuration));
			float[] response = client.getResponseTime(0, 1,time, time+slotDuration);
			System.out.println("AB Response Time "+response[0]);
			ab.println(slotnum+" "+response[0]+" "+response[1]);
			ab.flush();
			response = client.getResponseTime(1, 1,time, time+slotDuration);
			System.out.println("Redis Response Time "+response[0]);
			red.println(slotnum+" "+response[0]+" "+response[1]);
			red.flush();
			time += slotDuration;
			slotnum++;
		}
		client.disConnectDB();
		ab.close();
		red.close();
	}
}
