package Geles;

import java.io.IOException;
import java.util.List;

import ngsep.clustering.Dendrogram;
/**
 * Sample Clustering Interface
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 *
 */
public interface SampleClusteringAlgorithm {
	
	/**
	 * Method for clustering the samples (cluster each well)
	 * @param wells The list of wells identified in the image
	 * @return Dendogram A dendogram of the clustering
	 */
	public Dendrogram clusterSamples(List<Well> wells);

	public void saveDistanceMatrix(String outputFilePrefix) throws IOException;
}
