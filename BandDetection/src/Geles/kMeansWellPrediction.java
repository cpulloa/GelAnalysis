package Geles;

import java.util.*;

public class kMeansWellPrediction {

	private int imageRows;
	private int imageColumns;
	private List<Band> bands;
	private int k;
	private double sumVariance;
	List<BandCluster> clusters= new ArrayList<BandCluster>();
	
	/**
	 * Contructor
	 * @param imageRows
	 * @param imageColumns
	 * @param bands
	 */
	public kMeansWellPrediction(int imageRows, int imageColumns, List<Band> bands, int k){
		this.imageRows=imageRows;
		this.imageColumns=imageColumns;
		this.bands=bands;
		this.k=k;
	}
	
	public void clusterWells(){
		// random k centers prototype clusters
		for(int i=0; i<k; i++){
			int c = (int)Math.round( (i+0.5) * imageColumns/k);
			BandCluster bandCluster = new BandCluster(c);
			clusters.add(bandCluster);
		}

		//iterate clustering
		int counter=0;
		while(!done(clusters)){
			clearClusters(clusters);
			assignBands(clusters);
			calculateCentroids(clusters);
			counter++;
		}
		System.out.println("\t No. Iterations: " + counter);
		
		this.sumVariance=calculateVariance(clusters);
		 
	}

	private double calculateVariance(List<BandCluster> clusters) {
		double sum=0;
		for(BandCluster cluster:clusters){
			cluster.calculateVariance();
			sum += cluster.getSumVariance();
		}
		return sum;
	}

	private void calculateCentroids(List<BandCluster> clusters) {
		for(BandCluster cluster:clusters){
			if(cluster.getBands().isEmpty()){
				cluster.setEnded(true);
			}
			int oldCentroid = cluster.getCentroid();
			int newCentroid = cluster.calculateCentroid();

			cluster.setCentroid(newCentroid);
			
			if(oldCentroid == newCentroid){
				cluster.setEnded(true);
			}
			else{
				cluster.setEnded(false);
			}
		}
		
	}

	private void assignBands(List<BandCluster> clusters) {
		
		//calculate distance to each center and assign to closest cluster

				for(Band band:bands){
					double distance;
					double dist = 100000;
					int closest=0;
					band.calculateCentroid();
					int[] c = band.getCentroid();
					for(int i=0; i<k; i++){
						distance = Math.sqrt(Math.pow((c[1]-clusters.get(i).getCentroid()), 2));
						if(distance<dist){
							closest=i;
							dist=distance;
						}		
					}
					clusters.get(closest).getBands().add(band);

				}
	}

	private void clearClusters(List<BandCluster> clusters) {
		for(BandCluster cluster: clusters){
			cluster.clearCluster();
		}
		
	}

	private boolean done(List<BandCluster> clusters) {
		for(BandCluster cluster:clusters){
			if(cluster.isStable() == false){
				return false;
			}
		}
		return true;
	}
	
	public List<BandCluster> getClusters(){
		return clusters;
	}
	
	public double getVariance(){
		return sumVariance;
	}
	
}
