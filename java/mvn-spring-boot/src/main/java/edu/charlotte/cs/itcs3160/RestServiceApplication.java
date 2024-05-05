/** **
* ITCS 3160-0002, Spring 2024
* Ashton Cox, ashtonmcox@outlook.com
* University of North Carolina at Charlotte
 */
package edu.charlotte.cs.itcs3160;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceApplication.class);

    private static final String JDBC_PSQL_CONN_LOCAL = "jdbc:postgresql://localhost:5432/dbproj";
    private static final String JDBC_PSQL_CONN_DOCKER = "jdbc:postgresql://db:5432/dbproj";
    private static final String JDBC_USER = "scott";
    private static final String JDBC_PASS = "tiger";

    private static String jdbcURL = JDBC_PSQL_CONN_LOCAL;

    /**
     * REST API uses local database unless docker is provided as argument.
     */
    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.contains("docker")) {
                jdbcURL = JDBC_PSQL_CONN_DOCKER;
            }
        }
        SpringApplication.run(RestServiceApplication.class, args);
    }

    /**
     * Connection is started with {@code AUTO_COMMIT = false}
     *
     * @return a connection to the postgresql database.
     */
    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, JDBC_USER, JDBC_PASS);
            conn.setAutoCommit(false);
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            logger.error("Could not obtain connection to the database: ", ex);
        }
        return null;
    }
}
