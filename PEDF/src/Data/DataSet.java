/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Amir72c
 */
public class DataSet implements Serializable {

    public int caseIndex;
    public int eventIndex;
    public int timeIndex;
    public Header header;
    public ArrayList<FullCase> myFullCases = new ArrayList();
    public ArrayList<FullCase> myTimedFullCases = new ArrayList();

    public DataSet randomSample(double percent) {
        DataSet output = new DataSet();
        output.caseIndex = caseIndex;
        output.eventIndex = eventIndex;
        output.timeIndex = timeIndex;
        output.header = header;
        ArrayList<Integer> indexes = new ArrayList();
        for (int i = 0; i < myFullCases.size(); i++) {
            indexes.add(i);
        }
        for (int i = 0; i < ((float) percent / 100f) * myFullCases.size(); i++) {
            int randomIndex = (int) Math.floor(Math.random() * indexes.size());
            int tempIndex = indexes.get(randomIndex);
            output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(tempIndex).staticTransactions, myFullCases.get(tempIndex).dynamicTransactions));
            indexes.remove(randomIndex);
        }
        output.myTimedFullCases = DataSetProcessor.extractTimedFullCases(output.myFullCases);
        return output;
    }

    public DataSet linearSample(double percent, boolean fromEnd) {
        DataSet output = new DataSet();
        output.caseIndex = caseIndex;
        output.eventIndex = eventIndex;
        output.timeIndex = timeIndex;
        output.header = header;
        if (fromEnd == false) {
            for (int i = 0; i < ((float) percent / 100f) * myFullCases.size(); i++) {
                output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(i).staticTransactions, myFullCases.get(i).dynamicTransactions));
            }
        } else {
            for (int i = myFullCases.size() - 1; i > Math.round(((float) (100 - percent) / 100f) * myFullCases.size()); i--) {
                output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(i).staticTransactions, myFullCases.get(i).dynamicTransactions));
            }
        }
        output.myTimedFullCases = DataSetProcessor.extractTimedFullCases(output.myFullCases);
        return output;
    }

    public DataSet linearSample(double percent, boolean fromEnd, boolean timeSorted) {
        DataSet output = new DataSet();
        output.caseIndex = caseIndex;
        output.eventIndex = eventIndex;
        output.timeIndex = timeIndex;
        output.header = header;
        if (timeSorted == true) {
            if (fromEnd == false) {
                for (int i = 0; i < ((float) percent / 100f) * myFullCases.size(); i++) {
                    output.myFullCases.add(new FullCase(timeIndex, myTimedFullCases.get(i).staticTransactions, myTimedFullCases.get(i).dynamicTransactions));
                }
            } else {
                for (int i = myFullCases.size() - 1; i > Math.round(((float) (100 - percent) / 100f) * myFullCases.size()); i--) {
                    output.myFullCases.add(new FullCase(timeIndex, myTimedFullCases.get(i).staticTransactions, myTimedFullCases.get(i).dynamicTransactions));
                }
            }
        } else {
            if (fromEnd == false) {
                for (int i = 0; i < ((float) percent / 100f) * myFullCases.size(); i++) {
                    output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(i).staticTransactions, myFullCases.get(i).dynamicTransactions));
                }
            } else {
                for (int i = myFullCases.size() - 1; i > Math.round(((float) (100 - percent) / 100f) * myFullCases.size()); i--) {
                    output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(i).staticTransactions, myFullCases.get(i).dynamicTransactions));
                }
            }
        }
        output.myTimedFullCases = DataSetProcessor.extractTimedFullCases(output.myFullCases);
        return output;
    }

    private DataSet linearSample(double startPrecent, double endPercent, boolean timeSorted) {
        DataSet output = new DataSet();
        output.caseIndex = caseIndex;
        output.eventIndex = eventIndex;
        output.timeIndex = timeIndex;
        output.header = header;
        if (timeSorted == false) {
            for (int i = (int) (((float) startPrecent / 100f) * myFullCases.size()); i < ((float) endPercent / 100f) * myFullCases.size(); i++) {
                output.myFullCases.add(new FullCase(timeIndex, myFullCases.get(i).staticTransactions, myFullCases.get(i).dynamicTransactions));
            }
        } else {
            for (int i = (int) (((float) startPrecent / 100f) * myFullCases.size()); i < ((float) endPercent / 100f) * myFullCases.size(); i++) {
                output.myFullCases.add(new FullCase(timeIndex, myTimedFullCases.get(i).staticTransactions, myTimedFullCases.get(i).dynamicTransactions));
            }
        }
        output.myTimedFullCases = DataSetProcessor.extractTimedFullCases(output.myFullCases);
        return output;
    }

    public DataSet[] randomSubSamples(double percent, int numGroups) {
        DataSet output[] = new DataSet[numGroups];
        for (int g = 0; g < numGroups; g++) {
            output[g] = randomSample(percent / numGroups);
        }
        return output;
    }

    public DataSet[] linearSubSamples(double percent, int numGroups, boolean fromEnd, boolean timeSorted) {
        DataSet output[] = new DataSet[numGroups];
        if (fromEnd == false) {
            for (int g = 0; g < numGroups; g++) {
                output[g] = linearSample((percent / numGroups) * g, (percent / numGroups) * (g + 1), timeSorted);
            }
        } else {
            for (int g = 0; g < numGroups; g++) {
                output[g] = linearSample(100 - (percent / numGroups) * (g + 1), 100 - (percent / numGroups) * g, timeSorted);
            }
        }
        return output;
    }

    public DataSet emptyClone() {
        DataSet output = new DataSet();
        output.caseIndex = this.caseIndex;
        output.eventIndex = this.eventIndex;
        output.timeIndex = this.timeIndex;
        output.header = this.header;

        return output;
    }

    public DataSet deepClone() {
        DataSet output = new DataSet();
        output.caseIndex = this.caseIndex;
        output.eventIndex = this.eventIndex;
        output.timeIndex = this.timeIndex;
        output.header = this.header;
        for (int i = 0; i < this.myFullCases.size(); i++) {
            output.myFullCases.add(this.myFullCases.get(i).deepClone());
            output.myTimedFullCases.add(this.myTimedFullCases.get(i).deepClone());
        }
        return output;
    }

    public DataSet deepClone(int startIndex, int endIndex) {
        DataSet output = new DataSet();
        output.caseIndex = this.caseIndex;
        output.eventIndex = this.eventIndex;
        output.timeIndex = this.timeIndex;
        output.header = this.header;
        for (int i = startIndex; i < endIndex; i++) {
            output.myFullCases.add(this.myFullCases.get(i).deepClone());
            output.myTimedFullCases.add(this.myTimedFullCases.get(i).deepClone());
        }
        return output;
    }

}
