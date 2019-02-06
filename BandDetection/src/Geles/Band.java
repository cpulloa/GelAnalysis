package Geles;

import java.util.*;

/**
 * Stores information of detected bands
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 */
public class Band {
	private int bandID;
	private int startRow;
	private int endRow;
	private int startColumn;
	private int endColumn;
	private int wellID;
	private int[] centroid = new int[2];
	
	private int alleleClusterId=-1;
	
	/**
	 * Creates a new band object with the given information
	 * @param startRow The initial row where the band signal starts
	 * @param endRow The row where the band signal ends
	 * @param startColumn The initial column where the band signal starts
	 * @param endColumn The row where the band signal ends
	 */
	public Band(int startRow, int endRow, int startColumn, int endColumn, int id) {
		this.startRow=startRow;
		this.endRow=endRow;
		this.startColumn=startColumn;
		this.endColumn=endColumn;
		this.bandID=id;
	}
	
	/**
	 * @return wellID The identifier of the corresponding well
	 */
	public int getWellID(){
		return wellID;
	}
	
	
	/**
	 * @param wellID the wellID to set
	 */
	public void setWellID(int wellID) {
		this.wellID = wellID;
	}

	/**
	 * @return bandID The identifier of the band in the corresponding well
	 */
	public int getBandID(){
		return bandID;
	}
	/**
	 * @return the startRow
	 */
	public int getStartRow() {
		return startRow;
	}

	/**
	 * @return the endRow
	 */
	public int getEndRow() {
		return endRow;
	}

	/**
	 * @return the startColumn
	 */
	public int getStartColumn() {
		return startColumn;
	}

	/**
	 * @return the endColumn
	 */
	public int getEndColumn() {
		return endColumn;
	}

	/**
	 * @return bandHeight The height the given band occupies vertically on the well
	 */
	public int calculateBandHeight(){
		return endRow-startRow;
	}

	/**
	 * @return the alleleClusterId
	 */
	public int getAlleleClusterId() {
		return alleleClusterId;
	}

	/**
	 * @param alleleClusterId the alleleClusterId to set
	 */
	public void setAlleleClusterId(int alleleClusterId) {
		this.alleleClusterId = alleleClusterId;
	}
	
	public static double [][] calculateEuclideanDistances(List<Band> bands) {
		int totalBands = bands.size();
		double [][] euclideanDistances = new double[totalBands][totalBands];
        
        for(int i=0;i<totalBands;i++){
        	Band b1 = bands.get(i);
        	euclideanDistances[i][i] = 0;
        	for(int j=i+1; j<totalBands;j++){
        		Band b2 = bands.get(j);
        		double distance=1000;
        		if(b1.getWellID()!=b2.getWellID()) {
        			double squareDifferenceSum = Math.pow(b1.getStartRow()-b2.getStartRow(), 2);
            		squareDifferenceSum+=Math.pow(b1.getEndRow()-b2.getEndRow(), 2);
            		distance=Math.sqrt(squareDifferenceSum);
        		}
        		
        		//System.out.println("CalculatedDistance("+i+","+j+"): "+ distance);
    			euclideanDistances[i][j]=distance;
    			euclideanDistances[j][i]=distance;		
        	}
        }
        return euclideanDistances;
	}
	
	public void calculateCentroid(){
		// centroid: row, col
		centroid[0] = startRow + (endRow-startRow)/2;
		centroid[1] = startColumn + (endColumn-startColumn)/2;
	}
	
	public int[] getCentroid(){
		return centroid;
	}
}
