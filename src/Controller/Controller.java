/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package Controller;

import Cplex.CplexResponse;
import Enumerators.EMachineTypes;
import Enumerators.ESlotDurationMetric;
import Statistics.DBClass;
import Statistics.DBUtilities;
import Utilities.WebUtilities;
import Statistics.VMStats;
import Statistics.HostStats;
import Statistics.NetRateStats;
import Cplex.Scheduler;
import Cplex.SchedulerData;
import Enumerators.EAlgorithms;
import Enumerators.EStatsUpdateMethod;
import Statistics.SimulatorStats;
import Utilities.SimWebRequestUtilities;
import Utilities.Utilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;
import omlBasePackage.OMLMPFieldDef;
import omlBasePackage.OMLTypes;
import org.json.JSONException;

/**
 *
 * @author kostas
 */
public class Controller {
    
    Configuration _config;
    Slot[] _slots;
    
    Host[] _hosts;
    WebClient[] _clients;
    
    HostStats[] _hostStats;
    List<VMStats> _activeVMStats;
    
    WebUtilities _webUtilities;
    DBUtilities _dbUtilities;
    
    Timer _machineStatsTimer;
    int _numberOfMachineStatsPerSlot=0;
    int _currentInstance=0;
    int vmIDs=0;
    
    SchedulerData _cplexData;
    Scheduler scheduler;
    Provider[] _provider;
    
    
    int[] vmsRequested;
    int[] vmsSatisfied;
    int[] vmsDeleted;
    int[] smallVmsRequested;
    int[] smallVmsSatisfied;
    int[] mediumVmsRequested;
    int[] mediumVmsSatisfied;
    int[] largeVmsRequested;
    int[] largeVmsSatisfied;
    
    int[][][][] vmDeactivationMatrix;
    int[][] _webRequestPattern;
    
    Controller(Host[] hosts,WebClient[] clients, Configuration config, Slot[] slots, DBUtilities dbUtilities,Provider[] _provider) {
        
        this._config=config;
        this._slots=slots;
        this._hosts=hosts;
        this._clients=clients;
        this._webUtilities=new WebUtilities(config);
        this._dbUtilities=dbUtilities;
        this._hostStats=new HostStats[_config.getHostsNumber()];
        this._activeVMStats=new ArrayList<>();
        this._numberOfMachineStatsPerSlot=_config.getNumberOfMachineStatsPerSlot();
        this._cplexData=new SchedulerData(config);
        this._provider =_provider;
        
        initializeArrays();
        _cplexData.initializeWebRequestMatrix(_webRequestPattern);
        
        
    }
    
    void Run(int slot) throws IOException {
        
        System.out.println("******* -- Slot:"+slot+" Controller Run*******");
        
        SimulatorStats simulatorStatistics=new SimulatorStats(_config.getProvidersNumber());
        scheduler=new Scheduler(_config);
        
        if(false)
            startNodesStatsTimer(slot); // for Statistics updates
        
        try {
            
            // ----------- Load VM Request Lists
            for (int p = 0; p < _config.getProvidersNumber(); p++) {
                for (ServiceRequest serviceRequest :  _slots[slot].getServiceRequests2Activate()[p]) {
                    addVMsPerService(serviceRequest,slot);
                }
            }
            
            // ----------- Load VM Request Matrix
            int[][][] vmRequestMatrix=loadVMRequestMatrix(slot);             //requestMatrix[v][s][p]
           // System.out.println(Arrays.deepToString(vmRequestMatrix));
           
            
            // ----------- Load VM Deactivation Matrix
            prepareVmDeactivationMatrix(slot,vmDeactivationMatrix,simulatorStatistics);
            // System.out.println(Arrays.deepToString(vmDeactivationMatrix));
            
            // -----------delete VMs
            deleteVMs(slot);
            
            // -----------Update WebRequest Pattern
           // int[][] requestPattern=Utilities.findRequestPattern(_config);
            
            // -----------Update Cplex data Parameters
            _cplexData.updateParameters(_webRequestPattern, vmRequestMatrix, vmDeactivationMatrix);
            
                        
            // ----------- Run Cplex
            int[][][][] activationMatrix=new int[_cplexData.N][_cplexData.P][_cplexData.V][_cplexData.S];
            
            if(_slots[slot].getVmRequests2Activate().length>0){
                
                if((_config.getAlgorithm()).equals(EAlgorithms.FF.toString()))
                    activationMatrix=scheduler.RunFF(_cplexData);
                else if((_config.getAlgorithm()).equals(EAlgorithms.FFRR.toString()))
                    activationMatrix=scheduler.RunFFRR(_cplexData);
                else if((_config.getAlgorithm()).equals(EAlgorithms.FFRandom.toString()))
                    activationMatrix=scheduler.RunFF_Random(_cplexData);
                else 
                    activationMatrix=scheduler.RunLyapunov(_cplexData);
                
            }
           
            
            scheduler.updateData(_cplexData, activationMatrix);
            CplexResponse cplexResponse=updatePenaltyAndUtility(_cplexData, activationMatrix);
            
            // ----------- Update Statistics Object
            double netBenefit=cplexResponse.getNetBenefit();
            updateDbStatisticsObject(vmRequestMatrix,activationMatrix,netBenefit,simulatorStatistics,slot,_cplexData);
            
            for (int i = 0; i < _config.getProvidersNumber(); i++) {
                _dbUtilities.updateSimulatorStatistics2DB(slot,simulatorStatistics,i);
            }
            
            
            // ----------- Create VMs (Actual and Objects)
            VMRequest request=null;
            LoadVM loadObject;
            Thread thread;
            
            
            for (int i = 0; i < _config.getHostsNumber(); i++) {
                List<VMRequest> vm2CreatePerHost=cplexSolution2VMRequets(slot, i,activationMatrix);
                
                System.out.println("MESSAGE: Host:"+i+" Bring up: "+vm2CreatePerHost.size()+"VMs");
                
                for (Iterator iterator = vm2CreatePerHost.iterator(); iterator.hasNext();) {
                    request = (VMRequest) iterator.next();
                    
                    loadObject=new LoadVM(slot,request,i,_hosts[i].getNodeName());
                    thread = new Thread(loadObject);
                    thread.start();
                    Thread.sleep(0);
                    //Thread.sleep(5000);
                    
                }
                
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // n[i][j][v][s]: # of allocated VMs of v v for service s of provider j at AP i
    private List<VMRequest> cplexSolution2VMRequets(int slot, int hostID, int[][][][] activationMatrix){
        
        int vms2Load=-1;
        
        List<VMRequest> vmList2Create=new ArrayList<>();
        List<VMRequest> listOfRequestedVMs;
        
        String _vmType="";
        
        for (int p = 0; p < _config.getProvidersNumber(); p++) {
            
            listOfRequestedVMs=_slots[slot].getVmRequests2Activate()[p];
            
            for (int v = 0; v < _config.getVmTypesNumber(); v++) {
                
                if(v==0)
                    _vmType=EMachineTypes.small.toString();
                else if(v==1)
                    _vmType=EMachineTypes.medium.toString();
                else if(v==2)
                    _vmType=EMachineTypes.large.toString();
                
                for (int s = 0; s < _config.getServicesNumber(); s++) {
                    
                    vms2Load=activationMatrix[hostID][p][v][s];
                    
                    for(Iterator iterator = listOfRequestedVMs.iterator(); iterator.hasNext();) {
                        VMRequest nextRequest = (VMRequest)iterator.next();
                        
                        if(nextRequest.getServiceID()==s&&nextRequest.getVmType().equals(_vmType))
                        {
                            while(vms2Load>0){
                                vmList2Create.add(nextRequest);
                                vms2Load--;
                            }
                        }
                        
                    }
                }
            }
            
        }
        
        // Returns A list of VMs to activate for a specific host
        return vmList2Create;
    }
    
    private void deleteVMs(int slot) throws IOException, InterruptedException {
        
        List<Integer> requestID2RemoveThisSlot=new ArrayList<>();
        
        // Step 1: Find RequestIDs to remove
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            for (int j = 0; j < _slots[slot].getVmRequests2Remove()[i].size(); j++) {
                requestID2RemoveThisSlot.add(_slots[slot].getVmRequests2Remove()[i].get(j).getVmID());
            }
        }
        
        // Step 2: Find the VMs based on the vmID to remove and Update Host object
        
        for (int i = 0; i < _config.getHostsNumber(); i++) {
            for (int j = 0; j < _hosts[i].getVMs().size(); j++) {
                if(requestID2RemoveThisSlot.contains(_hosts[i].getVMs().get(j).getVmReuestId())){
                    _hosts[i].getVMs().get(j).setSlotDeactivated(slot);
                    _hosts[i].getVMs().get(j).setActive(false);
                }
            }
        }
        
        /* Block To Activate in real system
        String hostName="";
        String vmName="";
        
        Thread thread;
        //Step 3: Delete the VM
        for (int i = 0; i < _config.getHostsNumber(); i++) {
        hostName=_hosts[i].getNodeName();
        
        for (int j = 0; j < _hosts[i].getVMs().size(); j++) {
        
        if(requestID2RemoveThisSlot.contains(_hosts[i].getVMs().get(j).getVmReuestId())&!_hosts[i].getVMs().get(j).isActive()){
        vmName=_hosts[i].getVMs().get(j).getVmName();
        
        
        
        DeleteVM deleter=new DeleteVM(vmName,hostName);
        thread=new Thread(deleter);
        thread.start();
        Thread.sleep(5000);
        
        
        
        }
        }
        }*/
        
        System.out.println("Method Call: Delete VMs Called");
    }
    
    private  CplexResponse updatePenaltyAndUtility(SchedulerData data, int[][][][] activationMatrix){
    
        double netBenefit=0;
        double penalty=0;
        double utility=0;
     
        for (int i = 0; i < data.N; i++) 
        	for (int j=0;j < data.P; j++)
        		for (int s=0;s < data.S; s++)
        			for (int v=0;v < data.V; v++)
        				utility += activationMatrix[i][j][v][s]*data.w[v];
        
        System.out.println(Arrays.deepToString(activationMatrix));
        
        penalty=0; //Cost

        for (int j=0;j < data.P; j++)
        {  
        	
        	for (int s=0;s < data.S; s++)
        	{
        		double temp =0;
        		for (int v=0;v < data.V; v++)
        			for (int i = 0; i < data.N; i++)
        				temp += data.n[i][j][v][s]*data.ksi(s, j, v);
        		
        		penalty +=Math.max((data.r[j][s]-temp),0)*data.pen[j][s];
        	}
        } 
    	 
        netBenefit = utility - penalty;

         System.out.println();
         System.out.println();
         System.out.println();
         
        System.out.println("**** utility: "+utility);
        System.out.println();
        System.out.println();
        System.out.println();
        CplexResponse response =new CplexResponse(activationMatrix, netBenefit, utility, penalty);
    
        return response;
    }
    
    
    
    
    private int[][][] loadVMRequestMatrix(int slot) {
        
        int[][][] vmRequestMatrix=new int[_config.getProvidersNumber()][_config.getVmTypesNumber()][_config.getServicesNumber()];//: # of allocated VMs of v v for service s of provider j at AP i
        List<VMRequest> listOfRequestedVMs=null;
        int v=-1;
        int s=-1;
        
        for (int p = 0; p < _config.getProvidersNumber(); p++) {
            
            listOfRequestedVMs=_slots[slot].getVmRequests2Activate()[p];
            
            for (VMRequest nextRequest : listOfRequestedVMs) {
                
                if(EMachineTypes.small.toString().equals(nextRequest.getVmType()))
                    v=0;
                else if(EMachineTypes.medium.toString().equals(nextRequest.getVmType()))
                    v=1;
                else if(EMachineTypes.large.toString().equals(nextRequest.getVmType()))
                    v=2;
                
                s=nextRequest.getServiceID();
                
                
                vmRequestMatrix[p][v][s]++;
            }
            
        }
        
        return vmRequestMatrix;
    }
    
    private void prepareVmDeactivationMatrix(int slot,int[][][][] deactivationMatrix,SimulatorStats simulatorStatistics) {
        
        int N=_config.getHostsNumber();
        int P=_config.getProvidersNumber();
        int V=_config.getVmTypesNumber();
        int S=_config.getServicesNumber();
        
        List<VMRequest> vmRequests2RemoveThisSlot=Utilities.findVMequests2RemoveThisSlot(slot,_slots,_config);
        
        for (int i=0;i<N;i++)
            for (int j=0;j<P;j++)
                for (int v=0;v<V;v++)
                    for (int s=0;s<S;s++)
                    {
                        deactivationMatrix[i][j][v][s]=0;
                    }
        
        
        int hostID=-1;
        int providerID=-1;
        int vmTypeID=-1;
        int serviceID=-1;
        
        for (Iterator iterator = vmRequests2RemoveThisSlot.iterator(); iterator.hasNext();) {
            VMRequest next = (VMRequest)iterator.next();
            
            for (int i = 0; i < _hosts.length; i++) {
                for (int j = 0; j < _hosts[i].getVMs().size(); j++) {
                    
                    if(_hosts[i].getVMs().get(j).getVmReuestId()==next.getVmID()&&_hosts[i].getVMs().get(j).isActive()){
                        
                        hostID=i;
                        providerID=next.getProviderID();
                        serviceID=next.getServiceID();
                        
                        switch (next.getVmType()){
                            case "small":
                                vmTypeID=0;
                                break;
                            case "medium":
                                vmTypeID=1;
                                break;
                            case "large":
                                vmTypeID=2;
                                break;
                        }
                        
                        
                        deactivationMatrix[hostID][providerID][vmTypeID][serviceID]++;
                        vmsDeleted[providerID]++;
                        
                    }
                    
                    
                }
                
            }
            
            
        }
        
        
    }
    
    private int[][][][] tempScheduler(double[][][] vmRequestMatrix) {
        // activationMatrix[i][j][v][s]: # of allocated VMs of v v for service s of provider j at AP i
        //requestMatrix[v][s][p]
        
        int[][][][] activationMatrix=new int[_config.getHostsNumber()][_config.getProvidersNumber()][_config.getVmTypesNumber()][_config.getServicesNumber()];
        
        for (int j = 0; j < _config.getProvidersNumber(); j++) {
            for (int v = 0; v < _config.getVmTypesNumber(); v++) {
                for (int s = 0; s < _config.getServicesNumber(); s++) {
                    activationMatrix[0][j][v][s]=(int)vmRequestMatrix[v][s][j];
                    
                }
            }
            
        }
        
        return activationMatrix;
        
        
        
    }
    
    private void updateDbStatisticsObject(int[][][] vmRequestMatrix, int[][][][] activationMatrix,double netBenefit,SimulatorStats simulatorStatistics,int slot, SchedulerData data) {
        
        int smallVMsRequestedSlot=0;
        int smallVMsSatisfiedSlot=0;
        int mediumVMsRequestedSlot=0;
        int mediumVMsSatisfiedSlot=0;
        int largeVMsRequestedSlot=0;
        int largeVMsSatisfiedSlot=0;
        
        int numberOfRequests=0;
        int totalNumberOfRequestsRequestedSlot=0;
        int totalNumberOfRequestsSatisfiedSlot=0;
        
        // Update Requested
        for (int p = 0; p < _config.getProvidersNumber(); p++) {
            smallVMsRequestedSlot=0;
            mediumVMsRequestedSlot=0;
            largeVMsRequestedSlot=0;
            totalNumberOfRequestsRequestedSlot=0;
            
            for (int v = 0; v < _config.vmTypesNumber; v++) {
                for (int s = 0; s < _config.getServicesNumber(); s++) {
                    numberOfRequests=vmRequestMatrix[p][v][s];
                    
                    if(v==0)
                        smallVMsRequestedSlot+=numberOfRequests;
                    else if(v==1)
                        mediumVMsRequestedSlot+=numberOfRequests;
                    else if(v==2)
                        largeVMsRequestedSlot+=numberOfRequests;
                    
                    totalNumberOfRequestsRequestedSlot+=numberOfRequests;
                }
            }
           
            simulatorStatistics.getSmallVmsRequestedSlot()[p]=smallVMsRequestedSlot;
            simulatorStatistics.getMediumVmsRequestedSlot()[p]=mediumVMsRequestedSlot;
            simulatorStatistics.getLargeVmsRequestedSlot()[p]=largeVMsRequestedSlot;
            simulatorStatistics.getVmsRequestedSlot()[p]=totalNumberOfRequestsRequestedSlot;
            
            vmsRequested[p]+=totalNumberOfRequestsRequestedSlot;
            smallVmsRequested[p]+=smallVMsRequestedSlot;
            mediumVmsRequested[p]+=mediumVMsRequestedSlot;
            largeVmsRequested[p]+=largeVMsRequestedSlot;
            
            simulatorStatistics.getVmsRequested()[p]=vmsRequested[p];
            simulatorStatistics.getSmallVmsRequested()[p]=smallVmsRequested[p];
            simulatorStatistics.getMediumVmsRequested()[p]=mediumVmsRequested[p];
            simulatorStatistics.getLargeVmsRequested()[p]= largeVmsRequested[p];
            
        }
        
        // Update Satisfied
        
        for (int p = 0; p < _config.getProvidersNumber(); p++) {
            smallVMsSatisfiedSlot=0;
            mediumVMsSatisfiedSlot=0;
            largeVMsSatisfiedSlot=0;
            totalNumberOfRequestsSatisfiedSlot=0;
            
            for (int i = 0; i < _hosts.length; i++) {
                for (int v = 0; v < _config.vmTypesNumber; v++) {
                    for (int s = 0; s < _config.getServicesNumber(); s++) {
                        numberOfRequests=activationMatrix[i][p][v][s];
                        totalNumberOfRequestsSatisfiedSlot+=numberOfRequests;
                        if(v==0)
                            smallVMsSatisfiedSlot+=numberOfRequests;
                        else if(v==1)
                            mediumVMsSatisfiedSlot+=numberOfRequests;
                        else if(v==2)
                            largeVMsSatisfiedSlot+=numberOfRequests;
                    }
                }
                
            }
            
            simulatorStatistics.getVmsSatisfiedSlot()[p]=totalNumberOfRequestsSatisfiedSlot;
            simulatorStatistics.getSmallVmsSatisfiedSlot()[p]=smallVMsSatisfiedSlot;
            simulatorStatistics.getMediumVmsSatisfiedSlot()[p]=mediumVMsSatisfiedSlot;
            simulatorStatistics.getLargeVmsSatisfiedSlot()[p]=largeVMsSatisfiedSlot;
            
            vmsSatisfied[p]+=totalNumberOfRequestsSatisfiedSlot;
            smallVmsSatisfied[p]+=smallVMsSatisfiedSlot;
            mediumVmsSatisfied[p]+=mediumVMsSatisfiedSlot;
            largeVmsSatisfied[p]+=largeVMsSatisfiedSlot;
            
            simulatorStatistics.getVmsSatisfied()[p]=vmsSatisfied[p];
            simulatorStatistics.getSmallVmsSatisfied()[p]=smallVmsSatisfied[p];
            simulatorStatistics.getMediumVmsSatisfied()[p]=mediumVmsSatisfied[p];
            simulatorStatistics.getLargeVmsSatisfied()[p]= largeVmsSatisfied[p];
            
            simulatorStatistics.setNetBenefit(netBenefit);
            simulatorStatistics.setSlot(slot);
        }
        
        // VMs deleted
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            simulatorStatistics.getVmsDeleted()[i]=vmsDeleted[i];
         }
        
        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            simulatorStatistics.getActiveVMsSlot()[i]=findActiveVMs(i, data);
            
        }
    }
    
    private void initializeArrays() {
        
        int N=_config.getHostsNumber();
        int P=_config.getProvidersNumber();
        int V=_config.getVmTypesNumber();
        int S=_config.getServicesNumber();
        
        //Deactivation matrix
        vmDeactivationMatrix = new int [N][P][V][S]; // D[i][j][v][s]: # of removed VMs of v v for service s of provider j from AP i
        
        //WebRequest matrix
        _webRequestPattern=new int[P][S];
        for (int p = 0; p < P; p++) {
            for (int s = 0; s < S; s++) {
                _webRequestPattern[p][s]=(int)_provider[p].getRequestsForService().get(s).getNumberOfRequests();
            }
        }
        
        
        //VM Statistics Matrices
        vmsRequested=new int[P];
        vmsSatisfied=new int[P];
        vmsDeleted=new int[P];
        
        smallVmsRequested=new int[P];
        smallVmsSatisfied=new int[P];
        mediumVmsRequested=new int[P];
        mediumVmsSatisfied=new int[P];
        largeVmsRequested=new int[P];
        largeVmsSatisfied=new int[P];
        
        
        for (int i = 0; i < P; i++) {
            vmsRequested[i]=0;
            vmsSatisfied[i]=0;
            vmsDeleted[i]=0;
            smallVmsRequested[i]=0;
            smallVmsSatisfied[i]=0;
            mediumVmsRequested[i]=0;
            mediumVmsSatisfied[i]=0;
            largeVmsRequested[i]=0;
            largeVmsSatisfied[i]=0;
        }
        
    }
    
    
    class DeleteVM implements Runnable{
        
        private Thread _thread;
        private String threadName;
        private String hostName;
        private String vmName;
        
        public boolean deleted=false;
        
        DeleteVM(String hostName,String vmName){
            
            this.hostName=hostName;
            this.vmName=vmName;
            threadName = hostName+"-"+vmName;
            
            System.out.println("Creating " +  threadName );
        }
        public void run() {
            
            try {
                
                System.out.println("Delete Thread: " + threadName + " started");
                
                _webUtilities.deleteVM(hostName, vmName);
                
                // Let the thread sleep for a while.
                Thread.sleep(0);
                
            } catch (Exception e) {
                System.out.println("Thread " +  threadName + " interrupted.");
            }
            
            System.out.println("Delete Thread " +  threadName + " finished.");
            deleted=true;
        }
        
        
        public boolean isDeleted() {
            return deleted;
        }
        
        
        
        
    }
    
    public class LoadVM implements Runnable{
        
        private Thread _thread;
        String threadName;
        String hostName;
        VMRequest request;
        
        public boolean loaded=false;
        int slot;
        int hostID;
        Hashtable hostVMpair;
        
        LoadVM(int slot,VMRequest request,int hostID,String hostName){
            
            this.slot=slot;
            this.hostName=hostName;
            this.hostID=hostID;
            this.request=request;
            this.threadName = hostName+"-"+request.getVmID();
            this. hostVMpair=new Hashtable();
            
            System.out.println("Creating " +  threadName );
        }
        
        public void run() {
            
            try {
                
                System.out.println("Load VM Thread: " + threadName + " started");
                
                /* Block To Activate in real system
                createVM(slot,request,hostName);
                startVM(request,hostName);
                */
                createVMobject(slot,request,hostID,hostName);
                
                Thread.sleep(0);
                
            } catch (Exception e) {
                System.out.println("Thread " +  threadName + " interrupted.");
            }
            
            System.out.println("Delete Thread " +  threadName + " finished.");
            loaded=true;
        }
        
        
        
        
        private boolean createVM(int slot,VMRequest request,String nodeName) throws IOException {
            
            Hashtable vmParameters;
            
            boolean vmCreated=false;
            boolean vmCreateCommandSend=false;
            
            
            System.out.println("provider:"+request.providerID+" - activate: "+request.getVmID());
            
            //Step 1: Add VM on the Physical node
            vmParameters=Utilities.determineVMparameters(request,nodeName);
            vmCreateCommandSend=_webUtilities.createVM(vmParameters);
            
            
            return vmCreateCommandSend;
            
            
        }
        
        private void startVM(VMRequest request, String hostName) throws IOException {
            
            Boolean vmCreated=false;
            Hashtable  vmParameters=Utilities.determineVMparameters(request,hostName);
            int counter=0;
            
            while(!vmCreated&counter<200){
                
                vmCreated=_webUtilities.checkVMListOnHost(hostName,String.valueOf(vmParameters.get("vmName")));
                System.out.println("Bring VM up attempt:"+ counter+"-requestID:"+request.getVmID());
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // handle the exception...
                    // For example consider calling Thread.currentThread().interrupt(); here.
                }
                counter++;
            }
            
            if(counter==200){
                System.out.println("Failed to load requestID:"+request.getVmID());
            }
            String vmName=String.valueOf(vmParameters.get("vmName"));
            _webUtilities.startVM(vmName,hostName);
            
        }
        
        private void createVMobject(int slot, VMRequest request, int hostID,String hostName) {
            
            Hashtable  vmParameters=Utilities.determineVMparameters(request,hostName);
            
            _hosts[hostID].getVMs().add(new VM(vmParameters,request,slot,vmIDs,hostID,_hosts[hostID].getNodeName(),_config));
            vmIDs++;
            
        }
        
    }
    
    class StatisticsTimer extends TimerTask {
        
        int slot;
        
        StatisticsTimer(int slot){
            this.slot=slot;
        }
        
        public void run() {
            
            if(_currentInstance<_numberOfMachineStatsPerSlot){
                
                try {
                    
                    _dbUtilities.updateAllHostStatistics(slot,_currentInstance);
                    _dbUtilities.updateAllHostStatistics2DB(slot,_currentInstance);
                    _dbUtilities.updateAllHostStatisticsInterfacesDB(slot,_currentInstance);
                    
                    _dbUtilities.updateActiveVMStatistics(slot,_currentInstance);
                    
                    _currentInstance++;
                    
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                _machineStatsTimer.cancel();
            }
            
        }
        
        
    }
    
    private void startNodesStatsTimer(int slot) {
        
        int statsUpdateInterval=_config.getSlotDuration()/_config.getNumberOfMachineStatsPerSlot();
        
        _machineStatsTimer = new Timer();
        _currentInstance=0;
        
        if(_config.getSlotDurationMetric().equals(ESlotDurationMetric.milliseconds.toString()))
            _machineStatsTimer.scheduleAtFixedRate(new StatisticsTimer(slot),0 ,statsUpdateInterval);
        else if(_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString()))
            _machineStatsTimer.scheduleAtFixedRate(new StatisticsTimer(slot),0 ,statsUpdateInterval*1000);
        else if(_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString()))
            _machineStatsTimer.scheduleAtFixedRate(new StatisticsTimer(slot),0 ,60*statsUpdateInterval*1000);
        else if(_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString()))
            _machineStatsTimer.scheduleAtFixedRate(new StatisticsTimer(slot),0 ,3600*statsUpdateInterval*1000);
    }
    
    public SchedulerData getCplexData() {
        return _cplexData;
    }
    
    private void addVMsPerService(ServiceRequest _serviceRequest,int slot)
    {
        
        int slot2AddVM=_serviceRequest.getSlotStart();
        
        if(slot!=slot2AddVM )
            System.out.println("Error in slot handling Service Request");
        
        int slot2RemoveVM=_serviceRequest.getSlotEnd();
        int providerID=_serviceRequest.getProviderID();
        int lifetime=_serviceRequest.getLifetime();
        int serviceID=_serviceRequest.getServiceID();
        int serviceRequestID=_serviceRequest.getServiceRequestID();
        String serviceName=_serviceRequest.getServiceName();
        
        List<VMRequest> newVmRequest = new ArrayList<>();
        
        List<String> vms=determineVMs(providerID,serviceID,_cplexData );
        
        if(!vms.isEmpty()){
            
            int index=0;
            
            while(index< vms.size()) {
                
                newVmRequest.add(new VMRequest(providerID,serviceID,lifetime,serviceName,serviceRequestID));
                
                newVmRequest.get(newVmRequest.size()-1).setSlotStart(slot);
                newVmRequest.get(newVmRequest.size()-1).setVmType(vms.get(index));
                
                _slots[slot].getVmRequests2Activate()[providerID].add(newVmRequest.get(newVmRequest.size()-1));
                
                //remove vm during this slot
                newVmRequest.get(newVmRequest.size()-1).setSlotEnd(slot2RemoveVM);
                
                if(slot2RemoveVM<_config.getNumberOfSlots())
                    _slots[slot2RemoveVM].getVmRequests2Remove()[providerID].add(newVmRequest.get(newVmRequest.size()-1));
                
                index++;
            }
            
        }
        
        
    }
    private List<String> determineVMs(int providerID, int serviceID, SchedulerData data) {
        
        List<String> vms=new ArrayList<>();
        
        int[] _vms=data.f(data, providerID, serviceID);
        
        for (int i = 0; i < _vms.length; i++) {
            if(i==0)
                for (int j = 0; j < _vms[i]; j++)
                    vms.add("small");
            else if  (i==1)
                for (int j = 0; j < _vms[i]; j++)
                    vms.add("medium");
            else if(i==2)
                for (int j = 0; j < _vms[i]; j++)
                    vms.add("large");
        }
        
        return vms;
        
    }
    
    public  void updateServiceRequestPattern(int[][][] _simulatorWebRequestPattern, int slot ) {
        
        int simpleMovingAverageParameter=3;
        int index=0;
        int requestsMade=0;
        
        try {
            
            if (slot>0){
                if(EStatsUpdateMethod.simple_moving_average.toString().equals(_config.getStatsUpdateMethod())){
                    
                    index=_slots.length-simpleMovingAverageParameter;
                    
                    if(index-1>=0)
                        for (int p = 0; p < _config.getProvidersNumber() ; p++) {
                            for (int s = 0; s < _config.getServicesNumber(); s++) {
                                requestsMade=0;
                                
                                for (int i = index-1; i <= slot; i++) {
                                    requestsMade+=_simulatorWebRequestPattern[i][p][s];
                                }
                                _webRequestPattern[p][s]=requestsMade/simpleMovingAverageParameter;
                            }
                            
                        }
                    
                }
                else if(EStatsUpdateMethod.cumulative_moving_average.toString().equals(_config.getStatsUpdateMethod())){
                    
                    for (int p = 0; p < _config.getProvidersNumber() ; p++) {
                        for (int s = 0; s < _config.getServicesNumber(); s++) {
                            
                            requestsMade=0;
                            for (int i = 0; i <=slot; i++) {
                                requestsMade+=_simulatorWebRequestPattern[i][p][s];
                            }
                            _webRequestPattern[p][s]=requestsMade/slot;
                        }
                        
                    }
                    
                }
                else if(EStatsUpdateMethod.weighted_moving_average.toString().equals(_config.getStatsUpdateMethod())){
                    
                    
                }
                else if(EStatsUpdateMethod.exponential_moving_average.toString().equals(_config.getStatsUpdateMethod())){
                    
                    
                }
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public int findActiveVMs(int providerID,SchedulerData data){
    
        int activeVMs=0;
        
            for (int i = 0; i < data.N; i++) {
                for (int v = 0; v < data.V; v++) {
                    for (int s = 0; s < data.S; s++) {
                        
                        activeVMs+=data.n[i][providerID][v][s];
                    }
                   
                }
            }
    
        return activeVMs;
    }
    
    
    
    
    
    
}
