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

            return stream(sheet.spliterator(), false)
                    .map(r -> stream(r.spliterator(), false).collect(toList()))
                    .collect(toList());
        } catch (IOException|InvalidFormatException e) {
            e.printStackTrace();
        }
        return emptyList();
    }

    private static void insertData(List<List<Cell>> lists) {
        String conString = "jdbc:firebirdsql:embedded:C:/Program Files/SafeScan/TA/TADATA.FDB?encoding=NONE";

        try (Connection con = DriverManager.getConnection(conString, "SYSDBA", "a")) {
            String sql = String.format("INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME) VALUES(%1$s, %1$s, ?, ?)", "coalesce((select max(id) + 1 from users), 100)");

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                int pos = 1;
                stmt.setString(pos++, "Höri");
                stmt.setString(pos++, "Müller");
                stmt.execute();
            }
            printContent("USERS", con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printTablesColumnsContents(Connection con) throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        printTables(md);
        printColumns("USERS", md);
        printContent("USERS", con);
    }

    private static void printContent(String table, Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(String.format("select * from %s", table))) {
                printResultSet(rs);
            }
        }
    }

    private static void printColumns(String table, DatabaseMetaData md) throws SQLException {
        try (ResultSet rs = md.getColumns(null, null, table, "%")) {
            printResultSet(rs);
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = rs.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println("");
        }
    }

    private static void printTables(DatabaseMetaData md) throws SQLException {
        try (ResultSet rs = md.getTables(null, null, "%", null)) {
            while (rs.next()) {
                System.out.println(rs.getString(3));
            }
        }
    }
}
