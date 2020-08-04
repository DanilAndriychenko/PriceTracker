package org.econia;

import com.mysql.cj.jdbc.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBProcessor {

    private static final String USERNAME = "econiadj_pricemonitoring";
    private static final String PASSWORD = "PriceMonitoring";
    private static final String URL =
            "jdbc:mysql://54.36.173.35:3306/econiadj_pricemonitoring?useSSL=false&serverTimezone=UTC";

    Logger logger = Logger.getLogger(DBProcessor.class.getName());

    private Connection connection;

    public DBProcessor(){
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            String msg = "Error occurred in attempt to register driver.\n" + e.toString();
            logger.log(Level.SEVERE, msg);
        }
    }

    public Connection getConnection(String url, String username, String password){
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                String msg = "Error occurred in attempt to get connection.\n" + e.toString();
                logger.log(Level.SEVERE, msg);
            }
        }
        return connection;
    }
}
