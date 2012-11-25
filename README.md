# Poll app
---

## Killer Cocktail: Play! Framework, MongoDB & Red Hat OpenShift

This is a sample application developed for
[Red Hat's Developer Day](http://www.redhat.com/summit/developerday/schedule.html). It will let you create polls and
share them on Twitter so your followers can answer them.

The application features Play Framework 2 with Java and Scala, MongoDB support, and deployment on
[OpenShift](https://openshift.redhat.com).

The application will be up and running for a couple of days after the presentation at:
[https://poll-opensas.rhcloud.com/](https://poll-opensas.rhcloud.com/)

## Quickguide to have it up and running on OpenShift

First we will create a Do-It-Yourself application on OpenShift.

```bash
rhc app create -a poll -t diy-0.1 -l <your OpenShift login> -p <your OpenShift password>

cd polls
```

Now we will add TwitterPoll's github repository as a remote and fetch it's contents

```bash
git remote add github https://github.com/opensas/twitterpoll.git

git pull -s recursive -X theirs github master
```

Next we will add mongodb support to our application running on OpenShift:

```bash
rhc app cartridge add -c mongodb-2.0 -a poll -l <your OpenShift login> -p <your OpenShift password>

RESULT:

MongoDB 2.0 database added.  Please make note of these credentials:

       Root User: admin
   Root Password: xxxxxx
   Database Name: poll

Connection URL: mongodb://xxx.xxx.xxx.xxx:27017/

You can manage your new MongoDB by also embedding rockmongo-1.1
```

We will follow the advice and install rockmongo

```bash
rhc app cartridge add -c rockmongo-1.1 -a poll -l <your OpenShift login> -p <your OpenShift password>

RESULT:

rockmongo-1.1 added.  Please make note of these MongoDB credentials again:

   RockMongo User    : admin
   RockMongo Password: xxxxxx

URL: https://poll-playdemo.rhcloud.com/rockmongo/
```

It's generally a good practice to define a custom account to access the database, instead of using the admin account
provided by OpenShift, so we'll create the database and user to be used by the application, according to the
conf/openshift.conf file.

Click on *databases*, *Create New Database*, and enter *polls* as database name.

Now click on the *polls* database on the left side of the screen, then click on *More*, *Authentication*.

Then click on *Add User* and enter *pollUser* as UserName, and *pollPass* as Password. (passwords are case-sensitive).

Now we are ready to deploy our app on the cloud. Just use the stage task to prepare your deployment

```bash
play clean compile stage
```

And add your changes to git's index, commit and push the repo upstream:

```
git add .
git commit -m "a nice message"
git push origin
```

That's it, you can now see your application running at:

```
http://poll-yournamespace.rhcloud.com
```

The first time you push the application, it will take quite a few minutes to complete, because git has to upload play's
dependencies, but after that git is smart enough to just upload the differences.

To deploy your changes, you can just repeat the steps from *play clean compile stage*, or use the helper script
'openshift_deploy'.

*NOTE* this application is currently configured to use the current stable version of play framework: version 2.0.1.
In case you decide to go with another version be sure to update *project/plugins.sbt* and *project/build.properties*
file accordingly.

To find out more about how to deploy a Play Framework application on OpenShift, have a look at
[this screencast](http://playlatam.wordpress.com/2012/05/21/deploying-play-framework-2-apps-with-java-and-scala-to-openshift/)
and check this [quickstart](https://github.com/opensas/play2-openshift-quickstart) on github.