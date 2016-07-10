package ij.plugin.filter.SME_PROJECTION_SRC;

import java.util.ArrayList;
import java.util.Random;

/** Class implementing the KMean algorithm as a plugin for ImageJ where the entry is a set of pixels defined by a set of features.
 * The class implements the functionalities to cluster the pixels around K centers and return pixel clusteur labels, cluster centers
 * and the cluster pixel vectors.
 * elton.rexhepaj@gmail.com
 */

public class SME_ENS_Kmeans_Engine {

    private double[][] data;         // data to cluster
    private int numClusters;    // number of clusters
    private double[][] clusterCenters;   // cluster centers
    private int dataSize;               // size of the data
    private int dataDim;                // dimention of the data
    private ArrayList[] clusters;     // calculated clusters
    private double[] clusterVars;        // cluster variances
    private double[] clusterLabels ;  // ER: Defining a variable to hold the labels of each entry point in the data
    private double epsilon;

    /** The constructor creating an object of KMeans.
     * deterministic manner by using a seed of 100
     * @param data  The input pixel features organised as 2D matrix where each row correspond to a pixels and each coloumn
     *             correspond to features describing the pixel properties.
     * @param numClusters	The number of clusteurs to pass to the KMeans algorithm.
     * @param randomizeCenters  Boolean indicating if TRUE whether cluster centers are initialised randomly everytime the
     *                          constructor is called. If FALSE Cluster centers are by default initialised randomly in a
     *                          deterministic manner by using a seed of 100.
     */
    public SME_ENS_Kmeans_Engine(double[][] data, int numClusters, boolean randomizeCenters)
    {
        this.dataSize = data.length;
        this.dataDim = data[0].length;

        // Initialise the class variables

        this.data = data;
        this.numClusters = numClusters;
        this.clusterCenters =  new double[numClusters][dataDim];
        this.clusterLabels  = new double[dataSize];

        clusters = new ArrayList[numClusters];
        for(int i=0;i<numClusters;i++)
        {
            clusters[i] = new ArrayList();
        }
        clusterVars = new double[numClusters];

        epsilon = 0.01;

        //TODO: Discuss choice of seed

        {
            randomizeCentersFixedSeed(numClusters, data,100);
        }
    }

    /** Method initalising the kmean centers randomly.
     * @param data  The input pixel features organised as 2D matrix where each row correspond to a pixels and each coloumn
     *             correspond to features describing the pixel properties.
     * @param numClusters	The number of clusteurs to pass to the KMeans algorithm.
     */
    private void randomizeCentersFixedSeed(int numClusters, double[][] data, int seedRandint) {
        Random r = new Random(seedRandint);
        r.setSeed(100);
        int[] check = new int[numClusters];
        for (int i = 0; i < numClusters; i++) {
            int rand = r.nextInt(dataSize);
            if (check[i] == 0) {
                this.clusterCenters[i] = data[rand].clone();
                check[i] = 1;
            } else {
                i--;
            }
        }
    }

    private void calculateClusterCenters()
    {
        for(int i=0;i<numClusters;i++)
        {
            int clustSize = clusters[i].size();

            for(int k= 0; k < dataDim; k++)
            {

                double sum = 0d;
                for(int j =0; j < clustSize; j ++)
                {
                    double[] elem = (double[]) clusters[i].get(j);
                    sum += elem[k];
                }

                clusterCenters[i][k] = sum / clustSize;
            }
        }
    }

    private void calculateClusterVars()
    {
        for(int i=0;i<numClusters;i++)
        {
            int clustSize = clusters[i].size();
            Double sum = 0d;

            for(int j =0; j < clustSize; j ++)
            {

                double[] elem = (double[])clusters[i].get(j);

                for(int k= 0; k < dataDim; k++)
                {
                    sum += Math.pow( elem[k] -
                            getClusterCenters()[i][k], 2);
                }
            }

            clusterVars[i] = sum / clustSize;
        }
    }

    public double getTotalVar()
    {
        double total = 0d;
        for(int i=0;i< numClusters;i++)
        {
            total += clusterVars[i];
        }

        return total;
    }

    public double[] getClusterVars()
    {
        return  clusterVars;
    }

    public ArrayList[] getClusters()
    {
        return clusters;
    }

    private void assignData()
    {
        for(int k=0;k<numClusters;k++)
        {
            clusters[k].clear();
        }

        for(int i=0; i<dataSize; i++)
        {

            int clust = 0;
            double dist = Double.MAX_VALUE;
            double newdist = 0;

            for(int j=0; j<numClusters; j++)
            {
                newdist = distToCenter( data[i], j );
                if( newdist <= dist )
                {
                    clust = j;
                    dist = newdist;
                }
            }

            clusters[clust].add(data[i]);
            clusterLabels[i] = clust;
        }

    }

    private double distToCenter( double[] datum, int j )
    {
        double sum = 0d;
        for(int i=0;i < dataDim; i++)
        {
            sum += Math.pow(( datum[i] - getClusterCenters()[j][i] ), 2);
        }

        return Math.sqrt(sum);
    }

    public void calculateClusters()
    {

        double var1 = Double.MAX_VALUE;
        double var2;
        double delta;


            calculateClusterCenters();
            assignData();
            calculateClusterVars();
            var2 = getTotalVar();
            if (Double.isNaN(var2))    // if this happens, there must be some empty clusters
            {
                delta = Double.MAX_VALUE;
                randomizeCentersFixedSeed(numClusters, data,100);
                assignData();
                calculateClusterCenters();
                calculateClusterVars();
            }
            else
            {
                delta = Math.abs(var1 - var2);
                var1 = var2;
            }


    }

    public void setEpsilon(double epsilon)
    {
        if(epsilon > 0)
        {
            this.epsilon = epsilon;
        }
    }

    /** Method to return the final clustering labels for each pixel.
     @return the cluster labels as vector of doubles
     */
    public double[] getClusterLabels()
    {
        return clusterLabels;
    }

    /**
     * @return the clusterCenters
     */
    public double[][] getClusterCenters() {
        return clusterCenters;
    }
}

