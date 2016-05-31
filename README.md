# SME

ImageJ Plugin instructions:

Prerequisites:
1. Java SDK 1.8 (link to download and install - http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
Please note that, you need to uninstall java 1.7 and then install java 1.8. In this way, imageJ/Fiji will automatically detect java 1.8 and use it as default.

Steps:
1. Place plugin 'SME_PROJECTION_ENS.jar' in Plugins folder of Fiji/ImageJ
2. Open image stack
3. Access plugin in simple or advanced mode from Plugins tab on Fiji/ImageJ 
3.1 For simple version, select in case of multi-channel image, the channel to create reference manifold from and then to which channels to apply.
3.2 In case of advanced version, the user can execute the algorithm step-wise (Step 1-SML, Step2- Kmeans and Step 3- energy optimization) or all at once using the batch mode. All intermediate results and even the energy and PSI plots are generated as separate images.

MatlaB Toolbox instructions:
1. Please download the SME_toolbox and unzip the folder.
2. Run the SME_demo.m
