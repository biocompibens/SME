## ImageJ Plugin instructions:

Prerequisites and installation:

1. If Fiji/ImageJ is not installed then download and install a version preferably bundled with JRE 1.8 (http://imagej.net/Fiji/Downloads). 

2. If you have an ImageJ or Fiji version already installed, make sure Java SDK 1.8 or higher is also installed on your system (or download and install it from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

3. Download and place plugin 'SME_stacking.jar' in the 'plugins' folder of your Fiji/ImageJ installation.

Usage:

1. Open an image stack

2. In the "plugins" menu press "SME Stacking". If the stack is single channel, it will be processed directly. If the stack is multi channel (that you can create from separate channels using "Image>Color>Merge Channels"), you will be asked to specify the reference channel from which the manifold is computed. The extraction is then processed from all channel to produced a color 2D image. 

3. Be patient! Depending on the size of the image the process can take between 1 and 10 minutes.

## Matlab Toolbox instructions:

1. Clone the src_matlab folder

2. Run the 'SME_demo_single_channel.m' for single channel stack and run the 'SME_demo_multiple_channel.m' for multi channel stack
