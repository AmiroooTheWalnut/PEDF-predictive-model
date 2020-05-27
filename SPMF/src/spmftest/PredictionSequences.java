/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spmftest;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author user
 */
public class PredictionSequences {

    public SequenceDatabase knownSeq;
    public SequenceDatabase realSeq;
    public SequenceDatabase predictedSeq;
    
    public PredictionSequences(SequenceDatabase passed_knownSeq,SequenceDatabase passed_realSeq,SequenceDatabase passed_predictedSeq)
    {
        knownSeq=passed_knownSeq;
        realSeq=passed_realSeq;
        predictedSeq=passed_predictedSeq;
    }
    
}
