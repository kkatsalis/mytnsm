/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

import java.util.ArrayList;
import java.util.List;
import omlBasePackage.OMLBase;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import omlBasePackage.OmlMP;

/**
 *
 * @author kostas
 */
public class DBClass {

    OMLBase omlclient;

    ArrayList<OMLMPFieldDef> hostStatsSchema;
    ArrayList<OMLMPFieldDef> vmStatsSchema;
    ArrayList<OMLMPFieldDef> vmInterfacesStatsSchema;
    ArrayList<OMLMPFieldDef> hostInterfacesStatsSchema;
    
    ArrayList<OMLMPFieldDef> simulatorStatsSchema;
    ArrayList<OMLMPFieldDef> webClientABStatsSchema;
    ArrayList<OMLMPFieldDef> webClientServiceStatsSchema;
    ArrayList<OMLMPFieldDef> webClientServiceStatsSlotSchema;
    
    OmlMP mp_hostStats;
    OmlMP mp_hostIinterfaceStats;
    OmlMP mp_vmStats;
    OmlMP mp_vmIinterfaceStats;
    OmlMP mp_simulatorStats;
    OmlMP mp_webClientABStats;
    OmlMP mp_webClientServiceStats;
    OmlMP mp_webClientServiceStatsSlot;
    
    //(String oml_app_name, String oml_exp_id, String oml_name, String oml_server)            
    public DBClass(int simID, String algorithm) {

        System.out.println("********** DB Initialization Phase ****************");

        String oml_exp_id = "katsalis_cloud_sim_" + String.valueOf(simID);
        String oml_name = "katsalis_cloud_sim_" + String.valueOf(simID);

        omlclient = new OMLBase("kkatsalis", oml_exp_id, oml_name, "tcp:nitlab.inf.uth.gr:3003");

        // Activate in real system
        initiliazeHostDBTableSchema();
        initiliazeHostInterfaceDBTableSchema();
        initiliazeVMInterfaceDBTableSchema();
        initiliazeVMBTableSchema();
        initiliazeWebClientABTableSchema();

        initiliazeWebClientServiceTableSchema();
        initiliazeWebClientServiceSlotTableSchema();
        initializeSimulatorStats();

        mp_hostStats = new OmlMP(hostStatsSchema);
        mp_hostIinterfaceStats = new OmlMP(hostInterfacesStatsSchema);
        mp_vmStats = new OmlMP(vmStatsSchema);
        mp_vmIinterfaceStats = new OmlMP(vmInterfacesStatsSchema);
        mp_webClientABStats = new OmlMP(webClientABStatsSchema);

        mp_webClientServiceStats = new OmlMP(webClientServiceStatsSchema);
        mp_webClientServiceStatsSlot = new OmlMP(webClientServiceStatsSlotSchema);
        mp_simulatorStats = new OmlMP(simulatorStatsSchema);

        /* Activate in real system
         omlclient.addmp("hostStatsTable", mp_hostStats); 
         omlclient.addmp("hostInterfaceStatsTable",mp_hostIinterfaceStats);
         omlclient.addmp("vmStatsTable",mp_vmStats );
         omlclient.addmp("vmInterfaceStatsTable",mp_vmIinterfaceStats);
         omlclient.addmp("webClientABStatsTable",mp_webClientABStats);
         */
        String tableName;

        tableName = "sim" + simID + "_" + algorithm + "_webClientStats";
        omlclient.addmp(tableName, mp_webClientServiceStats);

        tableName = "sim" + simID + "_" + algorithm + "_simStats";
        omlclient.addmp(tableName, mp_simulatorStats);
        
         tableName = "sim" + simID + "_" + algorithm + "_webClientStatsSlot";
         omlclient.addmp(tableName, mp_webClientServiceStatsSlot);

        try {
            omlclient.start();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("********** End of DB Initialization Phase ****************");
        System.out.println();

    }

    private void initiliazeHostDBTableSchema() {

        hostStatsSchema = new ArrayList<>();
        hostStatsSchema.add(new OMLMPFieldDef("Slot", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Measurement", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Time", OMLTypes.OML_LONG_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Hostname", OMLTypes.OML_STRING_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Arch", OMLTypes.OML_STRING_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Physical_CPUs", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Count", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Running", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Blocked", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Paused", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Shutdown", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Shutoff", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Crashed", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Active", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Inactive", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("CPU_percentage", OMLTypes.OML_DOUBLE_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Total_hardware_memory_KB", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Total_memory_KB", OMLTypes.OML_INT32_VALUE));
        hostStatsSchema.add(new OMLMPFieldDef("Total_guest_memory_KB", OMLTypes.OML_INT32_VALUE));

        System.out.println("DB message (1 of 5): Host Stats Schema Created");
    }

    private void initiliazeHostInterfaceDBTableSchema() {

        hostInterfacesStatsSchema = new ArrayList<>();

        hostInterfacesStatsSchema.add(new OMLMPFieldDef("Slot", OMLTypes.OML_INT32_VALUE));
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("Measurement", OMLTypes.OML_INT32_VALUE)); // Measurement per slot
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("HostName", OMLTypes.OML_STRING_VALUE));
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("Interface", OMLTypes.OML_STRING_VALUE));
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("TimeStamp", OMLTypes.OML_LONG_VALUE));
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("Kbps_in", OMLTypes.OML_DOUBLE_VALUE));
        hostInterfacesStatsSchema.add(new OMLMPFieldDef("Kbps_out", OMLTypes.OML_DOUBLE_VALUE));

        System.out.println("DB message (2 of 5): Host Interfaces Stats Schema Created");
    }

    private void initiliazeVMBTableSchema() {

        vmStatsSchema = new ArrayList<>();

        vmStatsSchema.add(new OMLMPFieldDef("Slot", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Measurement", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("HostName", OMLTypes.OML_STRING_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Domain_ID", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Domain_name", OMLTypes.OML_STRING_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("CPU_ns", OMLTypes.OML_DOUBLE_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("CPU_percentage", OMLTypes.OML_DOUBLE_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Mem_bytes", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Mem_percentage", OMLTypes.OML_DOUBLE_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Block_RDRQ", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Block_WRRQ", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Net_RXBY", OMLTypes.OML_INT32_VALUE));
        vmStatsSchema.add(new OMLMPFieldDef("Net_TXBY", OMLTypes.OML_INT32_VALUE));

        System.out.println("DB message (3 of 5): VM Stats Schema Created");
    }

    private void initiliazeVMInterfaceDBTableSchema() {

        vmInterfacesStatsSchema = new ArrayList<>();

        vmInterfacesStatsSchema.add(new OMLMPFieldDef("Slot", OMLTypes.OML_INT32_VALUE));
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("Measurement", OMLTypes.OML_INT32_VALUE)); // Measurement per slot
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("VMName", OMLTypes.OML_STRING_VALUE));
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("Interface", OMLTypes.OML_STRING_VALUE));
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("TimeStamp", OMLTypes.OML_LONG_VALUE));
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("Kbps_in", OMLTypes.OML_DOUBLE_VALUE));
        vmInterfacesStatsSchema.add(new OMLMPFieldDef("Kbps_out", OMLTypes.OML_DOUBLE_VALUE));

        System.out.println("DB message (4 of 5): VM Interfaces Stats Schema Created");
    }

    private void initiliazeWebClientABTableSchema() {

        webClientABStatsSchema = new ArrayList<>();

        webClientABStatsSchema.add(new OMLMPFieldDef("Slot", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Measurement", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("WebClient", OMLTypes.OML_STRING_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Server_Software", OMLTypes.OML_STRING_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Server_Hostname", OMLTypes.OML_STRING_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Document_Path", OMLTypes.OML_STRING_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Server_Port", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Document_Length_bytes", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Concurrency_Level", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Complete_requests", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Failed_requests_number", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Failed_requests_Connect", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Failed_requests_Receive", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Failed_requests_Length", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Failed_requests_Exceptions", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Non_2xx_responses", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Keep_Alive_requests", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Total_transferred_bytes", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("HTML_transferred_bytes", OMLTypes.OML_INT32_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Time_taken_for_tests_seconds", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Requests_per_second_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Time_per_request_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Time_per_request_mean_across_all_concurrent_requests", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Transfer_rate_received", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Connect_min", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Connect_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Connect_sd", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Connect_median", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Connect_max", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Processing_min", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Processing_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Processing_sd", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Processing_median", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Processing_max", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Waiting_min", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Waiting_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Waiting_sd", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Waiting_median", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Waiting_max", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Total_min", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Total_mean", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Total_sd", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Total_median", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Connection_Times_Total_max", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_50", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_66", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_75", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_80", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_90", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_95", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_98", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_99", OMLTypes.OML_DOUBLE_VALUE));
        webClientABStatsSchema.add(new OMLMPFieldDef("Percentage_100", OMLTypes.OML_DOUBLE_VALUE));

        System.out.println("DB message (5 of 5): Web Client AB Stats Schema Created");
    }

    private void initiliazeWebClientServiceTableSchema() {

        webClientServiceStatsSchema = new ArrayList<>();

        webClientServiceStatsSchema.add(new OMLMPFieldDef("simulationID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("runID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("algorithm", OMLTypes.OML_STRING_VALUE));

        webClientServiceStatsSchema.add(new OMLMPFieldDef("slot", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("clientID", OMLTypes.OML_STRING_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("providerID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("serviceID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("responseTime", OMLTypes.OML_DOUBLE_VALUE));
        webClientServiceStatsSchema.add(new OMLMPFieldDef("type", OMLTypes.OML_STRING_VALUE));

        System.out.println("DB message: initiliazeWebClientServiceTableSchema Called");
    }

    private void initializeSimulatorStats() {

        simulatorStatsSchema = new ArrayList<>();

        simulatorStatsSchema.add(new OMLMPFieldDef("simulationID", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("runID", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("algorithm", OMLTypes.OML_STRING_VALUE));

        simulatorStatsSchema.add(new OMLMPFieldDef("slot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("providerID", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("netBenefit", OMLTypes.OML_DOUBLE_VALUE));

        // From the beggining of time
        simulatorStatsSchema.add(new OMLMPFieldDef("vmsRequested", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("vmsSatisfied", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("vmsDeleted", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("smallVmsRequested", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("smallVmsSatisfied", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("mediumVmsRequested", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("mediumVmsSatisfied", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("largeVmsRequested", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("largeVmsSatisfied", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("numberOfActiveVMs", OMLTypes.OML_INT32_VALUE));
        // Fom this slot
        simulatorStatsSchema.add(new OMLMPFieldDef("activeVMsSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("vmsRequestedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("vmsSatisfiedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("smallVmsRquestedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("smallVmsSatisfiedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("mediumVmsRequestedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("mediumVmsSatisfiedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("largeVmsRequestedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("largeVmsSatisfiedSlot", OMLTypes.OML_INT32_VALUE));
        simulatorStatsSchema.add(new OMLMPFieldDef("numberOfActiveVMsSlot", OMLTypes.OML_INT32_VALUE));

        System.out.println("DB message: initializeSimulatorStats Called");
    }

    private void initiliazeWebClientServiceSlotTableSchema() {

        webClientServiceStatsSlotSchema = new ArrayList<>();

        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("simulationID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("runID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("algorithm", OMLTypes.OML_STRING_VALUE));

        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("slot", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("providerID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("serviceID", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("requestsNumber", OMLTypes.OML_INT32_VALUE));
        webClientServiceStatsSlotSchema.add(new OMLMPFieldDef("responseTime", OMLTypes.OML_DOUBLE_VALUE));

        System.out.println("DB message: initiliazeWebClientServiceSlotTableSchema Called");

    }

    public OMLBase getOmlclient() {
        return omlclient;
    }

    public OmlMP getMp_hostStats() {
        return mp_hostStats;
    }

    public OmlMP getMp_hostIinterfaceStats() {
        return mp_hostIinterfaceStats;
    }

    public OmlMP getMp_vmStats() {
        return mp_vmStats;
    }

    public OmlMP getMp_vmIinterfaceStats() {
        return mp_vmIinterfaceStats;
    }

    public OmlMP getMp_webClientABStats() {
        return mp_webClientABStats;
    }

    public OmlMP getMp_webClientServiceStats() {
        return mp_webClientServiceStats;
    }

    public OmlMP getMp_simulatorStats() {
        return mp_simulatorStats;
    }

    public OmlMP getMp_webClientServiceStatsSlot() {
        return mp_webClientServiceStatsSlot;
    }
    
    
}
