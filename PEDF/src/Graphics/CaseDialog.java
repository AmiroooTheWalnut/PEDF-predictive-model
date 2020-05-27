/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Graphics;

import Data.DataSet;
import Data.RawDataSet;

/**
 *
 * @author Amir72c
 */
public class CaseDialog extends javax.swing.JDialog {

    DataSet myDataSet;

    /**
     * Creates new form CaseDialog
     *
     * @param parent
     * @param dataSet
     * @param modal
     */
    public CaseDialog(java.awt.Frame parent, DataSet dataSet, boolean modal) {
        super(parent, modal);
        initComponents();
        myDataSet = dataSet;
        setStaticData(myDataSet, jCheckBox1.isSelected());
    }

    private void setStaticData(DataSet dataSet, boolean isSorted) {
        int numRows = 0;
        if (isSorted == false) {
            for (int i = 0; i < dataSet.myFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myFullCases.get(i).staticTransactions.size(); j++) {
                    numRows = numRows + 1;
                }
                numRows = numRows + 1;
            }
            String data[][] = new String[numRows][dataSet.header.features.size() + 1];
            String IDs[][] = new String[numRows][1];
            int rowCounter = 0;
            for (int i = 0; i < dataSet.myFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myFullCases.get(i).staticTransactions.size(); j++) {
                    IDs[rowCounter][0] = String.valueOf(dataSet.myFullCases.get(i).staticTransactions.get(j).indexId);
//                IDs[rowCounter][0]=dataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName;
                    for (int k = 0; k < dataSet.myFullCases.get(i).staticTransactions.get(j).data.length; k++) {
                        data[rowCounter][k] = dataSet.myFullCases.get(i).staticTransactions.get(j).data[k];
                    }
                    data[rowCounter][dataSet.myFullCases.get(i).staticTransactions.get(j).data.length] = String.valueOf(dataSet.myFullCases.get(i).staticTransactions.get(j).duration);
                    rowCounter = rowCounter + 1;
                }
                for (int k = 0; k < dataSet.header.features.size(); k++) {
                    data[rowCounter][k] = "~";
                }
                rowCounter = rowCounter + 1;
            }

            String headers[] = new String[dataSet.header.features.size() + 1];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                headers[i] = dataSet.header.features.get(i).name;
            }
            headers[dataSet.header.features.size()] = "Duration";
            jTable2.setModel(new javax.swing.table.DefaultTableModel(data, headers));
            String temporaryTypes[][] = new String[1][dataSet.header.features.size()];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                temporaryTypes[0][i] = dataSet.header.features.get(i).type;
            }
            jTable1.setModel(new javax.swing.table.DefaultTableModel(temporaryTypes, headers));
            String IDHeader[] = new String[1];
            IDHeader[0] = "ID";
            jTable3.setModel(new javax.swing.table.DefaultTableModel(IDs, IDHeader));

        } else {
            for (int i = 0; i < dataSet.myTimedFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myTimedFullCases.get(i).staticTransactions.size(); j++) {
                    numRows = numRows + 1;
                }
                numRows = numRows + 1;
            }
            String data[][] = new String[numRows][dataSet.header.features.size() + 1];
            String IDs[][] = new String[numRows][1];
            int rowCounter = 0;
            for (int i = 0; i < dataSet.myTimedFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myTimedFullCases.get(i).staticTransactions.size(); j++) {
                    IDs[rowCounter][0] = String.valueOf(dataSet.myTimedFullCases.get(i).staticTransactions.get(j).indexId);
//                IDs[rowCounter][0]=dataSet.myFullCases.get(i).staticTransactions.get(j).nextEventName;
                    for (int k = 0; k < dataSet.myTimedFullCases.get(i).staticTransactions.get(j).data.length; k++) {
                        data[rowCounter][k] = dataSet.myTimedFullCases.get(i).staticTransactions.get(j).data[k];
                    }
                    data[rowCounter][dataSet.myTimedFullCases.get(i).staticTransactions.get(j).data.length] = String.valueOf(dataSet.myTimedFullCases.get(i).staticTransactions.get(j).duration);
                    rowCounter = rowCounter + 1;
                }
                for (int k = 0; k < dataSet.header.features.size(); k++) {
                    data[rowCounter][k] = "~";
                }
                rowCounter = rowCounter + 1;
            }

            String headers[] = new String[dataSet.header.features.size() + 1];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                headers[i] = dataSet.header.features.get(i).name;
            }
            headers[dataSet.header.features.size()] = "Duration";
            jTable2.setModel(new javax.swing.table.DefaultTableModel(data, headers));
            String temporaryTypes[][] = new String[1][dataSet.header.features.size()];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                temporaryTypes[0][i] = dataSet.header.features.get(i).type;
            }
            jTable1.setModel(new javax.swing.table.DefaultTableModel(temporaryTypes, headers));
            String IDHeader[] = new String[1];
            IDHeader[0] = "ID";
            jTable3.setModel(new javax.swing.table.DefaultTableModel(IDs, IDHeader));

        }
    }

    private void setDynamicData(DataSet dataSet, boolean isSorted) {
        if (isSorted == false) {
            int numRows = 0;
            for (int i = 0; i < dataSet.myFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myFullCases.get(i).dynamicTransactions.size(); j++) {
                    numRows = numRows + 1;
                }
                numRows = numRows + 1;
            }
            String data[][] = new String[numRows][dataSet.header.features.size() + 1];
            String IDs[][] = new String[numRows][1];
            int rowCounter = 0;
            for (int i = 0; i < dataSet.myFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myFullCases.get(i).dynamicTransactions.size(); j++) {
                    IDs[rowCounter][0] = String.valueOf(dataSet.myFullCases.get(i).dynamicTransactions.get(j).indexId);
                    for (int k = 0; k < dataSet.myFullCases.get(i).dynamicTransactions.get(j).data.length; k++) {
                        data[rowCounter][k] = dataSet.myFullCases.get(i).dynamicTransactions.get(j).data[k];
                    }
                    data[rowCounter][dataSet.myFullCases.get(i).dynamicTransactions.get(j).data.length] = String.valueOf(dataSet.myFullCases.get(i).dynamicTransactions.get(j).duration);
                    rowCounter = rowCounter + 1;
                }
                for (int k = 0; k < dataSet.header.features.size(); k++) {
                    data[rowCounter][k] = "~";
                }
                rowCounter = rowCounter + 1;
            }
            String headers[] = new String[dataSet.header.features.size() + 1];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                headers[i] = dataSet.header.features.get(i).name;
            }
            headers[dataSet.header.features.size()] = "Duration";
            jTable2.setModel(new javax.swing.table.DefaultTableModel(data, headers));
            String temporaryTypes[][] = new String[1][dataSet.header.features.size()];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                temporaryTypes[0][i] = dataSet.header.features.get(i).type;
            }
            jTable1.setModel(new javax.swing.table.DefaultTableModel(temporaryTypes, headers));
            String IDHeader[] = new String[1];
            IDHeader[0] = "ID";
            jTable3.setModel(new javax.swing.table.DefaultTableModel(IDs, IDHeader));
        } else {
            int numRows = 0;
            for (int i = 0; i < dataSet.myTimedFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myTimedFullCases.get(i).dynamicTransactions.size(); j++) {
                    numRows = numRows + 1;
                }
                numRows = numRows + 1;
            }
            String data[][] = new String[numRows][dataSet.header.features.size() + 1];
            String IDs[][] = new String[numRows][1];
            int rowCounter = 0;
            for (int i = 0; i < dataSet.myTimedFullCases.size(); i++) {
                for (int j = 0; j < dataSet.myTimedFullCases.get(i).dynamicTransactions.size(); j++) {
                    IDs[rowCounter][0] = String.valueOf(dataSet.myTimedFullCases.get(i).dynamicTransactions.get(j).indexId);
                    for (int k = 0; k < dataSet.myTimedFullCases.get(i).dynamicTransactions.get(j).data.length; k++) {
                        data[rowCounter][k] = dataSet.myTimedFullCases.get(i).dynamicTransactions.get(j).data[k];
                    }
                    data[rowCounter][dataSet.myTimedFullCases.get(i).dynamicTransactions.get(j).data.length] = String.valueOf(dataSet.myTimedFullCases.get(i).dynamicTransactions.get(j).duration);
                    rowCounter = rowCounter + 1;
                }
                for (int k = 0; k < dataSet.header.features.size(); k++) {
                    data[rowCounter][k] = "~";
                }
                rowCounter = rowCounter + 1;
            }
            String headers[] = new String[dataSet.header.features.size() + 1];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                headers[i] = dataSet.header.features.get(i).name;
            }
            headers[dataSet.header.features.size()] = "Duration";
            jTable2.setModel(new javax.swing.table.DefaultTableModel(data, headers));
            String temporaryTypes[][] = new String[1][dataSet.header.features.size()];
            for (int i = 0; i < dataSet.header.features.size(); i++) {
                temporaryTypes[0][i] = dataSet.header.features.get(i).type;
            }
            jTable1.setModel(new javax.swing.table.DefaultTableModel(temporaryTypes, headers));
            String IDHeader[] = new String[1];
            IDHeader[0] = "ID";
            jTable3.setModel(new javax.swing.table.DefaultTableModel(IDs, IDHeader));
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Static");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Dynamic");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jCheckBox1.setText("Sorted by date?");
        jCheckBox1.setToolTipText("");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 706, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBox1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
//        System.out.println("State changed!");
        setStaticData(myDataSet, jCheckBox1.isSelected());
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
//        System.out.println("State changed!");
        setDynamicData(myDataSet, jCheckBox1.isSelected());
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        if(jRadioButton1.isSelected())
        {
            setStaticData(myDataSet, jCheckBox1.isSelected());
        }else{
            setDynamicData(myDataSet, jCheckBox1.isSelected());
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    // End of variables declaration//GEN-END:variables
}
