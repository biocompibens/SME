package SME_PROJECTION_SRC;

/**
 * K-Means clustering interface.
 */
public interface SME_KMeans_Paralel extends Runnable {
    
    /** 
     * Adds a KMeansListener to be notified of significant happenings.
     * 
     * @param l  the listener to be added.
     */
    public void addKMeansListener(SME_KMeansListener l);

    /**
     * Removes a KMeansListener from the listener list.
     *
     * @param l the listener to be removed.
     */
    public void removeKMeansListener(SME_KMeansListener l);
    
    /**
     * Get the clusters computed by the algorithm.  This method should
     * not be called until clustering has completed successfully.
     * 
     * @return an array of Cluster objects.
     */
    public SME_Cluster[] getClusters();
 
}
