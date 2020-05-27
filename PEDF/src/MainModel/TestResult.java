/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import Data.DataSet;
import Data.FullCase;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Amir72c
 */
public class TestResult implements Serializable {

    public DataSet realData;
    public ArrayList<FullCase> predictions;
    public double realDurations[];
    public double predictedDurations[];
//    public double durationMeanAbsoluteError;
//    public double durationRootMeanSquaredErrorPercentage;
//    public double durationMeanAbsolutePercentageError;
    public double sumDurationError;
    public double sumEventError;
    public double sumFeatureError;
    public int numEventInconformities[];
    public int timeInconformityDays[];
    public double featureInconformities[];

    private String resultsInString;

    TestResult(ArrayList<FullCase> passed_predictions, DataSet passed_realData, int passed_numEventInconformities[], int passed_timeInconformityDays[], double passed_featureInconformities[]) {
        predictions = passed_predictions;
        realData = passed_realData;
        numEventInconformities = passed_numEventInconformities;
        timeInconformityDays = passed_timeInconformityDays;
        featureInconformities=passed_featureInconformities;
        calcMeasures();
        generateResultsInString();
    }

    private void calcMeasures() {

        sumEventError = 0;
        sumDurationError = 0;
        sumFeatureError=0;
        for (int j = 0; j < numEventInconformities.length; j++) {
            if (numEventInconformities[j] > -1) {
                sumEventError = sumEventError + numEventInconformities[j];
            }
        }
        for (int j = 0; j < timeInconformityDays.length; j++) {
            if (timeInconformityDays[j] > -1) {
                sumDurationError = sumDurationError + timeInconformityDays[j];
            }
        }
        for (int j = 0; j < featureInconformities.length; j++) {
            if (featureInconformities[j] > -1) {
                sumFeatureError = sumFeatureError + featureInconformities[j];
            }
        }

        double MAE = 0;
        double realDurations_calc[] = new double[realData.myFullCases.size()];
        for (int i = 0; i < realData.myFullCases.size(); i++) {
            realDurations_calc[i] = realData.myFullCases.get(i).dynamicTransactions.get(realData.myFullCases.get(i).dynamicTransactions.size() - 1).duration;
        }
        realDurations = realDurations_calc;

        double predictedDurations_calc[] = new double[predictions.size()];
        for (int i = 0; i < predictions.size(); i++) {
            predictedDurations_calc[i] = predictions.get(i).dynamicTransactions.get(predictions.get(i).dynamicTransactions.size() - 1).duration;
        }
        predictedDurations = predictedDurations_calc;

//        for (int i = 0; i < realDurations.length; i++) {
//            MAE = MAE + Math.abs(realDurations[i] - predictedDurations[i]);
//        }
//        MAE = MAE / realDurations.length;
//        durationMeanAbsoluteError = MAE;
//
//        durationMeanAbsolutePercentageError = 0;
//        for (int i = 0; i < realDurations.length; i++) {
//            durationMeanAbsolutePercentageError = durationMeanAbsolutePercentageError + Math.abs((realDurations[i] - predictedDurations[i]) / realDurations[i]);
//        }
//        durationMeanAbsolutePercentageError = (durationMeanAbsolutePercentageError / (float) realDurations.length) * 100;
//
//        durationRootMeanSquaredErrorPercentage = 0;
//        for (int i = 0; i < realDurations.length; i++) {
//            durationRootMeanSquaredErrorPercentage = durationRootMeanSquaredErrorPercentage + Math.pow((realDurations[i] - predictedDurations[i]) / realDurations[i], 2);
//        }
//        durationRootMeanSquaredErrorPercentage = (durationRootMeanSquaredErrorPercentage / (float) realDurations.length);
//        durationRootMeanSquaredErrorPercentage = Math.sqrt(durationRootMeanSquaredErrorPercentage);
//        durationRootMeanSquaredErrorPercentage = durationRootMeanSquaredErrorPercentage * 100;
    }

    private void generateResultsInString() {
        StringBuilder resultText = new StringBuilder();
        for (int i = 0; i < this.predictions.size(); i++) {
            resultText.append("%%%");
            resultText.append("\n");
            for (int j = 0; j < this.predictions.get(i).staticTransactions.size(); j++) {
                if (this.predictions.get(i).staticTransactions.get(j).isPredicted == true) {
                    for (int k = 0; k < this.predictions.get(i).staticTransactions.get(j).data.length; k++) {
                        resultText.append(this.predictions.get(i).staticTransactions.get(j).data[k]);
                        resultText.append(",");
                    }
                    resultText.append("Duration: ").append(this.predictions.get(i).staticTransactions.get(j).duration);
                    resultText.append(",");
                    resultText.append("*PREDICTED*");

                } else {
                    for (int k = 0; k < this.predictions.get(i).staticTransactions.get(j).data.length; k++) {
                        resultText.append(this.predictions.get(i).staticTransactions.get(j).data[k]);
                        resultText.append(",");
                    }
                    resultText.append("Duration: ").append(this.predictions.get(i).staticTransactions.get(j).duration);
                    resultText.append(",");
                    resultText.append("KNOWN");
                }
                resultText.append("\n");
            }
            boolean knownFinished = false;
            for (int j = 0; j < realData.myFullCases.get(i).staticTransactions.size(); j++) {
                if (knownFinished == false) {
                    if (this.predictions.get(i).staticTransactions.get(j).isPredicted == true) {
                        knownFinished = true;
                    }
                }
                if (knownFinished == true) {
                    resultText.append("REALITY: ");
                    resultText.append(realData.myFullCases.get(i).staticTransactions.get(j).data[realData.eventIndex]);
                    resultText.append(",");
                } else {
                    resultText.append("REALITY KNOWN: ");
                    resultText.append(realData.myFullCases.get(i).staticTransactions.get(j).data[realData.eventIndex]);
                    resultText.append(",");
                }
                for (int k = 0; k < realData.myFullCases.get(i).staticTransactions.get(j).data.length; k++) {
                    resultText.append(realData.myFullCases.get(i).staticTransactions.get(j).data[k]);
                    resultText.append(",");
                }
                resultText.append("\n");
            }
            resultText.append("Real duration: ").append(this.realDurations[i]);
            resultText.append("\n");
            resultText.append("Predicted duration: ").append(this.predictedDurations[i]);
            resultText.append("\n");
            resultText.append("Case event inconformity: ").append(this.numEventInconformities[i]);
            resultText.append("\n");
            resultText.append("Case time inconformity: ").append(this.timeInconformityDays[i]);
            resultText.append("\n");
            resultText.append("%%%");
            resultText.append("\n");
            resultText.append("\n");

        }
        resultText.append("Summation event error:");
        resultText.append("\n");
        resultText.append(this.sumEventError);
        resultText.append("\n");
        resultText.append("Summation duration error:");
        resultText.append("\n");
        resultText.append(this.sumDurationError);
        resultText.append("\n");
        resultText.append("Summation feature error:");
        resultText.append("\n");
        resultText.append(this.sumFeatureError);
        resultText.append("\n");
        resultsInString = resultText.toString();
    }

    public String getRestultsInString() {
        return resultsInString;
    }

}
