import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

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


	public static void abBenchmark(int requests, int concurrency, int buffer, String url)
	{
		ProcessBuilder pb =
				new ProcessBuilder("ab", "-n", ""+requests,"-c", 
						""+concurrency, "-b", ""+buffer, url);
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
	}

	public static void redisBenchmark(String ip)
	{
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
	}


	public static void main(String[] args){
		abBenchmark(50000,100, 5000, "http://10.95.196.78:80/");
		redisBenchmark("10.95.196.143");

	}
}