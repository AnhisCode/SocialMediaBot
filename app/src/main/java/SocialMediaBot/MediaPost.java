package SocialMediaBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;

public class MediaPost {

    public static void embedTest(TextChannel channel){
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle("User is Streaming!!");
        streamInfo.setColor(0xFFC0CB);
        streamInfo.addField("Title", "Playing Game", false);
        streamInfo.setDescription("Watch now at: twitch.tv/User");
        streamInfo.setFooter("Bot created by Anh :)");

        if (channel != null) {
            channel.sendMessageEmbeds(streamInfo.build()).queue();
        }
        streamInfo.clear();
    }


    // post on discord when a user goes live
    public static void discordNotifyLive(String channelID, String userName, String imageURL, String title, String gameName) {

        // TODO this works but make it prettier

        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle(userName + " is Streaming!!");
        streamInfo.setColor(0xFFC0CB);
        streamInfo.setImage(imageURL);
        streamInfo.addField(title, "Playing " + gameName, false);
        streamInfo.setDescription("Watch now at: twitch.tv/" + userName);
        streamInfo.setFooter("Bot created by Anh :)");

        if (channel != null) {
            channel.sendMessageEmbeds(streamInfo.build()).queue();
        }
        streamInfo.clear();
    }

}
