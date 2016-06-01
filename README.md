## ImageJ Plugin instructions:

Prerequisites and installation:

1. If Fiji/ImageJ is not installed then download and install a version preferably bundled with JRE 1.8 (http://imagej.net/Fiji/Downloads). 

2. If you have an ImageJ or Fiji version already installed, make sure Java SDK 1.8 or higher is also installed on your system (or download and install it from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
Once done, ImageJ/Fiji will automatically detect Java 1.8 and use it as default.

3. Download and place plugin 'SME_stacking.jar' in the 'plugins' folder of your Fiji/ImageJ installation.

Usage in simple mode (default):

1. Open an image stack

2. In the "plugins>SME Stacking" submenu press "Process stack". If the stack is single channel, it will be processed directly. If the stack is multi channel, you will be asked to specify the reference channel from which the manifold is computed before extraction in all channels. 

Usage in advanced mode (in case you are interested in monitoring the algorithm step by step):

1. Open an image stack

2. In the "plugins>SME Stacking" submenu press "Advanced mode". 

3. The advanced mode offers an interface to see each step of the algorithm operating separately. This mode is of no interest for most users.

## MatlaB Toolbox instructions:

1. Please download SME_stacking.rar and unzip the folder.

2. Run the SME_demo.m
