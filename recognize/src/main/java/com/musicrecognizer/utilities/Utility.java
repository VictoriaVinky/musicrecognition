
package com.musicrecognizer.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.musicrecognizer.data.MediaFile;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

public class Utility {

    /**
     * @param audioFile
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    public static MediaFile parserMetadata(File audioFile) throws IOException, SAXException, TikaException {
        InputStream input = new FileInputStream(audioFile);
        return parserMetadata(input);
    }

    public static MediaFile parserMetadata(InputStream input) throws IOException, SAXException, TikaException {
        MediaFile mediaFile = new MediaFile();

        ContentHandler handler = new DefaultHandler();
        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        ParseContext parseCtx = new ParseContext();
        parser.parse(input, handler, metadata, parseCtx);

        mediaFile.title = metadata.get("title");
        mediaFile.artist = metadata.get(XMPDM.ARTIST);
        mediaFile.album = metadata.get(XMPDM.ALBUM);
        mediaFile.albumArtist = metadata.get(XMPDM.ALBUM_ARTIST);
        mediaFile.genre = metadata.get(XMPDM.GENRE);
        mediaFile.trackNumber = metadata.get(XMPDM.TRACK_NUMBER);
        mediaFile.releaseDate = metadata.get(XMPDM.RELEASE_DATE);

        return mediaFile;
    }

    /**
     * @param source
     * @param dest
     * @param outChannelNumber
     * @param outSampleRate
     */
    public static void decodeAndResample(File source, File dest, int outChannelNumber, int outSampleRate) {
        AudioAttributes audioAttrs = new AudioAttributes();
        audioAttrs.setCodec(Constants.CODEC_PCM_S16LE);
        audioAttrs.setChannels(outChannelNumber);
        audioAttrs.setSamplingRate(outSampleRate);

        EncodingAttributes encodingAttrs = new EncodingAttributes();
        encodingAttrs.setFormat(Constants.FORMAT_U16LE);
        encodingAttrs.setAudioAttributes(audioAttrs);

        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, dest, encodingAttrs);
        } catch (IllegalArgumentException | EncoderException e) {
            e.printStackTrace();
        }
    }
}
