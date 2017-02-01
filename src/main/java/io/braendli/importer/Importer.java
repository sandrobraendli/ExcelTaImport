package io.braendli.importer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Importer {
    public static void main(String[] args) {
        insertData(readExcel());
    }

    private static List<List<Cell>> readExcel() {
        try (InputStream inp = Importer.class.getResourceAsStream("/test_users.xlsx")) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            return stream(sheet.spliterator(), false).skip(1)
                    .map(r -> stream(r.spliterator(), false).collect(toList()))
                    .collect(toList());
        } catch (IOException|InvalidFormatException e) {
            e.printStackTrace();
        }
        return emptyList();
    }

    private static void insertData(List<List<Cell>> list) {
        String conString = "jdbc:firebirdsql:embedded:C:/Program Files/SafeScan/TA/TADATA.FDB?encoding=NONE";

        try (Connection con = DriverManager.getConnection(conString, "SYSDBA", "a")) {
            String sql = String.format("INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME, IDCARD) VALUES(%1$s, %1$s, ?, ?, ?)", "coalesce((select max(id) + 1 from users), 100)");


            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                for (List<Cell> row : list) {
                    int pos = 1;
                    stmt.setString(pos++, row.get(pos - 2).getStringCellValue());
                    stmt.setString(pos++, row.get(pos - 2).getStringCellValue());
                    stmt.setInt(pos++, (int) row.get(pos - 2).getNumericCellValue());
                    stmt.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
