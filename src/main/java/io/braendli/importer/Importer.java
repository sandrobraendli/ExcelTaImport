package io.braendli.importer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Importer {
    public static void importToDatabase(boolean deleteOldData, File excelFile, File databaseFile) {
        try {
            try (Connection con = getDatabaseConnection(databaseFile)) {
                List<List<Cell>> cells = readExcel(excelFile);

                if (deleteOldData) {
                    deleteOldData(con);
                }

                insertData(cells, con);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteOldData(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.execute("delete from attendant where userid >= 100");
            stmt.execute("delete from users where id >= 100");
        }
    }

    private static Connection getDatabaseConnection(File databaseFile) throws SQLException {
        String conString = String.format("jdbc:firebirdsql:embedded:%s?encoding=NONE", databaseFile.getAbsolutePath());
        return DriverManager.getConnection(conString, "SYSDBA", "a");
    }

    private static List<List<Cell>> readExcel(File excelFile) {
        try (InputStream inp = new FileInputStream(excelFile)) {
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

    private static void insertData(List<List<Cell>> list, Connection con) throws SQLException {
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
    }
}
