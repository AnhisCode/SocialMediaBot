package SocialMediaBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateDB {

    static String url = "jdbc:sqlite:monitored_channel.db";
    static Connection connection = null;

    // creates a database if there are none
    public static void createDB(){
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS monitored_channels " +
                    "(serverName string, channelName string, twitchUserName string)");
            System.out.println("------------DB created------------");
        } catch (Exception e) {
            System.out.println("_________________________ERROR_________________________");
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }




}
