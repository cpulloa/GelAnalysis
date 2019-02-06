package Geles;

import java.util.*;

// Band cluster used in k means clustering of wells

public class BandCluster{
	 private List<Band> bands = new ArrayList<Band>();
	 private int centroid;
	 private boolean done = false;
	 private double sumVariance;
	 
	 public BandCluster(int centroid){
		 this.centroid=centroid;
	 }

	 public int calculateCentroid(){
		 double sum=0;
		 for(Band b:bands){
			 int[] bandC = b.getCentroid();
			 sum += bandC[1];
		 }
		 int newCentroid = (int)Math.round(sum/bands.size());
		 return newCentroid;
	 }
	 
	 public int getCentroid(){
		return centroid;
	 }

	 public void setCentroid(int centroid){
		this.centroid = centroid;
	 }

	    public List<Band> getBands(){
		return bands;
	 }

	 public boolean isStable(){
		return done;
	 }

	 public void setEnded(boolean end) {
		this.done = end;
	 }

	 public void clearCluster() {
	  	bands.clear();
	 }
	 
	 public void calculateVariance(){
		 double sum =0;
		 for(Band b:bands){
			 double var = Math.pow((centroid-b.getCentroid()[1]),2);
			 sum += var;
		 }
		 
		 this.sumVariance=sum;
	 }
	 
	 public double getSumVariance(){
		 return sumVariance;
	 }
}
