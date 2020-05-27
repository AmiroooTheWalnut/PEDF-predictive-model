/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainModel;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.bayes.BayesNet;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Canopy;
import weka.clusterers.CascadeSimpleKMeans;
import weka.clusterers.EM;
import weka.clusterers.GenClustPlusPlus;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.XMeans;
import weka.core.SelectedTag;
import weka.core.Tag;

/**
 *
 * @author user
 */

public class ClustererSelector {
    
//    static int maxClusters=2;
    
    public static AbstractClusterer selectClusterer(String name, int numClusters)
    {
//        System.out.println("numClusters: "+numClusters);
        AbstractClusterer output=null;
        if(name.equals("Canopy"))
        {
            output=new Canopy();
        }else if(name.equals("EM"))
        {
            output=new EM();
        }else if(name.equals("CascadeKMeans"))
        {
            output=new CascadeSimpleKMeans();
            ((CascadeSimpleKMeans)output).setMaxNumClusters(numClusters);
        }else if(name.equals("XMeans"))
        {
            output=new XMeans();
            ((XMeans)output).setMaxNumClusters(numClusters);
        }else if(name.equals("KMeans"))
        {
            output=new SimpleKMeans();
            try {
                ((SimpleKMeans)output).setNumClusters(numClusters);
            } catch (Exception ex) {
                Logger.getLogger(ClustererSelector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if(name.equals("HierarchicalClusterer"))
        {
            output=new HierarchicalClusterer();
            Tag tag[]=new Tag[1];
            tag[0]=new Tag();
            tag[0].setIDStr("AVERAGE");
            SelectedTag linkType=new SelectedTag("AVERAGE",tag);
            
            ((HierarchicalClusterer)output).setLinkType(linkType);
            ((HierarchicalClusterer)output).setNumClusters(numClusters);
        }else if(name.equals("GenClustPlusPlus"))
        {
            output=new GenClustPlusPlus();
            try {
                ((GenClustPlusPlus)output).setNumGenerations(20);
            } catch (Exception ex) {
                Logger.getLogger(ClustererSelector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return output;
    }
}
