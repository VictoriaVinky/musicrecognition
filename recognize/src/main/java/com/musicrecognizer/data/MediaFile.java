
package com.musicrecognizer.data;

public class MediaFile {
    public long id;
    public String title;
    public String artist;
    public String album;
    public String albumArtist;
    public String genre;
    public String trackNumber;
    public String releaseDate;
    public String lyricPath;
    public String albumArtPath;

    @Override
    public String toString() {
        return "MediaFile id : " + id + " title : " + title + " artist : " + artist + " album : " + album + " albumArtist : "
                + albumArtist + " genre : " + genre + " track : " + trackNumber + " year : " + releaseDate + " lyric_path : " + lyricPath
                + " albumart_path : " + albumArtPath;
    }
}
