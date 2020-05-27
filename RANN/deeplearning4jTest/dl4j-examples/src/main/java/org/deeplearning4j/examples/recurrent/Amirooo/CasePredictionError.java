package org.deeplearning4j.examples.recurrent.Amirooo;

public class CasePredictionError {
    public int eventPredictionError;
    public double durationError;
    public double featuresError;

    public CasePredictionError(){

    }

    public CasePredictionError(int passed_eventPredictionError, double passed_durationError, double passed_featuresError)
    {
        eventPredictionError=passed_eventPredictionError;
        durationError=passed_durationError;
        featuresError=passed_featuresError;
    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("\\/\\/\\/");
        sb.append(System.lineSeparator());
        sb.append("Event prediction error: ");
        sb.append(eventPredictionError);
        sb.append(System.lineSeparator());

        sb.append("Duration prediction error: ");
        sb.append(durationError);
        sb.append(System.lineSeparator());

        sb.append("Feature prediction error: ");
        sb.append(featuresError);
        sb.append(System.lineSeparator());
        sb.append("^^^");
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
