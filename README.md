## ImageJ Plugin instructions:

Prerequisites:

1. Make sure Java SDK 1.8 or higher is installed on your system (link to download and install - http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
Please note that you will need to uninstall java 1.7 before installation of java 1.8. Once installed, imageJ/Fiji will automatically detect java 1.8 and use it as default.

Steps:

1. Download and place plugin 'SME_PROJECTION_ENS.jar' in the 'plugins' folder of Fiji/ImageJ

2. Open an image stack

3. The plugin menu (plugin) Access the plugin in simple or advanced mode from Plugins tab on Fiji/ImageJ 

For simple version, select in case of multi-channel image, the channel to create reference manifold from and then to which channels to apply.

In case of advanced version, the user can execute the algorithm step-wise (Step 1-SML, Step2- Kmeans and Step 3- energy optimization) or all at once using the batch mode. All intermediate results and even the energy and PSI plots are generated as separate images.

## MatlaB Toolbox instructions:

1. Please download the SME_toolbox.rar and unzip the folder.

2. Run the SME_demo.m
