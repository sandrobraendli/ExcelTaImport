package io.braendli.importer

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.stream.Collectors.toList
import java.util.stream.StreamSupport.stream

object Importer {
    private val LOG = LoggerFactory.getLogger(Importer::class.java)
    private const val IMPORT_START_ID = 100

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

    fun importToDatabase(deleteOldData: Boolean, excelFile: File, databaseFile: File) {
        getDatabaseConnection(databaseFile).use { con ->
            LOG.info("Start reading exel file: {}", excelFile)
            val users = readExcel(excelFile)

            if (deleteOldData) {
                LOG.info("Deleting old data")
                deleteOldData(con)
            }
            LOG.info("Importing into database")
            insertData(users, con)
        }
    }

    private fun deleteOldData(con: Connection) {
        con.createStatement().use { stmt ->
            stmt.execute("delete from attendant where userid >= $IMPORT_START_ID")
            stmt.execute("delete from users where id >= $IMPORT_START_ID")
        }
    }

    private fun getDatabaseConnection(databaseFile: File): Connection {
        val conString = "jdbc:firebirdsql:embedded:${databaseFile.absolutePath}?encoding=NONE"
        LOG.info("Connecting to database: {}", conString)
        return DriverManager.getConnection(conString, "SYSDBA", "a")
    }

    private fun readExcel(excelFile: File): List<User> {
        FileInputStream(excelFile).use { inp ->
            val wb = WorkbookFactory.create(inp)
            val sheet = wb.getSheetAt(0)

            return stream<Row>(sheet.spliterator(), false).skip(1)
                    .map { r -> stream<Cell>(r.spliterator(), false).collect(toList()) }
                    .filter { l -> l.size >= 6 && !l.contains(null) }
                    .map(::toUser)
                    .collect(toList())
        }
    }

    private fun toUser(cells: List<Cell>): User {
        var i = 0
        return User(
            "${cells[++i]}",
            "${cells[++i]}",
            "${cells[++i]}",
            "${cells[++i]}",
            cells[++i].numericCellValue.toInt()
        )
    }

    private fun insertData(list: List<User>, con: Connection) {
        val insertString = """INSERT INTO USERS(ID, USERNAME, FIRSTNAME, LASTNAME, EMAIL, STREET, IDCARD)
             VALUES(?, ?, ?, ?, ?, ?, ?)"""
        var id = getStartId(con)

        con.prepareStatement(insertString).use { insertStmt ->
            for (user in list) {
                LOG.debug("Importing user: {}", user)
                insertUser(id, user, insertStmt)
                id++
            }
        }
    }

    private fun insertUser(id: Int, user: User, insertStmt: PreparedStatement) {
        val (first, last, grad, funktion, card) = user
        var pos = 0
        insertStmt.setInt(++pos, id)
        insertStmt.setBytes(++pos, encode("$first $last"))
        insertStmt.setBytes(++pos, encode(first))
        insertStmt.setBytes(++pos, encode(last))
        insertStmt.setBytes(++pos, encode(grad))
        insertStmt.setBytes(++pos, encode(funktion))
        insertStmt.setInt(++pos, card)
        insertStmt.execute()
    }

    private fun encode(text: String) = text.toByteArray(StandardCharsets.ISO_8859_1)

    private fun getStartId(con: Connection): Int {
        con.createStatement().use { stmt ->
            stmt.executeQuery("select max(id) from users").use { rs ->
                rs.next()
                val id = rs.getInt(1)
                return if (id < IMPORT_START_ID) IMPORT_START_ID else id + 1
            }
        }
    }
}
