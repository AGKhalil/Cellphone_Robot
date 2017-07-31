# ROS research

## ROS and android

### Method1: [rosjava](http://wiki.ros.org/rosjava) & [android_ndk](http://wiki.ros.org/android_ndk)

#### cons: 
* Troublesome to compile 

* does not support jade and lunar

* cumbersome for smart phone

#### pros:
* full functionality

### Method2: [rosbridge_suite](http://wiki.ros.org/rosbridge_suite)

* Rosbridge provides a JSON API to ROS functionality for non-ROS programs. There are a variety of front ends that interface with rosbridge, including a WebSocket server for web browsers to interact with.

[This](https://github.com/djilk/ROSBridgeClient) is an android rosbridgeclient sample.
