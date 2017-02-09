/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

/**
 *
 * @author kostas
 */
public class ABStats {

int slot;
int measurement;
String clientName;

String Server_Software;
String Server_Hostname;
String Server_Port;
String Document_Path;
String Document_Length_bytes;
String Concurrency_Level;
String Time_taken_for_tests_seconds;
String Complete_requests;
String Failed_requests_number;
String Failed_requests_Connect;
String Failed_requests_Receive;
String Failed_requests_Length;
String Failed_requests_Exceptions;
String Non_2xx_responses;
String Keep_Alive_requests;
String Total_transferred_bytes;
String HTML_transferred_bytes;
String Requests_per_second_mean;
String Time_per_request_mean;
String Time_per_request_mean_across_all_concurrent_requests;
String Transfer_rate_received;
String Connection_Times_Connect_min;
String Connection_Times_Connect_mean;
String Connection_Times_Connect_sd;
String Connection_Times_Connect_median;
String Connection_Times_Connect_max;
String Connection_Times_Processing_min;
String Connection_Times_Processing_mean;
String Connection_Times_Processing_sd;
String Connection_Times_Processing_median;
String Connection_Times_Processing_max;
String Connection_Times_Waiting_min;
String Connection_Times_Waiting_mean;
String Connection_Times_Waiting_sd;
String Connection_Times_Waiting_median;
String Connection_Times_Waiting_max;
String Connection_Times_Total_min;
String Connection_Times_Total_mean;
String Connection_Times_Total_sd;
String Connection_Times_Total_median;
String Connection_Times_Total_max;
String Percentage_50;
String Percentage_66;
String Percentage_75;
String Percentage_80;
String Percentage_90;
String Percentage_95;
String Percentage_98;
String Percentage_99;
String Percentage_100;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getMeasurement() {
        return measurement;
    }

    public void setMeasurement(int measurement) {
        this.measurement = measurement;
    }

    public String getServer_Software() {
        return Server_Software;
    }

    public void setServer_Software(String Server_Software) {
        this.Server_Software = Server_Software;
    }

    public String getServer_Hostname() {
        return Server_Hostname;
    }

    public void setServer_Hostname(String Server_Hostname) {
        this.Server_Hostname = Server_Hostname;
    }

    public String getServer_Port() {
        return Server_Port;
    }

    public void setServer_Port(String Server_Port) {
        this.Server_Port = Server_Port;
    }

    public String getDocument_Path() {
        return Document_Path;
    }

    public void setDocument_Path(String Document_Path) {
        this.Document_Path = Document_Path;
    }

    public String getDocument_Length_bytes() {
        return Document_Length_bytes;
    }

    public void setDocument_Length_bytes(String Document_Length_bytes) {
        this.Document_Length_bytes = Document_Length_bytes;
    }

    public String getConcurrency_Level() {
        return Concurrency_Level;
    }

    public void setConcurrency_Level(String Concurrency_Level) {
        this.Concurrency_Level = Concurrency_Level;
    }

    public String getTime_taken_for_tests_seconds() {
        return Time_taken_for_tests_seconds;
    }

    public void setTime_taken_for_tests_seconds(String Time_taken_for_tests_seconds) {
        this.Time_taken_for_tests_seconds = Time_taken_for_tests_seconds;
    }

    public String getComplete_requests() {
        return Complete_requests;
    }

    public void setComplete_requests(String Complete_requests) {
        this.Complete_requests = Complete_requests;
    }

    public String getFailed_requests_number() {
        return Failed_requests_number;
    }

    public void setFailed_requests_number(String Failed_requests_number) {
        this.Failed_requests_number = Failed_requests_number;
    }

    public String getFailed_requests_Connect() {
        return Failed_requests_Connect;
    }

    public void setFailed_requests_Connect(String Failed_requests_Connect) {
        this.Failed_requests_Connect = Failed_requests_Connect;
    }

    public String getFailed_requests_Receive() {
        return Failed_requests_Receive;
    }

    public void setFailed_requests_Receive(String Failed_requests_Receive) {
        this.Failed_requests_Receive = Failed_requests_Receive;
    }

    public String getFailed_requests_Length() {
        return Failed_requests_Length;
    }

    public void setFailed_requests_Length(String Failed_requests_Length) {
        this.Failed_requests_Length = Failed_requests_Length;
    }

    public String getFailed_requests_Exceptions() {
        return Failed_requests_Exceptions;
    }

    public void setFailed_requests_Exceptions(String Failed_requests_Exceptions) {
        this.Failed_requests_Exceptions = Failed_requests_Exceptions;
    }

    public String getNon_2xx_responses() {
        return Non_2xx_responses;
    }

    public void setNon_2xx_responses(String Non_2xx_responses) {
        this.Non_2xx_responses = Non_2xx_responses;
    }

    public String getKeep_Alive_requests() {
        return Keep_Alive_requests;
    }

    public void setKeep_Alive_requests(String Keep_Alive_requests) {
        this.Keep_Alive_requests = Keep_Alive_requests;
    }

    public String getTotal_transferred_bytes() {
        return Total_transferred_bytes;
    }

    public void setTotal_transferred_bytes(String Total_transferred_bytes) {
        this.Total_transferred_bytes = Total_transferred_bytes;
    }

    public String getHTML_transferred_bytes() {
        return HTML_transferred_bytes;
    }

    public void setHTML_transferred_bytes(String HTML_transferred_bytes) {
        this.HTML_transferred_bytes = HTML_transferred_bytes;
    }

    public String getRequests_per_second_mean() {
        return Requests_per_second_mean;
    }

    public void setRequests_per_second_mean(String Requests_per_second_mean) {
        this.Requests_per_second_mean = Requests_per_second_mean;
    }

    public String getTime_per_request_mean() {
        return Time_per_request_mean;
    }

    public void setTime_per_request_mean(String Time_per_request_mean) {
        this.Time_per_request_mean = Time_per_request_mean;
    }

    public String getTime_per_request_mean_across_all_concurrent_requests() {
        return Time_per_request_mean_across_all_concurrent_requests;
    }

    public void setTime_per_request_mean_across_all_concurrent_requests(String Time_per_request_mean_across_all_concurrent_requests) {
        this.Time_per_request_mean_across_all_concurrent_requests = Time_per_request_mean_across_all_concurrent_requests;
    }

    public String getTransfer_rate_received() {
        return Transfer_rate_received;
    }

    public void setTransfer_rate_received(String Transfer_rate_received) {
        this.Transfer_rate_received = Transfer_rate_received;
    }

    public String getConnection_Times_Connect_min() {
        return Connection_Times_Connect_min;
    }

    public void setConnection_Times_Connect_min(String Connection_Times_Connect_min) {
        this.Connection_Times_Connect_min = Connection_Times_Connect_min;
    }

    public String getConnection_Times_Connect_mean() {
        return Connection_Times_Connect_mean;
    }

    public void setConnection_Times_Connect_mean(String Connection_Times_Connect_mean) {
        this.Connection_Times_Connect_mean = Connection_Times_Connect_mean;
    }

    public String getConnection_Times_Connect_sd() {
        return Connection_Times_Connect_sd;
    }

    public void setConnection_Times_Connect_sd(String Connection_Times_Connect_sd) {
        this.Connection_Times_Connect_sd = Connection_Times_Connect_sd;
    }

    public String getConnection_Times_Connect_median() {
        return Connection_Times_Connect_median;
    }

    public void setConnection_Times_Connect_median(String Connection_Times_Connect_median) {
        this.Connection_Times_Connect_median = Connection_Times_Connect_median;
    }

    public String getConnection_Times_Connect_max() {
        return Connection_Times_Connect_max;
    }

    public void setConnection_Times_Connect_max(String Connection_Times_Connect_max) {
        this.Connection_Times_Connect_max = Connection_Times_Connect_max;
    }

    public String getConnection_Times_Processing_min() {
        return Connection_Times_Processing_min;
    }

    public void setConnection_Times_Processing_min(String Connection_Times_Processing_min) {
        this.Connection_Times_Processing_min = Connection_Times_Processing_min;
    }

    public String getConnection_Times_Processing_mean() {
        return Connection_Times_Processing_mean;
    }

    public void setConnection_Times_Processing_mean(String Connection_Times_Processing_mean) {
        this.Connection_Times_Processing_mean = Connection_Times_Processing_mean;
    }

    public String getConnection_Times_Processing_sd() {
        return Connection_Times_Processing_sd;
    }

    public void setConnection_Times_Processing_sd(String Connection_Times_Processing_sd) {
        this.Connection_Times_Processing_sd = Connection_Times_Processing_sd;
    }

    public String getConnection_Times_Processing_median() {
        return Connection_Times_Processing_median;
    }

    public void setConnection_Times_Processing_median(String Connection_Times_Processing_median) {
        this.Connection_Times_Processing_median = Connection_Times_Processing_median;
    }

    public String getConnection_Times_Processing_max() {
        return Connection_Times_Processing_max;
    }

    public void setConnection_Times_Processing_max(String Connection_Times_Processing_max) {
        this.Connection_Times_Processing_max = Connection_Times_Processing_max;
    }

    public String getConnection_Times_Waiting_min() {
        return Connection_Times_Waiting_min;
    }

    public void setConnection_Times_Waiting_min(String Connection_Times_Waiting_min) {
        this.Connection_Times_Waiting_min = Connection_Times_Waiting_min;
    }

    public String getConnection_Times_Waiting_mean() {
        return Connection_Times_Waiting_mean;
    }

    public void setConnection_Times_Waiting_mean(String Connection_Times_Waiting_mean) {
        this.Connection_Times_Waiting_mean = Connection_Times_Waiting_mean;
    }

    public String getConnection_Times_Waiting_sd() {
        return Connection_Times_Waiting_sd;
    }

    public void setConnection_Times_Waiting_sd(String Connection_Times_Waiting_sd) {
        this.Connection_Times_Waiting_sd = Connection_Times_Waiting_sd;
    }

    public String getConnection_Times_Waiting_median() {
        return Connection_Times_Waiting_median;
    }

    public void setConnection_Times_Waiting_median(String Connection_Times_Waiting_median) {
        this.Connection_Times_Waiting_median = Connection_Times_Waiting_median;
    }

    public String getConnection_Times_Waiting_max() {
        return Connection_Times_Waiting_max;
    }

    public void setConnection_Times_Waiting_max(String Connection_Times_Waiting_max) {
        this.Connection_Times_Waiting_max = Connection_Times_Waiting_max;
    }

    public String getConnection_Times_Total_min() {
        return Connection_Times_Total_min;
    }

    public void setConnection_Times_Total_min(String Connection_Times_Total_min) {
        this.Connection_Times_Total_min = Connection_Times_Total_min;
    }

    public String getConnection_Times_Total_mean() {
        return Connection_Times_Total_mean;
    }

    public void setConnection_Times_Total_mean(String Connection_Times_Total_mean) {
        this.Connection_Times_Total_mean = Connection_Times_Total_mean;
    }

    public String getConnection_Times_Total_sd() {
        return Connection_Times_Total_sd;
    }

    public void setConnection_Times_Total_sd(String Connection_Times_Total_sd) {
        this.Connection_Times_Total_sd = Connection_Times_Total_sd;
    }

    public String getConnection_Times_Total_median() {
        return Connection_Times_Total_median;
    }

    public void setConnection_Times_Total_median(String Connection_Times_Total_median) {
        this.Connection_Times_Total_median = Connection_Times_Total_median;
    }

    public String getConnection_Times_Total_max() {
        return Connection_Times_Total_max;
    }

    public void setConnection_Times_Total_max(String Connection_Times_Total_max) {
        this.Connection_Times_Total_max = Connection_Times_Total_max;
    }

    public String getPercentage_50() {
        return Percentage_50;
    }

    public void setPercentage_50(String Percentage_50) {
        this.Percentage_50 = Percentage_50;
    }

    public String getPercentage_66() {
        return Percentage_66;
    }

    public void setPercentage_66(String Percentage_66) {
        this.Percentage_66 = Percentage_66;
    }

    public String getPercentage_75() {
        return Percentage_75;
    }

    public void setPercentage_75(String Percentage_75) {
        this.Percentage_75 = Percentage_75;
    }

    public String getPercentage_80() {
        return Percentage_80;
    }

    public void setPercentage_80(String Percentage_80) {
        this.Percentage_80 = Percentage_80;
    }

    public String getPercentage_90() {
        return Percentage_90;
    }

    public void setPercentage_90(String Percentage_90) {
        this.Percentage_90 = Percentage_90;
    }

    public String getPercentage_95() {
        return Percentage_95;
    }

    public void setPercentage_95(String Percentage_95) {
        this.Percentage_95 = Percentage_95;
    }

    public String getPercentage_98() {
        return Percentage_98;
    }

    public void setPercentage_98(String Percentage_98) {
        this.Percentage_98 = Percentage_98;
    }

    public String getPercentage_99() {
        return Percentage_99;
    }

    public void setPercentage_99(String Percentage_99) {
        this.Percentage_99 = Percentage_99;
    }

    public String getPercentage_100() {
        return Percentage_100;
    }

    public void setPercentage_100(String Percentage_100) {
        this.Percentage_100 = Percentage_100;
    }

  




}
