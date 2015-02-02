package bio.inf;

import java.util.*;

public class Fasta {

    public HashMap<String, String> db;
    public HashMap<String, String> maxOverallSubstring;
    public HashMap<String, Integer> scoreForSubstr;
    public HashMap<String, String[]> resultAligns;
    public HashMap<String, Integer> resultScores;
    public ArrayList<String> badKeys;
    public PenaltyInfo penaltyInfo;
    public ConfigParams configParams;

    private enum From {
        DEFAULT,
        UP,
        LEFT,
        DIAG,

        toString();
    }

    private StringBuilder result0 = new StringBuilder();
    private StringBuilder result1 = new StringBuilder();

    Fasta(HashMap db, PenaltyInfo penaltyInfo, ConfigParams configParams) {
        maxOverallSubstring = new HashMap<String, String>();
        scoreForSubstr = new HashMap<String, Integer>();
        resultAligns = new HashMap<String, String[]>();
        resultScores = new HashMap<String, Integer>();
        badKeys = new ArrayList<String>();
        this.db = db;
        this.penaltyInfo = penaltyInfo;
        this.configParams = configParams;
    }

    /*
    * -разбиваем все строки из банка на подстроки длиной из configParams
    * -ищем, в какой строке нет подстрок, встречающихся во входной строке
    * -отбрасываем, получившиеся на предыдущем шаге подстроки
    * */
    public void checkOutSubstrs() {
        for (String key : db.keySet()) {
            //System.out.println(key);
            String lineDB = db.get(key);
            boolean f = false;
            for (int i = 0; i + configParams.substringLength < lineDB.length(); i++) {
                CharSequence subLineDB = lineDB.subSequence(i, i + configParams.substringLength);
                if (configParams.inputString.contains(subLineDB)) {
                    f = true;
                    break;
                }
            }
            if (!f) {
                badKeys.add(key);
                //System.out.println(key); // 2 штучки
            }
        }

        for (String bad: badKeys) {
            if (db.containsKey(bad)) {
                db.remove(bad);
            }
        }
    }

    public String LCS (String s1, String s2)
    {
        int[][] a = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i < s1.length() + 1; i++)
            for (int j = 0; j < s2.length() + 1; j++)
                a[i][j] = 0;

        int u = 0,  v = 0;

        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                if (s1.charAt(i) == s2.charAt(j))
                {
                    a[i + 1][j + 1] = a[i][j] + 1;
                    if (a[i + 1][j + 1] > a[u][v])
                    {
                        u = i + 1;
                        v = j + 1;
                    }
                }
            }
        }

        return s2.substring(v - a[u][v], v);
    }

    public void maxSubstrs() {
        for (String key : db.keySet()) {
            String lineDB = db.get(key);
            String subLineDB = LCS(configParams.inputString, lineDB);
            if (subLineDB.length() < configParams.minOverallSubstr) {
                badKeys.add(key);
            } else {
                maxOverallSubstring.put(key, subLineDB);
            }
        }

        for (String bad: badKeys) {
            if (db.containsKey(bad)) {
                db.remove(bad);
            }
            //System.out.println(bad); // уже 14
        }

        /*for (String key: maxOverallSubstring.keySet()) {
            System.out.println(key + " " + maxOverallSubstring.get(key));
        }*/
    }

    public void scoresForSubstrs() {
        for (String key: maxOverallSubstring.keySet()) {
            String maxSubstr = maxOverallSubstring.get(key);
            int score = 0;
            for (int i = 0; i < maxSubstr.length(); i++) {
                Character c = maxSubstr.charAt(i);
                for (int j = 0; j < penaltyInfo.alphabet.length; j++) {
                    if (c == penaltyInfo.alphabet[j].charAt(0)) {
                        score += penaltyInfo.penaltyMatrix[j][j];
                        break;
                    }
                }
            }
            if (score < configParams.minScoreForSubstr) {
                badKeys.add(key);
            } else {
                scoreForSubstr.put(key, score);
            }
            //System.out.println(key + " " + score);*/
        }

        for (String bad: badKeys) {
            if (db.containsKey(bad)) {
                db.remove(bad);
            }
            //System.out.println(bad); // уже 14
        }
    }

    public void NW(String inputStrKey, String str1, int gapsLimit) throws Exception {
        System.out.println("*********************************");
        System.out.println("stringFromDB:");
        System.out.println(inputStrKey);
        System.out.println("inputString:");
        System.out.println(str1);
        if (Math.abs(inputStrKey.length() - str1.length()) > gapsLimit) {
            throw new Exception("Ошибка: число gap-ов меньше, чем разность длин последовательностей"
                    + "\n (|" + inputStrKey.length() + " - " + str1.length() + "| > " + gapsLimit +")");
        }
        /*
		 * resultTable - матрица для нахождения выравнивания с наивысшей оценкой
		 * routes - матрица путей (откуда пришли)
		 */
        int[][] resultTable = new int[str1.length() + 1][inputStrKey.length() + 1];
        From[][] routes = new From[str1.length() + 1][inputStrKey.length() + 1];

		/*
		 * DEFAULT - значит нельзя дальше, поскольку исчерпали количество gap-ов
		 * cначала всё в DEFAULT
		 */
        for (int i = 0; i < str1.length() + 1; i++) {
            for(int j = 0; j < inputStrKey.length() + 1; j++) {
                resultTable[i][j] = 0;
                routes[i][j] = From.DEFAULT;
            }
        }
        /*
        * заполняем 1-ю строку и 1-й столбец до допустимого значения gap-ов
        * заполнение зависит от матрицы penalty_Matrix
        * по-хорошему сделать универсальный вариант
        * */
        resultTable[0][0] = penaltyInfo.penaltyMatrix[penaltyInfo.penaltyMatrix.length - 1][penaltyInfo.penaltyMatrix.length - 1];

        for (int i = 1; i <= Math.min(gapsLimit, inputStrKey.length()) ; i++) {
            resultTable[0][i] = i * penaltyInfo.penaltyMatrix[0][penaltyInfo.penaltyMatrix.length - 1];
            routes[0][i] = From.LEFT;
        }
        for (int i = 1; i <= Math.min(gapsLimit, str1.length()); i++) {
            resultTable[i][0] = i * penaltyInfo.penaltyMatrix[0][penaltyInfo.penaltyMatrix.length - 1];
            routes[i][0] = From.UP;
        }

        /*
		 * заполняем матрицы
		 * если по диагонали увидели DEFAULT, значит уже исчерпали количество gap-ов и continue
		 * также не учитываем результаты полученные из ячеек с DEFAULT
		 */
        for (int i = 1; i < str1.length() + 1; i++) {
            for (int j = 1; j < inputStrKey.length() + 1; j++) {
                if (routes[i-1][j-1] == From.DEFAULT)
                    continue;

                int realIndexInString1 = i - 1;
                int realIndexInString0 = j - 1;

                char char1 = str1.charAt(realIndexInString1);
                char char0 = inputStrKey.charAt(realIndexInString0);

                int indexInAlphabetForString1 = 0;
                int indexInAlphabetForString0 = 0;

                for (int k = 0; k < penaltyInfo.alphabet.length; k++) {
                    if (char1 == penaltyInfo.alphabet[k].charAt(0)) {
                        indexInAlphabetForString1 = k;
                        break;
                    }
                }
                for (int k = 0; k < penaltyInfo.alphabet.length; k++) {
                    if (char0 == penaltyInfo.alphabet[k].charAt(0)) {
                        indexInAlphabetForString0 = k;
                        break;
                    }
                }

                int score = penaltyInfo.penaltyMatrix[indexInAlphabetForString1][indexInAlphabetForString0];

                int[] ways = {
                        resultTable[i-1][j-1] + score,
                        resultTable[i-1][j] + penaltyInfo.penaltyMatrix[0][penaltyInfo.penaltyMatrix.length - 1],
                        resultTable[i][j-1] + penaltyInfo.penaltyMatrix[0][penaltyInfo.penaltyMatrix.length - 1]
                };

                if (routes[i-1][j] != From.DEFAULT && ways[1] > ways[0]) {
                    routes[i][j] = From.UP;
                    resultTable[i][j] = ways[1];
                } else if (routes[i][j-1] != From.DEFAULT && ways[2] > ways[0]) {
                    routes[i][j] = From.LEFT;
                    resultTable[i][j] = ways[2];
                } else {
                    routes[i][j] = From.DIAG;
                    resultTable[i][j] = ways[0];
                }
            }
        }

        /*
        *  print matrix
        * */
        /*
         for (int i = 0; i < str1.length(); i++) {
            for (int j = 0; j < inputStrKey.length(); j++) {
                System.out.print(resultTable[i][j] + "  ");
            }
            System.out.println();
        }
        */

        /*
        * пишем скор в бд resultScores
        * */
        resultScores.put(inputStrKey, resultTable[str1.length()][inputStrKey.length()]);
        /*
        * идем по матрице маршрутов и считаем resultScore
        * */
        int i = str1.length();
        int j = inputStrKey.length();
        String[] resultStrings = {"", ""};

        while (i > 0 || j > 0) {
            switch (routes[i][j]) {
                case DIAG:
                    resultStrings[0] = inputStrKey.charAt(j - 1) + resultStrings[0];
                    resultStrings[1] = str1.charAt(i - 1) + resultStrings[1];
                    i--;
                    j--;
                    //System.out.print("DIAG ");
                    break;
                case UP:
                    resultStrings[0] = "-" + resultStrings[0];
                    resultStrings[1] = str1.charAt(i - 1) + resultStrings[1];
                    i--;
                    //System.out.print("UP ");
                    break;
                case LEFT:
                    resultStrings[0] = inputStrKey.charAt(j - 1) + resultStrings[0];
                    resultStrings[1] = "-" + resultStrings[1];
                    j--;
                    //System.out.print("LEFT ");
                    break;
                default:
                    System.err.append("FATAL ERROR!");
            }
        }
        /*
        * пишем выранивание в бд resultAligns
        * */
        resultAligns.put(inputStrKey, resultStrings);

        System.out.println();
        System.out.println("RESULTS:");
        System.out.println("SCORE " + resultScores.get(inputStrKey));
        System.out.println(resultAligns.get(inputStrKey)[0]);
        System.out.println(resultAligns.get(inputStrKey)[1]);
        System.out.println();
    }

    public void NWgaps() throws Exception {
        for (String key: db.keySet()) {
            String lineDB = db.get(key);
            NW(lineDB, configParams.inputString, configParams.gapsLimit);
        }
    }

    public void algorithm() throws Exception {
        checkOutSubstrs();
        maxSubstrs();
        scoresForSubstrs();
        try {
            NWgaps();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}