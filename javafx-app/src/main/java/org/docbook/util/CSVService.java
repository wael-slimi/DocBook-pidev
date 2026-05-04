package org.docbook.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVService {

    public static <T> void exportToCSV(List<String[]> data, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String[] row : data) {
                writer.append(String.join(",", escapeSpecialCharacters(row)));
                writer.append("\n");
            }
        }
    }

    private static String[] escapeSpecialCharacters(String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i] == null) {
                row[i] = "";
            } else {
                row[i] = "\"" + row[i].replace("\"", "\"\"").replace("\n", " ") + "\"";
            }
        }
        return row;
    }
}