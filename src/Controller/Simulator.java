package Controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Cplex.SchedulerData;
import Enumerators.EGeneratorType;
import Enumerators.ESlotDurationMetric;
import Statistics.ABStats;
import Statistics.DBClass;
import Statistics.DBUtilities;
import Statistics.WebRequestStats;
import Statistics.WebRequestStatsSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jsc.distributions.Exponential;
import jsc.distributions.Pareto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

import Controller.*;
import Utilities.*;
/**
 *
 * @author kostas
 */
public class Simulator {

    int slot = 0;

    Configuration _config;

    List<String> _hostNames;
    List<String> _clientNames;

    Host[] _hosts;
    WebClient[] _webClients;
    Slot[] _slots;

    Controller _controller;

    // How to create requests per Service, one per provider
    Exponential[][] _rateExponentialGenerator;
    Pareto[][] _rateParetoGenerator;

    // Lifetime of VM one per provider
    Exponential[][] _lifetimeExponentialGenerator;
    Pareto[][] _lifetimeParetoGenerator;

    // How to create web requests per Service, one per provider
    Exponential[][] _webRequestArrivalRateExponentialGenerator;
    Pareto[][] _webRequestArrivalRateParetoGenerator;

    DBClass _db;
    DBUtilities _dbUtilities;
    Random rand;

    long experimentStart;
    long experimentStop;

    Timer controllerTimer;
    Timer[][][] _clientsTimer;

    WebUtilities _webUtility;
    Provider[] _provider;
    SimWebRequestUtilities _fakeWebUtilities;

    int[][][] _webRequestPattern;

    WebRequestStatsSlot[] _webRequestStatsSlot;

    public Simulator(String algorithm, int simulatorID, int runID) {

        this._config = new Configuration(algorithm, simulatorID, runID);
        this._hostNames = _config.getHostNames();
        this._clientNames = _config.getClientNames();
        this.controllerTimer = new Timer();
        this._clientsTimer = new Timer[_clientNames.size()][_config.getProvidersNumber()][_config.getServicesNumber()];

        this._webUtility = new WebUtilities(_config);
        this._db = new DBClass(_config.getSimulationID(), _config.getAlgorithm());
        this._dbUtilities = new DBUtilities(_hosts, _webUtility, _db, _config);

        System.out.println("********** System Initialization Phase ****************");

        initializeNodesAndSlots(); //creates: Hosts, Clients, Slots
        initializeProviders();
        initializeServiceArrivalRateGenerators();
        initializeLifeTimeGenerators();
        initializeWebRequestsArrivalRateGenerators();

        initiliazeWebRequestStatsSlot();
        this._controller = new Controller(_hosts, _webClients, _config, _slots, _dbUtilities, _provider);

        addInitialServiceRequestEvents();

        this._fakeWebUtilities = new SimWebRequestUtilities(_provider, _config);

        System.out.println("********** End of System Initialization Phase **************");
        System.out.println();

        if (true) {
            this.startClientsRequests();
        }
    }

    private void initializeLifeTimeGenerators() {

        // Lifetime of VM one per provider
        _lifetimeExponentialGenerator = new Exponential[_config.getProvidersNumber()][_config.getServicesNumber()];
        _lifetimeParetoGenerator = new Pareto[_config.getProvidersNumber()][_config.getServicesNumber()];

        String lifetimeType = "";
        String parameter = "";
        double lamda;
        double location;
        double shape;

        int servicesNumber = 0;

        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            servicesNumber = _provider[i].getRequestsForService().size();

            for (int j = 0; j < servicesNumber; j++) {
                parameter = "provider" + i + "_service" + j + "_lifetimeType";
                lifetimeType = String.valueOf(_provider[i].getRequestsForService().get(j).getLifeTimeConfig().get(parameter));

                if (lifetimeType.equals(EGeneratorType.Exponential.toString())) {

                    parameter = "provider" + i + "_service" + j + "_lifetime_lamda";
                    lamda = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getLifeTimeConfig().get(parameter)));
                    _lifetimeExponentialGenerator[i][j] = new Exponential(lamda);

                } else if (lifetimeType.equals(EGeneratorType.Pareto.toString())) {

                    parameter = "provider" + i + "_service" + j + "_lifetime_location";
                    location = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getLifeTimeConfig().get(parameter)));
                    parameter = "provider" + i + "_service" + j + "_lifetime_shape";
                    shape = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getLifeTimeConfig().get(parameter)));

                    _lifetimeParetoGenerator[i][j] = new Pareto(location, shape); //Dummy object
                }

            }

        }
    }

    private void initializeServiceArrivalRateGenerators() {

        // How to create requests per Service, one per provider
        _rateExponentialGenerator = new Exponential[_config.getProvidersNumber()][_config.getServicesNumber()];
        _rateParetoGenerator = new Pareto[_config.getProvidersNumber()][_config.getServicesNumber()];
 
       
        String rateType = "";
        String parameter = "";
        double lamda;
        double location;
        double shape;

        int servicesNumber = 0;

        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            servicesNumber = _provider[i].getRequestsForService().size();

            for (int j = 0; j < servicesNumber; j++) {
                parameter = "provider" + i + "_service" + j + "_RateType";
                rateType = String.valueOf(_provider[i].getRequestsForService().get(j).getRequestRateConfig().get(parameter));

                if (rateType.equals(EGeneratorType.Exponential.toString())) {

                    parameter = "provider" + i + "_service" + j + "_rate_lamda";
                    lamda = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getRequestRateConfig().get(parameter)));
                    _rateExponentialGenerator[i][j] = new Exponential(lamda);

                } else if (rateType.equals(EGeneratorType.Pareto.toString())) {

                    parameter = "provider" + i + "_service" + j + "_rate_location";
                    location = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getRequestRateConfig().get(parameter)));
                    parameter = "provider" + i + "_service" + j + "_rate_shape";
                    shape = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getRequestRateConfig().get(parameter)));

                    _rateParetoGenerator[i][j] = new Pareto(location, shape); //Dummy object
                }

            }

        }

    }

    private void initializeWebRequestsArrivalRateGenerators() {

        // How to create requests per Service, one per provider
        _webRequestArrivalRateExponentialGenerator = new Exponential[_config.getProvidersNumber()][_config.getServicesNumber()];
        _webRequestArrivalRateParetoGenerator = new Pareto[_config.getProvidersNumber()][_config.getServicesNumber()];

        String rateType = "";
        String parameter = "";
        double lamda;
        double location;
        double shape;

        int servicesNumber = 0;

        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            servicesNumber = _provider[i].getRequestsForService().size();

            for (int j = 0; j < servicesNumber; j++) {
                parameter = "provider" + i + "_service" + j + "_webRequestType";
                rateType = String.valueOf(_provider[i].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().get(parameter));

                if (rateType.equals(EGeneratorType.Exponential.toString())) {

                    parameter = "provider" + i + "_service" + j + "_webRequest_lamda";
                    lamda = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().get(parameter)));
                    lamda = (double) 1 / lamda;
                    _webRequestArrivalRateExponentialGenerator[i][j] = new Exponential(lamda);

                } else if (rateType.equals(EGeneratorType.Pareto.toString())) {

                    parameter = "provider" + i + "_service" + j + "_webRequest_location";
                    location = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().get(parameter)));
                    parameter = "provider" + i + "_service" + j + "_webRequest_shape";
                    shape = Double.valueOf(String.valueOf(_provider[i].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().get(parameter)));

                    _webRequestArrivalRateParetoGenerator[i][j] = new Pareto(location, shape); //Dummy object
                }

            }

        }

    }

    private void initializeNodesAndSlots() {

        Random rand = new Random();

        // Initialize Hosts
        this._hosts = new Host[_config.getHostNames().size()];

        for (int i = 0; i < _hosts.length; i++) {
            _hosts[i] = new Host(i, _config, _hostNames.get(i));
        }

        System.out.println("Simulator Initialization: Host Objects - OK");

        // Initialize Clients
        this._webClients = new WebClient[_config.getClientNames().size()];

        for (int c = 0; c < _webClients.length; c++) {
            _webClients[c] = new WebClient(_config, c, rand.nextInt(_config.getProvidersNumber()), _clientNames.get(c), _controller);
            for (int p = 0; p < _config.getProvidersNumber(); p++) {
                for (int s = 0; s < _config.getServicesNumber(); s++) {
                    _clientsTimer[c][p][s] = new Timer();
                }

            }

        }

        System.out.println("Simulator Initialization: Client Objects - OK");

        // Initialize Slots
        _slots = new Slot[_config.getNumberOfSlots()];

        for (int i = 0; i < _config.getNumberOfSlots(); i++) {
            _slots[i] = new Slot(i, _config);

        }

        _webRequestPattern = new int[_config.getNumberOfSlots()][_config.getProvidersNumber()][_config.getServicesNumber()];

        System.out.println("Simulator Initialization: Slot Objects - OK");

    }

    private void startClientsRequests() {

        System.out.println("********** Clients Requests Loader ****************");

        for (int c = 0; c < _webClients.length; c++) {
            for (int p = 0; p < _config.getProvidersNumber(); p++) {
                for (int s = 0; s < _config.getServicesNumber(); s++) {
                    _clientsTimer[c][p][s].schedule(new ExecuteClientRequest(_webRequestStatsSlot, c, p, s, 0), 100); //Start the Client Requests (initial delay 100)
                }
            }
            System.out.println("****** All Clients Request Generators Loaded ******");
            System.out.println();
        }
    }

    private void addInitialServiceRequestEvents() {

        int runningSlot = 0;
        int serviceRequests = 1;
        int servicesNumber = 0;
        //add first VM in slot 0

        for (int i = 0; i < _config.getProvidersNumber(); i++) {

            servicesNumber = _provider[i].getRequestsForService().size();

            for (int j = 0; j < servicesNumber; j++) {

                CreateNewServiceRequest(0, j, runningSlot, true);

                runningSlot = 0;

                while (runningSlot < _config.getNumberOfSlots()) {

                    runningSlot = CreateNewServiceRequest(i, j, runningSlot, false);
                    serviceRequests++;

                }
            }
        }

        System.out.println("Simulator Initialization:" + serviceRequests);

    }

    //Returns the new running slot (this can be also 0)
    private int CreateNewServiceRequest(int providerID, int serviceID, int currentSlot, boolean firstSlot) {
        int slot2AddService = 0;
        int slot2RemoveService = 0;

        int lifetime = calculateServiceLifeTime(providerID, serviceID);
        String serviceName = "";

        if (lifetime < 1) {
            lifetime = 1;
        }

        // Slot calculation
        int slotDistance;

        if (firstSlot) {
            slotDistance = 0;
        } else {
            slotDistance = calculateSlotsAway(providerID, serviceID);
        }

        slot2AddService = currentSlot + slotDistance;
        slot2RemoveService = slot2AddService + lifetime;

        if (slot2AddService < _config.getNumberOfSlots()) {

            serviceName = _provider[providerID].getRequestsForService().get(serviceID).getServiceName();

            ServiceRequest newServiceRequest = new ServiceRequest(providerID, serviceID, lifetime, serviceName);

            newServiceRequest.setSlotStart(slot2AddService);
            newServiceRequest.setSlotRemove(slot2RemoveService);

            _slots[slot2AddService].getServiceRequests2Activate()[providerID].add(newServiceRequest);

            if (slot2RemoveService < _config.getNumberOfSlots()) {
                _slots[slot2RemoveService].getServiceRequests2Remove()[providerID].add(newServiceRequest);
            }

        } else {
            System.out.println("failed to add request");
        }

        return slot2AddService;

    }

    private int calculateSlotsAway(int providerID, int serviceID) {

        int interArrivalTime = -1;
        double min = 0;
        double max = 0;
        Double value;
        String parameter = "provider" + providerID + "_service" + serviceID + "_RateType";

        String generatorType = String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getRequestRateConfig().get(parameter));

        switch (EGeneratorType.valueOf(generatorType)) {
            case Exponential:
                value = _rateExponentialGenerator[providerID][serviceID].random();
                interArrivalTime = value.intValue();
                break;

            case Pareto:
                value = _webRequestArrivalRateParetoGenerator[providerID][serviceID].random();
                interArrivalTime = value.intValue();

                break;

            case Random:

                parameter = "provider" + providerID + "_service" + serviceID + "_rate_min";
                min = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getRequestRateConfig().get(parameter)));

                parameter = "provider" + providerID + "_service" + serviceID + "_rate_max";
                max = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getRequestRateConfig().get(parameter)));

                interArrivalTime = Utilities.randInt((int) min, (int) max);
                break;

            default:
                break;
        }

        return interArrivalTime;

    }

    private int calculateServiceLifeTime(int providerID, int serviceID) {

        int lifetime = -1;
        double min = 0;
        double max = 0;
        Double value;
        String parameter = "provider" + providerID + "_service" + serviceID + "_lifetimeType";

        String generatorType = String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLifeTimeConfig().get(parameter));

        switch (EGeneratorType.valueOf(generatorType)) {
            case Exponential:
                value = _lifetimeExponentialGenerator[providerID][serviceID].random();
                lifetime = value.intValue();
                break;

            case Pareto:
                value = _lifetimeParetoGenerator[providerID][serviceID].random();
                lifetime = value.intValue();

                break;

            case Random:

                parameter = "provider" + providerID + "_service" + serviceID + "_lifetime_min";
                min = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLifeTimeConfig().get(parameter)));

                parameter = "provider" + providerID + "_service" + serviceID + "_lifetime_max";
                max = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getLifeTimeConfig().get(parameter)));

                lifetime = Utilities.randInt((int) min, (int) max);
                break;

            default:
                break;
        }

        if (lifetime != 0) {
            return lifetime;
        } else {
            return 5;
        }

    }

    private double calculateWebRequestInterarrivalInterval(int providerID, int serviceID) {

        double interArrivalTime = -1;
        long interval=0;
        double min = 0;
        double max = 0;
        Double value;
        String parameter = "provider" + providerID + "_service" + serviceID + "_webRequestType";

        String generatorType = String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getWebRequestsArrivalRateConfig().get(parameter));

        switch (EGeneratorType.valueOf(generatorType)) {
            case Exponential:
                value = _webRequestArrivalRateExponentialGenerator[providerID][serviceID].random();
                interArrivalTime = value.doubleValue();
                break;

            case Pareto:
                value = _webRequestArrivalRateParetoGenerator[providerID][serviceID].random();
                interArrivalTime = value.intValue();

                break;

            case Random:

                parameter = "provider" + providerID + "_service" + serviceID + "_webRequest_min";
                min = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getWebRequestsArrivalRateConfig().get(parameter)));

                parameter = "provider" + providerID + "_service" + serviceID + "_webRequest_max";
                max = Double.valueOf(String.valueOf(this._provider[providerID].getRequestsForService().get(serviceID).getWebRequestsArrivalRateConfig().get(parameter)));

                interArrivalTime = Utilities.randInt((int) min, (int) max);
                break;

            default:
                break;
        }

               
        return interArrivalTime;

    }

    public final void StartExperiment() {

        int duration = _config.getSlotDuration();

        experimentStart = System.currentTimeMillis();

        for (int i = 0; i < _slots.length; i++) {

            System.out.println("---------------- SLOT " + i + " -----------------------------");

            for (int j = 0; j < _config.providersNumber; j++) {

                //  System.out.println("------ Provider "+j+" ---------");
                for (ServiceRequest e : _slots[i].getServiceRequests2Activate()[j]) {
                    System.out.println("Create: " + e.serviceRequestID);
                }

                for (ServiceRequest e : _slots[i].getServiceRequests2Remove()[j]) {
                    System.out.println("Delete: " + e.serviceRequestID);
                }

                System.out.println("");
            }
        }

        System.out.println("Simulator started! Time instant: " + experimentStart);

        if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.milliseconds.toString())) {
            controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, duration);
        } else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString())) {
            controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, duration * 1000);
        } else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString())) {
            controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, 60 * duration * 1000);
        } else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString())) {
            controllerTimer.scheduleAtFixedRate(new RunSlot(), 0, 3600 * duration * 1000);
        }

    }

    private void initializeProviders() {

        _provider = new Provider[_config.getProvidersNumber()];

        for (int i = 0; i < _config.getProvidersNumber(); i++) {
            _provider[i] = new Provider(i);
            loadArrivalRateParameters(i);
            loadLifetimeGeneratorParameters(i);
            loadWebRequestsGeneratorParameters(i);
            loadLocalServiceTimeGeneratorParameters(i);
            loadCloudServiceTimeGeneratorParameters(i);
        }

    }

    private void loadArrivalRateParameters(int providerID) {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        int providerServicesNumber = 0;
        String parameter = "";
        String serviceName;
        int numberOfRequests;
        String serviceRateType = "";
        double value = -1;
        int index = -1;

        // InterArrival Time
//        parameter="provider"+providerID+"_servicesNumber";
//        providerServicesNumber=Integer.valueOf((String)property.getProperty(parameter));
        providerServicesNumber = Integer.valueOf((String) property.getProperty("servicesNumber"));

        for (int j = 0; j < providerServicesNumber; j++) {

            //ServiceName
            parameter = "provider" + providerID + "_service" + j + "_name";
            serviceName = String.valueOf((String) property.getProperty(parameter));
            //Number of requets
            parameter = "provider" + providerID + "_service" + j + "_estimatedRequets";
            numberOfRequests = Integer.valueOf((String) property.getProperty(parameter));

            _provider[providerID].getRequestsForService().add(new ServiceRequestRates(providerID, j, numberOfRequests, serviceName));
            index = j;

            //Rate Configuration
            parameter = "provider" + providerID + "_service" + j + "_RateType";
            serviceRateType = String.valueOf((String) property.getProperty(parameter));
            _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, serviceRateType);

            if (EGeneratorType.Exponential.toString().equals(serviceRateType)) {

                parameter = "provider" + providerID + "_service" + j + "_rate_lamda";
                value = Double.valueOf((String) property.getProperty(parameter));
                value = (double) 1 / value;

                _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, value);
            } else if (EGeneratorType.Pareto.toString().equals(serviceRateType)) {

                parameter = "provider" + providerID + "_service" + j + "_rate_location";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_rate_shape";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, value);

            } else if (EGeneratorType.Random.toString().equals(serviceRateType)) {

                parameter = "provider" + providerID + "_service" + j + "_rate_min";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_rate_max";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(index).getRequestRateConfig().put(parameter, value);

            }

        }

    }

    private void loadLifetimeGeneratorParameters(int providerID) {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        int providerServicesNumber = 0;
        String parameter = "";
        String serviceLifetimeType = "";
        double value = -1;

        // InterArrival Time
//        parameter="provider"+providerID+"_servicesNumber";
//        providerServicesNumber=Integer.valueOf((String)property.getProperty(parameter));
        providerServicesNumber = Integer.valueOf((String) property.getProperty("servicesNumber"));

        for (int j = 0; j < providerServicesNumber; j++) {

            //Lifetime Configuration
            parameter = "provider" + providerID + "_service" + j + "_lifetimeType";
            serviceLifetimeType = String.valueOf((String) property.getProperty(parameter));
            _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, serviceLifetimeType);

            if (EGeneratorType.Exponential.toString().equals(serviceLifetimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_lifetime_lamda";
                value = Double.valueOf((String) property.getProperty(parameter));

                _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, value);
            } else if (EGeneratorType.Pareto.toString().equals(serviceLifetimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_lifetime_location";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_lifetime_shape";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, value);

            } else if (EGeneratorType.Random.toString().equals(serviceLifetimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_lifetime_min";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_lifetime_max";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLifeTimeConfig().put(parameter, value);

            }

        }

    }

    private void loadLocalServiceTimeGeneratorParameters(int providerID) {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        int providerServicesNumber = 0;
        String parameter = "";
        String localServiceTimeType = "";
        double value = -1;

        // InterArrival Time
        // parameter="provider"+providerID+"_servicesNumber";
        // providerServicesNumber=Integer.valueOf((String)property.getProperty(parameter));
        providerServicesNumber = Integer.valueOf((String) property.getProperty("servicesNumber"));

        for (int j = 0; j < providerServicesNumber; j++) {

            //Lifetime Configuration
            parameter = "provider" + providerID + "_service" + j + "_localServiceRateType";
            localServiceTimeType = String.valueOf((String) property.getProperty(parameter));
            _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, localServiceTimeType);

            if (EGeneratorType.Exponential.toString().equals(localServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_localServiceRate_lamda";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, value);

            } else if (EGeneratorType.Pareto.toString().equals(localServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_localServiceRate_location";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_localServiceRate_shape";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, value);

            } else if (EGeneratorType.Random.toString().equals(localServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_localServiceRate_min";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_localServiceRate_max";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getLocalServiceTimeConfig().put(parameter, value);
            }

        }
    }

    private void loadCloudServiceTimeGeneratorParameters(int providerID) {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        int providerServicesNumber = 0;
        String parameter = "";
        String cloudServiceTimeType = "";
        double value = -1;

        // InterArrival Time
        // parameter="provider"+providerID+"_servicesNumber";
        // providerServicesNumber=Integer.valueOf((String)property.getProperty(parameter));
        providerServicesNumber = Integer.valueOf((String) property.getProperty("servicesNumber"));

        for (int j = 0; j < providerServicesNumber; j++) {

            //Lifetime Configuration
            parameter = "provider" + providerID + "_service" + j + "_cloudServiceRateType";
            cloudServiceTimeType = String.valueOf((String) property.getProperty(parameter));
            _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, cloudServiceTimeType);

            if (EGeneratorType.Exponential.toString().equals(cloudServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_cloudServiceRate_lamda";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, value);

            } else if (EGeneratorType.Pareto.toString().equals(cloudServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_cloudServiceRate_location";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_cloudServiceRate_shape";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, value);

            } else if (EGeneratorType.Random.toString().equals(cloudServiceTimeType)) {

                parameter = "provider" + providerID + "_service" + j + "_cloudServiceRate_min";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_cloudServiceRate_max";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getCloudServiceTimeConfig().put(parameter, value);
            }

        }

    }

    private void loadWebRequestsGeneratorParameters(int providerID) {

        Properties property = new Properties();
        InputStream input = null;
        String filename = "simulation.properties";

        input = Configuration.class.getClassLoader().getResourceAsStream(filename);

        try {
            // load a properties file
            property.load(input);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

        int providerServicesNumber = 0;
        String parameter = "";
        String webRequestType = "";
        double value = -1;

        // InterArrival Time
        // parameter="provider"+providerID+"_servicesNumber";
        // providerServicesNumber=Integer.valueOf((String)property.getProperty(parameter));
        providerServicesNumber = Integer.valueOf((String) property.getProperty("servicesNumber"));

        for (int j = 0; j < providerServicesNumber; j++) {

            //Lifetime Configuration
            parameter = "provider" + providerID + "_service" + j + "_webRequestType";
            webRequestType = String.valueOf((String) property.getProperty(parameter));
            _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, webRequestType);

            if (EGeneratorType.Exponential.toString().equals(webRequestType)) {

                parameter = "provider" + providerID + "_service" + j + "_webRequest_lamda";
                value = Double.valueOf((String) property.getProperty(parameter));

                _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, value);
            } else if (EGeneratorType.Pareto.toString().equals(webRequestType)) {

                parameter = "provider" + providerID + "_service" + j + "_webRequest_location";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_webRequest_shape";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, value);

            } else if (EGeneratorType.Random.toString().equals(webRequestType)) {

                parameter = "provider" + providerID + "_service" + j + "_webRequest_min";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, value);

                parameter = "provider" + providerID + "_service" + j + "_webRequest_max";
                value = Double.valueOf((String) property.getProperty(parameter));
                _provider[providerID].getRequestsForService().get(j).getWebRequestsArrivalRateConfig().put(parameter, value);

            }

        }
    }

    private void initiliazeWebRequestStatsSlot() {

        int p = _config.getProvidersNumber();
        int s = _config.getServicesNumber();

        this._webRequestStatsSlot = new WebRequestStatsSlot[_config.getNumberOfSlots()];

        for (int i = 0; i < _config.getNumberOfSlots(); i++) {
            _webRequestStatsSlot[i] = new WebRequestStatsSlot(slot, p, s);
        }
    }

    class ExecuteClientRequest extends TimerTask {

        int providerID;
        int serviceID;
        int clientID;
        int measurement = 0;
        WebRequestStatsSlot[] _webRequestStatsSlot;

        public ExecuteClientRequest(WebRequestStatsSlot[] webRequestStatsSlot, int clientID, int providerID, int serviceID, int measurement) {
            this.providerID = providerID;
            this.serviceID = serviceID;
            this.measurement = measurement;
            this.clientID = clientID;
            _webRequestStatsSlot = webRequestStatsSlot;
        }

        @Override
        public void run() {

            String clientName = _clientNames.get(clientID);
            String vmIP = chooseVMforService(serviceID, providerID, clientName);

            WebRequestStats stats = new WebRequestStats();

            stats.setClientID(clientID);
            stats.setSlot(slot);
            stats.setProviderID(providerID);
            stats.setServiceID(serviceID);

            double responseTime;
            String type;
            if (_config.getCloudVM_IPs().contains(vmIP)) {
                responseTime = _fakeWebUtilities.calculateCloudServiceTime(providerID, serviceID);
                type = "cloud";
            } else {
                responseTime = _fakeWebUtilities.calculateLocalServiceTime(providerID, serviceID);
                type = "local";
            }
            stats.setType(type);
            stats.setResponseTime(responseTime);

//            // Enable if you want per request stats
//            if (_config.getSkipWebClientStats() == 0) {
//                _dbUtilities.updateWebClientStatistics2DB(slot, stats);
//            } else if (measurement % _config.getSkipWebClientStats() == 0) {
//                _dbUtilities.updateWebClientStatistics2DB(slot, stats);
//            }

            //**************************************************
            // Used to calculate the Average Response Time Values
            if (slot < _config.getNumberOfSlots()) {
                this._webRequestStatsSlot[slot].getResponseTime()[providerID][serviceID] += responseTime;
                this._webRequestStatsSlot[slot].getNumberOfRequests()[providerID][serviceID]++;
            }

            // update Web Requests Statistics Array
            if (slot < _slots.length) {
                _webRequestPattern[slot][providerID][serviceID]++;

            }
            
            // Schedule new event
             int duration = _config.getSlotDuration();

            if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.seconds.toString())) {
                duration = 1000 * duration;
            } else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.minutes.toString())) {
                duration = 60 * duration * 1000;
            } else if (_config.getSlotDurationMetric().equals(ESlotDurationMetric.hours.toString())) {
                duration = 3600 * duration * 1000;
            }

            double x=duration * calculateWebRequestInterarrivalInterval(providerID, serviceID);
            
            long delay = (long)x ;

            _clientsTimer[clientID][providerID][serviceID].schedule(new ExecuteClientRequest(_webRequestStatsSlot, clientID, providerID, serviceID, measurement + 1), delay);
           
            
            

        }

    }

    //Algorithm: Choose a VM in the hosting Node else choose at Random
    private String chooseVMforService(int serviceID, int providerID, String webClient) {

        String vmIP = "";
        String hostApName = "";
        Random random = new Random();

        //Step 1: Find the hosting node
        for (int i = 0; i < _webClients.length; i++) {
            if (_webClients[i].getClientName().equals(webClient)) {
                hostApName = _webClients[i].getApName();
            }
        }

        //Step 2: Find all the VMs that can be used
        try {

            CopyOnWriteArrayList<VM> potentialVMs = new CopyOnWriteArrayList<>();

            for (Host _host : _hosts) {
                for (Iterator iterator = _host.getVMs().iterator(); iterator.hasNext();) {
                    VM nextVM = (VM) iterator.next();

                    if (nextVM.isActive() & nextVM.getProviderID() == providerID & nextVM.getServiceID() == serviceID) {
                        potentialVMs.add(nextVM);
                    }
                }
            }

            if (potentialVMs.size() > 0) {
                vmIP = potentialVMs.get(random.nextInt(potentialVMs.size())).getIp();
                return vmIP;

            } else if (potentialVMs.isEmpty()) {
                return _config.getCloudVM_IPs().get(random.nextInt(_config.getCloudVM_IPs().size()));
            }

//        //Step 3: Find the local VM
//           for (Iterator iterator = potentialVMs.iterator(); iterator.hasNext();) {
//               VM nextVM = (VM)iterator.next();
//
//               if(hostApName.equals(nextVM.getHostname()))
//                    vmIP=nextVM.getIp();
//        }
        } catch (Exception e) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            System.out.println(e);
            System.exit(0);
        }

        return null;

    }

    class RunSlot extends TimerTask {

        public void run() {

            try {
                if (slot < _config.getNumberOfSlots()) {

                    _controller.updateServiceRequestPattern(_webRequestPattern, slot);
                    _controller.Run(slot);

                    _dbUtilities.updateWebClientStatistics2DBPerSlot(slot, _webRequestStatsSlot);
                    slot++;

                } else {
                    experimentStop = System.currentTimeMillis();
                    controllerTimer.cancel();
                  
                    _db.getOmlclient().close();
                    System.exit(0);
                }
            } catch (IOException ex) {
                _db.getOmlclient().close();
                Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
