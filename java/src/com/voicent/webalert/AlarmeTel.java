package com.voicent.webalert;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class AlarmeTel {

    public static void main(String args[]) throws InterruptedException {

        File listeAppel = new File("c:/temp/testctf.voc");
        if (listeAppel.exists())
            if (!listeAppel.delete())
                EarlyWarning.appLogger.warn("Can't delete file " + listeAppel.getAbsolutePath());

        try (FileChannel in = new FileInputStream("c:/temp/boissier.voc").getChannel(); FileChannel out = new FileOutputStream("c:/temp/testctf.voc").getChannel()) {
            // Init

            // Copie depuis le in vers le out
            in.transferTo(0, in.size(), out);
        } catch (Exception e) {
            e.printStackTrace(); // n'importe quelle exception
        } // finalement on ferme

        Voicent voicent = new Voicent();

        voicent.callTillConfirm("C:/Program Files/Voicent/BroadcastByPhone/bin/vcast.exe", "C:/temp/testctf.voc", "C:/Program Files/Voicent/MyRecordings/sample_message.wav", "1234");
    }
}