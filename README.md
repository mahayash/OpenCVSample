How to add OpenCV library to Android Studio

Prerequisite
Install NDK and assign location of install NDK to Android Studio

Sample#12 - Steps to add OpenCV library to Android Studio

1.	Add C++ support to Android Studio
a.	Create new project and check the "Include C++ Support"
b.	Follow the wizard without any changes
c.	Under customize c++ support, check mark both “Exception support” & “Runtime Type information Support”, then click on finish. Your new project with C++ support is ready to use
d.	Now when you build the project and run it on the device/emulator you will observe the text “Hello from c++” writing inside native-app.cpp file, which is a c++ code.

2.	Add openCV libraray
a.	Download the openCV for Android latest libraray from opencv.org (http://opencv.org/downloads.html)
b.	 From android studio File > Import new module > path of where you have stored your android for opencv sdk (~OpenCV-android-sdk\sdk\java)
c.	Once the path is set, you will see the module name OpenCVLibraryXXX , click finish
d.	Uncheck all the checkboxes, click finish
e.	This will load the openCV module to the project
3.	Configure the OpenCV build.gradle
a.	The gradle file of both your app & openCV has to be in sysnc
b.	Copy –paste value of these 4 parameters (compileSdkVersion, buildToolVersion, minSdkVersion, targetSdkVersion) from your app to that of OpenCV build.gradle file
c.	Open module dependency setting, add the OpenCVLibraryXXX to your project
d.	Add new folder under src/main/ with name jniLibs, and copy paste all the CPU architecture file from openCVSDK (~\OpenCV-android-sdk\sdk\native\libs) and paste in jniLibs folder
e.	With these steps we have finished OpenCV configuration

4.	Test the openCV code
a.	Write the code to test if OpenCV library has been loaded successfully



