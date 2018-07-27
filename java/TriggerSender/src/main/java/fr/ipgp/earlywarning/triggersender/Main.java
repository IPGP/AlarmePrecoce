package fr.ipgp.earlywarning.triggersender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    private static void print(String s)
    {
        System.out.println(s);
    }

    public static void main(final String args[]) throws IOException {
        File f = new File(".");
        System.out.println(f.getCanonicalPath());

        Options options = parseArgs(args);

        try {
            Configuration.readConfiguration(options.configurationFile);
        } catch (FileNotFoundException e) {
            System.err.println("Configuration file not found: '" + options.configurationFile + "'");
            System.exit(-1);
        }

        Sender sender = new Sender();
        sender.send("default");
    }

    private static class Options {
        String configurationFile;
    }

    private static Options parseArgs(final String args[])
    {
        Options options = new Options();
        for(String arg : args)
        {
            if (arg.startsWith("--file="))
                options.configurationFile = arg.split("=")[1];
        }

        if (options.configurationFile == null)
        {
            System.err.println("Missing parameter --file");
            System.exit(-1);
        }

        return options;
    }

    private static void applyConfiguration()
    {

    }


}
