package io.braendli.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.stream.Collectors.toList
import java.util.stream.StreamSupport.stream

object Importer {
    private val LOG = LoggerFactory.getLogger(Importer::class.java)

    init {
        try {
            System.setProperty("java.library.path", File("lib").absolutePath)
            val sysPaths = ClassLoader::class.java.getDeclaredField("sys_paths")
            sysPaths.isAccessible = true
            sysPaths.set(null, null)
            Class.forName("org.firebirdsql.gds.impl.jni.JniGDSImpl")
            LOG.debug("Native library loaded")
        } catch (e: Exception) {
            LOG.error("Loading native library failed", e)
        }
    }

    @Throws(Exception::class)
    fun importToDatabase(deleteOldData: Boolean, excelFile: File, databaseFile: File) {
        getDatabaseConnection(databaseFile).use { con ->
            LOG.info("Start reading exel file: {}", excelFile)
            val cells = readExcel(excelFile)

            if (deleteOldData) {
                LOG.info("Deleting old data")
                deleteOldData(con)
            }
            LOG.info("Importing into database")
            insertData(cells, con)
        }
    }

    @Throws(SQLException::class)
    private fun deleteOldData(con: Connection) {
        con.createStatement().use { stmt ->
            stmt.execute("delete from attendant where userid >= 100")
            stmt.execute("delete from users where id >= 100")
        }
    }

    @Throws(SQLException::class)
    private fun getDatabaseConnection(databaseFile: File): Connection {
        val conString = String.format("jdbc:firebirdsql:embedded:%s?encoding=NONE", databaseFile.absolutePath)
        LOG.info("Connecting to database: {}", conString)
        return DriverManager.getConnection(conString, "SYSDBA", "a")
    }

    @Throws(Exception::class)
    private fun readExcel(excelFile: File): List<List<Cell>> {
        FileInputStream(excelFile).use { inp ->
            val wb = WorkbookFactory.create(inp)
            val sheet = wb.getSheetAt(0)

            return stream<Row>(sheet.spliterator(), false).skip(1)
                    .map { r -> stream<Cell>(r.spliterator(), false).collect(toList()) }
                    .filter { l -> l.size >= 3 && !l.contains(null) }
                    .collect(toList())
        }
    }

    @Throws(SQLException::class)
    private fun insertData(list: List<List<Cell>>, con: Connection) {
        val insertString = "INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME, IDCARD) VALUES(?, ?, ?, ?, ?)"
        var id = getStartId(con)

        con.prepareStatement(insertString).use { insertStmt ->
            for (row in list) {
                LOG.debug("Importing user: {}", row)
                var pos = 1
                insertStmt.setInt(pos++, id)
                insertStmt.setInt(pos++, id)
                insertStmt.setString(pos++, row[pos - 4].stringCellValue)
                insertStmt.setString(pos++, row[pos - 4].stringCellValue)
                insertStmt.setInt(pos++, row[pos - 4].numericCellValue.toInt())
                insertStmt.execute()
                id++
            }
        }
    }

    @Throws(SQLException::class)
    private fun getStartId(con: Connection): Int {
        con.createStatement().use { stmt ->
            stmt.executeQuery("select max(id) from users").use { rs ->
                rs.next()
                val id = rs.getInt(1)
                return if (id < 100) 100 else id + 1
            }
        }
    }
}
