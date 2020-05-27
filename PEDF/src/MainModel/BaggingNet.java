/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import Data.FullCase;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Amir72c
 */
public class BaggingNet implements Serializable {

    DataSet myDataSets[];
    public ArrayList<SimpleNet> nets = new ArrayList();
    public Date netDates[];

    public void trainNet(DataSet dataSets[]) {
        myDataSets = dataSets;
        netDates = new Date[dataSets.length];
        for (int i = 0; i < dataSets.length; i++) {
            SimpleNet tempNet = new SimpleNet();
            tempNet.trainNet(dataSets[i],"Naive bayes","Canopy",10,true);//NOT FLEXIBLE
            nets.add(tempNet);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = new Date();
            try {
                date = formatter.parse(dataSets[i].myTimedFullCases.get(0).staticTransactions.get(0).data[dataSets[i].timeIndex]);
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            netDates[i] = date;
        }
    }

    public TestResult[] testNet(DataSet testData, int knownCasePercent) {
        TestResult rawResults[] = new TestResult[nets.size()];
        for (int i = 0; i < nets.size(); i++) {
            rawResults[i] = nets.get(i).testNet(testData, knownCasePercent);
            int netSumEventInconformity=0;
            int netSumTimeInconformity=0;
            for(int j=0;j<rawResults[i].numEventInconformities.length;j++)
            {
                if(rawResults[i].numEventInconformities[j]>-1)
                {
                    netSumEventInconformity=netSumEventInconformity+rawResults[i].numEventInconformities[j];
                }
            }
            for(int j=0;j<rawResults[i].timeInconformityDays.length;j++)
            {
                if(rawResults[i].timeInconformityDays[j]>-1)
                {
                    netSumTimeInconformity=netSumTimeInconformity+rawResults[i].timeInconformityDays[j];
                }
            }
            System.out.println("NET NUM: "+i);
            System.out.println("EVENT INCONFORMITIES: "+netSumEventInconformity);
            System.out.println("TIME INCONFORMITIES: "+netSumTimeInconformity);
        }
        return rawResults;//UNDER CONSTRUCTION
    }
    
    public void updateNet(FullCase newData)
    {
        
    }

}
