package org.bb.vityok.novinator.db;

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

    private final static Backend INSTANCE = new Backend();

    private Connection conn;

    protected Backend() {
	try {
	    setup();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static Backend getInstance() {
	return INSTANCE;
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
     *
     * @see #main(String[])
     */
    public void setup()
	throws Exception
    {

        System.out.println("SimpleApp starting in " + framework + " mode");

	DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

        /* We will be using Statement and PreparedStatement objects for
         * executing SQL. These objects, as well as Connections and ResultSets,
         * are resources that should be released explicitly after use, hence
         * the try-catch-finally pattern used below.
         * We are storing the Statement and Prepared statement object references
         * in an array list for convenience.
         */
        conn = null;
        ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
        PreparedStatement psInsert;
        PreparedStatement psUpdate;
        Statement s;
        ResultSet rs = null;
        try
	    {
		Properties props = new Properties(); // connection properties
		// providing a user name and password is optional in the embedded
		// and derbyclient frameworks
		props.put("user", "user1");
		props.put("password", "user1");

		/* By default, the schema APP will be used when no username is
		 * provided.
		 * Otherwise, the schema name is the same as the user name (in this
		 * case "user1" or USER1.)
		 *
		 * Note that user authentication is off by default, meaning that any
		 * user can connect to your database using any password. To enable
		 * authentication, see the Derby Developer's Guide.
		 */

		String dbName = "derbyDB"; // the name of the database

		/*
		 * This connection specifies create=true in the connection URL to
		 * cause the database to be created when connecting for the first
		 * time. To remove the database, remove the directory derbyDB (the
		 * same as the database name) and its contents.
		 *
		 * The directory derbyDB will be created under the directory that
		 * the system property derby.system.home points to, or the current
		 * directory (user.dir) if derby.system.home is not set.
		 */
		conn = DriverManager.getConnection(protocol + dbName
						   + ";create=true", props);

		System.out.println("Connected to and created database " + dbName);

		/* Creating a statement object that we can use for running various
		 * SQL statements commands against the database.*/
		s = conn.createStatement();
		statements.add(s);

		// We create a table...
		s.execute("create table location(num int, addr varchar(40))");
		System.out.println("Created table location");

		// and add a few rows...

		/* It is recommended to use PreparedStatements when you are
		 * repeating execution of an SQL statement. PreparedStatements also
		 * allows you to parameterize variables. By using PreparedStatements
		 * you may increase performance (because the Derby engine does not
		 * have to recompile the SQL statement each time it is executed) and
		 * improve security (because of Java type checking).
		 */
		// parameter 1 is num (int), parameter 2 is addr (varchar)
		psInsert = conn.prepareStatement(
						 "insert into location values (?, ?)");
		statements.add(psInsert);

		psInsert.setInt(1, 1956);
		psInsert.setString(2, "Webster St.");
		psInsert.executeUpdate();
		System.out.println("Inserted 1956 Webster");

		psInsert.setInt(1, 1910);
		psInsert.setString(2, "Union St.");
		psInsert.executeUpdate();
		System.out.println("Inserted 1910 Union");

		// Let's update some rows as well...

		// parameter 1 and 3 are num (int), parameter 2 is addr (varchar)
		psUpdate = conn.prepareStatement(
						 "update location set num=?, addr=? where num=?");
		statements.add(psUpdate);

		psUpdate.setInt(1, 180);
		psUpdate.setString(2, "Grand Ave.");
		psUpdate.setInt(3, 1956);
		psUpdate.executeUpdate();
		System.out.println("Updated 1956 Webster to 180 Grand");

		psUpdate.setInt(1, 300);
		psUpdate.setString(2, "Lakeshore Ave.");
		psUpdate.setInt(3, 180);
		psUpdate.executeUpdate();
		System.out.println("Updated 180 Grand to 300 Lakeshore");

		/*
		  We select the rows and verify the results.
		*/
		rs = s.executeQuery(
				    "SELECT num, addr FROM location ORDER BY num");

		/* we expect the first returned column to be an integer (num),
		 * and second to be a String (addr). Rows are sorted by street
		 * number (num).
		 *
		 * Normally, it is best to use a pattern of
		 *  while(rs.next()) {
		 *    // do something with the result set
		 *  }
		 * to process all returned rows, but we are only expecting two rows
		 * this time, and want the verification code to be easy to
		 * comprehend, so we use a different pattern.
		 */

		int number; // street number retrieved from the database
		boolean failure = false;
		if (!rs.next())
		    {
			failure = true;
			reportFailure("No rows in ResultSet");
		    }

		if ((number = rs.getInt(1)) != 300)
		    {
			failure = true;
			reportFailure(
				      "Wrong row returned, expected num=300, got " + number);
		    }

		if (!rs.next())
		    {
			failure = true;
			reportFailure("Too few rows");
		    }

		if ((number = rs.getInt(1)) != 1910)
		    {
			failure = true;
			reportFailure(
				      "Wrong row returned, expected num=1910, got " + number);
		    }

		if (rs.next())
		    {
			failure = true;
			reportFailure("Too many rows");
		    }

		if (!failure) {
		    System.out.println("Verified the rows");
		}

		// delete the table
		s.execute("drop table location");
		System.out.println("Dropped table location");



	    }
        catch (SQLException sqle)
	    {
		printSQLException(sqle);
	    } finally {
            // release all open resources to avoid unnecessary memory usage

            // ResultSet
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }

            // Statements and PreparedStatements
            int i = 0;
            while (!statements.isEmpty()) {
                // PreparedStatement extend Statement
                Statement st = (Statement)statements.remove(i);
                try {
                    if (st != null) {
                        st.close();
                        st = null;
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }
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
