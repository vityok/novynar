package org.bb.vityok.novinar.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;

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

    private String dbName;

    private Connection conn;

    public Backend() {
        this(DEFAULT_DB_NAME);
    }

    public Backend(String dbName) {
        this.dbName = dbName;
	try {
	    setup();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public Connection getConnection()
    {
	return conn;
    }

    /**
     * <p>
     * Starts the actual demo activities. This includes creating a database by
     * making a connection to Derby (automatically loading the driver),
     * creating a table in the database, and inserting, updating and retrieving
     * some data. Some of the retrieved data is then verified (compared) against
     * the expected results. Finally, the table is deleted and, if the embedded
     * framework is used, the database is shut down.</p>
     * <p>
     * Generally, when using a client/server framework, other clients may be
     * (or want to be) connected to the database, so you should be careful about
     * doing shutdown unless you know that no one else needs to access the
     * database until it is rebooted. That is why this demo will not shut down
     * the database unless it is running Derby embedded.</p>
     */
    public void setup()
	throws Exception
    {

        System.out.println("Database backend is starting in " + framework + " mode. SETUP");

	DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

        /* We will be using Statement and PreparedStatement objects for
         * executing SQL. These objects, as well as Connections and ResultSets,
         * are resources that should be released explicitly after use, hence
         * the try-catch-finally pattern used below.
         * We are storing the Statement and Prepared statement object references
         * in an array list for convenience.
         */
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

	    System.out.println("Connected to the database " + dbName);

	    s = conn.createStatement();

            // try creating the news_item table and quietly ignore the SQLException if it already exists
            s.execute("CREATE TABLE news_item("
                      + "news_item_id INT NOT NULL PRIMARY KEY "
                      + "  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                      + "channel_id INT NOT NULL, "
                      + "title VARCHAR(1024), "
                      + "link VARCHAR(2048), "
                      + "description VARCHAR(12288), "
                      + "creator VARCHAR(256), "
                      + "date TIMESTAMP, "
                      + "subject VARCHAR(6144)"
                      +")");
            System.out.println("Created tables CHANNEL and NEWS_ITEM");

	} catch (SQLException sqle) {
            if (!sqle.getSQLState().equals("X0Y32")) {
                // X0Y32 means that this table already exists,
                // complain only if something bad happened
                printSQLException(sqle);
            }
	}
    }

    /**
     * Reports a data verification failure to System.err with the given message.
     *
     * @param message A message describing what failed.
     */
    private void reportFailure(String message) {
        System.err.println("\nData verification failed:");
        System.err.println('\t' + message);
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
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
		System.err.println("\n----- SQLException -----");
		System.err.println("  SQL State:  " + e.getSQLState());
		System.err.println("  Error Code: " + e.getErrorCode());
		System.err.println("  Message:    " + e.getMessage());
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
        if (framework.equals("embedded")) {
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
		    System.out.println("Derby shut down normally");
		    // Note that for single database shutdown, the expected
		    // SQL state is "08006", and the error code is 45000.
		} else {
		    // if the error code or SQLState is different, we have
		    // an unexpected exception (shutdown failed)
		    System.err.println("Derby did not shut down normally");
		    printSQLException(se);
		}
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
}
