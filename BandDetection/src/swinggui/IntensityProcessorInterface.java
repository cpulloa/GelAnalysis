package swinggui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import Geles.IntensityProcessor;
import Geles.Well;
import Geles.Band;

public class IntensityProcessorInterface extends JFrame {

	private ImagePanel imagePanel;
	private ButtonsPanel buttonsPanel;
	private IntensityProcessor processor;
	public IntensityProcessorInterface () {
		processor = new IntensityProcessor();
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		imagePanel = new ImagePanel(this);
		add (imagePanel, BorderLayout.CENTER);
		buttonsPanel = new ButtonsPanel(this);
		add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	public void load() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("./"));
		int answer = jfc.showOpenDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			processor.loadImage(jfc.getSelectedFile().getAbsolutePath());
			imagePanel.loadImage(processor.getImage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calculate() {
		processor.processImage();
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		List<Well> wells = processor.getWells();
		imagePanel.paintWells(wells);
		
	}
	public void save() {
		JFileChooser jfc = new JFileChooser();
		int answer = jfc.showSaveDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			processor.saveResults(jfc.getSelectedFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		IntensityProcessorInterface frame = new IntensityProcessorInterface();
		frame.setVisible(true);

	}

	public void deleteSelectedBand() {
		Band selected = imagePanel.getSelectedBand();
		if(selected == null) return;
		processor.deleteBand(selected);
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		
	}

	public void addBand() {
		// TODO Auto-generated method stub
		Band toCreate = imagePanel.getBandToCreate();
		if(toCreate == null) return;
		processor.addBand(toCreate);
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
	}

	public void clusterBands() {
		processor.clusterBands();
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		List<Well> wells = processor.getWells();
		imagePanel.paintWells(wells);
		
	}

	

}
