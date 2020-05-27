/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import Data.DynamicTransaction;
import Data.NormalFeature;
import Data.StaticTransaction;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Canopy;
import weka.clusterers.CascadeSimpleKMeans;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.XMeans;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author Amir72c
 */
public class Link implements Serializable {

    public String name;
    public AbstractClusterer clusterer;
    public ArrayList<DynamicTransaction> myDynamicData = new ArrayList();
    public ArrayList<StaticTransaction> myStaticData = new ArrayList();
    public double assignments[];
    public int numClusters;
    public ArrayList<StaticTransaction> clusterCentroid = new ArrayList();
    
    public int minimumRecordToCluster=20;

    public Link(String passedName) {
        name = passedName;
    }

    public void trainLink(DataSet dataSet, String modelName, int passed_numClusters) throws Exception {
        Instances trainInstances = generateWekaDataSet(dataSet);
        if (myStaticData.size() > minimumRecordToCluster) {
            //Heavy algorithm
//            clusterer = new EM();
//            ((EM) clusterer).setNumExecutionSlots(6);
//            ((EM) clusterer).setMaxIterations(10);
//            ((EM) clusterer).setMinStdDev(0.01);
//            ((EM) clusterer).setMaximumNumberOfClusters(20);

            //Medium algorithm
//            clusterer=new CascadeSimpleKMeans();
//            ((CascadeSimpleKMeans) clusterer).setMaxNumClusters(20);
//            ((CascadeSimpleKMeans) clusterer).setMinNumClusters(2);
//            ((CascadeSimpleKMeans) clusterer).setRestarts(5);
            
            //Light algorithm
            clusterer = ClustererSelector.selectClusterer(modelName,passed_numClusters);

            clusterer.buildClusterer(trainInstances);

            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);
            eval.evaluateClusterer(trainInstances);
            assignments = eval.getClusterAssignments();
            numClusters = eval.getNumClusters();
        } else {
            numClusters = 1;
            assignments = new double[myStaticData.size()];
            for (int j = 0; j < myStaticData.size(); j++) {
                assignments[j] = 0;
            }
        }
        checkAssignments();
        calculateCentroids(dataSet, trainInstances);
    }

    private void checkAssignments() {
        boolean isValid[] = new boolean[numClusters];
        for (int i = 0; i < isValid.length; i++) {
            isValid[i] = false;
        }
        for (int i = 0; i < assignments.length; i++) {
            isValid[(int) assignments[i]] = true;
        }
        boolean isCorrect = true;
        for (int i = 0; i < isValid.length; i++) {
            if (isValid[i] == false) {
                isCorrect = false;
                break;
            }
        }
        if (isCorrect == false) {
            int newClusterIndices[] = new int[numClusters];
            int tempNumClusters = 0;
            for (int i = 0; i < isValid.length; i++) {
                if (isValid[i] == true) {
                    newClusterIndices[i] = tempNumClusters;
                    tempNumClusters = tempNumClusters + 1;
                }
            }
            numClusters = tempNumClusters;
            for (int i = 0; i < assignments.length; i++) {
                assignments[i] = newClusterIndices[(int) assignments[i]];
            }
        }
    }

    private void calculateCentroids(DataSet dataSet, Instances trainInstances) {
        Instances clusters[] = new Instances[numClusters];
        for (int j = 0; j < assignments.length; j++) {
            if (clusters[(int) assignments[j]] == null) {
                clusters[(int) assignments[j]] = new Instances(trainInstances, j, 1);
            } else {
                clusters[(int) assignments[j]].add(trainInstances.get(j));
            }
        }
        for (int c = 0; c < clusters.length; c++) {
            StaticTransaction center = new StaticTransaction(-1);
            center.data = new String[dataSet.header.features.size()];
            for (int f = 0; f < dataSet.header.features.size(); f++) {
                boolean isFoundAttribute = false;
                for (int f_p = 0; f_p < clusters[c].numAttributes(); f_p++) {
                    if (clusters[c].attribute(f_p).name().equals(dataSet.header.features.get(f).name)) {
                        isFoundAttribute = true;
                        if (clusters[c].attribute(f_p).isNumeric()) {
                            center.data[f] = String.valueOf(clusters[c].attributeStats(f_p).numericStats.mean);
                        } else if (clusters[c].attribute(f_p).isNominal()) {
                            int maxFreqIndex = -1;
                            int maxFreqValue = Integer.MIN_VALUE;
                            for (int nc = 0; nc < clusters[c].attributeStats(f_p).nominalCounts.length; nc++) {
                                if (clusters[c].attributeStats(f_p).nominalCounts[nc] > maxFreqValue) {
                                    maxFreqIndex = nc;
                                    maxFreqValue = clusters[c].attributeStats(f_p).nominalCounts[nc];
                                }
                            }
                            center.data[f] = clusters[c].attribute(f_p).value(maxFreqIndex);
                        }
                        center.duration = (long) clusters[c].attributeStats(clusters[c].attribute("Duration").index()).numericStats.mean;
                        break;
                    }
                }
                if (isFoundAttribute == false) {
                    center.data[f] = "NaN";
                }

            }
            clusterCentroid.add(center);
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

        arffData.append("@DATA").append("\n");

        for (int i = 0; i < myStaticData.size(); i++) {

            //System.out.println("data columns: "+myParent.baseDataDetails.data[i].length);
            for (int j = 0; j < myStaticData.get(i).data.length; j++) {
                if (j != dataSet.caseIndex && j != dataSet.eventIndex && j != dataSet.timeIndex) {
                    if (myStaticData.get(i).data[j].length() > 0) {
                        arffData.append(myStaticData.get(i).data[j]);
                    } else {
                        arffData.append("?");
                    }
                    arffData.append(",");
                }
            }
            arffData.append(myStaticData.get(i).duration);
            arffData.append("\n");
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
}
