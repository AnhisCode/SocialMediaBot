/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package SocialMediaBot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

import javax.security.auth.login.LoginException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class App {

    private static String discordToken = "";
    private static String twitchToken = "";
    private static TwitchClient twitchClient;
    public static JDABuilder jda;

    // list of channel to add
    public static ArrayList<String> channelName = new ArrayList<>();

    public static void main(String[] args) throws LoginException {
        //----------Discord setup----------
        try {
            Dotenv dotenv = Dotenv.load();
            discordToken = dotenv.get("DISCORDTOKEN");
            twitchToken = dotenv.get("TWITCHTOKEN");
        } catch (Exception e) {
            e.printStackTrace();
        }
        jda = JDABuilder.createDefault(discordToken);
        jda.setStatus(OnlineStatus.ONLINE);
        jda.addEventListeners(new Commands());
        jda.build();

        //----------DB setup----------
        // create user database
        if (Files.notExists(Paths.get("monitored_channel.db"))) {
            UpdateDB.createDB();
        }

        //---------twitch setup-----------
        OAuth2Credential credential = new OAuth2Credential("twitch", twitchToken);


        // build the twitchClient class
        twitchClient = TwitchClientBuilder.builder()
                .withDefaultAuthToken(credential)
                .withChatAccount(credential)
                .withEnableHelix(true)
                .withEnableChat(true)
                .withDefaultEventHandler(SimpleEventHandler.class)
                .build();


        // sets up the channels to monitor
        channelName = UpdateDB.getMonitoredUsers();
        for (String channel : channelName) {
            twitchClient.getChat().joinChannel(channel);
            twitchClient.getClientHelper().enableStreamEventListener(channel);
        }

        // The monitored channel receives a message from a user
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelMessageEvent.class, event ->
        {
            // which channel got the message
            String channel = event.getChannel().getName();
            // user ID of the sender
            String userID = event.getUser().getId();
            // username of the sender
            String userName = event.getUser().getName();
            // The sent message
            String message = event.getMessage();

            UpdateDB.updateStreamerDB(userName,userID,channel);
            //System.out.println("[" + channel + "]" + userName + ": " + message);
        });

        // the monitored channel goes live
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).onEvent(ChannelGoLiveEvent.class, event ->
        {
            // name of the channel that just went live
            String streamerName = event.getChannel().getName();
            // Title of the stream
            String title = event.getStream().getTitle();
            // the url image of the stream
            String imageLink = event.getStream().getThumbnailUrl(1280, 720);
            // name of the game being played
            String gameName = event.getStream().getGameName();
            // Viewer count of the stream
            int viewerCount = event.getStream().getViewerCount();

            // obtaining the url of the profile picture
            UserList resultList = twitchClient.getHelix().getUsers(null, null, Arrays.asList(streamerName)).execute();
            ArrayList<User> users = new ArrayList<>(resultList.getUsers());
            String profileIconURL = users.get(0).getProfileImageUrl();

            ArrayList<String> channelIDList = UpdateDB.getChannelID(streamerName);

            for(String channelID: channelIDList){
                MediaPost.discordNotifyLive(channelID,streamerName,imageLink,title,gameName,viewerCount,profileIconURL);
            }

            System.out.printf("Channel: %s is Live! Playing %s\n%s%n", streamerName, gameName, title);
        });

    }

    // add new user to monitor
    public static void addMonitoredUser(String userName){
        twitchClient.getChat().joinChannel(userName);
        twitchClient.getClientHelper().enableStreamEventListener(userName);
    }

    public static void removeMonitoredUser(String userName){
        twitchClient.getChat().leaveChannel(userName);
        twitchClient.getClientHelper().disableStreamEventListener(userName);
    }

}
