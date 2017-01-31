package io.braendli.importer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class Importer {
    public static void main(String[] args) {
        try {
            String connectionString = "jdbc:firebirdsql:embedded:C:/Program Files/SafeScan/TA/TADATA.FDB";

            try (Connection con = DriverManager.getConnection(connectionString, "SYSDBA", "a")) {
                DatabaseMetaData md = con.getMetaData();
                //printTables(md);
                //printColumns("USERS", md);
                printContent("USERS", con);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static void readExcel() throws IOException, InvalidFormatException {
        InputStream inp = new FileInputStream("workbook.xls");
        //InputStream inp = new FileInputStream("workbook.xlsx");

        Workbook wb = WorkbookFactory.create(inp);
        Sheet sheet = wb.getSheetAt(0);
        Row row = sheet.getRow(2);
        Cell cell = row.getCell(3);
        if (cell == null)
            cell = row.createCell(3);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("a test");

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }
}
