/*
 * This class was automatically generated as part of the simple java archetype
 * written by rook and pawn software
 */
package com.rookandpawn.rss;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.digester.rss.Channel;
import org.apache.commons.digester.rss.Item;
import org.apache.commons.digester.rss.RSSDigester;

public class Main {

    private static URL url = null;
    private static File downloadFolder = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        File currFile;
        String currDescrition;
        String[] sFields;
        URL currUrl;
        InputStream fileDownloader;
        ByteArrayOutputStream buffer;
        String fileName;
        DataOutputStream dos;

        FileIndex.init("history.txt");

        try {
            if ((args == null) || (args.length < 2)) {
                System.out.println("Application must be run with two "
                        + "arguments. 1. feed url; 2. download location");
                return;
            }

            url = new URL(args[0]);
            downloadFolder = new File(args[1]);

            RSSDigester digester = new RSSDigester();
            HttpURLConnection httpSource
                    = (HttpURLConnection) url.openConnection();
            Channel channel
                    = (Channel) digester.parse(httpSource.getInputStream());
            if (channel == null) {
                throw new Exception("can't communicate with " + url);
            }

            Item rssItems[] = channel.findItems();

            for (Item rssItem : rssItems) {
                currUrl = new URL(rssItem.getLink());
                currDescrition = rssItem.getDescription();
                sFields = currDescrition.split(";");
                for (String sField : sFields) {

                    // This logic is specific, and should be generalized
                    if (!sField.toLowerCase().trim().startsWith("filename")) {
                        continue;
                    }

                    fileName = sField.trim().split("\\s+", 2)[1].trim();

                    if (FileIndex.isDownloaded(fileName)) {
                        break;
                    }

                    httpSource = (HttpURLConnection) currUrl.openConnection();

                    // Just in case set user agent to something unobjectionable
                    httpSource.addRequestProperty("Accept",
                            "text/html,application/xhtml+xml,"
                                    + "application/xml;q=0.9,*/*;q=0.8");
                    httpSource.addRequestProperty("User-Agent",
                            "Mozilla/5.0 (Macintosh; "
                                    + "Intel Mac OS X 10.9; rv:26.0) "
                                    + "Gecko/20100101 Firefox/26.0");

                    // download the file in managebly sized chunks
                    fileDownloader = httpSource.getInputStream();
                    buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[8192];
                    while ((nRead =
                            fileDownloader.read(data, 0, data.length)) >= 0) {
                        buffer.write(data, 0, nRead);
                    }
                    fileDownloader.close();
                    buffer.flush();

                    // Write the file to disk as a .tmp file
                    currFile = new File(downloadFolder, fileName + ".tmp");
                    currFile.createNewFile();
                    dos = new DataOutputStream(new FileOutputStream(
                            currFile));
                    dos.write(buffer.toByteArray());
                    dos.flush();
                    dos.close();
                    buffer.close();

                    // rename the .tmp file to correct filename.  This should
                    // probably also be conifurable :P
                    currFile.renameTo(new File(downloadFolder,
                            fileName + ".torrent"));
                    FileIndex.add(fileName);

                }
            }
        }
        catch (Exception ex) {
            System.err.println("Error reading from feed.  Try again later");
            ex.printStackTrace();
        }

        FileIndex.finish();
    }
}
