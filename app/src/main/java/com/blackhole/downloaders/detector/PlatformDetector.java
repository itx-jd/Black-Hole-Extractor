package com.blackhole.downloaders.detector;

public class PlatformDetector {

    public static String getPlatformName(String url) {

        if(url.contains("youtube") || url.contains("youtu.be")) {
            return "YouTube";
        } else if (url.contains("facebook") || url.contains("fb.com") || url.contains("fb.watch")) {
            return "Facebook";
        } else if (url.contains("instagram")) {
            return "Instagram";
        } else if (url.contains("twitter") || url.contains("x.com")) {
            return "X (Twitter)";
        } else if (url.contains("vimeo")) {
            return "Vimeo";
        } else if (url.contains("dailymotion")) {
            return "Dailymotion";
        } else if (url.contains("tiktok")) {
            return "TikTok";
        } else if (url.contains("pinterest")) {
            return "Pinterest";
        } else if (url.contains("linkedin")) {
            return "LinkedIn";
        } else if (url.contains("tumblr")) {
            return "Tumblr";
        } else if (url.contains("snapchat") || url.contains("t.snapchat")) {
            return "Snapchat";
        } else if (url.contains("reddit")) {
            return "Reddit";
        } else if (url.contains("soundcloud")) {
            return "SoundCloud";
        } else if (url.contains("spotify")) {
            return "Spotify";
        } else if (url.contains("twitch")) {
            return "Twitch";
        } else if (url.contains("mixcloud")) {
            return "Mixcloud";
        } else if (url.contains("bandcamp")) {
            return "Bandcamp";
        } else if (url.contains("bitchute")) {
            return "BitChute";
        } else if (url.contains("rumble")) {
            return "Rumble";
        } else if (url.contains("odnoklassniki")) {
            return "Odnoklassniki";
        } else if (url.contains("vk")) {
            return "VK";
        } else if (url.contains("metacafe")) {
            return "Metacafe";
        } else if (url.contains("break")) {
            return "Break";
        } else if (url.contains("buzzfeed")) {
            return "BuzzFeed";
        } else if (url.contains("flickr")) {
            return "Flickr";
        } else if (url.contains("imgur")) {
            return "Imgur";
        } else if (url.contains("9gag")) {
            return "9GAG";
        } else if (url.contains("gfycat")) {
            return "Gfycat";
        } else if(url.contains("streamable")) {
            return "Streamable";
        } else if(url.contains("ted")) {
            return "TED";
        } else if(url.contains("archive")) {
            return "Internet Archive";
        } else if(url.contains("bloomberg")) {
            return "Bloomberg";
        } else if(url.contains("cnn")) {
            return "CNN";
        } else if(url.contains("bbc")) {
            return "BBC";
        } else if(url.contains("foxnews")) {
            return "Fox News";
        } else if(url.contains("msnbc")) {
            return "MSNBC";
        } else if(url.contains("nbcnews")) {
            return "NBC News";
        } else if(url.contains("abcnews")) {
            return "ABC News";
        } else if(url.contains("cbsnews")) {
            return "CBS News";
        } else if(url.contains("usatoday")) {
            return "USA Today";
        } else if(url.contains("nytimes")) {
            return "The New York Times";
        } else if(url.contains("washingtonpost")) {
            return "The Washington Post";
        } else if(url.contains("wsj")) {
            return "The Wall Street Journal";
        } else if(url.contains("latimes")) {
            return "Los Angeles Times";
        } else if(url.contains("chicagotribune")) {
            return "Chicago Tribune";
        } else if(url.contains("huffpost")) {
            return "HuffPost";
        } else if(url.contains("vox")) {
            return "Vox";
        } else if(url.contains("vice")) {
            return "Vice";
        } else if(url.contains("salon")) {
            return "Salon";
        } else if(url.contains("slate")) {
            return "Slate";
        } else if(url.contains("politico")) {
            return "Politico";
        } else if(url.contains("thehill")) {
            return "The Hill";
        } else if(url.contains("theatlantic")) {
            return "The Atlantic";
        } else if(url.contains("newyorker")) {
            return "The New Yorker";
        } else if(url.contains("forbes")) {
            return "Forbes";
        } else if(url.contains("fortune")) {
            return "Fortune";
        } else if(url.contains("businessinsider")) {
            return "Business Insider";
        } else if(url.contains("bloomberg")) {
            return "Bloomberg";
        } else if(url.contains("wistia")) {
            return "Wistia";
        } else if(url.contains("zalando")) {
            return "Zalando";
        } else if(url.contains("vero")) {
            return "Vero";
        } else if(url.contains("yandex")) {
            return "Yandex Video";
        } else if(url.contains("kuaishou")) {
            return "Kuaishou";
        } else if(url.contains("toutiao")) {
            return "Toutiao";
        } else if(url.contains("tera")){
            return "Terabox";
        } else if(url.contains("threads")){
            return "Threads";
        }else {
            return "Unknown";
        }

    }
}
