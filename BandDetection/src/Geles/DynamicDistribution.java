package Geles;

import java.util.*;

import JSci.maths.statistics.NormalDistribution;
import JSci.maths.statistics.ProbabilityDistribution;
import ngsep.math.Distribution;

public class DynamicDistribution {

	private double[][] intensityMatrix;
	private double defaultMeanBwd=0;
	private double defaultVarBwd=100;
	private double defaultMeanSig=255;
	private double defaultVarSig=25;
	
	//private double defaultPriorProb=0.5;
	private double defPriorBwd=0.7;
	private double tolerance = 0.03;
	private double maxIter=100;
	private double bwdMean;
	private double bwdVar;
	private double signalMean;
	private double signalVar;
	private ProbabilityDistribution signalDistribution;
	private ProbabilityDistribution bwdDistribution;
	
	
	public DynamicDistribution(double [][] intensityMatrix){
		this.intensityMatrix =intensityMatrix;
		
		//First iteration
		//double[][] likelihoodBwd = new double[intensityMatrix.length][intensityMatrix[0].length];
		//double[][] likelihoodSignal = new double[intensityMatrix.length][intensityMatrix[0].length];
		//likelihoodBwd=calculateLikelihood(defaultMeanBwd, defaultVarBwd);
		//likelihoodSignal=calculateLikelihood(defaultMeanSig, defaultVarSig);
		
		double[][] weightBwd = new double[intensityMatrix.length][intensityMatrix[0].length];
		double[][] weightSignal = new double[intensityMatrix.length][intensityMatrix[0].length];
		//weightBwd=calculateEMweights(defaultPriorProb, defaultPriorProb, likelihoodBwd, likelihoodSignal);
		//weightSignal=getComplementaryWeight(weightBwd);
		
		List<double[][]> weights = calculateWeights(defaultMeanBwd,defaultMeanSig,defaultVarBwd,defaultVarSig, defPriorBwd, (1-defPriorBwd));
		
		weightBwd= weights.get(0);
		weightSignal=weights.get(1);
		bwdMean=calculateMean(weightBwd);
		bwdVar=calculateVar(weightBwd, bwdMean);
		signalMean=calculateMean(weightSignal);
		signalVar=calculateVar(weightSignal, signalMean);
		double priorBwd = calculatePrior(weightBwd);
		double priorSignal = calculatePrior(weightSignal);
		
		//Next iterations
		double preBwdMean=defaultMeanBwd;
		double preSignalMean=defaultMeanSig;
		double bwdDifference = Math.abs(bwdMean-preBwdMean);
		double signalDifference = Math.abs(signalMean-preSignalMean);
		preBwdMean=bwdMean;
		preSignalMean=signalMean;
		int iter=1;
		//|| (signalDifference > tolerance && iter<maxIter)
		while(bwdDifference > tolerance && iter<maxIter){
			//likelihoodBwd=calculateLikelihood(bwdMean, bwdVar);
			//likelihoodSignal=calculateLikelihood(signalMean, signalVar);
			
			//weightBwd=calculateEMweights(priorBwd, priorSignal, likelihoodBwd, likelihoodSignal);
			//weightSignal=getComplementaryWeight(weightBwd);
			
			weights = calculateWeights(bwdMean,signalMean,bwdVar, signalVar, priorBwd, priorSignal);
			weightBwd=weights.get(0);
			weightSignal=weights.get(1);
			
			bwdMean=calculateMean(weightBwd);
			bwdVar=calculateVar(weightBwd, bwdMean);
			signalMean=calculateMean(weightSignal);
			signalVar=calculateVar(weightSignal, signalMean);
			
			priorBwd = calculatePrior(weightBwd);
			priorSignal = calculatePrior(weightSignal);
			
			bwdDifference= Math.abs(bwdMean-preBwdMean);
			signalDifference= Math.abs(signalMean-preSignalMean);
			
			preBwdMean=bwdMean;
			preSignalMean=signalMean;
			iter++;
			
			System.out.println("bwdDIff: "+bwdDifference+"\t iter: "+iter+ "\t bwdMean: "+bwdMean+"  bwdVar: "+bwdVar +"\t signalMean: "+signalMean+  "    signalVar: "+signalVar);
		}
		if(bwdMean > signalMean-20) System.err.println("WARN: Background average "+bwdMean+" is larger than signal average: "+signalMean+". Try enhancing the contrast");
		
		if(bwdVar<1000){
			bwdVar=1000;
		}
		if(signalVar<1000){
			signalVar=1000;
		}
		System.out.println("bwdMean: "+bwdMean+"  bwdVar: "+bwdVar +"\t signalMean: "+signalMean+  "    signalVar: "+signalVar);
	}
	
	/**
	 * Probability density function of the intensity in the image
	 * given it is signal or background P(Xi | b)
	 * @param double var the variance of the distribution
	 * @param double mean the mean of the distribution
	 * @param double[][] intensityMatrix the intensity values of the image
	 * @return double [][] answer the probability value
	 */
	public List<double[][]> calculateWeights(double meanA, double meanB, double varA, double varB, double priorA, double priorB){
		List<double[][]> weights = new ArrayList<double[][]>();
		double [][] likelihoodA= new double[intensityMatrix.length][intensityMatrix[0].length];
		double [][] likelihoodB= new double[intensityMatrix.length][intensityMatrix[0].length];
		double [][] weightA= new double[intensityMatrix.length][intensityMatrix[0].length];
		double [][] weightB= new double[intensityMatrix.length][intensityMatrix[0].length];
		
		for(int i=0; i<intensityMatrix.length; i++){
			for(int j=0; j<intensityMatrix[0].length; j++){
				//calculate Likelihood
				likelihoodA[i][j]=(1.0/Math.sqrt(2*Math.PI*varA))*Math.exp(Math.pow((intensityMatrix[i][j] - meanA),2)/(-2*varA));
				likelihoodB[i][j]=(1.0/Math.sqrt(2*Math.PI*varB))*Math.exp(Math.pow((intensityMatrix[i][j] - meanB),2)/(-2*varB));
				
				//calculate Weights
				weightB[i][j]= (likelihoodB[i][j]*priorB)/((likelihoodB[i][j]*priorB)+(likelihoodA[i][j]*priorA));
				weightA[i][j]=1-weightB[i][j];
				
			}
		}
		weights.add(weightA);
		weights.add(weightB);
		
		return weights;
	}
	
	/**
	 * Calculate weights for EM
	 * @param double priorA
	 * @param dobule priorB
	 * @param double[][] probXgivenA
	 * @param double[][] probXgivenB
	 * @return double[][] b the weights for expectation maximization algorith
	 */
	public double[][] calculateEMweights(double priorA, double priorB, double[][] probXgivenA, double[][] probXgivenB){
		double [][] answer= new double[probXgivenA.length][probXgivenA[0].length];
		for(int i=0; i<probXgivenA.length; i++){
			for(int j=0; j<probXgivenA[0].length; j++){
				answer[i][j]= (probXgivenB[i][j]*priorB)/((probXgivenB[i][j]*priorB)+(probXgivenA[i][j]*priorA));
			}
		}
		return answer;
	}
	
	/**
	 * complementaryWeight
	 */
	public double[][] getComplementaryWeight(double[][] weightMatrix){
		double [][] complementary = new double[intensityMatrix.length][intensityMatrix[0].length];
		for(int i=0; i<intensityMatrix.length; i++){
			for(int j=0; j<intensityMatrix[0].length; j++){
				complementary[i][j]=1-weightMatrix[i][j];
			}
		}
		return complementary;
	}
	
	/**
	 * Calculate the mean
	 */
	public double calculateMean(double[][] weights){
		double productSumNumerator =0;
		double sumDenominator=0;
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				productSumNumerator = productSumNumerator + (weights[i][j]*intensityMatrix[i][j]);
				sumDenominator = sumDenominator + weights[i][j];
			}
		}
		
		double answer = productSumNumerator/sumDenominator;
		
		return answer;
	}
	
	/**
	 * Calculate the variance
	 */
	public double calculateVar(double[][] weights, double mean){
		double productSumNumerator =0;
		double sumDenominator=0;
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				productSumNumerator = productSumNumerator + (weights[i][j]*Math.pow((intensityMatrix[i][j]-mean), 2));
				sumDenominator = sumDenominator + weights[i][j];
			}
		}
		
		double answer = productSumNumerator/sumDenominator;
		
		return answer;
	}
	
	/**
	 * Calculate priors
	 */
	public double calculatePrior(double[][] weights){
		double numerator = 0;
		double denominator=0;
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[i].length; j++){
				numerator=numerator+weights[i][j];
				denominator++;
			}
		}
		return numerator/denominator;
	}
	
	/**
	 * @return bwdMean The mean of the background intensity value distribution
	 */
	public double getBwdMean(){
		return bwdMean;
	}
	
	/**
	 * @return signalMean The mean of the signal intensity value distribution
	 */
	public double getSignalMean(){
		return signalMean;
	}
	
	/**
	 * @return bwdVar The variance of the background intensity value distribution
	 */
	public double getBwdVar(){
		return bwdVar;
	}
	
	/**
	 * @return signalVar The variance of the background intensity value distribution
	 */
	public double getsignalVar(){
		return signalVar;
	}
}
