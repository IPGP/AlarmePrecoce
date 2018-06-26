package com.voicent.webalert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AlarmeTel {

    public static void main(String args[]) throws InterruptedException {

        File listeAppel = new File("c:/temp/testctf.voc");
        if (listeAppel.exists()) {
            listeAppel.delete();
        }

        FileChannel in = null; // canal d'entree
        FileChannel out = null; // canal de sortie

        try {
            // Init
            in = new FileInputStream("c:/temp/boissier.voc").getChannel();
            out = new FileOutputStream("c:/temp/testctf.voc").getChannel();

            // Copie depuis le in vers le out
            in.transferTo(0, in.size(), out);
        } catch (Exception e) {
            e.printStackTrace(); // n'importe quelle exception
        } finally { // finalement on ferme
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        Voicent voicent = new Voicent();

        voicent.callTillConfirm("C:/Program Files/Voicent/BroadcastByPhone/bin/vcast.exe", "C:/temp/testctf.voc", "C:/Program Files/Voicent/MyRecordings/sample_message.wav", "1234");
    }
}