package swinggui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Geles.Band;
import Geles.Well;

public class ImagePanel extends JPanel implements MouseListener {
	private IntensityProcessorInterface parent;
	private BufferedImage image;
	private List<Well> wells =new ArrayList<>();
	private List<Band> bands = new ArrayList<>();
	private List<Color> bandColors = new ArrayList<>();
	private Band selectedBand = null;
	
	private int rowNewBand = 0;
	private int columnNewBand = 0;
	private Band bandToCreate = null;
	
	
	public ImagePanel (IntensityProcessorInterface parent) {
		this.parent = parent;
		bandColors.add(Color.RED);
		bandColors.add(Color.CYAN);
		bandColors.add(Color.YELLOW);
		bandColors.add(Color.GREEN);
		bandColors.add(Color.MAGENTA);
		bandColors.add(Color.ORANGE);
		bandColors.add(Color.PINK);
		bandColors.add(new Color(255, 100, 100));
		bandColors.add(new Color(100, 255, 100));
		bandColors.add(new Color(100, 100, 255));
		addMouseListener(this);
	}

	public void loadImage(BufferedImage image) {
		this.image = image;
		bands =new ArrayList<>();
		wells =new ArrayList<>();
		repaint();
		
	}

	public void paintWells(List<Well> wells) {
		this.wells = wells;
		repaint();
	}
	public void paintBands(List<Band> bands){
		this.bands = bands;
		selectedBand = null;
		bandToCreate = null;
		repaint();
	}

	/**
	 * @return the selectedBand
	 */
	public Band getSelectedBand() {
		return selectedBand;
	}

	/**
	 * @return the bandToCreate
	 */
	public Band getBandToCreate() {
		return bandToCreate;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D) g;
		if(image!=null) {
			g2D.drawImage(image, 0, 0, null);
		}
		
		g2D.setStroke(new BasicStroke(2));
		for(Band band: bands) {
			int alleleCluster = band.getAlleleClusterId();
			if(alleleCluster>=0) {
				g2D.setColor(bandColors.get(alleleCluster%10));
				g2D.drawString(""+alleleCluster, band.getCentroid()[1], band.getCentroid()[0]);
			} else {
				g2D.setColor(Color.RED);
			}
			
			g2D.drawRect(band.getStartColumn(), band.getStartRow(), band.getEndColumn()-band.getStartColumn(), band.getEndRow()-band.getStartRow());
			if(band==selectedBand) g2D.fillRect(band.getStartColumn(), band.getStartRow(), band.getEndColumn()-band.getStartColumn(), band.getEndRow()-band.getStartRow());
		}
		
		for(Well well:wells) {
			g2D.setColor(Color.WHITE);
			g2D.drawRect(well.getStartCol(), well.getStartRow(), well.getWellWidth(), well.getWellHeight());
			
//			for(Band band: well.getBands()) {
//				int alleleCluster = band.getAlleleClusterId();
//				if(alleleCluster>=0) {
//					alleleCluster %=10;
//					g2D.setColor(bandColors.get(alleleCluster));
//				} else {
//					g2D.setColor(Color.WHITE);
//				}
//				
//				g2D.drawRect(well.getStartCol(), band.getStartRow(), well.getWellWidth(), band.getEndRow()-band.getStartRow());
//			}
		}
		if (bandToCreate!=null) {
			g2D.setColor(Color.WHITE);
			Band band = bandToCreate;
			g2D.drawRect(band.getStartColumn(), band.getStartRow(), band.getEndColumn()-band.getStartColumn(), band.getEndRow()-band.getStartRow());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		bandToCreate = null;
		int column = e.getX();
		int row = e.getY();
		System.out.println("Selected row: "+row+" column: "+column);
		selectedBand = null;
		for(Band band:bands) {
			//System.out.println("Start row: "+band.getStartRow()+" column: "+band.getStartColumn());
			if(band.getStartRow()<=row && band.getEndRow()>=row && band.getStartColumn()<=column && band.getEndColumn()>=column) {
				selectedBand=band;
				break;
			}
		}
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		columnNewBand = e.getX();
		rowNewBand = e.getY();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int column = e.getX();
		int row = e.getY();
		if(rowNewBand>=row || columnNewBand>=column) return;
		bandToCreate = new Band(rowNewBand, row, columnNewBand, column, 0);
		repaint();
	}
	
	
}
