package Geles;

import java.util.List;

import ngsep.graphs.CliquesFinder;

/**
 * Groups together band signals
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 */
public class HeuristicCliqueBandClusteringAlgorithm implements BandsClusteringAlgorithm {

	@Override
	public int clusterBands(List<Band> bands) {
		double [][] distances = Band.calculateEuclideanDistances(bands);
		boolean[][] consistencyMatrix=calculateConsistencyMatrix(distances);
        List<List<Integer>> cliques = CliquesFinder.findCliques(consistencyMatrix);
        int c=0;
        for(List<Integer> group:cliques) {
        	for(Integer band:group){
        		bands.get(band).setAlleleClusterId(c);
        	}
        	c++;
        }
        
        //Look for unclustered bands
        for(Band b:bands){
        	if(b.getAlleleClusterId()==-1){
        		b.setAlleleClusterId(c);
        		c++;
        	}
        }
        return c;
	}
	
	private boolean[][] calculateConsistencyMatrix(double [][] distances){
		int threshold = 10;
		boolean [][] consistencyMatrix = new boolean [distances.length][distances[0].length];
		
		for(int i=0; i<consistencyMatrix.length;i++){
			for(int j=0;j<consistencyMatrix[0].length;j++) {
				consistencyMatrix[i][j] = (distances[i][j]<threshold);
			}
		}
		return consistencyMatrix;
	}

	

}
