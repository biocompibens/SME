## ImageJ Plugin instructions:

Prerequisites and installation:

1. If Fiji/ImageJ is not installed then download and install a version preferably bundled with JRE 1.8 (http://imagej.net/Fiji/Downloads). 

2. If you have an ImageJ or Fiji version already installed, make sure it is unsing Java 1.8 or higher.

3. Download 'SME_stacking.zip' and unzip it. Place SME_stacking.jar in the 'plugins' folder of your Fiji/ImageJ installation.

Usage:

1. Open an image stack

2. In the "plugins" menu press "SME Stacking". If the stack is single channel, it will be processed directly. If the stack is multi channel (can be create from separate channels using "Image>Color>Merge Channels", make sure "Create composite" is checked), you will be asked to specify the reference channel from which the manifold is computed. The extraction is then processed from all channel to produce a color 2D image. 

3. Be patient! Depending on the size of the image the process can take between 1 and 10 minutes.

## Matlab Toolbox instructions:

1. Clone the src_matlab folder

2. Run the 'SME_demo_single_channel.m' for single channel stack and run the 'SME_demo_multiple_channel.m' for multi channel stack

## Sample datasets:

The sample_datasets folder contains sample 3D image stack with one or two channels

