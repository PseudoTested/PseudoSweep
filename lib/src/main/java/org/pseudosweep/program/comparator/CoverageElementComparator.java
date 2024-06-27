package org.pseudosweep.program.comparator;

import org.pseudosweep.program.Block;
import org.pseudosweep.program.CoverageElement;
import org.pseudosweep.program.Decision;
import org.pseudosweep.program.Stmt;

import java.util.Comparator;

public class CoverageElementComparator implements Comparator<CoverageElement> {

    @Override
    public int compare(CoverageElement one, CoverageElement two) {
        int val = 0;

        if (one instanceof Stmt s1) {
            if (two instanceof Stmt s2) {
                val = s1.compareTo(s2);
            } else {
                val = 1;
            }
        } else if (one instanceof Decision d1) {
            if (two instanceof Decision d2) {
                val = d1.compareTo(d2);
            } else if (two instanceof Stmt) {
                val = -1;
            }
        }
        if (one instanceof Block b1) {
            if (two instanceof Block b2) {
                val = b1.compareTo(b2);
            } else {
                val = 1;
            }
        }
        return val;
    }

}
