# TwitterPoll
---

## Killer Cocktail: Play! Framework, MongoDB & Red Hat OpenShift

This is a sample application developed for
[Red Hat's Developer Day](http://www.redhat.com/summit/developerday/schedule.html). It will let you create polls and
share them on Twitter so your followers can answer them.

The application features Play Framework 2 with Java and Scala, MongoDB support, and deployment on
[OpenShift](https://openshift.redhat.com).

The application will be up and running at:
[https://poll-opensas.rhcloud.com/](https://poll-opensas.rhcloud.com/)

Source is available at [https://github.com/opensas/twitterpoll](https://github.com/opensas/twitterpoll)

Slides available at [https://poll-opensas.rhcloud.com/assets/slides/slides.html](https://poll-opensas.rhcloud.com/assets/slides/slides.html)

## How does it work?

[TwitterPoll](https://poll-opensas.rhcloud.com/) is a simple web application. It lets you create polls, save them to a MongoDB database, answer and then share them on twitter.

To be able to vote and create polls, you first need to log in with twitter.

Then you enter the question and three possible answers.

You also have a rest web service to fetch all the current polls, developed in Java (have a look at [/api/polls](https://poll-opensas.rhcloud.com/api/polls)) just to showcase the interaction of Scala and Java code.

The application has been kept simple enough to be shown in a live editing session and to serve as a building block for an application interacting with Twitter and MongoDB.

## Future improvements

Version 2.1 of Play! (current release candidate version) will be featuring a new non-blocking, asynchronous MongoDB driver, called [ReactiveMongo](http://reactivemongo.org/).

Using this driver will allow us to avoid any kind of blocking request.

## Dependencies

Casbah: Officially supported Scala driver for MongoDB (https://github.com/mongodb/casbah)

Salat: Salat is a simple serialization library for case classes (https://github.com/novus/salat)

Play-Salat: MongoDB Salat plugin for Play Framework 2 (https://github.com/leon/play-salat)

## Quickguide to have it up and running on OpenShift

First of al you'll need a valid play framework installation. Just follow this [instructions](http://www.playframework.org/documentation/latest/Installing) to download and install the latest stable version of Play Framework.

To test it locally, you will also need a mongoDB server. Check the [instructions](http://www.mongodb.org/display/DOCS/Quickstart) corresponding to your operating system.

Now, to deploy it on OpenShift, you'll first have to [register an account](https://openshift.redhat.com/app/account/new) and then follow [this instructions](https://openshift.redhat.com/community/get-started) to install the client tools and set up your development environment. You have more detailed instructions [here](https://openshift.redhat.com/community/developers/install-the-client-tools)

Then we will create a Do-It-Yourself application on OpenShift.

```bash
rhc app create -a poll -t diy-0.1 -l <your OpenShift login> -p <your OpenShift password>

cd poll
```

Now we will add TwitterPoll's github repository as a remote and fetch it's contents

```bash
git remote add github https://github.com/opensas/twitterpoll.git

git pull -s recursive -X theirs github master
```

Next we will add mongodb support to our application running on OpenShift:

```bash
rhc cartridge add mongodb-2.2 -a poll -l <your OpenShift login> -p <your OpenShift password>

Success
mongodb-2.2
===========
  Properties
  ==========
    Connection URL = mongodb://xxx.xxx.xxx.xxx:27017/
    Username       = admin
    Password       = xxxxxx
    Database Name  = poll
```

You can manage your new MongoDB by also embedding rockmongo-1.1.

```bash
rhc cartridge add rockmongo-1.1 -a poll -l <your OpenShift login> -p <your OpenShift password>

Success
rockmongo-1.1
=============
  Properties
  ==========
    Password       = xxxxxx
    Connection URL = https://poll-<your namespace>.rhcloud.com/rockmongo/
    Username       = admin

```

It's generally a good practice to define a custom account to access the database, instead of using the admin account
provided by OpenShift, so we'll create the database and a user to be used by the application, according to the
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

##Licence

TwitterPoll is distributed under the Apache 2 licence.