package SocialMediaBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;


public class MediaPost {

    public static String defaultColour = "0xFFC0CB";

    // post on discord when a user goes live
    public static void discordNotifyLive(String channelID, String userName, String imageURL,
                                         String title, String gameName, String profileIconURL, String defaultMessage, String embedColour) {

        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle("**"+userName + " is Streaming!!**", "https://www.twitch.tv/"+userName);
        streamInfo.setColor(Color.decode(embedColour));
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
    public static void discordNotifyOffline(String channelID, String userName, String profileIconURL, String embedColour) {
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle(":zzz:**"+userName + " Has Finished Streaming!!:zzz:**", "https://www.twitch.tv/"+userName);
        streamInfo.setColor(Color.decode(embedColour));
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
    public static void displayLeaderboard(TextChannel channel, String streamerName, String embedColour){
        ArrayList<ArrayList> topViewers = UpdateDB.getViewerLeaderBoard(streamerName);
        ArrayList<String> viewers;
        ArrayList<Integer> messageCount;

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
        leaderboardInfo.setColor(Color.decode(embedColour));
        leaderboardInfo.setFooter("Type: \"=>leaderboard "+streamerName+"\" to update this leaderboard :)");

        channel.sendMessageEmbeds(leaderboardInfo.build()).queue();
        leaderboardInfo.clear();

    }


    // info post about instruction
    public static void displayInfo(TextChannel channel){
        EmbedBuilder displayInfo = new EmbedBuilder();
        displayInfo.setTitle("**:heart: Thank you for using MediaBot! :heart:**");
        displayInfo.addField("Owner Commands :person_in_tuxedo:", "```=>adduser <Twitch Username>```" +
                "```=>removeuser <Twitch Username>```To get started as a server owner, please use **adduser** " +
                "command in a channel the bot has access to to start monitoring a streamer. To remove a " +
                "streamer and stop getting notified when they go live, use the **removeuser** command in the same " +
                "channel.\n ``` Example: =>adduser anhiswow```",false);
        displayInfo.addField("User Commands :person_bowing:", "```=>leaderboard <Twitch Username>```" +
                "Use this command to show the leaderboard of the top 10 chatters for a streamer. If all the" +
                " entry is None, please ask the server owner to see if the streamer is monitored or the " +
                "name is spelled correctly.\n ``` Example: =>removeuser anhiswow```",false);
        displayInfo.addField("Customisation:tools:", "```=>customisation``` To change colour" +
                " and messages of embed notification check out **customisation**. *This is for" +
                " owners only", false);
        displayInfo.addField("My bot isn't working", "After you use the **adduser** command, make sure the bot" +
                " send a message saying \"User has been added to monitored channel\", this means the bot works. " +
                "If this message doesnt appear, please make sure the bot has enough permission to send messages and embeds in the text channel. " +
                "If the problem persists, please give the bot admin permission over the text channel. Any further " +
                "inquiries please contact me at Anh#4402", false);
        displayInfo.setImage("https://i.ibb.co/M5pn7Tz/Media-Bot.png");
        displayInfo.setColor(Color.decode(defaultColour));
        displayInfo.setFooter("When filling out the twitch username, please dont include the <> ");

        channel.sendMessageEmbeds(displayInfo.build()).queue();
        displayInfo.clear();

    }


    // info post about customisation
    public static void displayCustomisationInfo(TextChannel channel){
        EmbedBuilder displayInfo = new EmbedBuilder();
        displayInfo.setTitle("**:tools:Customisation:tools:**");
        displayInfo.addField("Change Colour :paintbrush:", "```=>setcolour <twitch username> <0x??????>```" +
                "```=>setcolourall <0x??????>``` To change the colour of one streamer please use **setcolour**." +
                "To change the colour of all streamers in a channel use **setcolourall**." +
                " 0x?????? is the hexadecimal RGB code. use https://www.rapidtables.com/web/color/RGB_Color.html to" +
                " choose colour.\n ``` Example: =>setcolour anhiswow 0xA12D45```",false);
        displayInfo.addField("Change Notification Message :speech_left:", "```=>setmessage <twitch username> " +
                "<Message (can be longer than one word)>```" +
                "```=>setmessageall <Message (can be longer than one word)>```" +
                "To change the notification message of one streamer please use **setmessage**. " +
                "To change the notification message of all streamer in a given channel, use **setmessageall**.\n " +
                "``` Example: =>setmessage anhiswow Hey @everyone, Anh is streaming!```",false);
        displayInfo.setImage("https://i.ibb.co/M5pn7Tz/Media-Bot.png");
        displayInfo.setColor(Color.decode(defaultColour));
        displayInfo.setFooter("Bot developed by Anh :)");

        channel.sendMessageEmbeds(displayInfo.build()).queue();
        displayInfo.clear();

    }


    // show the user what colour they changed to
    public static void displayColour(String channelID, String embedColour){
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder displayColourInfo = new EmbedBuilder();
        displayColourInfo.setTitle("**Colour Changed**");
        displayColourInfo.setColor(Color.decode(embedColour));

        channel.sendMessageEmbeds(displayColourInfo.build()).queue();
        displayColourInfo.clear();
    }

    // show the user what user they added
    public static void displayAddUser(String channelID, String streamerName){
        String profileIconURL = App.getPlayerProfileIMG(streamerName);
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder addUserInfo = new EmbedBuilder();
        addUserInfo.setTitle("**User Added**");
        addUserInfo.setImage(profileIconURL);
        addUserInfo.setColor(Color.decode(defaultColour));

        channel.sendMessageEmbeds(addUserInfo.build()).queue();
        addUserInfo.clear();
    }

    // log info to own channel
    public static void logInfo(String info){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String channelID = "934336790876274718";
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        channel.sendMessage(info + " - " + dtf.format(now)).queue();
    }
}
