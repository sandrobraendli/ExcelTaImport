package io.braendli.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Importer {
    private static final Logger LOG = LoggerFactory.getLogger(Importer.class);

    public static void importToDatabase(boolean deleteOldData, File excelFile, File databaseFile) throws Exception {
        try (Connection con = getDatabaseConnection(databaseFile)) {
            LOG.info("Start reading exel file: {}", excelFile);
            List<List<Cell>> cells = readExcel(excelFile);

            if (deleteOldData) {
                LOG.info("Deleting old data");
                deleteOldData(con);
            }
            LOG.info("Importing into database");
            insertData(cells, con);
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
        LOG.info("Connecting to database: {}", conString);
        return DriverManager.getConnection(conString, "SYSDBA", "a");
    }

    private static List<List<Cell>> readExcel(File excelFile) throws Exception {
        try (InputStream inp = new FileInputStream(excelFile)) {
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0);

            return stream(sheet.spliterator(), false).skip(1)
                    .map(r -> stream(r.spliterator(), false).collect(toList()))
                    .filter(l -> l.size() >= 3 && !l.contains(null))
                    .collect(toList());
        }
    }

    private static void insertData(List<List<Cell>> list, Connection con) throws SQLException {
        String sql = String.format("INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME, IDCARD) VALUES(%1$s, %1$s, ?, ?, ?)", "coalesce((select max(id) + 1 from users), 100)");

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (List<Cell> row : list) {
                LOG.debug("Importing user: {}", row);
                int pos = 1;
                stmt.setString(pos++, row.get(pos - 2).getStringCellValue());
                stmt.setString(pos++, row.get(pos - 2).getStringCellValue());
                stmt.setInt(pos++, (int) row.get(pos - 2).getNumericCellValue());
                stmt.execute();
            }
        }
    }
}
