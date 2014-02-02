package com.vaavud.server.analysis.post;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class Plot {

    private final static DecimalFormat df = new DecimalFormat("#.###");
    
    public static String getRow(Integer NCol, ValueCols[] valueColsList) {
        List<Double> row = new ArrayList<Double>(NCol);
        
        for (int i = 0; i<NCol; i++) {
            row.add(null);
        }
        
        for (ValueCols valueCols: valueColsList) {
            for(int col : valueCols.cols) {
                row.set(col, valueCols.value);
            }
        }
        
        return formatedStringRow(row);
    }

    
    private static String formatedStringRow(List<Double> row) {
        
        StringBuilder sb = new StringBuilder(100);
        sb.append("{c:[");
        for (Double value: row) {
            if (value == null) {
                sb.append(" {},");             
            }
            else {
                sb.append(" {v:");
                sb.append(df.format(value));
                sb.append("},");
            }
        }
        
        sb.append("]},");
        
        return sb.toString();
    }
}
