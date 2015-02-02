package bio.inf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Formatter {

    Formatter() {}

    public HashMap convertDB(String dbString) {
        HashMap<String, String> db = new HashMap<String, String>();
        String[] newDBString = dbString.trim().split(">");
        for (int i = 0; i < newDBString.length; i++) {
            String[] dbInfo = newDBString[i].trim().split("\\n");
            String key = dbInfo[0].trim().split("\\s")[0].trim().replaceAll("(gi\\|)|(sp\\|)", "");
            String value = "";
            for (int j = 1; j < dbInfo.length; j++) {
                value += dbInfo[j];
            }
            db.put(key, value);
            //System.out.println(key);
            //System.out.println(value);
        }
        return db;
    }

    public PenaltyInfo convertPenalty(String penaltyString) {
        String newPenaltyString = penaltyString.replaceAll("((#)(.*)(\\n))|($[\\n])", "");
        String[] matrix = newPenaltyString.trim().split("\\n");
        String[] alphabet = matrix[0].trim().replaceAll("\\s+", " ").trim().split(" ");
        int[][] penaltyMatrix = new int[alphabet.length][alphabet.length];
        for (int i = 1; i < matrix.length; i++) {
            String[] lineMatrix = matrix[i].trim().replaceAll("\\s+", " ").trim().split(" ");
            for(int j = 1; j < lineMatrix.length; j++) {
                penaltyMatrix[i-1][j-1] = Integer.parseInt(lineMatrix[j]);
            }
        }
        PenaltyInfo pi = new PenaltyInfo(alphabet, penaltyMatrix);
        return pi;

        /*
        for (int i = 0; i < penaltyMatrix.length; i++) {
            for(int j = 0; j < penaltyMatrix.length; j++) {
                System.out.print(penaltyMatrix[i][j] + "  ");
            }
            System.out.println();
        }

        System.out.println(penaltyString);
        */
    }

    public ConfigParams converConfig(String config) {
        String[] allConfigParams = config.trim().split("\\n");
        ConfigParams cp = new ConfigParams();
        cp.inputString = allConfigParams[0].trim().split("\\s")[1];
        cp.substringLength = Integer.parseInt(allConfigParams[1].trim().split("\\s")[1]);
        cp.minOverallSubstr = Integer.parseInt(allConfigParams[2].trim().split("\\s")[1]);
        cp.minScoreForSubstr = Integer.parseInt(allConfigParams[3].trim().split("\\s")[1]);
        cp.gapsLimit = Integer.parseInt(allConfigParams[4].trim().split("\\s")[1]);
        if (cp.gapsLimit == -1)
            cp.gapsLimit = Integer.MAX_VALUE;
        //System.out.println(cp.inputString);
        //System.out.println(cp.substringLength);
        return cp;
    }
}
