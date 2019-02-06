/**
 * 
 */
package Geles;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Stores image intensity and size information from a text file
 * @author Cindy Ulloa, H�ctor Ruiz, Jorge Duitama
 */
public class GelImage {
	private int totalRows;
	private int totalCols;
	private int totalWells;
	private String filename;
	
	/**
	 * Creates a new image with the given information
	 * @param filename Path to text file containing image information
	 * @throws IOException 
	 */
	public GelImage(String filename) throws IOException{
		this.filename =filename;
		processImageWell();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void processImageWell() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String linea1= in.readLine();
		String[] primera = linea1.split("\t");
		totalRows=Integer.parseInt(primera[0]);
		totalCols=Integer.parseInt(primera[1]);
		totalWells=Integer.parseInt(primera[2]);
		in.close();
	}
	
	/**
	 * @return int Number of rows of pixels in the image 
	 */
	public int getTotalRows(){
		return totalRows;
	}
	
	/**
	 * @return int Number of columns of pixels in the image
	 */
	public int getTotalCols(){
		return totalCols;
	}
	
	/**
	 * @return int Number of wells in the gel image
	 */
	public int getTotalWells(){
		return totalWells;
	}
	
}
