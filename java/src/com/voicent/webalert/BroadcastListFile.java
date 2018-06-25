/*
 * Voicent Communucations, Inc Sample Code
 * http://www.voicent.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL VOICENT COMMUNICATIONS, INC OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ----------------------------------------------------------------------
 */

package com.voicent.webalert;

//import java.io.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * fixed format: [ name, phone number, [other values]* CRLF ]*
 */

public class BroadcastListFile implements BroadcastList {
    private final ArrayList<String> curRecord_ = new ArrayList<>();
    private int total_ = -1;
    private BufferedReader br_ = null;
    private String filename_ = null;

    public BroadcastListFile(String filename) {
        try {
            filename_ = filename;
            br_ = new BufferedReader(new FileReader(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        getTotal();
        reset();
    }

    public boolean next() {
        curRecord_.clear();

        try {
            String line;
            while (true) {
                line = br_.readLine();
                if (line == null)
                    return false;
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                break;
            }

            StringTokenizer tkz = new StringTokenizer(line, ",");
            while (tkz.hasMoreTokens()) {
                String tk = tkz.nextToken();
                tk = tk.trim();
                curRecord_.add(tk);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getValue(String name) {
        if (BroadcastList.NAME.equals(name))
            return curRecord_.get(0);

        if (BroadcastList.PHONE.equals(name))
            return curRecord_.get(1);

        // implement your name value pair here
        return null;
    }

    public int getTotal() {
        if (total_ == -1) {
            try {
                total_ = 0;
                String line;
                while ((line = br_.readLine()) != null) {
                    if (line.length() == 0 || line.charAt(0) == '#')
                        continue;
                    total_++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                total_ = -1;
            }
        }

        return total_;
    }

    public void reset() {
        try {
            br_.close();
            br_ = new BufferedReader(new FileReader(filename_));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
