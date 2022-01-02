package SocialMediaBot;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateDB {

    static String url = "jdbc:sqlite:monitored_channel.db";
    static Connection connection = null;

    // creates a database if there are none
    public static void createDB() {
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS monitored_channels " +
                    "(serverName string, serverID string, channelName string, channelID string, twitchUserName string)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS twitch_viewers " +
                    "(twitchUserName string, twitchID string, messageCount int, streamerName string)");
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


    // update streamer database
    public static void updateStreamerDB(String viewerName, String viewerID, String streamerName) {
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            //VALUES(twitchUserName, twitchID, messageCount, streamerName)");
            ResultSet rs = statement.executeQuery("SELECT * FROM twitch_viewers " +
                    "WHERE twitchID = " + viewerID + " AND streamerName = '" + streamerName+"'");
            if (rs.next()) {
                // user does exist
                String updateStatement = "UPDATE twitch_viewers SET messageCount=? " +
                        "WHERE twitchID=? AND streamerName=?";

                PreparedStatement preparedStatement =
                        connection.prepareStatement(updateStatement);

                int tempCount = rs.getInt("messageCount");
                preparedStatement.setInt(1, tempCount + 1);
                preparedStatement.setString(2, viewerID);
                preparedStatement.setString(3, streamerName);
                preparedStatement.executeUpdate();

                // if username is changed update name
                if (!Objects.equals(viewerName, rs.getString("twitchUserName"))) {
                    String updateName = "UPDATE twitch_viewers SET twitchUserName=? WHERE twitchID=?";

                    preparedStatement =
                            connection.prepareStatement(updateName);

                    preparedStatement.setString(1, viewerName);
                    preparedStatement.setString(2, viewerID);
                    preparedStatement.executeUpdate();
                }

            } else {
                // user doesnt exist
                String insertStatement = "INSERT INTO twitch_viewers VALUES(?,?,1,?)";
                PreparedStatement preparedStatement =
                        connection.prepareStatement(insertStatement);
                preparedStatement.setString(1, viewerName);
                preparedStatement.setString(2, viewerID);
                preparedStatement.setString(3, streamerName);
                preparedStatement.executeUpdate();
            }

        } catch (Exception e) {
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


    // return list of top viewer based on streamer
    public static ArrayList<ArrayList> getViewerLeaderBoard(String streamerName){
        ArrayList<ArrayList> viewerAndMessageCount = new ArrayList<>();
        ArrayList<String> topViewer = new ArrayList<>();
        ArrayList<Integer> messageCount = new ArrayList<>();
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            //VALUES(twitchUserName, twitchID, messageCount, streamerName)");
            ResultSet topList = statement.executeQuery("SELECT twitchUserName, messageCount FROM twitch_viewers WHERE " +
                    "streamerName='"+streamerName+"' ORDER BY messageCount DESC LIMIT 10");
            while(topList.next()){
                topViewer.add(topList.getString("twitchUserName"));
                messageCount.add(topList.getInt("messageCount"));
            }

            // fill up empty space if there are any
            while(topViewer.size() < 3){
                topViewer.add("None");
                messageCount.add(0);
            }

        } catch (SQLException e){
            e.printStackTrace();
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

        viewerAndMessageCount.add(topViewer);
        viewerAndMessageCount.add(messageCount);

        return viewerAndMessageCount;
    }


    // add user command
    public static boolean addTwitchUser(String serverName, String serverID, String channelName, String channelID, String twitchName) {
        try {
            connection = DriverManager.getConnection(url);

            // check if the entry already exists
            Statement statement = connection.createStatement();
            ResultSet entries = statement.executeQuery("SELECT serverID, channelID, twitchUserName" +
                    " FROM monitored_channels");

            while (entries.next()) {
                // it does
                if (Objects.equals(entries.getString("serverID"), serverID) &&
                        Objects.equals(entries.getString("channelID"), channelID) &&
                        Objects.equals(entries.getString("twitchUserName"), twitchName)) {
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
        } catch (SQLException e) {
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

    // remove user command
    public static boolean removeTwitchUser(String serverID, String channelID, String twitchName) {
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


    // get all channelID with user
    public static ArrayList<String> getChannelID(String userName) {
        ArrayList<String> channelIDs = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "SELECT channelID FROM monitored_channels WHERE twitchUserName = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, userName);
            ResultSet idList = preparedStatement.executeQuery();

            while (idList.next()) {
                channelIDs.add(idList.getString("channelID"));
            }
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
        return channelIDs;
    }


    // get all channelID with user
    public static ArrayList<String> getMonitoredUsers() {
        ArrayList<String> monitoredUsers = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "SELECT twitchUserName FROM monitored_channels";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            ResultSet userList = preparedStatement.executeQuery();

            while (userList.next()) {
                monitoredUsers.add(userList.getString("twitchUserName"));
            }
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
        return monitoredUsers;
    }
}
