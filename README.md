# Kanopy test Android application

## Description
This Android application allows to access repositories, its commits list and details from GitHub to through GitHub API (https://developer.github.com/v3/)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

You will need to dispose of a computer with Android Studios and ADB installed. You will also need an Android smartphone or an emulated device.

### Installing

#### With the APK
* Download the KanopyTest.APK file from GitHub.
* If your device is ready, install the APK to your phone with the following ADB command.
```
$ adb install ~/YourDirectory/KanopyTest.apk
$ adb shell am start -n "fr.kanopytest.maximedonnet.kanopytest/fr.kanopytest.maximedonnet.kanopytest.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```

#### With the source code
* Download the source code from GitHub.
* Launch Android Studios and select File > New > Import project ...
* Navigate to the root path of the downloaded project and validate.
* Is your device is ready, press the Run 'App' button.

### Usage
* The first activity allows you to find GitHub repositories by their names.
* Click on an item repositories list to display its 30 last commits.
* On the new activity, you can filter commits by committer name and/or message content.
* When you select a commit, a new activity appears to show its details.
* You can click on a user avatar to access its details through the web browser.

## Authors

* **Maxime DONNET** - *Initial work* - [KanopyTest](https://github.com/maxou75/KanopyTest)

