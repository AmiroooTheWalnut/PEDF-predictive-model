/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import java.util.ArrayList;

/**
 *
 * @author Amir72c
 */
public class Simulator {
    
    int batchSize;
    int numFolds;
    DataSet allDataSet;
    ArrayList<DataSet> passedFixedDataSets=new ArrayList();
    DataSet fillingDataSet;
    String netType;
    int knownPercent;
    SimpleNet sn;
    ArrayList<TestResult> results=new ArrayList();
    
    Simulator(int passed_batchSize,int passed_numFolds,DataSet passed_allDataSet,String passed_netType,int passed_knownPercent)
    {
        batchSize=passed_batchSize;
        netType=passed_netType;
        knownPercent=passed_knownPercent;
    }
    
    public void run()
    {
        fillingDataSet=allDataSet.emptyClone();
        for(int i=0;i<allDataSet.myTimedFullCases.size();i++)
        {
            if(fillingDataSet.myTimedFullCases.size()>batchSize)
            {
                passedFixedDataSets.add(fillingDataSet.deepClone());
                fillingDataSet=allDataSet.emptyClone();
                sn=new SimpleNet();
                sn.trainNet(passedFixedDataSets.get(passedFixedDataSets.size()-1),"Naive bayes","Canopy",10,8);//NOT FLEXIBLE
            }else{
                fillingDataSet.myFullCases.add(allDataSet.myFullCases.get(i).deepClone());
                fillingDataSet.myTimedFullCases.add(allDataSet.myTimedFullCases.get(i).deepClone());
                if(passedFixedDataSets.size()>0)
                {
                    if(netType.toLowerCase().equals("singlenet"))
                    {
                        results.add(sn.testNet(fillingDataSet, knownPercent));
                        
                    }else if(netType.toLowerCase().equals("baggingnet")){
                        
                    }else{
                        System.out.println("NET TYPE IS INCORRECT!");
                    }
                }
            }
        }
    }
}
