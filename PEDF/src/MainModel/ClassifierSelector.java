/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;

/**
 *
 * @author user
 */
public class ClassifierSelector {
    public static AbstractClassifier selectClassifier(String name)
    {
        AbstractClassifier output=null;
        if(name.equals("NaiveBayes"))
        {
            output=new NaiveBayes();
        }else if(name.equals("RandomForest"))
        {
            output=new RandomForest();
        }else if(name.equals("MultilayerPerceptron"))
        {
            output=new MultilayerPerceptron();
        }else if(name.equals("BayesNet"))
        {
            output=new BayesNet();
        }
        return output;
    }
}
