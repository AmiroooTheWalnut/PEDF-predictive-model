/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spmftest;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import java.util.List;

/**
 *
 * @author user
 */
public class CustomSequence extends Sequence implements Comparable {

    public CustomSequence(int id) {
        super(id);
    }

    public CustomSequence(Sequence seq) {
        super(seq);
    }

    public CustomSequence(int id, List<Item> items) {
        super(id, items);
    }

    @Override
    public int compareTo(Object o) {
        if (this.getId() == ((Sequence) o).getId()) {
            return 0;
        } else if (this.getId() > ((Sequence) o).getId()) {
            return 1;
        } else {
            return -1;
        }
    }
}
