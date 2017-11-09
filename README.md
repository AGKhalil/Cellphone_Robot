# User Guide

Welcome to the Cellphone_Robot project! Through this project we are aiming to expand on Android based robotics. myRobot, the app within this repository, was created to control a small ROS controlled car-robot. The project showcases how an Android phone can significantly enhance a ROS-based robot through leveraging the many APIs out there. This project utlizes multiple libraries as well as APIs. In this user guide we will walk you through setting up the project for yourself and setting up the necessary access keys to use the app in tandem with a ROS project. That being said, all the libraries and APIs can be replaced or expanded upon so do not feel limited to our implementation of the repo. 

This repo allow the robot to: access a Natural Language Processor, send SMS, send Slack messages, send GroupMe messages, post to Twitter, perform basic image processing, and communicate back and forth with a ROS network. In our hardware setup, we had the ROS network running on a Raspberry Pi 3 mounted on the RC robot itself. We used a portable rechargable battery to make this happen. The phone is also mounted on the robot using a cellphone holder. Have fun experimenting!

This user guide focuses mainly on setting up the Android app; however, to communicate with the ROS network a few steps need to be taken. This [repo](https://github.com/wang3303/ros_cellphonerobot) contains the ROS project we created to test the app with. To use the app you do not need to clone the whole project. There is only one script that your ROS project needs to have. That user guide will show you how to set up the script for your own uses, or you can just clone the whole project.

## Libraries
### OpenCV
OpenCV provides some tutorials and documentation through [here](https://opencv.org/platforms/android/). This library is processor dependent, so you need to find out your phone's ARM processor version and allocate the correct file to the jniLibs directory. 

First, make sure you have the [Android Debug Bridge (adb)](https://developer.android.com/studio/command-line/adb.html) installed.

After the installation is complete, run this: `adb shell getprop ro.product.cpu.abilist`. The output will tell you the phone's ARM processor version. For example, when I run the above command on a [Huawei Honor 5x](http://www.hihonor.com/global/products/mobile-phones/honor5x/index.html), the output is `arm64-v8a,armeabi-v7a,armeabi`.

I can pick any processor of the three to allocate to the OpenCV library. For guide's purpose, let's choose `arm64-v8a`. 

Next, go to [processor_libs](/processor_libs)

This project is still in early development, further updates and robots will be coming soon. In the meantime please feel free to delve into our extensive [wiki](https://github.com/AGKhalil/Cellphone_Robot/wiki) or the first [research paper](https://github.com/AGKhalil/Cellphone_Robot/blob/master/Cell%20Phone%20Robot%20Paper.pdf) written surrounding this project and Lily, our first robot.

Here's a picture of Lily!
<p align="center">
<img src="https://github.com/AGKhalil/Cellphone_Robot/blob/master/wiki_images/Lily/FrontShot.png" width="400">
</p>
<p align="center">
Front view of Lily
</p>