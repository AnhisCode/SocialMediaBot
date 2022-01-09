package SocialMediaBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;


public class MediaPost {

    // post on discord when a user goes live
    public static void discordNotifyLive(String channelID, String userName, String imageURL, String title, String gameName, String profileIconURL, String defaultMessage) {

        TextChannel channel = Commands.jda.getTextChannelById(channelID);

        System.out.println(channel.canTalk());

        System.out.println(channel);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle("**"+userName + " is Streaming!!**", "https://www.twitch.tv/"+userName);
        streamInfo.setColor(Color.decode("0xFFC0CB"));
        streamInfo.setThumbnail(profileIconURL);
        streamInfo.setImage(imageURL);
        streamInfo.addField("Game :video_game:", "Playing " + gameName, true);
        streamInfo.addField("Watch now at :tv:", "https://www.twitch.tv/"+userName, true);
        streamInfo.setDescription("**"+title+"**");
        streamInfo.setFooter("Type: \"=>leaderboard "+userName+"\" to show twitch viewer leaderboard :)");

        if (channel != null) {
            if(Objects.equals(defaultMessage, "{default_message}")) {
                channel.sendMessage(":white_flower: Hey! @everyone :white_flower:").queue();
            } else {
                channel.sendMessage(defaultMessage).queue();
            }
            channel.sendMessageEmbeds(streamInfo.build()).queue();
        }
        streamInfo.clear();
    }


    // post on discord when a user goes offline
    public static void discordNotifyOffline(String channelID, String userName, String profileIconURL) {
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle(":zzz:**"+userName + " Has Finished Streaming!!:zzz:**", "https://www.twitch.tv/"+userName);
        streamInfo.setColor(Color.decode("0xFFC0CB"));
        streamInfo.setThumbnail(profileIconURL);
        streamInfo.setDescription("**Thank you and otsukaresama deshita "+userName+"**\n Thanks everyone for tuning in!");
        streamInfo.setFooter("Did you enjoy the stream?");

        if (channel != null) {
            channel.sendMessageEmbeds(streamInfo.build()).queue(message -> {
                message.addReaction("U+2705").queue();
                message.addReaction("U+274C").queue();
            });
        }
        streamInfo.clear();
    }


    // display leaderboard
    public static void displayLeaderboard(TextChannel channel, String streamerName){
        ArrayList<ArrayList> topViewers = UpdateDB.getViewerLeaderBoard(streamerName);
        ArrayList<String> viewers = new ArrayList<>();
        ArrayList<Integer> messageCount = new ArrayList<>();

        viewers = topViewers.get(0);
        messageCount = topViewers.get(1);

        // top 4 to 10
        StringBuilder top4To10 = new StringBuilder();

        // iterate over each map entry descending order
        for (int i = viewers.size()-1; i > 2; i--) {
            top4To10.insert(0, String.format("#%s: **%s** - %s Messages\n", i+1,
                    viewers.get(i), messageCount.get(i)));
        }

        EmbedBuilder leaderboardInfo = new EmbedBuilder();
        leaderboardInfo.setTitle("**"+ streamerName + "'s Top 10 Chatters!!**");
        leaderboardInfo.addField("#1 Viewer :first_place:", String.format("**%s** - %s messages",viewers.get(0), messageCount.get(0)), true);
        leaderboardInfo.addField("#2 Viewer :second_place: ", String.format("**%s** - %s messages",viewers.get(1), messageCount.get(1)), true);
        leaderboardInfo.addField("#3 Viewer :third_place:", String.format("**%s** - %s messages",viewers.get(2), messageCount.get(2)), true);
        leaderboardInfo.addField("The Rest :medal:",top4To10.toString(),false);
        leaderboardInfo.setColor(Color.decode("0xFFC0CB"));
        leaderboardInfo.setFooter("Type: \"=>leaderboard "+streamerName+"\" to update this leaderboard :)");

        channel.sendMessageEmbeds(leaderboardInfo.build()).queue();
        leaderboardInfo.clear();

    }


    // info post about instruction
    public static void displayInfo(TextChannel channel){
        EmbedBuilder leaderboardInfo = new EmbedBuilder();
        leaderboardInfo.setTitle("**:heart: Thank you for using MediaBot! :heart:**");
        leaderboardInfo.addField("Owner Commands :person_in_tuxedo:", "```=>adduser <Twitch Username>```" +
                "```=>removeuser <Twitch Username>```To get started as a server owner, please use **adduser** " +
                "command in a channel the bot has access to to start monitoring a streamer. To remove a " +
                "streamer and stop getting notified when they go live, use the **removeuser** command in the same " +
                "channel",false);
        leaderboardInfo.addField("User Commands :person_bowing:", "```=>leaderboard <Twitch Username>```" +
                "Use this command to show the leaderboard of the top 10 chatters for a streamer. If all the" +
                " entry is None, please ask the server owner to see if the streamer is monitored or the " +
                "name is spelled correctly.\n ```=>getstarted``` Use this command to bring up this info box " +
                "again",false);
        leaderboardInfo.setImage("https://i.ibb.co/M5pn7Tz/Media-Bot.png");
        leaderboardInfo.setColor(Color.decode("0xFFC0CB"));
        leaderboardInfo.setFooter("Bot developed by Anh :)");

        channel.sendMessageEmbeds(leaderboardInfo.build()).queue();
        leaderboardInfo.clear();

    }

}
