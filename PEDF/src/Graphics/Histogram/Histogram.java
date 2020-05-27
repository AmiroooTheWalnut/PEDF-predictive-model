/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Graphics.Histogram;

import Data.DataSet;
import Data.Transaction;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Amir72c
 */
public class Histogram extends JPanel{
    
    int margin=5;
    
    public Histogram(DataSet dataSet,ArrayList<Transaction> transactions,int featureIndex) {
        
    }
    
    private ArrayList<Column> extractColumns(DataSet dataSet,ArrayList<Transaction> transactions,int featureIndex)
    {
        ArrayList<Column> output=new ArrayList();
        
        return output;
    }
    
    @Override
    public void paintComponents(Graphics g)
    {
        
    }
    
    private void drawAxis(Graphics g)
    {
        g.drawLine(margin, this.getHeight()-2*margin, this.getWidth()-2*margin, this.getHeight()-2*margin);
        g.drawLine(margin, this.getHeight()-2*margin, margin, this.getHeight()-2*margin);
    }
}
