package SocialMediaBot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

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
                    "(serverName string, serverID string, channelName string, channelID string, twitchUserName string)");
            System.out.println("------------DB created------------");
        } catch (Exception e) {
            System.out.println("_________________________ERROR_________________________");
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public static boolean addTwitchUser(String serverName, String serverID, String channelName, String channelID,String twitchName){
        try {
            connection = DriverManager.getConnection(url);

            // check if the entry already exists
            Statement statement = connection.createStatement();
            ResultSet entries = statement.executeQuery("SELECT serverID, channelID, twitchUserName" +
                    " FROM monitored_channels");

            while(entries.next()){
                // it does
                if(Objects.equals(entries.getString("serverID"), serverID) &&
                    Objects.equals(entries.getString("channelID"), channelID) &&
                    Objects.equals(entries.getString("twitchUserName"), twitchName)){
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException e) {
                        // connection close failed.
                        System.err.println(e.getMessage());
                    }
                    return false;
                }
            }

            String insertStatement = "INSERT INTO monitored_channels VALUES(?,?,?,?,?)";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, serverName);
            preparedStatement.setString(2, serverID);
            preparedStatement.setString(3, channelName);
            preparedStatement.setString(4, channelID);
            preparedStatement.setString(5, twitchName);
            preparedStatement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
            return true;
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e.getMessage());
            return false;
        }
    }


    public static boolean removeTwitchUser(String serverID, String channelID, String twitchName){
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "DELETE FROM monitored_channels WHERE serverID = ? AND " +
                    "channelID = ? AND twitchUserName = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, channelID);
            preparedStatement.setString(3, twitchName);
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("_________________________ERROR_________________________");
            System.err.println(e.getMessage());
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

    }

}
