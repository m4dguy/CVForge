#CVForge 0.2.0 beta
To be pushed next week. In case of interest, feel free to contact me.

Soon to be fixed bugs and limitations: 
Only single-channel (gray-scale) images are supported.
No support for macros and image stacks.
Windows-only.

You can find the newest version on github:
https://github.com/m4dguy/CVForge



CVForge aims to close the gap between the popular tool ImageJ and the powerful OpenCV library.
This plugin gives access to all methods of OpenCV, independent from it's version.
Expand and boost ImageJ with OpenCV's selection of powerful methods.
Use the guides and documentation on the OpenCV website for detailed information on efficient usage:
http://docs.opencv.org/2.4/doc/tutorials/tutorials.html
http://docs.opencv.org/2.4/modules/refman.html
http://opencv.org/opencv-java-api.html


 
#Why CVForge might be for you

##Considerable speed boost:
Under-the-hood usage of C/C++ implementations lets you perform many operations in the blink of an eye.
Most methods you already know from ImageJ run faster and smoother here.

##Expand your ImageJ functionality:
OpenCV is a massive library containing a vast selection of cutting-edge methods.
If ImageJ is missing something you are looking for, you might find it here.

##Comfortable user-interface: 
OpenCV is a complex library which is meant to be used by programmers.
CVForge provides you a graphical user interface to use OpenCV in the most comfortable way possible.
Easily reimplement code from tutorials by clicking your way through the CVForge's interface!

##Independent of OpenCV version:
Download the newest OpenCV version or use one of the earlier versions.
OpenCV is an actively maintained library which gets updated and extended regularly.
You are also free to load and use your own build of the OpenCV library.
No matter your preference, you can switch between different versions on-the-fly and choose the one which suits you best.

##Interface for Scripting:
Convert ImageJ objects to OpenCV objects and vice versa.
CVForge itself uses converters and gives you access to them for your own scripts.

Dynamically switches between x64/x86:
No matter if x86 or x64, CVForge runs on both architectures and dynamically switches to the one currently running.



#Installation:
* Copy "CVForge_[version number].jar" and "CVForgeExecution.jar" into the ImageJ plugin folder.
* Install an OpenCV jar as described below.

##Automatic OpenCV jar installation:
* Launch CVForge from inside of ImageJ
* In CVForge, click "Plugin" > "Install"
* Navigate to the OpenCV folder you downloaded from the OpenCV website
* Go to "build" > "java"
* Select and open the file "opencv-[version number].jar"
* CVForge will copy the necessary files into the plugin folder of ImageJ
* Restart ImageJ

##Manual OpenCV jar installation:
* Navigate to the OpenCV folder you downloaded from the OpenCV website
* Go to "build" > "java"
* Copy-paste the content of this folder (subfolers included!) into ImageJ's plugin folder
* CVForge will detect and list the installed jar
