/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import Data.DataSetProcessor;
import Data.DynamicTransaction;
import Data.FullCase;
import Data.NormalFeature;
import Data.StaticTransaction;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author Amir72c
 */
public class SimpleNet implements Serializable {

    DataSet myDataSet;
    public ArrayList<Event> events = new ArrayList();
    public ArrayList<Link> links = new ArrayList();

    public void generateNet(DataSet dataSet) {
        myDataSet = dataSet;
        events = DataSetProcessor.extractEvents(myDataSet);
        links = DataSetProcessor.extractLinks(myDataSet);
        setLinksData();
    }

    public void trainNet(DataSet dataSet, String clasifier, String clusterer, int numClusters, int numThreads) {
        long startTime=System.currentTimeMillis();
        generateNet(dataSet);
        try {
            trainLinks(clusterer, numClusters, numThreads);
            trainEvents(clasifier, numThreads);
        } catch (Exception ex) {
            Logger.getLogger(SimpleNet.class.getName()).log(Level.SEVERE, null, ex);
        }
        long endTime=System.currentTimeMillis();
        System.out.println("Time to generate and train the net(s): "+(endTime-startTime)/1000);
    }

    private void trainLinks(String model, int numClusters, int numThreads) throws Exception {
        if (numThreads>1) {
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < links.size(); i++) {
                final int final_i = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Clustering link: " + links.get(final_i).name);
                        try {
                            links.get(final_i).trainLink(myDataSet, model, numClusters);
                        } catch (Exception ex) {
                            Logger.getLogger(SimpleNet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (InterruptedException e) {
            }
        } else {
            for (int i = 0; i < links.size(); i++) {
                System.out.println("Clustering link: " + links.get(i).name);
                links.get(i).trainLink(myDataSet, model, numClusters);
            }
        }
    }

    private void trainEvents(String model, int numThreads) throws Exception {
        if (numThreads>1) {
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < events.size(); i++) {
                final int final_i = i;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!events.get(final_i).name.equals("Start") && !events.get(final_i).name.equals("End")) {
                            System.out.println("Training classifer of " + events.get(final_i).name);
                            try {
                                events.get(final_i).trainEvent(myDataSet, model);
                            } catch (Exception ex) {
                                Logger.getLogger(SimpleNet.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
            }
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            } catch (InterruptedException e) {
            }
        } else {
            for (int i = 0; i < events.size(); i++) {
                if (!events.get(i).name.equals("Start") && !events.get(i).name.equals("End")) {
                    System.out.println("Training classifer of " + events.get(i).name);
                    events.get(i).trainEvent(myDataSet, model);
                }
            }
        }

    }

    private void setLinksData() {
        for (int i = 0; i < myDataSet.myFullCases.size(); i++) {
            if (myDataSet.myFullCases.get(i).dynamicTransactions.size() > 2) {
                for (int j = 0; j < myDataSet.myFullCases.get(i).dynamicTransactions.size(); j++) {
                    String linkName;
                    if (j == 0) {
                        linkName = "Start" + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex];
                        int outputingEventIndex = findEventIndex("Start");
                        int inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex]);
                        if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                            events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                            events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        DynamicTransaction tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data);
                        StaticTransaction tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(j).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(j).duration, myDataSet.myFullCases.get(i).staticTransactions.get(j).data);
                        links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                        links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);
                    } else if (j == myDataSet.myFullCases.get(i).dynamicTransactions.size() - 1) {

                        linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(j - 1).data[myDataSet.eventIndex] + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex];
                        int outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j - 1).data[myDataSet.eventIndex]);
                        int inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex]);
                        if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                            events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                            events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        DynamicTransaction tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data);
                        StaticTransaction tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(j).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(j).duration, myDataSet.myFullCases.get(i).staticTransactions.get(j).data);
                        links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                        links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);

                        linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex] + "->" + "End";
                        outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex]);
                        inputingEventIndex = findEventIndex("End");
                        if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                            events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                            events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                        }

                        tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data);
                        tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(j).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(j).duration, myDataSet.myFullCases.get(i).staticTransactions.get(j).data);
                        links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                        links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);
                    } else {
                        linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(j - 1).data[myDataSet.eventIndex] + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex];
                        int outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j - 1).data[myDataSet.eventIndex]);
                        int inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data[myDataSet.eventIndex]);
                        if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                            events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                            events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                        }
                        DynamicTransaction tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(j).data);
                        StaticTransaction tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(j).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(j).duration, myDataSet.myFullCases.get(i).staticTransactions.get(j).data);
                        links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                        links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);
                    }
                }
            } else if (myDataSet.myFullCases.get(i).dynamicTransactions.size() == 2) {
                String linkName;
                linkName = "Start" + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex];
                int outputingEventIndex = findEventIndex("Start");
                int inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex]);
                if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                    events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                }
                if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                    events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                }
                DynamicTransaction tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data);
                StaticTransaction tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(0).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(0).duration, myDataSet.myFullCases.get(i).staticTransactions.get(0).data);
                links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);

                linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex] + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data[myDataSet.eventIndex];
                outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex]);
                inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data[myDataSet.eventIndex]);
                if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                    events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                }
                if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                    events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                }
                tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(1).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data);
                tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(1).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(1).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(1).duration, myDataSet.myFullCases.get(i).staticTransactions.get(1).data);
                links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);

                linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data[myDataSet.eventIndex] + "->" + "End";
                outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data[myDataSet.eventIndex]);
                inputingEventIndex = findEventIndex("End");
                if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                    events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                }
                if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                    events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                }
                tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(1).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(1).data);
                tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(1).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(1).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(1).duration, myDataSet.myFullCases.get(i).staticTransactions.get(1).data);
                links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);
            } else if (myDataSet.myFullCases.get(i).dynamicTransactions.size() == 1) {
                String linkName;
                linkName = "Start" + "->" + myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex];
                int outputingEventIndex = findEventIndex("Start");
                int inputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex]);
                if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                    events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                }
                if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                    events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                }
                DynamicTransaction tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data);
                StaticTransaction tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(0).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(0).duration, myDataSet.myFullCases.get(i).staticTransactions.get(0).data);
                links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);

                linkName = myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex] + "->" + "End";
                outputingEventIndex = findEventIndex(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data[myDataSet.eventIndex]);
                inputingEventIndex = findEventIndex("End");
                if (events.get(outputingEventIndex).isLinkOutputUnique(linkName)) {
                    events.get(outputingEventIndex).outputLinks.add(links.get(findLinkIndex(linkName)));
                }
                if (events.get(inputingEventIndex).isLinkInputUnique(linkName)) {
                    events.get(inputingEventIndex).inputLinks.add(links.get(findLinkIndex(linkName)));
                }
                tempDynamicTransaction = new DynamicTransaction(myDataSet.myFullCases.get(i).dynamicTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).indexId, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).duration, myDataSet.myFullCases.get(i).dynamicTransactions.get(0).data);
                tempStaticTransaction = new StaticTransaction(myDataSet.myFullCases.get(i).staticTransactions.get(0).nextEventName, myDataSet.myFullCases.get(i).staticTransactions.get(0).indexId, myDataSet.myFullCases.get(i).staticTransactions.get(0).duration, myDataSet.myFullCases.get(i).staticTransactions.get(0).data);
                links.get(findLinkIndex(linkName)).myDynamicData.add(tempDynamicTransaction);
                links.get(findLinkIndex(linkName)).myStaticData.add(tempStaticTransaction);
            }
        }
    }

    public TestResult testNet(DataSet testData, int knownCasePercent) {
        ArrayList<FullCase> predictions = new ArrayList();
        for (int i = 0; i < testData.myFullCases.size(); i++) {
            ArrayList<StaticTransaction> staticTransactions = new ArrayList();
            ArrayList<DynamicTransaction> dynamicTransactions = new ArrayList();
            for (int j = 0; j < testData.myFullCases.get(i).dynamicTransactions.size() * ((float) knownCasePercent) / (100f); j++) {
                testData.myFullCases.get(i).staticTransactions.get(j).isPredicted = false;
                staticTransactions.add(testData.myFullCases.get(i).staticTransactions.get(j));
                dynamicTransactions.add(testData.myFullCases.get(i).dynamicTransactions.get(j));
            }
            predictions.add(new FullCase(testData.timeIndex, staticTransactions, dynamicTransactions));
        }
        for (int i = 0; i < predictions.size(); i++) {
            predictions.set(i, predictFullCase(testData, predictions.get(i)));
        }

        int numEventInconformities[] = new int[predictions.size()];
        int timeInconformityDays[] = new int[predictions.size()];
        double featureInconformities[] = new double[predictions.size()];
        for (int i = 0; i < predictions.size(); i++) {
            if (predictions.get(i).staticTransactions.size() >= testData.myFullCases.get(i).staticTransactions.size()) {
                for (int j = 0; j < predictions.get(i).staticTransactions.size(); j++) {
                    if (!predictions.get(i).staticTransactions.get(j).currentEventName.equals("End")) {
                        if (predictions.get(i).staticTransactions.get(j).isPredicted == true) {
                            if (j > 0) {
                                String currentEventName = null;
                                currentEventName = predictions.get(i).staticTransactions.get(j).data[testData.eventIndex];
                                if (j < testData.myFullCases.get(i).staticTransactions.size()) {
                                    if (!testData.myFullCases.get(i).staticTransactions.get(j).currentEventName.equals(currentEventName)) {
                                        numEventInconformities[i] = numEventInconformities[i] + 1;
                                    }
                                    for (int f = 0; f < testData.myFullCases.get(i).staticTransactions.get(j).data.length; f++) {
                                        if (testData.header.features.get(f) instanceof NormalFeature) {
                                            if (testData.header.features.get(f).type.equals("Numeric")) {
                                                if (predictions.get(i).staticTransactions.get(j).data[f].length() > 0 && testData.myFullCases.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(predictions.get(i).staticTransactions.get(j).data[f]) - Double.valueOf(testData.myFullCases.get(i).staticTransactions.get(j).data[f]));
                                                } else if (predictions.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(predictions.get(i).staticTransactions.get(j).data[f]));
                                                } else if (testData.myFullCases.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(testData.myFullCases.get(i).staticTransactions.get(j).data[f]));
                                                }
                                            } else {
                                                if (!predictions.get(i).staticTransactions.get(j).data[f].equals(testData.myFullCases.get(i).staticTransactions.get(j).data[f])) {
                                                    featureInconformities[i] = featureInconformities[i] + 1;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    numEventInconformities[i] = numEventInconformities[i] + 1;
                                    for (int f = 0; f < predictions.get(i).staticTransactions.get(j).data.length; f++) {
                                        if (testData.header.features.get(f) instanceof NormalFeature) {
                                            if (testData.header.features.get(f).type.equals("Numeric")) {
                                                if (predictions.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(predictions.get(i).staticTransactions.get(j).data[f]));
                                                }
                                            } else {
                                                featureInconformities[i] = featureInconformities[i] + 1;
                                            }
                                        }
                                    }
                                }

                                if (findEventIndex(currentEventName) < 0) {
                                    System.out.println("NEW EVENT FOUND!!!");
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
                                if (j < testData.myFullCases.get(i).staticTransactions.size()) {
                                    Date realTime = new Date();
                                    Date predictedTime = new Date();
                                    try {
                                        realTime = formatter.parse(testData.myFullCases.get(i).staticTransactions.get(j).data[myDataSet.timeIndex]);
                                        predictedTime = formatter.parse(predictions.get(i).staticTransactions.get(j).data[testData.timeIndex]);
                                    } catch (ParseException ex) {
                                        System.out.println("!" + ex.getMessage());
                                    }
                                    long realT = realTime.getTime();
                                    long predictedT = predictedTime.getTime();
                                    long diffTime = Math.abs(predictedT - realT);
                                    long diffEventDays = diffTime / (1000 * 60 * 60 * 24);
                                    timeInconformityDays[i] = timeInconformityDays[i] + Integer.parseInt(String.valueOf(diffEventDays));//EXPECTING DIFFERENCE TO BE IN INTEGER RANGE
                                } else {
                                    Date realTime = new Date();
                                    Date predictedTime = new Date();
                                    try {
                                        realTime = formatter.parse(testData.myFullCases.get(i).staticTransactions.get(testData.myFullCases.get(i).staticTransactions.size() - 1).data[myDataSet.timeIndex]);
                                        predictedTime = formatter.parse(predictions.get(i).staticTransactions.get(j).data[testData.timeIndex]);
                                    } catch (ParseException ex) {
                                        System.out.println("!!" + ex.getMessage());
                                    }
                                    long startTime = realTime.getTime();
                                    long endTime = predictedTime.getTime();
                                    long diffTime = Math.abs(endTime - startTime);
                                    long diffEventDays = diffTime / (1000 * 60 * 60 * 24);
                                    timeInconformityDays[i] = timeInconformityDays[i] + Integer.parseInt(String.valueOf(diffEventDays));//EXPECTING DIFFERENCE TO BE IN INTEGER RANGE
                                }
                            } else {
                                numEventInconformities[i] = 0;
                                timeInconformityDays[i] = 0;
                                System.out.println("NOTHING WAS KNOWN FROM TRANSACTION TO CALCULATE CONFORMITY!");
                                throw new IllegalArgumentException("NOTHING WAS KNOWN FROM TRANSACTION TO CALCULATE CONFORMITY!");
                            }
                        }
                    }
                }
            } else {
                for (int j = 0; j < testData.myFullCases.get(i).staticTransactions.size(); j++) {
                    if (j < predictions.get(i).staticTransactions.size()) {
                        if (!predictions.get(i).staticTransactions.get(j).currentEventName.equals("End")) {
                            if (predictions.get(i).staticTransactions.get(j).isPredicted == true) {
                                if (j > 0) {
                                    String currentEventName = null;
                                    currentEventName = predictions.get(i).staticTransactions.get(j).data[testData.eventIndex];
                                    if (!testData.myFullCases.get(i).staticTransactions.get(j).currentEventName.equals(currentEventName)) {
                                        numEventInconformities[i] = numEventInconformities[i] + 1;
                                    }

                                    for (int f = 0; f < testData.myFullCases.get(i).staticTransactions.get(j).data.length; f++) {
                                        if (testData.header.features.get(f) instanceof NormalFeature) {
                                            if (testData.header.features.get(f).type.equals("Numeric")) {
                                                if (predictions.get(i).staticTransactions.get(j).data[f].length() > 0 && testData.myFullCases.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(predictions.get(i).staticTransactions.get(j).data[f]) - Double.valueOf(testData.myFullCases.get(i).staticTransactions.get(j).data[f]));
                                                } else if (predictions.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(predictions.get(i).staticTransactions.get(j).data[f]));
                                                } else if (testData.myFullCases.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                                    featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(testData.myFullCases.get(i).staticTransactions.get(j).data[f]));
                                                }
                                            } else {
                                                if (!predictions.get(i).staticTransactions.get(j).data[f].equals(testData.myFullCases.get(i).staticTransactions.get(j).data[f])) {
                                                    featureInconformities[i] = featureInconformities[i] + 1;
                                                }
                                            }
                                        }
                                    }

                                    if (findEventIndex(currentEventName) < 0) {
                                        System.out.println("NEW EVENT FOUND!!!");
                                    }
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
                                    Date realTime = new Date();
                                    Date predictedTime = new Date();
                                    try {
                                        realTime = formatter.parse(testData.myFullCases.get(i).staticTransactions.get(j).data[myDataSet.timeIndex]);
                                        predictedTime = formatter.parse(predictions.get(i).staticTransactions.get(j).data[testData.timeIndex]);
                                    } catch (ParseException ex) {
                                        System.out.println("!!!" + ex.getMessage());
                                    }
                                    long startTime = realTime.getTime();
                                    long endTime = predictedTime.getTime();
                                    long diffTime = Math.abs(endTime - startTime);
                                    long diffEventDays = diffTime / (1000 * 60 * 60 * 24);
                                    timeInconformityDays[i] = timeInconformityDays[i] + Integer.parseInt(String.valueOf(diffEventDays));//EXPECTING DIFFERENCE TO BE IN INTEGER RANGE
                                } else {
                                    numEventInconformities[i] = 0;
                                    timeInconformityDays[i] = 0;
                                    System.out.println("NOTHING WAS KNOWN FROM TRANSACTION TO CALCULATE CONFORMITY!");
                                    throw new IllegalArgumentException("NOTHING WAS KNOWN FROM TRANSACTION TO CALCULATE CONFORMITY!");
                                }
                            }
                        }
                    } else {
                        numEventInconformities[i] = numEventInconformities[i] + 1;

                        for (int f = 0; f < testData.myFullCases.get(i).staticTransactions.get(j).data.length; f++) {
                            if (testData.header.features.get(f) instanceof NormalFeature) {
                                if (testData.header.features.get(f).type.equals("Numeric")) {
                                    if (testData.myFullCases.get(i).staticTransactions.get(j).data[f].length() > 0) {
                                        featureInconformities[i] = featureInconformities[i] + Math.abs(Double.valueOf(testData.myFullCases.get(i).staticTransactions.get(j).data[f]));
                                    }
                                } else {
                                    featureInconformities[i] = featureInconformities[i] + 1;
                                }
                            }
                        }

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
                        Date realTime = new Date();
                        Date predictedTime = new Date();
                        try {
                            realTime = formatter.parse(testData.myFullCases.get(i).staticTransactions.get(j).data[myDataSet.timeIndex]);
                            predictedTime = formatter.parse(predictions.get(i).staticTransactions.get(predictions.get(i).staticTransactions.size() - 1).data[testData.timeIndex]);
                        } catch (ParseException ex) {
                            System.out.println("!!!!" + ex.getMessage());
                        }
                        long startTime = realTime.getTime();
                        long endTime = predictedTime.getTime();
                        long diffTime = Math.abs(endTime - startTime);
                        long diffEventDays = diffTime / (1000 * 60 * 60 * 24);
                        timeInconformityDays[i] = timeInconformityDays[i] + Integer.parseInt(String.valueOf(diffEventDays));//EXPECTING DIFFERENCE TO BE IN INTEGER RANGE
                    }
                }
            }
        }

        int sumEventInconformity = 0;
        int sumTimeInconformity = 0;
        double sumFeatureInconformity = 0;
        for (int i = 0; i < numEventInconformities.length; i++) {
            sumEventInconformity = sumEventInconformity + numEventInconformities[i];
        }
        for (int i = 0; i < timeInconformityDays.length; i++) {
            sumTimeInconformity = sumTimeInconformity + timeInconformityDays[i];
        }
        for (int i = 0; i < featureInconformities.length; i++) {
            sumFeatureInconformity = sumFeatureInconformity + featureInconformities[i];
        }
        System.out.println("EVENT INCONFORMITY: " + sumEventInconformity);
        System.out.println("TIME INCONFORMITY: " + sumTimeInconformity);
        System.out.println("FEATURE INCONFORMITY: " + sumFeatureInconformity);

        return new TestResult(predictions, testData, numEventInconformities, timeInconformityDays, featureInconformities);
    }

    private FullCase predictFullCase(DataSet testData, FullCase input) {
        String lastEventName = input.staticTransactions.get(input.staticTransactions.size() - 1).data[testData.eventIndex];
        while (!lastEventName.equals("End")) {
            input.staticTransactions.add(predictIterate(testData, input.dynamicTransactions));

            input.dynamicTransactions.add(addTransaction(testData, input.staticTransactions.get(input.staticTransactions.size() - 1), input.dynamicTransactions.get(input.dynamicTransactions.size() - 1)));
            lastEventName = input.staticTransactions.get(input.staticTransactions.size() - 1).data[testData.eventIndex];
//            System.out.println(lastEventName);
        }
        return input;
    }

    private StaticTransaction predictIterate(DataSet testData, ArrayList<DynamicTransaction> input) {
        String lastEventName;
        String currentEventName = null;
        int validEventIndex = -1;
        for (int i = input.size() - 1; i > -1; i--) {
            currentEventName = input.get(i).data[testData.eventIndex];
            if (findEventIndex(currentEventName) > -1) {
                validEventIndex = i;
                break;
            }
        }
        if (validEventIndex == -1) {
            String terminationTransaction[] = new String[input.get(input.size() - 1).data.length];
            for (int i = 0; i < terminationTransaction.length; i++) {
                terminationTransaction[i] = "End";
            }
            System.out.println("THE TRANSACTIONS DON'T HAVE THE MINIMAL CONFORMITY!!!");
            StaticTransaction output = new StaticTransaction("", -1, -1, terminationTransaction);
            output.isPredicted = true;
            return output;
        } else {
            if (validEventIndex > 1) {
                lastEventName = input.get(validEventIndex - 1).data[testData.eventIndex];
            } else {
                lastEventName = "Start";
            }

            Event initEvent = events.get(findEventIndex(currentEventName));
            String str = initEvent.arffHeader;

            str = str + "@DATA" + "\n";

            for (int i = 0; i < input.get(input.size() - 1).data.length; i++) {
                if (i != testData.caseIndex && i != testData.eventIndex && i != testData.timeIndex) {
                    if (input.get(input.size() - 1).data[i].length() > 0) {
                        str = str + input.get(input.size() - 1).data[i];
                    } else {
                        str = str + "?";
                    }
                    str = str + ",";
                }
            }
            str = str + input.get(input.size() - 1).duration;
            str = str + ",";

            int inputIndex = findLinkIndex(lastEventName + "->" + currentEventName);
            if (inputIndex > -1) {
                for (int i = 0; i < initEvent.inputLinks.size(); i++) {
                    if (initEvent.inputLinks.get(i).name.equals(lastEventName + "->" + currentEventName)) {
                        str = str + "Input" + i;
                        break;
                    }
                }

            } else {
                str = str + "?";
            }

            str = str + ",";
            str = str + "?";

//            System.out.println(str);
            InputStream is = new ByteArrayInputStream(str.getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            ArffLoader.ArffReader arff;
            try {
                arff = new ArffLoader.ArffReader(br);
                Instances tempDatum = arff.getData();
                tempDatum.setClass(tempDatum.attribute("Output"));
                double result;
                if (initEvent.classifier != null) {
                    result = initEvent.classifier.classifyInstance(tempDatum.get(0));
                } else {
                    result = 0;
                }

                String nextStepRaw = tempDatum.attribute("Output").value((int) result);
                String nextStep[] = nextStepRaw.split("_");

//                System.out.println(nextStepRaw);
//                System.out.println(nextStep[1]);
//                System.out.println(nextStep[3]);
                int nextOutputLink = Integer.parseInt(nextStep[1]);
                int nextOutputCluster = Integer.parseInt(nextStep[3]);

                if (initEvent.outputLinks.get(nextOutputLink).name.split("->")[1].equals(initEvent.name)) {
                    System.out.println("DUPLICATE!!!");
                    System.out.println("initEvent.name: " + initEvent.name);
                    System.out.println("initEvent.outputLinks.get(nextOutputLink).name.split(\"->\")[1]: " + initEvent.outputLinks.get(nextOutputLink).name.split("->")[1]);
                    for (int i = 0; i < initEvent.outputLinks.size(); i++) {
                        if (!initEvent.outputLinks.get(i).name.split("->")[1].equals(initEvent.name)) {
                            nextOutputLink = i;
                            nextOutputCluster = 0;
                            System.out.println("Redirected into: " + initEvent.outputLinks.get(i).name.split("->")[1]);
                            break;
                        }
                    }
                }
//                System.out.println(initEvent.outputLinks.get(nextOutputLink).name);
//                System.out.println(initEvent.outputLinks.get(nextOutputLink).clusterCentroid.get(nextOutputCluster).data[0]);
                StaticTransaction output = initEvent.outputLinks.get(nextOutputLink).clusterCentroid.get(nextOutputCluster);
                output.data[testData.caseIndex] = input.get(0).data[testData.caseIndex];
                output.data[testData.eventIndex] = initEvent.outputLinks.get(nextOutputLink).name.split("->")[1];
                for (int i = 0; i < output.data.length; i++) {
                    if (output.data[i].equals("NaN")) {
                        output.data[i] = "";
                    }
                }
                output.currentEventName = output.data[testData.eventIndex];

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
                Date newTime = new Date();
                newTime = formatter.parse(input.get(input.size() - 1).data[myDataSet.timeIndex]);
                Calendar c = Calendar.getInstance();
                c.setTime(newTime);
                c.add(Calendar.DATE, (int) output.duration);
                Date newTimePlusDuration = c.getTime();
                output.data[testData.timeIndex] = formatter.format(newTimePlusDuration);
                output.isPredicted = true;
                return output;
            } catch (IOException | NumberFormatException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(SimpleNet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private DynamicTransaction addTransaction(DataSet testData, StaticTransaction staticTransaction, DynamicTransaction dynamicTransaction) {
        DynamicTransaction output = new DynamicTransaction(staticTransaction.indexId);
        output.data = new String[staticTransaction.data.length];
        for (int i = 0; i < dynamicTransaction.data.length; i++) {
            output.data[i] = new String();
            if (staticTransaction.data[i].length() > 0) {
                if (dynamicTransaction.data[i].length() > 0) {
                    if (testData.header.features.get(i).type.equals("Numeric")) {
                        output.data[i] = String.valueOf(Double.parseDouble(staticTransaction.data[i]) + Double.parseDouble(dynamicTransaction.data[i]));
                    } else {
                        output.data[i] = staticTransaction.data[i];
                    }
                } else {
                    output.data[i] = staticTransaction.data[i];
                }
            } else {
                if (dynamicTransaction.data[i].length() > 0) {
                    output.data[i] = dynamicTransaction.data[i];
                }
            }
        }
        output.duration = staticTransaction.duration + dynamicTransaction.duration;
        return output;
    }

    private int findEventIndex(String name) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private int findLinkIndex(String name) {
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void updateNet(FullCase newData) {

    }

}
