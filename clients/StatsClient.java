import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.sql.ResultSet;

public class StatsClient {
	Connection conn = null;
	String provider;
	String client_id;
	float request_rate;
	int requests;
	int concurrency;
	String url;

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
		String providerId = "vodafone";

		if (serviceId == 0)
			table = "ABSTATS";
		else if (serviceId == 1)
			table = "REDISSTATS";

		String qr = "SELECT REQUESTS FROM "+table+" WHERE PROVIDER='"+providerId+"' AND TS>="+start+" AND TS<="+end;
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

	
	float getResponseTime(int serviceId, int provId, long start, long end)
	{
		float latency = 0;
		int rows = 0;
		String providerId = "vodafone";
		
		
		if (serviceId == 0)
		{
			String qr = "SELECT LATENCIES FROM ABSTATS WHERE PROVIDER='"+
					providerId+
					"' AND TS>="+start+
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
			String qr = "SELECT GET_PERCENTILES,GET_LATENCIES FROM REDISSTATS WHERE PROVIDER='"+
					providerId+
					"' AND TS>="+start+
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
						rows++;
					} catch(NoSuchElementException e) {
						e.printStackTrace();
					}
				}

			} catch(SQLException e) {
				System.err.println("Query "+qr+" failed");
			}
		}
		return latency/rows;	
	}
	
	public static void main(String[] args) {
		StatsClient client = new StatsClient();
		client.connectDB();
		System.out.println("AB Requests "+client.getRequests(0, 0,1488984281769l, 1488984281769l));
		System.out.println("Redis Requests "+client.getRequests(1, 0,1488984284308l, 1488984284469l));
		System.out.println("AB Response Time "+client.getResponseTime(0, 0,1488984281751l, 1488984281769l));
		System.out.println("Redis Response Time "+client.getResponseTime(1, 0,1488984284308l, 1488984284469l));
	}
}
