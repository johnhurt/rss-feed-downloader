// <copyright file="FileIndex.java" company="RookAndPawn">
// Copyright (c) 2011 All Rights Reserved, http://rookandpawn.com/
package com.rookandpawn.rss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * keeps track of the recent history of downloaded files from rss feeds
 *
 * @author Kevin Guthrie
 */
public class FileIndex {

    private static String fileName;
    private static HashMap<String, Long> processed;

    /**
     * open the given file and read each entry in the form.
     *
     * timestamp1 filename1
     * timestamp2 filename2
     *
     * @param fileName
     */
    public static void init(String fileName) {
        String sLine;
        String[] sFields;
        BufferedReader br;
        processed = new HashMap<String, Long>();
        long tStamp;

        try {
            FileIndex.fileName = fileName;

            if (!(new File(fileName).exists())) {
                return;
            }

            br = new BufferedReader(new FileReader(fileName));

            long now = System.currentTimeMillis();

            while ((sLine = br.readLine()) != null) {
                sFields = sLine.split(" ", 2);
                tStamp = Long.parseLong(sFields[0]);

                // If the timestamp from the current entry is more than 30 days
                // old, then don't include it
                if ((now - tStamp) > (1000L * 60L * 60L * 24L * 30L)) {
                    continue;
                }

                processed.put(sFields[1].trim().toLowerCase(),
                        Long.parseLong(sFields[0]));
            }

            br.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Has the given file been downloaded already?
     *
     * @param fileName
     * @return
     */
    public static boolean isDownloaded(String fileName) {
        return processed.containsKey(fileName.toLowerCase().trim());
    }

    /**
     * Indicate that the given file has been downloaded
     *
     * @param fileName
     */
    public static void add(String fileName) {
        processed.put(fileName.toLowerCase().trim(),
                System.currentTimeMillis());
    }

    /**
     * save the list of processed files in to the history file with which it was
     * initiated
     */
    public static void finish() {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(fileName));

            for (Map.Entry<String, Long> entry : processed.entrySet()) {
                pw.println(entry.getValue() + " " + entry.getKey());
            }

            pw.flush();
            pw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
