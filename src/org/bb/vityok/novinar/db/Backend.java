package org.bb.vityok.novinar.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;


/** Database backend for storing and processing news items.
 *
 * Uses Apache Derby database that comes embedded with the Java
 * version 8 runtime.
 */
public class Backend
{
    /* the default framework is embedded */
    private final String framework = "embedded";
    private final String protocol = "jdbc:derby:";

    public static final String DEFAULT_DB_NAME = "novynarDB";

    public static final int DESCRIPTION_MAX_LENGTH = 102400;

    /** Database schema layout version supported by this backend.
     *
     * Based on the value stored in the novinar_meta_inf table can be used
     * for graceful migration from older versions to the newer releases.
     */
    public final static int SCHEMA_VERSION = 1;

    private String dbName;

    private Connection conn;

    private NewsItemDAO niDAO;

    private static Logger logger = Logger.getLogger(Backend.class.getName());

    /** Initialize the backend with the default database name. */
    public Backend()
    {
        this(DEFAULT_DB_NAME);
    }


    public Backend(String dbName)
    {
        logger.info("Backend setup started");
        this.dbName = dbName;
        try {
            setup();
            niDAO = new NewsItemDAO(this);
            logger.info("Backend setup finished");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "problems during backend initialization", e);
        }
    }

    public NewsItemDAO getNewsItemDAO()
    {
        return niDAO;
    }

    public Connection getConnection()
    {
        return conn;
    }

    /**
     * <p> Starts the actual database activities. This includes
     * creating a database by making a connection to Derby
     * (automatically loading the driver), creating a table in the
     * database, and inserting, updating and retrieving some
     * data. Some of the retrieved data is then verified (compared)
     * against the expected results. Finally, the table is deleted
     * and, if the embedded framework is used, the database is shut
     * down.</p>
     */
    public void setup()
        throws Exception
    {

        logger.info("Database backend is starting in " + framework + " mode. SETUP");

        conn = null;

        Statement s;

        try {

            /*
             * This connection specifies create=true in the connection
             * URL to cause the database to be created when connecting
             * for the first time. To remove the database, remove the
             * directory specified by the database name and its
             * contents.
             *
             * The directory dbName will be created under the
             * directory that the system property derby.system.home
             * points to, or the current directory (user.dir) if
             * derby.system.home is not set.
             */
            conn = DriverManager.getConnection(protocol + dbName
                                               + ";create=true", null);

            logger.info("Connected to the database " + dbName);

            s = conn.createStatement();

            // try creating the news_item table and quietly ignore the SQLException if it already exists
            s.execute("CREATE TABLE news_item("
                      + "news_item_id INT NOT NULL PRIMARY KEY "
                      + "  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                      + "channel_id INT NOT NULL, "
                      + "title VARCHAR(2048), "
                      + "link VARCHAR(2048), "
                      // some websites generate items with way too much markup to fit into a VARCHAR
                      // or even a LONG VARCHAR
                      + "description CHARACTER LARGE OBJECT (100 K), "
                      + "creator VARCHAR(1024), "
                      + "date TIMESTAMP, "
                      + "subject VARCHAR(6144), "
                      // is set to 1 when marked as read
                      + "is_read SMALLINT DEFAULT 0, "
                      // is set to 1 when marked as removed. Schema v1 adds is_trash column to track
                      // items thrown into the trash bin.
                      //
                      // An item that has is_trash flag set to 1 is displayed in the "Trash" folder
                      // only. But is not displayed in the channel folder. Only when is_removed is set
                      // to 1 the item becomes candidate for final removal from the database.
                      + "is_removed SMALLINT DEFAULT 0 "
                      + ")");
            logger.severe("Created table NEWS_ITEM");
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equals("X0Y32")) {
                // X0Y32 means that this table already exists,
                // complain only if something bad happened
                printSQLException(sqle);
            }
        }

        int schemaVersion = getSchemaVersion();
        if (schemaVersion == 0) {
            upgradeSchema_NULL_v1();
        }
    }

    /** Returns database layout schema version.
     *
     * @return database layout schema.
     */
    public int getSchemaVersion()
    {
        Connection conn = getConnection();

        String sql = "SELECT schema_version FROM novinar_meta_inf";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
        	int schema_version = rs.getInt("schema_version");
                return schema_version;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to load description for NewsItem: " + this, e);
        }
        return 0;
    }

    /**
     * Upgrade database schema from the earliest alpha development version to the
     * first "versioned" layout.
     *
     * <p>
     * Adds <tt>novinar_meta_inf</tt> table to store information about database
     * layout.
     *
     * <p>
     * Adds <tt>is_trash</tt> column to the <tt>news_item</tt> table.
     */
    public void upgradeSchema_NULL_v1()
    {
        Connection conn = getConnection();

        String sqlCreate = "CREATE TABLE novinar_meta_inf (schema_version INT)";
        try (PreparedStatement ps = conn.prepareStatement(sqlCreate)) {
            ps.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to create novinar_meta_inf table: ", e);
        }

        String sqlInsert = "INSERT INTO novinar_meta_inf (schema_version) VALUES (1)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to update novinar_meta_inf table: ", e);
        }

        // is set to 1 when the news item is moved to the trash bin, but is not yet a
        // candidate for the final removal from the database
        String sqlAlter = "ALTER TABLE news_item ADD COLUMN is_trash SMALLINT DEFAULT 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlAlter)) {
            ps.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "failed to alter news_item table: ", e);
        }

        logger.severe("finished upgrade to the v1 database schema layout");
    }



    /**
     * Prints details of an SQLException chain to Logger.
     *
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
	    {
		logger.severe("\n----- SQLException -----");
		logger.severe("  SQL State:  " + e.getSQLState());
		logger.severe("  Error Code: " + e.getErrorCode());
		logger.severe("  Message:    " + e.getMessage());
		// for stack traces, refer to derby.log or uncomment this:
		//e.printStackTrace(System.err);
		e = e.getNextException();
	    }
    }

    /** Graceful database shutdown.
     *
     * In embedded mode, an application should shut down the database.
     * If the application fails to shut down the database,
     * Derby will not perform a checkpoint when the JVM shuts down.
     * This means that it will take longer to boot (connect to) the
     * database the next time, because Derby needs to perform a recovery
     * operation.
     *
     * It is also possible to shut down the Derby system/engine, which
     * automatically shuts down all booted databases.
     *
     * Explicitly shutting down the database or the Derby engine with
     * the connection URL is preferred. This style of shutdown will
     * always throw an SQLException.
     *
     * Not shutting down when in a client environment, see method
     * Javadoc.
     */
    public void close()
    {
	try {
	    // the shutdown=true attribute shuts down Derby
	    DriverManager.getConnection("jdbc:derby:;shutdown=true");

	    // To shut down a specific database only, but keep the
	    // engine running (for example for connecting to other
	    // databases), specify a database in the connection URL:
	    //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
	} catch (SQLException se) {
	    if (( (se.getErrorCode() == 50000)
		  && ("XJ015".equals(se.getSQLState()) ))) {
		// we got the expected exception
		logger.info("Derby shut down normally");
		// Note that for single database shutdown, the expected
		// SQL state is "08006", and the error code is 45000.
	    } else {
		// if the error code or SQLState is different, we have
		// an unexpected exception (shutdown failed)
		logger.severe("Derby did not shut down normally");
		printSQLException(se);
	    }
	}

	//Connection
	try {
	    if (conn != null) {
		conn.close();
		conn = null;
	    }
	} catch (SQLException sqle) {
	    printSQLException(sqle);
	}
    }


    public Logger getLogger() {
        return logger;
    }
}
