## CVForge 0.4.0 beta
Watch the demo video on youtube:
* https://youtu.be/irnBuvW2dEo

Pre-built jars are available in the "build" directory.
Be aware that this software is in early beta stage. What you have here is work-in-progress and subject to changes.
If you encounter bugs or strange behaviour, please send a report to janmartens@live.de
A report should contain error messages, a list of steps leading to the bug and the image you used. 


CVForge aims to close the gap between the popular tool ImageJ and the powerful OpenCV library.
This plugin gives access to all methods of OpenCV, independent from it's version.
Expand and boost ImageJ with OpenCV's selection of powerful methods.
Use the guides and documentation on the OpenCV website for detailed information on efficient usage:
* http://docs.opencv.org/2.4/doc/tutorials/tutorials.html
* http://docs.opencv.org/2.4/modules/refman.html
* http://opencv.org/opencv-java-api.html


 
## Why CVForge might be for you

* Expand your ImageJ functionality:
OpenCV is a massive library containing a vast selection of cutting-edge methods.
It is actively maintained and gets updated and extended regularly.
If ImageJ is missing something you are looking for, you might find it here.

* Considerable speed boost:
Under-the-hood usage of C/C++ implementations lets you perform many operations in the blink of an eye.
Most methods you already know from ImageJ run faster and smoother here.

* Comfortable user-interface: 
OpenCV is a complex library which is meant to be used by programmers.
CVForge provides a graphical user interface to use OpenCV in the most comfortable way possible.
Easily reimplement code from tutorials by clicking your way through the interface!

* Independent of OpenCV version:
Download the latest OpenCV version or use an earlier one.
You are free to load and use your own build of the OpenCV library.
No matter your preference, you can switch between different versions on-the-fly and choose the one which suits you best.

* Macro support:
Enhance your productivity by recording your actions and running them as ImageJ macros.

* Interface for Scripting:
Convert ImageJ objects to OpenCV objects and vice versa.
CVForge itself uses converters and gives you access to them through the separate CVForgeConverion jar for your own scripts.

* Flexible and portable support:
No matter if Windows, Linux or Mac, CVForge runs on all major operating systems with only slight differences and full functionality.
Independent of x86 or x64 architectures, CVForge will dynamically switch to the one currently used.



## Installation

Installation:
* Copy "CVForge_[version number].jar", "CVForgeConversion.jar" and "ReflectionTools.jar" into the ImageJ plugin folder.
* To use OpenCV functions, you need to install an OpenCV jar (see the following sections).

Automatic OpenCV jar installation (Windows):
* Launch CVForge from inside of ImageJ
* In CVForge, click "Plugin" > "Install"
* Navigate to the OpenCV folder you downloaded from the OpenCV website
* Navigate to the "build" > "java" directory
* Select and open the file "opencv-[version number].jar"
* CVForge will copy the necessary files into the plugin folder of ImageJ
* Fiji users must restart the application.

Manual OpenCV jar installation (Windows):
* Navigate to the OpenCV folder you downloaded from the OpenCV website
* Go to "build" > "java"
* Copy-paste the content of this folder (subfolers included!) into ImageJ's plugin folder
* CVForge will detect and list the installed jar

OpenCV installation (Linux/ Mac):
* Download and build OpenCV on your system
* Once an OpenCV version with java a module is installed, CVForge will find and detect the jar in your system
