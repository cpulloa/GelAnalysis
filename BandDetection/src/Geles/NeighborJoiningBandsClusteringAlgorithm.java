package Geles;

import java.util.ArrayList;
import java.util.List;

import ngsep.variants.Sample;

public class NeighborJoiningBandsClusteringAlgorithm implements BandsClusteringAlgorithm {

	@Override
	public int clusterBands(List<Band> bands) {
		double [][] distances = Band.calculateEuclideanDistances(bands);
		List<Sample> bandSamples = new ArrayList<Sample>();
        for(int i=0; i<bands.size(); i++){
        	Sample s = new Sample(Integer.toString(i));
        	bandSamples.add(s);
        }
        /*DistanceMatrix matrix=new DistanceMatrix(bandSamples, distances);
		NeighborJoining njDendogram = new NeighborJoining();
		njDendogram.loadMatrix(matrix);
		Dendrogram njTree = njDendogram.constructNJTree();
		njTree.printTree(System.out);
		*/
        return -1;
	}

}
