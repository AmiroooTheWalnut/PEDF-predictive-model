/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import MainModel.Event;
import MainModel.Link;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Amir72c
 */
public class DataSetProcessor {

    public static DataSet generateDataSet(int caseIndex, int eventIndex, int timeIndex, RawDataSet rawDataSet) {
        DataSet output = new DataSet();
        Header header = new Header();
        header.features = extractFeatures(caseIndex, eventIndex, timeIndex, rawDataSet);
        output.header = header;
        output.myFullCases = extractFullCases(caseIndex, eventIndex, timeIndex, rawDataSet);
        output.myTimedFullCases = extractTimedFullCases(output.myFullCases);
        output.caseIndex = caseIndex;
        output.eventIndex = eventIndex;
        output.timeIndex = timeIndex;
        return output;
    }

    public static ArrayList<FullCase> extractTimedFullCases(ArrayList<FullCase> fullCases) {
        ArrayList<FullCase> output = new ArrayList();
        for (int i = 0; i < fullCases.size(); i++) {
            output.add(new FullCase(fullCases.get(i).timeIndex, fullCases.get(i).staticTransactions, fullCases.get(i).dynamicTransactions));
        }
        Collections.sort(output);
        return output;
    }

    public static ArrayList<Event> extractEvents(DataSet dataSet) {
        ArrayList<Event> output = new ArrayList();
        output.add(new Event("Start"));
        for (int i = 0; i < dataSet.myFullCases.size(); i++) {
            for (int j = 0; j < dataSet.myFullCases.get(i).staticTransactions.size(); j++) {
                String temporaryEventName = dataSet.myFullCases.get(i).staticTransactions.get(j).data[dataSet.eventIndex];
                if (isUniqueEvent(temporaryEventName, output) == true) {
                    output.add(new Event(temporaryEventName));
                }
            }
        }
        output.add(new Event("End"));
        return output;
    }

    public static ArrayList<Link> extractLinks(DataSet dataSet) {
        ArrayList<Link> output = new ArrayList();
        for (int i = 0; i < dataSet.myFullCases.size(); i++) {
            if (dataSet.myFullCases.get(i).staticTransactions.size() > 2) {
                for (int j = 0; j < dataSet.myFullCases.get(i).staticTransactions.size(); j++) {
                    String temporaryLinkName;
                    if (j == 0) {
                        temporaryLinkName = "Start" + "->" + dataSet.myFullCases.get(i).staticTransactions.get(j).data[dataSet.eventIndex];
                        if (isUniqueLink(temporaryLinkName, output) == true) {
                            output.add(new Link(temporaryLinkName));
                        }
                    } else if (j == dataSet.myFullCases.get(i).staticTransactions.size() - 1) {

                        temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(j - 1).data[dataSet.eventIndex] + "->" + dataSet.myFullCases.get(i).staticTransactions.get(j).data[dataSet.eventIndex];
                        if (isUniqueLink(temporaryLinkName, output) == true) {
                            output.add(new Link(temporaryLinkName));
                        }

                        temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(j).data[dataSet.eventIndex] + "->" + "End";
                        if (isUniqueLink(temporaryLinkName, output) == true) {
                            output.add(new Link(temporaryLinkName));
                        }
                    } else {
                        temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(j - 1).data[dataSet.eventIndex] + "->" + dataSet.myFullCases.get(i).staticTransactions.get(j).data[dataSet.eventIndex];
                        if (isUniqueLink(temporaryLinkName, output) == true) {
                            output.add(new Link(temporaryLinkName));
                        }
                    }

                }
            } else if (dataSet.myFullCases.get(i).staticTransactions.size() == 2) {
                String temporaryLinkName;
                temporaryLinkName = "Start" + "->" + dataSet.myFullCases.get(i).staticTransactions.get(0).data[dataSet.eventIndex];
                if (isUniqueLink(temporaryLinkName, output) == true) {
                    output.add(new Link(temporaryLinkName));
                }
                temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(0).data[dataSet.eventIndex] + "->" + dataSet.myFullCases.get(i).staticTransactions.get(1).data[dataSet.eventIndex];
                if (isUniqueLink(temporaryLinkName, output) == true) {
                    output.add(new Link(temporaryLinkName));
                }
                temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(1).data[dataSet.eventIndex] + "->" + "End";
                if (isUniqueLink(temporaryLinkName, output) == true) {
                    output.add(new Link(temporaryLinkName));
                }
            } else if (dataSet.myFullCases.get(i).staticTransactions.size() == 1) {
                String temporaryLinkName;
                temporaryLinkName = "Start" + "->" + dataSet.myFullCases.get(i).staticTransactions.get(0).data[dataSet.eventIndex];
                if (isUniqueLink(temporaryLinkName, output) == true) {
                    output.add(new Link(temporaryLinkName));
                }
                temporaryLinkName = dataSet.myFullCases.get(i).staticTransactions.get(0).data[dataSet.eventIndex] + "->" + "End";
                if (isUniqueLink(temporaryLinkName, output) == true) {
                    output.add(new Link(temporaryLinkName));
                }
            }
        }
        return output;
    }

    private static ArrayList<FullCase> extractFullCases(int caseIndex, int eventIndex, int timeIndex, RawDataSet rawDataSet) {
        ArrayList<FullCase> output = new ArrayList();
        String currentCase = rawDataSet.rawData[0][caseIndex];
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date initCaseTime = new Date();
        try {
            initCaseTime = formatter.parse(rawDataSet.rawData[0][timeIndex]);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
        Date previousEventTime = new Date(initCaseTime.getTime());
        FullCase fullCase = new FullCase(timeIndex);
        StaticTransaction staticTransaction = new StaticTransaction(0);
        DynamicTransaction dynamicTransaction = new DynamicTransaction(0);
        staticTransaction.data = rawDataSet.rawData[0];
        staticTransaction.duration = 0;
        staticTransaction.currentEventName=staticTransaction.data[eventIndex];
        dynamicTransaction.data = rawDataSet.rawData[0];
        dynamicTransaction.duration = 0;
        dynamicTransaction.currentEventName=dynamicTransaction.data[eventIndex];
        fullCase.staticTransactions.add(staticTransaction);
        fullCase.dynamicTransactions.add(dynamicTransaction);
        for (int i = 1; i < rawDataSet.rawData.length; i++) {
            if (rawDataSet.rawData[i][caseIndex].equals(currentCase)) {
                staticTransaction = new StaticTransaction(i);
                staticTransaction.data = rawDataSet.rawData[i];
                staticTransaction.currentEventName=staticTransaction.data[eventIndex];
                Date currentCaseTime = new Date();
                try {
                    currentCaseTime = formatter.parse(rawDataSet.rawData[i][timeIndex]);
                } catch (ParseException ex) {
                    System.out.println(ex.getMessage());
                }
                long startTime = previousEventTime.getTime();
                long endTime = currentCaseTime.getTime();
                long diffTime = endTime - startTime;
                long diffEventDays = diffTime / (1000 * 60 * 60 * 24);
                staticTransaction.duration = diffEventDays;
                dynamicTransaction = addTransaction(staticTransaction, dynamicTransaction, rawDataSet);
                fullCase.staticTransactions.add(staticTransaction);
                fullCase.dynamicTransactions.add(dynamicTransaction);
                previousEventTime = new Date(currentCaseTime.getTime());
            } else {
                output.add(fullCase);
                currentCase = rawDataSet.rawData[i][caseIndex];
                initCaseTime = new Date();
                try {
                    initCaseTime = formatter.parse(rawDataSet.rawData[i][timeIndex]);
                } catch (ParseException ex) {
                    System.out.println(ex.getMessage());
                }
                previousEventTime = new Date(initCaseTime.getTime());
                fullCase = new FullCase(timeIndex);
                staticTransaction = new StaticTransaction(i);
                dynamicTransaction = new DynamicTransaction(i);
                staticTransaction.data = rawDataSet.rawData[i];
                staticTransaction.duration = 0;
                staticTransaction.currentEventName=staticTransaction.data[eventIndex];
                dynamicTransaction.data = rawDataSet.rawData[i];
                dynamicTransaction.duration = 0;
                dynamicTransaction.currentEventName=dynamicTransaction.data[eventIndex];
                fullCase.staticTransactions.add(staticTransaction);
                fullCase.dynamicTransactions.add(dynamicTransaction);
            }
        }
        output = processNextEventInfo(output, eventIndex);
        return output;
    }

    private static ArrayList<FullCase> processNextEventInfo(ArrayList<FullCase> fullCases, int eventIndex) {
        for (int i = 0; i < fullCases.size(); i++) {
            for (int j = 0; j < fullCases.get(i).dynamicTransactions.size(); j++) {
                fullCases.get(i).dynamicTransactions.get(j).currentEventName = fullCases.get(i).dynamicTransactions.get(j).data[eventIndex];
                if (j == fullCases.get(i).dynamicTransactions.size() - 1) {
                    fullCases.get(i).dynamicTransactions.get(j).nextEventName = "End";
                    fullCases.get(i).staticTransactions.get(j).nextEventName = "End";
                } else {
                    fullCases.get(i).dynamicTransactions.get(j).nextEventName = fullCases.get(i).dynamicTransactions.get(j + 1).data[eventIndex];
                    fullCases.get(i).staticTransactions.get(j).nextEventName = fullCases.get(i).staticTransactions.get(j + 1).data[eventIndex];
                }
            }
        }
        return fullCases;
    }

    public static DynamicTransaction addTransaction(StaticTransaction staticTransaction, DynamicTransaction dynamicTransaction, RawDataSet rawDataSet) {
        DynamicTransaction output = new DynamicTransaction(staticTransaction.indexId);
        output.data = new String[staticTransaction.data.length];
        for (int i = 0; i < dynamicTransaction.data.length; i++) {
            output.data[i] = new String();
            if (staticTransaction.data[i].length() > 0) {
                if (dynamicTransaction.data[i].length() > 0) {
                    if (rawDataSet.types[i].equals("Numeric")) {
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

    private static ArrayList<Feature> extractFeatures(int caseIndex, int eventIndex, int timeIndex, RawDataSet rawDataSet) {
        ArrayList<Feature> output = new ArrayList();
        for (int i = 0; i < rawDataSet.headers.length; i++) {
            if (i != caseIndex && i != eventIndex && i != timeIndex) {
                NormalFeature feature = new NormalFeature();
                feature.name = rawDataSet.headers[i];
                feature.type = rawDataSet.types[i];
                if (feature.type.equals("Numeric")) {
                    double minValue = Double.POSITIVE_INFINITY;
                    double maxValue = Double.NEGATIVE_INFINITY;
                    for (int j = 0; j < rawDataSet.rawData.length; j++) {
                        if (rawDataSet.rawData[j][i].length() > 0) {
                            double value = Double.parseDouble(rawDataSet.rawData[j][i]);
                            if (value > maxValue) {
                                maxValue = value;
                            }
                            if (value < minValue) {
                                minValue = value;
                            }
                        }
                    }
                    feature.maxValue = maxValue;
                    feature.minValue = minValue;
                } else {
                    ArrayList<String> foundCategories = new ArrayList();
                    for (int j = 0; j < rawDataSet.rawData.length; j++) {
                        if (isUniqueCategory(rawDataSet.rawData[j][i], foundCategories) == true) {
                            foundCategories.add(rawDataSet.rawData[j][i]);
                        }
                    }
                    feature.categories = foundCategories.toArray(new String[foundCategories.size()]);
                }
                output.add(feature);
            } else {
                Feature feature = new Feature();
                feature.name = rawDataSet.headers[i];
                feature.type = rawDataSet.types[i];
                output.add(feature);
            }
        }
        return output;
    }

    private static boolean isUniqueEvent(String event, ArrayList<Event> events) {
        boolean isUnique = true;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).name.equals(event)) {
                isUnique = false;
            }
        }
        return isUnique;
    }

    private static boolean isUniqueLink(String link, ArrayList<Link> links) {
        boolean isUnique = true;
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).name.equals(link)) {
                isUnique = false;
            }
        }
        return isUnique;
    }

    private static boolean isUniqueCategory(String category, ArrayList<String> foundCategories) {
        boolean isUnique = true;
        for (int i = 0; i < foundCategories.size(); i++) {
            if (foundCategories.get(i).equals(category)) {
                isUnique = false;
            }
        }
        return isUnique;
    }
}
