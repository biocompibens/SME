## ImageJ Plugin instructions:

Prerequisites and installation:

1. If Fiji/ImageJ is not installed then download and install a version preferably bundled with JRE 1.8 (http://imagej.net/Fiji/Downloads). 

2. If you have an ImageJ or Fiji version already installed, make sure it is unsing Java 1.8 or higher.

3. Download 'SME_stacking.zip' and unzip it. Place SME_stacking.jar in the 'plugins' folder of your Fiji/ImageJ installation.

Usage:

1. Open an image stack

2. In the "plugins" menu press "SME Stacking". You can choose the reference channel from which the manifold is computed and the imaging modality of the microscope. Also as optional you may add several layers below and above the manifold.

3. Be patient! Depending on the size of the image the process can take between 1 and 15 minutes.

## Matlab Toolbox instructions:

1. Clone the src_matlab folder

2. Run the 'SME_demo_single_channel.m' for single channel stack and run the 'SME_demo_multiple_channel.m' for multi channel stack

3. Final composite image is saved in a folder created as the filename (** Input image needs to be in the tif/tiff format)

## Sample datasets:

The sample_datasets folder contains sample 3D image stack with one or multiple channels. The larger datasets are in zipped format, which needs to be unzipped before running with the plugin.

#Citation

If you use the toolbox, please cite the paper using

Shihavuddin, Asm; Basu, Sreetama & Rexhepaj, Elton et al. (2017 May 31), "Smooth 2D manifold extraction from 3D image stack.", Nature Communications 8, PMID 28561033, doi:10.1038/ncomms15554.
(https://www.nature.com/articles/ncomms15554)

