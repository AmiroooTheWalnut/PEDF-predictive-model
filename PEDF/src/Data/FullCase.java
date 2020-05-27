/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Amir72c
 */
public class FullCase implements Comparable<FullCase>, Serializable{

    public ArrayList<StaticTransaction> staticTransactions = new ArrayList();
    public ArrayList<DynamicTransaction> dynamicTransactions = new ArrayList();
    int timeIndex;

    public FullCase(int passedTimeIndex) {
        timeIndex = passedTimeIndex;
    }

    public FullCase(int passedTimeIndex,ArrayList<StaticTransaction> passedStaticTransactions,ArrayList<DynamicTransaction> passedDynamicTransactions) {
        timeIndex = passedTimeIndex;
        staticTransactions=passedStaticTransactions;
        dynamicTransactions=passedDynamicTransactions;
    }
    
    public FullCase deepClone()
    {
        FullCase output=new FullCase(timeIndex);
        for(int i=0;i<staticTransactions.size();i++)
        {
            String tempData[]=new String[staticTransactions.get(i).data.length];
            for(int j=0;j<staticTransactions.get(i).data.length;j++)
            {
                tempData[j]=staticTransactions.get(i).data[j];
            }
            output.staticTransactions.add(new StaticTransaction(staticTransactions.get(i).nextEventName,staticTransactions.get(i).indexId,staticTransactions.get(i).duration,tempData));
        }
        return output;
    }

    @Override
    public int compareTo(FullCase other) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.sss");
        Date selfDate = new Date();
        try {
            selfDate = formatter.parse(staticTransactions.get(0).data[timeIndex]);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
        Date otherDate = new Date();
        try {
            otherDate = formatter.parse(other.staticTransactions.get(0).data[timeIndex]);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
        long selfTime = selfDate.getTime();
        long otherTime = otherDate.getTime();
        return Long.compare(selfTime, otherTime);
    }

}
