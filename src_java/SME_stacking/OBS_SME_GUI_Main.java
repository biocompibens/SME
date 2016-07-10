package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.IJ;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Swing-based program for testing the versions of K-Means on 
 * randomly generated data.
 */
public class OBS_SME_GUI_Main extends JFrame implements ActionListener, SME_KMeansListener {

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane mMessageAreaSP = new JScrollPane();
    JTextArea mMessageArea = new JTextArea();
    JButton mRunButton = new JButton();
    JPanel mTopPanel = new JPanel();
    JLabel mImplementationLabel = new JLabel();
    JTextField mRandomSeedTF = new JTextField();
    JLabel mRandomSeedLabel = new JLabel();
    JTextField mClusterCountTF = new JTextField();
    JLabel mClusterCountLabel = new JLabel();
    JTextField mCoordCountTF = new JTextField();
    JLabel mCountLabel = new JLabel();
    JComboBox mImplementationCB = new JComboBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel mThreadCountLabel = new JLabel();
    JTextField mThreadCountTF = new JTextField();

    private boolean mRunning;
    private SME_KMeans_Paralel mKMeans;
    private SME_Cluster[] clustersKmeans;
    private static final String BASIC_KMEANS = "Basic K-Means Clustering";
    private static final String BENCHMARKED_KMEANS = "Benchmarked K-Means Clustering";
    private static final String CONCURRENT_KMEANS = "Concurrent K-Means Clustering";
    private double[][] coordinates ;

    public SME_Cluster[] getClustersKmeans() {
        return clustersKmeans;
    }

    public void sme_project(){}

    public void setClustersKmeans(SME_Cluster[] clustersKmeans) {
        this.clustersKmeans = clustersKmeans;
    }


    public OBS_SME_GUI_Main(int numClust_, double[][] result_fft) {
            
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        coordinates = result_fft;
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setSize(new Dimension(620, 760));
        setTitle("KMeans Test");
        mMessageArea.setText("");
        mRunButton.setText("Run KMeans");
        mRunButton.addActionListener(this);
        mImplementationLabel.setText("KMeans Implementation:");
        mImplementationCB.addActionListener(this);
        mRandomSeedTF.setText("1234");
        mRandomSeedTF.setColumns(10);
        mRandomSeedLabel.setText("Random Seed:");
        mClusterCountTF.setText("3");
        mClusterCountTF.setColumns(10);
        mClusterCountLabel.setText("Number of Clusters (K):");
        mCoordCountTF.setText(Integer.toString(result_fft.length));
        mCoordCountTF.setColumns(10);
        mCountLabel.setText("Number of Coordinates (N):");
        mThreadCountLabel.setEnabled(false);
        mThreadCountLabel.setText("Number of Threads:");
        mThreadCountTF.setEnabled(false);
        // Initialize the thread count textfield with the
        // number of available processors.
        mThreadCountTF.setText(String.valueOf(Runtime.getRuntime().availableProcessors()));
        mThreadCountTF.setColumns(10);
        mTopPanel.setLayout(gridBagLayout1);
        contentPane.add(mMessageAreaSP, java.awt.BorderLayout.CENTER);
        contentPane.add(mTopPanel, java.awt.BorderLayout.NORTH);
        mMessageAreaSP.getViewport().add(mMessageArea);
        mTopPanel.add(mCountLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(10, 10, 5, 0), 0, 0));
        mTopPanel.add(mCoordCountTF,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(10, 0, 5, 10), 0, 0));
        mTopPanel.add(mClusterCountLabel,
                      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.EAST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 10, 5, 0), 0, 0));
        mTopPanel.add(mClusterCountTF,
                      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.WEST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 0, 5, 10), 0, 0));
        mTopPanel.add(mRandomSeedLabel,
                      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.EAST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 10, 5, 0), 0, 0));
        mTopPanel.add(mRandomSeedTF,
                      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.WEST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 0, 5, 10), 0, 0));
        mTopPanel.add(mImplementationLabel,
                      new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.WEST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 10, 5, 0), 0, 0));
        mTopPanel.add(mImplementationCB,
                new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
                                       , GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(0, 2, 5, 10), 0, 0));
        mTopPanel.add(mThreadCountLabel,
                new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.EAST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 10, 5, 0), 0, 0));
        mTopPanel.add(mThreadCountTF,
                new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(0, 2, 5, 10), 0, 0));
        mTopPanel.add(mRunButton, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        
        mImplementationCB.addItem(BASIC_KMEANS);
        mImplementationCB.addItem(BENCHMARKED_KMEANS);
        mImplementationCB.addItem(CONCURRENT_KMEANS);
    }

    /**
     * Method for validating entries typed into text fields.
     */
    private static long getEnteredValue(JTextField tf, long min, long max) {
        long value = 0L;
        String s = tf.getText().trim();
        if (s.length() == 0) {
            throw new RuntimeException("blank entry");
        }
        try {
            value = Long.parseLong(s);
            if (value < min || value > max) {
                throw new RuntimeException("not in range [" + min + " - " + max + "]");
            }
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("invalid number");
        }
        return value;
    }
    
    /**
     * Generates the coordinates to be clustered.
     * 
     * @param coordCount the number of coordinates.
     * @param dimensions the length of the coordinates.
     * @param clusterCount the number of clusters in the distribution.
     * @param randomSeed the seed used by the random number generator.
     * @return
     */
    private static double[][] generateCoordinates(
            int coordCount, int dimensions, int clusterCount, long randomSeed)
    throws SME_InsufficientMemoryException {

        // Explicit garbage collection to reduce the likelihood of 
        // having insufficient memory.
        System.gc();
        
        long memRequired = 8L * (long) dimensions * (long) (coordCount + clusterCount);
        if (Runtime.getRuntime().freeMemory() < memRequired) {
            throw new SME_InsufficientMemoryException();
        }

        double[][] coordinates = new double[coordCount][dimensions];
        double[][] exemplars = new double[clusterCount][dimensions];

        Random random = new Random(randomSeed);
        for (int i=0; i<clusterCount; i++) {
            for (int j=0; j<dimensions; j++) {
                exemplars[i][j] = 100.0 * random.nextDouble();
            }
        }
 
        for (int i=0; i<coordCount; i++) {
            int cluster = random.nextInt(clusterCount);
            double[] exemplar = exemplars[cluster];
            double[] coord = coordinates[i];
            for (int j=0; j<dimensions; j++) {
                coord[j] = exemplar[j] + 50*random.nextGaussian();
            }
        }
        
        return coordinates;
        //return data2cluster;
    }

    /** Public method to show show help from ImageJ main help menu **/

    public void sme_gethelp(String helpIDcode){
        String helpMessage = "SME help:";

        if(helpIDcode=="about"){
            helpMessage = helpMessage.concat("About");
        }

        IJ.showMessage("SME Projection", helpMessage);
    }

    public synchronized void actionPerformed(ActionEvent e) {

        if (e.getSource() == mRunButton && !mRunning) {
            
            // Ensure entered parameters make sense.
            try {
                
                int coordCount = (int) getEnteredValue (mCoordCountTF, 
                        1L, (long) Integer.MAX_VALUE);
                int clusterCount = (int) getEnteredValue(mClusterCountTF,
                        1L, (long) (coordCount - 1));
                long randomSeed = getEnteredValue(mRandomSeedTF, 
                        Long.MIN_VALUE, Long.MAX_VALUE);

                //TODO: add number of dimensions for the test
                //double[][] coordinates = generateCoordinates(coordCount, 50, clusterCount, randomSeed);
                
                String implementation = (String) mImplementationCB.getSelectedItem();
                if (implementation == BASIC_KMEANS) {
                    mKMeans = new OBS_SME_BasicKMeansInterface(coordinates, clusterCount, 500, randomSeed);
                } else if (implementation == BENCHMARKED_KMEANS) {
                    mKMeans = new SME_BenchmarkedKMeansInterface(coordinates, clusterCount, 500, randomSeed);
                } else if (implementation == CONCURRENT_KMEANS) {
                    try {
                        int threadCount = (int) getEnteredValue(mThreadCountTF, 1L, 20L);
                        mKMeans = new SME_KMeans_Concurrent(coordinates, clusterCount, 500, randomSeed, threadCount);
                    } catch (RuntimeException rte2) {
                        JOptionPane.showMessageDialog(this, 
                                "The thread count entry is invalid (" + rte2.getMessage() +
                                ").\nPlease enter a thread count in the range [1 - 20].",
                                "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                if (mKMeans != null) {
                    // Force gc, so resources taken up by a previous run that haven't
                    // been freed yet, will not affect this test.
                    System.gc();

                    mMessageArea.setText("");
                    mKMeans.addKMeansListener(this);
                    mRunButton.setEnabled(false);
                    new Thread(mKMeans).start();
                    mRunning = true;
                    clustersKmeans = mKMeans.getClusters();
                }
                
            } catch (RuntimeException rte) {
                
                JOptionPane.showMessageDialog(this, 
                        "One or more entries are invalid (" + rte.getMessage() +
                        ").\nPlease enter positive numbers for the number of coordinates and clusters\n" + 
                        "The number of clusters must be less than the number of coordinates.",
                        "Invalid Entries", JOptionPane.ERROR_MESSAGE);
                
            }
            
        } else if (e.getSource() == mImplementationCB) {
            boolean b = mImplementationCB.getSelectedItem() == CONCURRENT_KMEANS;
            mThreadCountLabel.setEnabled(b);
            mThreadCountTF.setEnabled(b);
        }
    }

    /**
     * Displays an error dialog stating that insufficient memory is
     * available.
     */
    private void displayInsufficientMemoryDialog() {
        JOptionPane.showMessageDialog(this, 
                "Insufficient memory is available.  Try reducing the \n" + 
                "number of coordinates and/or the number of clusters.",
                "Insufficient Memory", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Cleanup after completion of k-means.
     */
    private void cleanupAfterKMeans() {
        if (mKMeans != null) {
            mKMeans.removeKMeansListener(this);
            mKMeans = null;
        }
        mRunning = false;
        mRunButton.setEnabled(true);
    }
    
    public void kmeansMessage(String message) {
        displayText(message);
    }
    
    public void kmeansComplete(SME_Cluster[] clusters, long executionTime) {
        displayText("K-Means complete: processing time (ms) = " + executionTime);
        displayText("Number of clusters: " + clusters.length);
        cleanupAfterKMeans();
    }
    
    public void kmeansError(Throwable err) {
        cleanupAfterKMeans();
        if (err instanceof SME_InsufficientMemoryException) {
            displayText("K-Means aborted because of insufficient memory.");
            displayInsufficientMemoryDialog();
        } else {
            Throwable t = (Throwable) err;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            displayText(sw.toString());
        }
    }
    
    private void displayText(String text) {
        mMessageArea.append(text);
        if (!text.endsWith("\n")) {
            mMessageArea.append("\n");
        }
    }

}
