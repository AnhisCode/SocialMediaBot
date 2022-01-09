package SocialMediaBot;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class UpdateDB {

    static String url = "jdbc:sqlite:" + App.dbLocation + App.dbName;
    static Connection connection = null;

    // creates a database if there are none
    public static void createDB() {
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS monitored_channels " +
                    "(serverName TEXT, serverID TEXT, channelName TEXT, channelID TEXT, " +
                    "twitchUserName TEXT, embedColour TEXT, defaultMessage TEXT, mediaService TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS twitch_viewers " +
                    "(twitchUserName TEXT, twitchID TEXT, messageCount int, streamerName TEXT)");
            System.out.println("------------DB created------------");
        } catch (Exception e) {
            System.out.println("_________________________ERROR at createDB_________________________");
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
                    "WHERE twitchID = " + viewerID + " AND streamerName = '" + streamerName + "'");
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
    public static ArrayList<ArrayList> getViewerLeaderBoard(String streamerName) {
        ArrayList<ArrayList> viewerAndMessageCount = new ArrayList<>();
        ArrayList<String> topViewer = new ArrayList<>();
        ArrayList<Integer> messageCount = new ArrayList<>();
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            //VALUES(twitchUserName, twitchID, messageCount, streamerName)");
            ResultSet topList = statement.executeQuery("SELECT twitchUserName, messageCount FROM twitch_viewers WHERE " +
                    "streamerName='" + streamerName + "' ORDER BY messageCount DESC LIMIT 10");
            while (topList.next()) {
                topViewer.add(topList.getString("twitchUserName"));
                messageCount.add(topList.getInt("messageCount"));
            }

            // fill up empty space if there are any
            while (topViewer.size() < 3) {
                topViewer.add("None");
                messageCount.add(0);
            }

        } catch (SQLException e) {
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
            ResultSet entries = statement.executeQuery("SELECT serverID, channelID, twitchUserName, mediaService" +
                    " FROM monitored_channels");

            while (entries.next()) {
                // it does
                if (Objects.equals(entries.getString("serverID"), serverID) &&
                        Objects.equals(entries.getString("channelID"), channelID) &&
                        Objects.equals(entries.getString("twitchUserName"), twitchName) &&
                        Objects.equals(entries.getString("mediaService"), "twitch")) {
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

            String insertStatement = "INSERT INTO monitored_channels VALUES(?,?,?,?,?,'0xFFC0CB','{default_message}','twitch')";
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
                    "channelID = ? AND twitchUserName = ? AND mediaService = 'twitch'";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, serverID);
            preparedStatement.setString(2, channelID);
            preparedStatement.setString(3, twitchName);
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("_________________________ERROR at removeTwitchUser_________________________");
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


    public static boolean twitchUserExists(String twitchName) {
        boolean exists = false;
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            String insertStatement = "SELECT twitchUserName FROM monitored_channels WHERE twitchUserName = " + twitchName;
            ResultSet result = statement.executeQuery(insertStatement);
            exists = result.next();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at twitchUserExists_________________________");
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
        return exists;
    }


    // remove a row based on channelID
    public static void removeByChannelID(String channelID) {
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "DELETE FROM monitored_channels WHERE channelID = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, channelID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at removeByChannelID_________________________");
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


    // get elements from database ordered by channelID based on twitch username
    public static ArrayList<String> getElement(String userName, String element) {
        ArrayList<String> elements = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url);
            // make sure the order is same using "order by"
            String insertStatement = "SELECT " + element + " FROM monitored_channels WHERE twitchUserName = ? AND mediaService = 'twitch' ORDER BY channelID";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, userName);
            ResultSet idList = preparedStatement.executeQuery();

            while (idList.next()) {
                elements.add(idList.getString(String.valueOf(element)));
            }
        } catch (Exception e) {
            System.out.println("_________________________ERROR at getElement: " + element + "_________________________");
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
        return elements;
    }


    // get elemend from a database ordered by channelID based on username and channelID
    public static ArrayList<String> getElement(String userName, String element, String channelID) {
        ArrayList<String> elements = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url);
            // make sure the order is same using "order by"
            String insertStatement = "SELECT " + element + " FROM monitored_channels WHERE twitchUserName = ? AND mediaService = 'twitch'" +
                    " AND channelID = ? ORDER BY channelID";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, channelID);
            ResultSet idList = preparedStatement.executeQuery();
            while (idList.next()) {
                elements.add(idList.getString(String.valueOf(element)));
            }
        } catch (Exception e) {
            System.out.println("_________________________ERROR at getElement: " + element + "_________________________");
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
        return elements;
    }


    // get all channelID with user
    public static ArrayList<String> getTwitchMonitoredUsers() {
        ArrayList<String> monitoredUsers = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "SELECT twitchUserName, mediaService FROM monitored_channels";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            ResultSet userList = preparedStatement.executeQuery();

            while (userList.next()) {
                if (Objects.equals(userList.getString("mediaService"), "twitch")) {
                    monitoredUsers.add(userList.getString("twitchUserName"));
                }
            }
        } catch (Exception e) {
            System.out.println("_________________________ERROR at getTwitchMonitoredUsers_________________________");
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


    // change colour for an individual
    public static void changeColourIndividual(String twitchName, String newColour, String channelID) {
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "UPDATE monitored_channels SET embedColour = ? WHERE twitchUserName = ? AND channelID = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, newColour);
            preparedStatement.setString(2, twitchName);
            preparedStatement.setString(3, channelID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at changeColourIndividual_________________________");
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


    // change colour for everyone in the same channel
    public static void changeColourAll(String newColour, String channelID) {
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "UPDATE monitored_channels SET embedColour = ? WHERE channelID = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, newColour);
            preparedStatement.setString(2, channelID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at changeColourAll_________________________");
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


    // change default message for an individual
    public static void changeMessageIndividual(String twitchName, String newMessage, String channelID){
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "UPDATE monitored_channels SET defaultMessage = ? WHERE twitchUserName = ? AND channelID = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, newMessage);
            preparedStatement.setString(2, twitchName);
            preparedStatement.setString(3, channelID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at changeMessageIndividual_________________________");
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


    // change message for everyone in the same channel
    public static void changeMessageAll(String newMessage, String channelID) {
        try {
            connection = DriverManager.getConnection(url);
            String insertStatement = "UPDATE monitored_channels SET defaultMessage = ? WHERE channelID = ?";
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, newMessage);
            preparedStatement.setString(2, channelID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("_________________________ERROR at changeMessageAll_________________________");
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

}


//        try{
//                connection=DriverManager.getConnection(url);
//                String insertStatement="";
//                PreparedStatement preparedStatement=
//                connection.prepareStatement(insertStatement);
//                preparedStatement.setString(1,twitchName);
//                preparedStatement.setString(2,newColour);
//                preparedStatement.executeQuery();
//                }catch(Exception e){
//                System.out.println("_________________________ERROR at changeColourIndividual_________________________");
//                System.err.println(e.getMessage());
//                }finally{
//                try{
//                if(connection!=null){
//                connection.close();
//                }
//                }catch(SQLException e){
//                // connection close failed.
//                System.err.println(e.getMessage());
//                }
//                }