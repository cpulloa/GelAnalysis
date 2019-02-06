package Geles;

public class TestGel25 {

	public static void main(String[] args) throws Exception {
		IntensityProcessor instance = new IntensityProcessor();
		String inputFile = "./gel25.jpg";
		instance.loadImage(inputFile);
		instance.processImage();
	}

}
