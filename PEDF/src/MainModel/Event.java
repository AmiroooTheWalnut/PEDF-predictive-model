/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import Data.DynamicTransaction;
import Data.NormalFeature;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 *
 * @author Amir72c
 */
public class Event implements Serializable{

    public String name;
    public ArrayList<Link> inputLinks = new ArrayList();
    public ArrayList<Link> outputLinks = new ArrayList();
    public AbstractClassifier classifier;
    public ArrayList<DynamicTransaction> myGeneratedDataSet = new ArrayList();
    public String arffHeader;
//    public String arffInternalHeader;

    public Event(String passedName) {
        name = passedName;
    }

    public void trainEvent(DataSet dataSet, String modelName) throws Exception {
        Instances trainDataSet = generateWekaDataSet(dataSet);
        if (trainDataSet.attribute("Output").numValues() > 1) {
            trainDataSet.setClass(trainDataSet.attribute("Output"));
            classifier = ClassifierSelector.selectClassifier(modelName);
            classifier.buildClassifier(trainDataSet);
//            arffInternalHeader=((NaiveBayes)classifier).m_Instances.toString();//USING OF REVISED WEKA
        }else{
            classifier=null;
        }
    }

    public void gatherData() {
        for (int i = 0; i < inputLinks.size(); i++) {
            for (int j = 0; j < inputLinks.get(i).myDynamicData.size(); j++) {
                myGeneratedDataSet.add(new DynamicTransaction(inputLinks.get(i).myDynamicData.get(j).nextEventName, inputLinks.get(i).myDynamicData.get(j).indexId, inputLinks.get(i).myDynamicData.get(j).duration, inputLinks.get(i).myDynamicData.get(j).data));
            }
        }
    }

    private Instances generateWekaDataSet(DataSet dataSet) {
        Instances output = null;
        StringBuilder arffData = new StringBuilder();
        arffData.append("@RELATION data" + "\n");
        for (int i = 0; i < dataSet.header.features.size(); i++) {
            if (dataSet.header.features.get(i) instanceof NormalFeature) {
                NormalFeature currentFeature = (NormalFeature) dataSet.header.features.get(i);
                if (currentFeature.type.equals("Numeric")) {
                    arffData.append("@ATTRIBUTE ").append("'").append(currentFeature.name).append("'").append(" numeric").append("\n");
                } else {
                    arffData.append("@ATTRIBUTE ").append("'").append(currentFeature.name).append("'").append(" {");
                    for (int j = 0; j < currentFeature.categories.length; j++) {
                        arffData.append("'");
                        arffData.append(currentFeature.categories[j]);
                        arffData.append("'");
                        if (j != currentFeature.categories.length - 1) {
                            arffData.append(",");
                        }
                    }
                    arffData.append("}");
                    arffData.append("\n");
                }
            }
        }

        arffData.append("@ATTRIBUTE ").append("'").append("Duration").append("'").append(" numeric").append("\n");

        arffData.append("@ATTRIBUTE ").append("'").append("Input").append("'").append(" {");
        for (int i = 0; i < inputLinks.size(); i++) {
            arffData.append("'");
            arffData.append("Input").append(i);
            arffData.append("'");
            if (i != inputLinks.size() - 1) {
                arffData.append(",");
            }
        }
        arffData.append("}");
        arffData.append("\n");

        arffData.append("@ATTRIBUTE ").append("'").append("Output").append("'").append(" {");
        for (int i = 0; i < outputLinks.size(); i++) {
            for (int j = 0; j < outputLinks.get(i).numClusters; j++) {
                arffData.append("'");
                arffData.append("Output_").append(i).append("_").append("Cluster_").append(j);
                arffData.append("'");
                if (!((i == outputLinks.size() - 1) && (j == outputLinks.get(i).numClusters - 1))) {
                    arffData.append(",");
                }
            }
        }
        arffData.append("}");
        arffData.append("\n");
        
        arffHeader=arffData.toString();

        arffData.append("@DATA").append("\n");

        gatherData();

        for (int i = 0; i < inputLinks.size(); i++) {
            for (int j = 0; j < inputLinks.get(i).myDynamicData.size(); j++) {
                for (int k = 0; k < inputLinks.get(i).myDynamicData.get(j).data.length; k++) {
                    if (k != dataSet.caseIndex && k != dataSet.eventIndex && k != dataSet.timeIndex) {
                        if (inputLinks.get(i).myDynamicData.get(j).data[k].length() > 0) {
                            arffData.append(inputLinks.get(i).myDynamicData.get(j).data[k]);
                        } else {
                            arffData.append("?");
                        }
                        arffData.append(",");
                    }
                }
                arffData.append(inputLinks.get(i).myDynamicData.get(j).duration);
                arffData.append(",");
                arffData.append("Input").append(i);
                arffData.append(",");
                boolean success = false;
                for (int l = 0; l < outputLinks.size(); l++) {
                    for (int m = 0; m < outputLinks.get(l).myDynamicData.size(); m++) {
                        if (inputLinks.get(i).myDynamicData.get(j).indexId == outputLinks.get(l).myDynamicData.get(m).indexId || inputLinks.get(i).myDynamicData.get(j).indexId + 1 == outputLinks.get(l).myDynamicData.get(m).indexId) {
                            arffData.append("Output_").append(l).append("_").append("Cluster_").append((int) outputLinks.get(l).assignments[m]);
                            success = true;
                            break;
                        }
                    }
                    if(success==true)
                    {
                        break;
                    }
                }
                if (success == false) {
                    System.out.println("NOT FOUND!!!");
//                    System.out.println("inputLinks.get(i).name: " + inputLinks.get(i).name);
//                    System.out.println("inputLinks.get(i).myDynamicData.get(j).data[0]: " + inputLinks.get(i).myDynamicData.get(j).data[0]);
//                    System.out.println("inputLinks.get(i).myDynamicData.get(j).indexId: " + inputLinks.get(i).myDynamicData.get(j).indexId);
                }
                arffData.append("\n");
            }
        }

        String str = arffData.toString();

//        System.out.println(str);

        InputStream is = new ByteArrayInputStream(str.getBytes());

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        ArffLoader.ArffReader arff;
        try {
            arff = new ArffLoader.ArffReader(br);
            output = arff.getData();
        } catch (IOException | NumberFormatException ex) {
            System.out.println(ex.getMessage());
        }
        return output;
    }

    public boolean isLinkInputUnique(String name) {
        for (int i = 0; i < inputLinks.size(); i++) {
            if (inputLinks.get(i).name.equals(name)) {
                return false;
            }
        }
        return true;
    }

    public boolean isLinkOutputUnique(String name) {
        for (int i = 0; i < outputLinks.size(); i++) {
            if (outputLinks.get(i).name.equals(name)) {
                return false;
            }
        }
        return true;
    }

}
