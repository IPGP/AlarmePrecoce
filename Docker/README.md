# EarlyWarning Docker image

EarlyWarning can work from within a Docker container. This allows you to have a separate instance and to enhance compatibility with other software.

## Installing from Docker repository

TODO

## Building the image yourself

To build the EarlyWarning Docker image, you will need to compile the EarlyWarning Java app. You will of course also need Docker.

To do this, once the EarlyWarning repository is cloned, do the following.

First, compile the EarlyWarning app using Maven and move the binaries to the right directory:

```
cd EarlyWarning
mvn package
mv target/EarlyWarning.jar ../Docker/earlywarning
cp src/main/resources ../Docker/earlywarning
```

Then, build the image using Docker:

```
cd ../Docker
make build
``` 

You can use the `configuration/earlywarning.xml` file to configure your EarlyWarning instance as you would do with a non-Docker one.

In the same way, edit the following file to match your configuration:

* `asterisk/sip.conf`
* `asterisk/rtp.conf`
* Any file that is mentioned in the **Configuration Guide**.

Please adapt the files in the `asterisk` folders rather than copy and pasting the contents from the **Configuration Guide**, as the configuration scheme differs.

Once you are done with the configuration, you can run your EarlyWarning Docker container using the following command:

```
make run
```

Which is the same as doing

```
./run.sh
```