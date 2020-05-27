/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import MainModel.BaggingNet;
import MainModel.SimpleNet;
import java.io.Serializable;

/**
 *
 * @author Amir72c
 */
public class AllData implements Serializable{
    public SimpleNet simpleNet;
    public BaggingNet baggingNet;
    public RawDataSet rawDataSet;
    public DataSet dataSet;
    public SimpleNet simpleNetSummary;
}