package bio.inf;

import java.io.*;
import java.util.HashMap;

public class Driver {

    public static void main(String[] args) throws Exception {
        /*
         *
         * input data
         *      args[0] - database of lines
         *      args[1] - configs of penalty
         *      args[2] - another configs
         *
         *      All files have special format of data.
         */
        String ADH_DB = "/Users/Marina/Desktop/fastaaaaaaaaaaaaaaaaaaaaaaa/ADH_DBX.txt";
        String penalty_matrix = "/Users/Marina/Desktop/fastaaaaaaaaaaaaaaaaaaaaaaa/penalty_matrix";
        String config = "/Users/Marina/Desktop/fastaaaaaaaaaaaaaaaaaaaaaaa/config";

        File fileAligns = new File(ADH_DB);
        File filePenalty = new File(penalty_matrix);
        File configurations = new File(config);

        BufferedReader brFileAligns = null;
        BufferedReader brFilePenalty = null;
        BufferedReader brConfiguration = null;

        try {
            brFileAligns = new BufferedReader(new InputStreamReader(new FileInputStream(fileAligns), "UTF-8"));
            brFilePenalty = new BufferedReader(new InputStreamReader(new FileInputStream(filePenalty), "UTF-8"));
            brConfiguration = new BufferedReader(new InputStreamReader(new FileInputStream(configurations), "UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String curLine;
        String allADH_DB = "";
        String allPenalty_matrix = "";
        String allConfig = "";

        try {
            while ((curLine = brFileAligns.readLine()) != null) {
                allADH_DB += curLine + "\n";
            }
            brFileAligns.close();

            while ((curLine = brFilePenalty.readLine()) != null) {
                allPenalty_matrix += curLine + "\n";
            }
            brFilePenalty.close();

            while ((curLine = brConfiguration.readLine()) != null) {
                allConfig += curLine + "\n";
            }
            brConfiguration.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // форматируем входные файлы
        Formatter f = new Formatter();
        HashMap<String, String> db = f.convertDB(allADH_DB);
        PenaltyInfo penaltyInfo = f.convertPenalty(allPenalty_matrix);
        ConfigParams configParams = f.converConfig(allConfig);

        Fasta fastaaaaa = new Fasta(db, penaltyInfo, configParams);
        fastaaaaa.algorithm();
    }
}
