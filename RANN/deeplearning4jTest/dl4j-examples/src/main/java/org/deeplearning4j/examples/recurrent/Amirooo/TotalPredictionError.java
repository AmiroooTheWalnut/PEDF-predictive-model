package org.deeplearning4j.examples.recurrent.Amirooo;

import java.util.ArrayList;

public class TotalPredictionError extends CasePredictionError {
    ArrayList<CasePredictionError> caseErrors=new ArrayList<>();

    public void updateErrors()
    {
        eventPredictionError=0;
        durationError=0;
        featuresError=0;
        for(int i=0;i<caseErrors.size();i++)
        {
            eventPredictionError=eventPredictionError+caseErrors.get(i).eventPredictionError;
            durationError=durationError+caseErrors.get(i).durationError;
            featuresError=featuresError+caseErrors.get(i).featuresError;
        }
    }

    @Override
    public String toString()
    {
        updateErrors();

        StringBuilder sb=new StringBuilder();
        sb.append("\\/\\/\\/");
        sb.append(System.lineSeparator());
        sb.append("Total Event prediction error: ");
        sb.append(eventPredictionError);
        sb.append(System.lineSeparator());

        sb.append("Total Duration prediction error: ");
        sb.append(durationError);
        sb.append(System.lineSeparator());

        sb.append("Total Duration days prediction error: ");
        sb.append(durationError / (double)(60 * 60 * 24));
        sb.append(System.lineSeparator());

        sb.append("Total Feature prediction error: ");
        sb.append(featuresError);
        sb.append(System.lineSeparator());
        sb.append("^^^");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

}
