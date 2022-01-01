package SocialMediaBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.util.stream.Stream;

public class MediaPost {

    // post on discord when a user goes live
    public static void discordNotifyLive(String channelID, String userName, String imageURL, String title, String gameName, int viewers, String profileIconURL) {

        // TODO this works but make it prettier
        TextChannel channel = Commands.jda.getTextChannelById(channelID);
        EmbedBuilder streamInfo = new EmbedBuilder();
        streamInfo.setTitle("**"+userName + " is Streaming!!**", "https://www.twitch.tv/"+userName);
        streamInfo.setColor(0xFFC0CB);
        streamInfo.setThumbnail(profileIconURL);
        streamInfo.setImage(imageURL);
        streamInfo.addField("Game :video_game:", "Playing " + gameName, true);
        streamInfo.addField("Viewers :people_holding_hands:", viewers + " viewers", true);
        streamInfo.addField("Watch now at :tv:", "https://www.twitch.tv/"+userName, true);
        streamInfo.setDescription("**"+title+"**");

        if (channel != null) {
            channel.sendMessage(":white_flower: Hey! @everyone :white_flower:").queue();
            channel.sendMessageEmbeds(streamInfo.build()).queue();
        }
        streamInfo.clear();

        // now send the leaderboard
        EmbedBuilder leaderboardInfo = new EmbedBuilder();
        leaderboardInfo.setTitle("**:speech_left:"+userName + "'s Top 10 Chatters!!:speech_left:**");
        leaderboardInfo.addField("#1 Viewer :first_place:", "placeHolder - 100 messages", true);
        leaderboardInfo.addField("#2 Viewer :second_place: ", "placeHolder - 80 messages", true);
        leaderboardInfo.addField("#3 Viewer :third_place:", "placeHolder - 70 messages", true);
        leaderboardInfo.addField("The Rest :medal:","-PlaceHolder-60\n-PlaceHolder-50\n-PlaceHolder-40\n-PlaceHolder-30\n-PlaceHolder-20\n",false);
        leaderboardInfo.setColor(0xFFC0CB);
        leaderboardInfo.setFooter("Bot created by Anh :)");

        channel.sendMessageEmbeds(leaderboardInfo.build()).queue();
        leaderboardInfo.clear();

    }

}
