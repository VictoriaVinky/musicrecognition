
package com.trainingmusic.data;

public class MediaFile {
    public String id;
    public String title;
    public String artist;
    public String album;
    public String albumArtist;
    public String genre;
    public String trackNumber;
    public String releaseDate;

    @Override
    public String toString() {
        return "MediaFile id : " + id + " title : " + title + " artist : " + artist + " album : " + album + " albumArtist : "
                + albumArtist + " genre : " + genre + " track : " + trackNumber + " year : " + releaseDate;
    }

    public String createUrlParameter() {
        return "mediaId=" + id + "&title=" + title + "&artist=" + artist + "&album=" + album + "&albumArtist="
                + albumArtist + "&genre=" + genre + "&trackNumber=" + trackNumber + "&releaseDate=" + releaseDate;
    }
}
