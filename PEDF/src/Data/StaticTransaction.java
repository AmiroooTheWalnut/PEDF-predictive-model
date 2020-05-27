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
public class StaticTransaction extends Transaction implements Serializable{
    
    public StaticTransaction(int passed_indexID)
    {
        super(passed_indexID);
    }
    
    public StaticTransaction(String passed_nextEventName,int passed_indexID,long passed_duration,String[] passed_date)
    {
        super(passed_nextEventName,passed_indexID,passed_duration,passed_date);
    }
    
}
