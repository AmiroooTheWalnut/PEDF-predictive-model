/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Amir72c
 */
public class RawDataReader {

    public RawDataSet readRawData(String filePath) {
        RawDataSet output = new RawDataSet();
        File rawDataFile = new File(filePath);
        output.headers = readRawHeader(rawDataFile);
        output.types = readRawTypes(rawDataFile, 500);
        output.rawData = readRawMainData(rawDataFile);
        return output;
    }

    private String[] readRawHeader(File file) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String rawHeader = br.readLine();
            String output[] = rawHeader.split(",");
            br.close();
            return output;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private String[] readRawTypes(File file, int sampleSize) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String currentLine;
            String rawHeader = br.readLine();//READING HEADER
            String output[] = new String[rawHeader.split(",").length];
            for (int i = 0; i < output.length; i++) {
                output[i] = "Numeric";//INIT THE TYPE ARRAY
            }
            int lineCounter = 0;
            while ((currentLine = br.readLine()) != null && lineCounter < sampleSize) {
                String values[] = currentLine.split(",", output.length);
                for (int i = 0; i < output.length; i++) {
                    if (isNumeric(values[i]) == false && output[i].equals("Numeric") && values[i].length()>0) {
                        output[i] = "Categorical";
                    }
                }
                lineCounter = lineCounter + 1;
            }
            br.close();
            return output;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private String[][] readRawMainData(File file) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String currentLine;
            String rawHeader[] = br.readLine().split(",");//SKIPING HEADER
            int numRows = 0;
            while (br.readLine() != null) {
                numRows = numRows + 1;
            }
            String data[][] = new String[numRows][rawHeader.length];
            br.close();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            br.readLine();//SKIPING HEADER
            int currentRowNumber = 0;
            while ((currentLine = br.readLine()) != null) {
                String row[] = currentLine.split(",", rawHeader.length);
                data[currentRowNumber] = row;
                currentRowNumber = currentRowNumber + 1;
            }
            return data;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
