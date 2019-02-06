package Geles;

import ngsep.clustering.Dendrogram;
import ngsep.hmm.ConstantTransitionHMM;
import ngsep.hmm.HMM;
import ngsep.math.LogMath;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import JSci.maths.statistics.NormalDistribution;
import JSci.maths.statistics.ProbabilityDistribution;

/**
 * Processes gel image 
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 *
 */
public class IntensityProcessor {
	private List<Well> wells = new ArrayList<>();
	private List<Band> bands = new ArrayList<>();
	private int imageRows;
	private int imageColumns;
	private BufferedImage image;
	private double  [] [] intensitiesMatrix;
	private int [][] binarySignalMatrix;
	private int[] typicalBandDimensions;
	
	private SampleClusteringAlgorithm sampleClustering;

	private Dendrogram clusteringTree;
    private double Pstart_signal=0.05;
    private double Ptransition=0.05;
    
	public static void main(String[] args) throws Exception {
		IntensityProcessor instance = new IntensityProcessor();
		String inputFile = args[0];
		String outputFilePrefix = args[1];
		instance.loadImage(inputFile);
		instance.processImage();
		instance.saveResults(outputFilePrefix);
		
		//GelImage gelImage= new GelImage(outputFile);

	}
	
	public void loadImage(String inputFile) throws IOException {
		image = ImageIO.read( new File (inputFile) );

        imageRows = image.getHeight();
        imageColumns = image.getWidth();
        System.out.println("Rows: "+imageRows+" Cols: "+imageColumns);
        
        //Calculate scores:Grey scales parameteres
        intensitiesMatrix = new double [imageRows][imageColumns];
        for( int j = 0; j < imageColumns; j++ )
        {
        	for( int i = 0; i < imageRows; i++ )
        	{
        		Color c = new Color(image.getRGB( j, i ) );
        		intensitiesMatrix[i][j] = getGrayIntensity(c);
        	}
        	//System.out.println("Col score pos "+j+": "+colScores[j]);
        }
        wells = new ArrayList<>();
        bands = new ArrayList<>();
	}
	
	private double getGrayIntensity(Color c) {
		double sum = c.getRed()+c.getGreen()+c.getBlue();
		if(sum<0) System.out.println("RGB:"+c.getRed()+" "+c.getGreen()+" "+c.getBlue()+" sum: "+sum);
		return sum/3;
	}
	
	public void processImage() {
        // STEP 1: Build HMM to identify wells and bands
        HMM hmm = buildHMM();
        
        //STEP 2: Identify signal
    	predictSignalHMM(hmm);
    	
        //STEP 3: Identify bands
	        //predict typical band
    	this.typicalBandDimensions = estimateBandDim(binarySignalMatrix);
    		//Identify bands using sliding window
    	predictBandsSlidingWindow(typicalBandDimensions,0.90); //0.9 threshold
    	
    	//STEP 4: Identify wells AND cluster bands and samples
    	clusterBands();
//    	createWellsKmeans();
//    	
//    	System.out.println("wells#: " + wells.size());
//        
//        //System.out.println("Total number of bands:\t"+bands.size());
//        //STEP 5: Cluster bands looking for variant alleles
//        BandsClusteringAlgorithm bandsClustering = new HeuristicCliqueBandClusteringAlgorithm();
//        int numClusters = bandsClustering.clusterBands(bands);
//        System.out.println("Total cliques: "+numClusters);
//        
//        //STEP 6: Cluster samples (Wells)
//        sampleClustering = new DiceSampleClustering();
//        clusteringTree=sampleClustering.clusterSamples(wells);
        
        
	}

	private HMM buildHMM() {
		//ProbabilityDistribution distBwd= new NormalDistribution(0,1600);
        //ProbabilityDistribution distSignal= new NormalDistribution(254,1600);
        DynamicDistribution intensityDistribution = new DynamicDistribution(intensitiesMatrix);
        double bwdMean = intensityDistribution.getBwdMean();
        double bwdVar = intensityDistribution.getBwdVar();
        double signalMean = intensityDistribution.getSignalMean();
        double signalVar = intensityDistribution.getsignalVar();
        
        //////////
        ProbabilityDistribution distBwd= new NormalDistribution(bwdMean,bwdVar);
        ProbabilityDistribution distSignal= new NormalDistribution(signalMean,signalVar);
        
        List<DistributionState> states = new ArrayList<>();
        DistributionState background = new DistributionState("background",1-Pstart_signal,distBwd);
        DistributionState signal = new DistributionState("signal",Pstart_signal,distSignal);
    	
    	states.add(background);
    	states.add(signal);
    	
    	ConstantTransitionHMM hmm = new ConstantTransitionHMM(states);
    	Double[][] tMatrix= new Double[2][2];
    	Double tChange=LogMath.log10(Ptransition);
    	Double tStay=LogMath.log10(1-Ptransition);
    	tMatrix[0][0]=tMatrix[1][1]=tStay;
    	tMatrix[0][1]=tMatrix[1][0]=tChange;
    	
    	hmm.setTransitions(tMatrix);
    	return hmm;
	}	

	public void predictWellsHMM(HMM hmm) {
		List<Double> columnScores = calculateColumnScores(intensitiesMatrix);
    	System.out.println("Scores: "+columnScores);
		double [][] posteriors = new double [columnScores.size()][2];
		hmm.calculatePosteriors(columnScores, posteriors);
		
    	int nextStart = -1;
    	for(int i=0;i<columnScores.size();i++){
    		if(posteriors[i][1]>0.99){
    			if(nextStart==-1) nextStart = i; 
    		} else if (nextStart>=0) {
    			createWell(nextStart,i-1);
    			nextStart = -1;
    		}
    	}
    	if(nextStart>=0) createWell(nextStart, columnScores.size()-1);
	}
	
	public void predictSignalHMM(HMM hmm) {
		List<Double> observations = new ArrayList<>();
		binarySignalMatrix= new int[imageRows][imageColumns];
		for(int i=0; i<imageRows; i++){
			for(int j=0; j<imageColumns; j++){
				observations.add(intensitiesMatrix[i][j]);
			}
			double [][] posteriors = new double [imageColumns][2];
			hmm.calculatePosteriors(observations, posteriors);
			
			
	    	for(int j=0;j<imageColumns;j++){
	    		//System.out.println(posteriors[j][0] +"\t" + posteriors[j][1]);
	    		if(posteriors[j][1]>0.99){
	    			binarySignalMatrix[i][j]=1; 
	    		} else {
	    			binarySignalMatrix[i][j]=0; 
	    		}
	    	}
	    	observations.clear();  	
		}
		saveBinSignal(binarySignalMatrix);	
	}
	
	private List<Double> calculateColumnScores(double[][] intensitiesMatrix) {
		// Calculates average of pixel and same-column neighbors
		double [][] avgNeighbors = new double[intensitiesMatrix.length][intensitiesMatrix[1].length];
		avgNeighbors=intensitiesMatrix;
		for(int i=1;i<intensitiesMatrix.length-1;i++){
			for(int j=1; j<intensitiesMatrix[1].length-1;j++){
				double avg=(intensitiesMatrix[i-1][j]+intensitiesMatrix[i+1][j]+intensitiesMatrix[i][j])/3;
				avgNeighbors[i][j]=avg;
			}
		}
		
		//Calculates the maximum average value in each column
		List<Double> columnScores=new ArrayList<>();
		for(int k=0;k<avgNeighbors[1].length;k++){
			double maxIntensity=0;
			for(int i=0;i<avgNeighbors.length;i++){
				if(avgNeighbors[i][k]>maxIntensity){
					maxIntensity=avgNeighbors[i][k];
				}	
			}
			columnScores.add((Double)maxIntensity);
		}
		return columnScores;
	}
	
	private int[] estimateBandDim(int[][] binarySignalMatrix){
		//Mean
		int[][] matrix = binarySignalMatrix;
		ArrayList<Integer> rowBandWidths = new ArrayList<>();
		ArrayList<Integer> colBandHeights=new ArrayList<>();		
		for(int i=0; i<imageRows; i++){
			int rowCounter=1; //Count number of adjacent signal pixels in each row
			
			for(int j=0; j<imageColumns-1; j++){
				
					if(matrix[i][j]==1 && matrix[i][j] == matrix[i][j+1]){
						rowCounter++;
					}
					if(matrix[i][j]==1 && matrix[i][j] != matrix[i][j+1]){
						if(rowCounter > 10){
							rowBandWidths.add(rowCounter);
						}
						rowCounter=1;
					}	
			}		
		}
		for(int j=0; j<imageColumns; j++){
			int colCounter=1; //Count number of adjacent signal pixels in each row
			
			for(int i=0; i<imageRows-1; i++){
				
					if(matrix[i][j]==1 && matrix[i][j] == matrix[i+1][j]){
						colCounter++;
					}
					if(matrix[i][j]==1 && matrix[i][j] != matrix[i+1][j]){
						if(colCounter > 1){
							colBandHeights.add(colCounter);
						}
						colCounter=1;
					}	
			}		
		}
		int[] meanBand = new int[2];
		int sumW=0;
		for(int w:rowBandWidths){
			sumW=sumW+w;
			
//			System.out.println("bandW:" + w);
		}
		int sumH=0;
		for(int h:colBandHeights){
			sumH=sumH+h;
	
//			System.out.println("bandH:" + h);
		}
		meanBand[1]=sumW/rowBandWidths.size();
		meanBand[0]=sumH/colBandHeights.size();
		
		return meanBand;
}

	private void createWell(int colFirst, int colLast) {
        Well well = new Well(0,colFirst,imageRows,colLast-colFirst+1,wells.size());
        wells.add(well);
	}

	public void predictWellsAlgorithm1() {
		List<Double> colScores = calculateColumnScores(intensitiesMatrix);
		double avg = 0;
		for(int i=0;i<colScores.size();i++) avg+=colScores.get(i);
		avg/=colScores.size();
		Integer nextS = null;
		for(int i=0;i<colScores.size();i++) {
			if(colScores.get(i)>avg) {
				if(nextS==null) nextS = i;
			} else if (nextS!=null) {
				if(i-nextS>10) {
					createWell(nextS, i-1);
				}
				nextS = null;
			}
		}
		if(nextS !=null) createWell(nextS, colScores.size()-1);
	}
	
	public void predictBandsHMM(Well well, HMM hmm, int cumulativeBands) {
		List<Double> rowScores = calculateRowScores(well);
		//for(int i=0; i<rowScores.size(); i++){
			//System.out.println("wellID: " + wellID + "row "+i+" : \t"+rowScores.get(i));
		//}
		double [][] posteriors = new double [rowScores.size()][2];
		hmm.calculatePosteriors(rowScores, posteriors);

    	int nextRowStart = -1;
    	int wellRowStart = well.getStartRow();
    	int k=cumulativeBands;
    	for(int n=0;n<rowScores.size();n++){
    		if(posteriors[n][1]>0.99){
    			if(nextRowStart==-1) nextRowStart=wellRowStart+n;
    		} else if (nextRowStart>=0) {
    			createBand(well, nextRowStart,wellRowStart+n-1,k);
    			k++;
    			nextRowStart = -1;
    		}
    	}
    	if (nextRowStart>=0) {
			createBand(well, nextRowStart,wellRowStart+rowScores.size()-1,k);
    	}
	}
	/**
	 * Creates bands according to hmm binary signal observations 
	 * @param typicalBand int array contain int [0]:windowHeight; [1]: windowWidth
	 * @param threshold double minimum fraction of signal in sliding window to create a band
	 */
	private void predictBandsSlidingWindow(int typicalBand [], double threshold){
		int windowH=typicalBand[0];
		int windowW=typicalBand[1];
		if(windowW<20){ //minimum band width
			windowW=20;
		}
		System.out.println("meanBandWidth: " + windowW + "\t meanBandHeight: " + windowH );
		int totPixels=windowH*windowW;
		int[][] observations=binarySignalMatrix;
		double ones = 0;
		double twos = 0;
		int count = 0;
		
		for(int i=0; i<imageRows-windowH; i++){ //start loop along all signal matrix
			for(int j=0; j<imageColumns-windowW; j++){
				
				if(observations[i][j] == 1){ // check if pixel is signal
					for(int y=0; y<windowH; y++){ //opens window
						for(int x=0; x<windowW;x++){
							if(observations[i+y][j+x] == 1){
								ones++;
							}
							else if(observations[i+y][j+x] == 2){
								twos++;
							}
						}
					}
				}
				
				//check if window has signal over threshold and no visited pixels
				if(twos == 0){
					if(ones/totPixels >= threshold){
						count++;
						Band band = new Band(i, i+windowH-1, j, j+windowW-1, count);
						bands.add(band);
						for(int y=0; y<windowH; y++){ //update observation matrix
							for(int x=0; x<windowW; x++){
								if(observations[i+y][j+x] == 1){
									observations[i+y][j+x] = 2;
								}
							}
						}
					}
				}
				ones=0;
				twos=0;
				
			}
		}
		
		System.out.println("Total bands identified: " + bands.size());
		
	}
	
	public void createWellsKmeans(){
		int bWidth = typicalBandDimensions[1];
		//int lowKvalue = (int)Math.round(imageColumns/(2*bWidth));
		int lowKvalue = 1;
		System.out.println("lowK: " + lowKvalue);
		int highKvalue = (int)Math.round(imageColumns/bWidth);
		System.out.println("highK: " + highKvalue);
		
		double[] var = new double[highKvalue-lowKvalue];

		for(int i=lowKvalue; i<highKvalue; i++){
			kMeansWellPrediction kmeansK = new kMeansWellPrediction(imageRows, imageColumns, bands, i);
			kmeansK.clusterWells();
			var[i-lowKvalue]=kmeansK.getVariance();

			System.out.println("K: " + i + "\t SE: " + var[i-lowKvalue]);
		}
		double maxdSE=0;
		int selectedK=0;
		double[] deltaVar = new double[var.length];
		deltaVar[0]=0;
		for(int i=1; i<var.length; i++){
			deltaVar[i]=var[i-1]-var[i];
		}
		for(int i=deltaVar.length-1; i>0;i--){
			double dse = deltaVar[i];
			if(dse>=maxdSE){
				maxdSE=dse;
				selectedK=i+lowKvalue;
			}
			if(dse>2*var[var.length-1]){
				break;
			}
		}
		System.out.println("\t seletedK = " + selectedK);
		//int k = 9;
		kMeansWellPrediction km = new kMeansWellPrediction(imageRows, imageColumns, bands, selectedK);
		km.clusterWells();
		
		List<BandCluster> clusters = km.getClusters();
		int co=0;
		for(BandCluster cluster:clusters){
			co++;
			System.out.println("Cluster#: " + co);
			int colFirst=imageColumns;
			int colLast=0;
			for(Band b:cluster.getBands()){
				int firstC=b.getStartColumn();
				int lastC=b.getEndColumn();
				if(firstC<colFirst){
					colFirst=firstC;
				}
				if(lastC>colLast){
					colLast=lastC;
				}
			}
			Well well = new Well(0,colFirst,imageRows,colLast-colFirst+1,wells.size());
			int wellID = well.getWellID();
			for(Band b:cluster.getBands()){
				System.out.println("band#: " + b.getBandID());
				b.setWellID(wellID);
				well.addBand(b);
			}
	        wells.add(well);
		}
		
	}
	
	private void createBand(Well well, int firstRow, int lastRow, int id) {
		Band band = new Band(firstRow,lastRow,well.getStartCol(),well.getStartCol()+well.getWellWidth(), id);
		band.setWellID(well.getWellID());
		well.addBand(band);
		bands.add(band);
	}
	

	private List<Double> calculateRowScores(Well well){
		//Calculates the maximum average value in each row of the well
		List<Double> rowScores=new ArrayList<>();
		int endRow = well.getStartRow()+well.getWellHeight();
		int endCol = well.getStartCol()+well.getWellWidth();
		for(int i=well.getStartRow();i<endRow;i++){
			List<Double> rowIntensities = new ArrayList<>();
			for(int j=well.getStartCol();j<endCol;j++){
				rowIntensities.add(intensitiesMatrix[i][j]);
			}
			Collections.sort(rowIntensities);
			rowScores.add(rowIntensities.get(well.getWellWidth()/2));
		}
		return rowScores;	
	}
	
	public void saveResults(String outputFilePrefix) throws IOException {
		String outputFile = outputFilePrefix+"_tree.nwk";
		try (PrintStream out = new PrintStream(outputFile)) {
			clusteringTree.printTree(out);
		}
		
		sampleClustering.saveDistanceMatrix(outputFilePrefix);
	}
	public void saveBinSignal(int[][] binarySignalMatrix){
		String outputFile = "BinarySingal.csv";
		try (PrintStream out = new PrintStream(outputFile)) {
			for(int i=0; i<binarySignalMatrix.length; i++){
				for(int j=0; j<binarySignalMatrix[0].length; j++){
					out.print(binarySignalMatrix[i][j]+",");
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	public double [][] getIntensities() {
		return intensitiesMatrix;
	}
	public List<Well> getWells() {
		return wells;
	}
	public List<Band> getBands(){
		return bands;
	}

	public void deleteBand(Band selected) {
		bands.remove(selected);
		int wellId = selected.getWellID();
		for(Well well:wells) {
			if(well.getWellID()==wellId) {
				well.removeBand(selected);
			}
		}
	}
	public void addBand(Band bandCoordinates) {
		Band band = new Band(bandCoordinates.getStartRow(), bandCoordinates.getEndRow(), bandCoordinates.getStartColumn(), bandCoordinates.getEndColumn(), bands.size());
		bands.add(band);
		
	}
	public void clusterBands() {
		wells = new ArrayList<>();
		//STEP 4: Identify wells
    	createWellsKmeans();
    	
    	System.out.println("wells#: " + wells.size());
        
        //System.out.println("Total number of bands:\t"+bands.size());
        //STEP 5: Cluster bands looking for variant alleles
    	for (Band b:bands) b.setAlleleClusterId(-1);
        BandsClusteringAlgorithm bandsClustering = new HeuristicCliqueBandClusteringAlgorithm();
        int numClusters = bandsClustering.clusterBands(bands);
        System.out.println("Total cliques: "+numClusters);
        
        //STEP 6: Cluster samples (Wells)
        sampleClustering = new DiceSampleClustering();
        clusteringTree=sampleClustering.clusterSamples(wells);
	}
	
}