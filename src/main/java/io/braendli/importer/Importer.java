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
import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Importer {
    private static final Logger LOG = LoggerFactory.getLogger(Importer.class);

    static {
        try {
            System.setProperty("java.library.path", new File("lib").getAbsolutePath());
            Field sysPaths = ClassLoader.class.getDeclaredField("sys_paths");
            sysPaths.setAccessible(true);
            sysPaths.set(null, null);
            Class.forName("org.firebirdsql.gds.impl.jni.JniGDSImpl");
            LOG.debug("Native library loaded");
        } catch (Exception e) {
            LOG.error("Loading native library failed", e);
        }
    }

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
        String insertString = "INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME, IDCARD) VALUES(?, ?, ?, ?, ?)";
        int id = getStartId(con);

        try (PreparedStatement insertStmt = con.prepareStatement(insertString)) {
            for (List<Cell> row : list) {
                LOG.debug("Importing user: {}", row);
                int pos = 1;
                insertStmt.setInt(pos++, id);
                insertStmt.setInt(pos++, id);
                insertStmt.setString(pos++, row.get(pos - 4).getStringCellValue());
                insertStmt.setString(pos++, row.get(pos - 4).getStringCellValue());
                insertStmt.setInt(pos++, (int) row.get(pos - 4).getNumericCellValue());
                insertStmt.execute();
                id++;
            }
        }
    }

    private static int getStartId(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("select max(id) from users")) {
                rs.next();
                int id = rs.getInt(1);
                return  id < 100 ? 100 : id + 1;
            }
        }
    }
}
