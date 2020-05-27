/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import java.io.Serializable;

/**
 *
 * @author Amir72c
 */
public class Transaction implements Serializable{
    public long duration;
    public String[] data;
    public String currentEventName;
    public String nextEventName;
    public int indexId;
    public boolean isPredicted=false;
    public Transaction(int passed_indexID)
    {
        indexId=passed_indexID;
    }
    
    public Transaction(String passed_nextEventName,int passed_indexID,long passed_duration,String[] passed_date)
    {
        indexId=passed_indexID;
        duration=passed_duration;
        data=passed_date;
        nextEventName=passed_nextEventName;
    }
}
