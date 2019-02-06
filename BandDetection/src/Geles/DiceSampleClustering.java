package Geles;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ngsep.clustering.*;

public class DiceSampleClustering implements SampleClusteringAlgorithm {

	private Dendrogram njTree;
	private int wellNum;
	private double [][] dice;

	@Override
	/**
	 * 
	 */
	public Dendrogram clusterSamples(List<Well> wells) {
			wellNum=wells.size();
			List<Band> bands= new ArrayList<>();
			for(Well w:wells){
				for(Band b:w.getBands()){
					bands.add(b);
				}
			}
	        int numBandAlleleClusters = 0;
	        for(Band band:bands){
	        	if(numBandAlleleClusters<band.getAlleleClusterId()+1){
	        		numBandAlleleClusters=band.getAlleleClusterId()+1;
	        	}
	        }
	        int [][] binaryMatrix = new int [numBandAlleleClusters][wells.size()];
			for(int i=0;i<binaryMatrix.length;i++) Arrays.fill(binaryMatrix[i], 0);
			
			for(Band band:bands) {
				binaryMatrix[band.getAlleleClusterId()][band.getWellID()]=1;
			}
			
	        //Print
	        System.out.println("BinaryMatrix: ");
	        for(int i=0;i<binaryMatrix.length;i++){
	        	for(int j=0; j<binaryMatrix[0].length;j++){
	        		System.out.print(binaryMatrix[i][j] +" ");
	        	}
	        	System.out.println();
	        }
	        
	        double [][] diceMatrix = calculateDiceDistance(binaryMatrix);
	        
	        List<String> ids = new ArrayList<String>();
	        for(Well well:wells){
	        	ids.add(""+well.getWellID());
	        }
	        DistanceMatrix matrix=new DistanceMatrix(ids, diceMatrix);
			NeighborJoining njDendogram = new NeighborJoining();
			njDendogram.loadMatrix(matrix);
			return njTree = njDendogram.constructNJTree();
		}

	/**
	 * 
	 * @param binaryMatrix
	 * @return
	 */
	private double[][] calculateDiceDistance(int[][] binaryMatrix) {
		dice = new double[wellNum][wellNum];
		
		for(int i=0; i<wellNum;i++){
			for(int j=0; j<wellNum;j++){
				double numerator=0;
				double denominator=0;
				for(int row=0; row<binaryMatrix.length; row++){
					if(binaryMatrix[row][i] == binaryMatrix[row][j] && binaryMatrix[row][i]== 1){
						//System.out.println("a: " + binaryMatrix[row][i] + "\t b: " + binaryMatrix[row][j] );
						numerator++;
					}
					if(binaryMatrix[row][i]==1){
						denominator++;
					}
					if(binaryMatrix[row][j]==1){
						denominator++;
					}
				}
				//System.out.println("num: "+ numerator + "\t den: " + denominator);
				double value = (2*numerator);
				if(denominator>0) value/=denominator;
				dice[i][j]=dice[j][i]=value;
			}
		}
		System.out.println("Dice matrix: ");
		
		for(int i=0; i<dice.length; i++){
			for(int j=0; j<dice[i].length; j++){
				System.out.print(dice[i][j]+" ");
			}
			System.out.println();
		}
			
		return dice;
	}
	
	public void saveDistanceMatrix(String outputFilePrefix) throws IOException{
		String outputFile = outputFilePrefix+"_dice.csv";
		try (PrintStream out = new PrintStream(outputFile)) {
			for(int j=0; j<wellNum; j++){
				for(int i=0; i<wellNum; i++){
					out.print(dice[i][j]+",");
				}
				out.println();
			}
		}
	}
	
	public double[][] getDistanceMatrix(){
		return dice;
	}
}
