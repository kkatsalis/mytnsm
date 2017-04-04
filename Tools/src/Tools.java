import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Tools {


	public static void main(String[] args) {

		int sim_id=1;
		int runs=20;
		int slots=100;
		int providers_number=2;
		//String algorithm="FirstFit";
		String algorithm="FirstFitSRR";

		int[][][] vms_requested=new int[providers_number][runs][slots];
		int[][][] vms_satisfied=new int[providers_number][runs][slots];
		double[][][] benefit=new double[providers_number][runs][slots];

		int[][] average_vms_requested=new int[providers_number][slots];
		int[][] average_vms_satisfied=new int[providers_number][slots];
		double[][] average_benefit=new double[providers_number][slots];

		try {
			String url = "jdbc:sqlite:";
			url+="Sim"+sim_id+"_"+algorithm;			
			String sql = "SELECT * FROM TOTAL";
			Connection conn = DriverManager.getConnection(url);
			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);	            
			int provider_id;
			int r;
			int slot_id;

			while (rs.next()) {
				provider_id=rs.getInt("provider_id");
				r=rs.getInt("run_id");
				slot_id=rs.getInt("slot");

				if(r==runs)
					break;
				
				vms_requested[provider_id][r][slot_id]=rs.getInt("total_all_vms_requested");
				vms_satisfied[provider_id][r][slot_id]=rs.getInt("total_all_vms_activated");
				benefit[provider_id][r][slot_id]=rs.getDouble("benefit");
			}

			int total_requested=0;
			int total_satisfied=0;
			double benefits=0;

			for (int p = 0; p < providers_number; p++) {
				for (int slot = 0; slot < slots; slot++) {
					total_requested=0;
					total_satisfied=0;
					benefits=0;

					for (int i = 0; i < runs; i++) {
						total_requested+= vms_requested[p][i][slot];
						total_satisfied+=vms_satisfied[p][i][slot];
						benefits+=benefit[p][i][slot];
					}
					average_vms_requested[p][slot]=total_requested/runs;
					average_vms_satisfied[p][slot]=total_satisfied/runs;
					average_benefit[p][slot]=benefits/runs;
				}
			}

			conn.close();
			System.out.println("Values retrieved");
			
			String url2 = "jdbc:sqlite:";

			url2+="Sim"+sim_id+"_"+algorithm;			
			Connection conn2 = DriverManager.getConnection(url2);

			String sql2= "INSERT INTO AVERAGES(sim_id, slot,algorithm,provider_id,avg_vms_requested,avg_vms_allocated,avg_benefit) VALUES(?,?,?,?,?,?,?)";

			for (int p = 0; p < providers_number; p++) {
				for (int slot = 0; slot < slots; slot++) {

					PreparedStatement pstmt = conn2.prepareStatement(sql2); 
					pstmt.setInt(1, sim_id);
					pstmt.setInt(2, slot);
					pstmt.setString(3, algorithm);
					pstmt.setInt(4, p);
					pstmt.setInt(5, average_vms_requested[p][slot]);
					pstmt.setInt(6, average_vms_satisfied[p][slot]);
					pstmt.setDouble(7, average_benefit[p][slot]);

					pstmt.executeUpdate();
				}
			}
			conn2.close();
			
			System.out.println("Averages calculated");
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
}
