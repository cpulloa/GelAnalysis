package swinggui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonsPanel extends JPanel implements ActionListener {
	private static final String ACTION_LOAD = "Load";
	private static final String ACTION_CALCULATE = "Calculate";
	private static final String ACTION_CLUSTER = "Cluster";
	private static final String ACTION_SAVE = "Save";
	private static final String ACTION_DELETE_BAND = "Delete";
	private static final String ACTION_ADD_BAND = "Add";
	
	private IntensityProcessorInterface parent;
	private JButton butLoad = new JButton("Load");
	private JButton butCalculate = new JButton("Calculate");
	private JButton butCluster = new JButton("Cluster");
	private JButton butSave = new JButton("Save");
	
	private JButton butAddBand = new JButton("Add");
	private JButton butDeleteBand = new JButton("Delete");
	
	
	
	public ButtonsPanel(IntensityProcessorInterface parent) {
		this.parent = parent;
		setLayout(new GridLayout(2, 4));
		butLoad.setActionCommand(ACTION_LOAD);
		butLoad.addActionListener(this);
		add(butLoad);
		
		butCalculate.setActionCommand(ACTION_CALCULATE);
		butCalculate.addActionListener(this);
		add(butCalculate);
		
		butCluster.setActionCommand(ACTION_CLUSTER);
		butCluster.addActionListener(this);
		add(butCluster);
		
		butSave.setActionCommand(ACTION_SAVE);
		butSave.addActionListener(this);
		add(butSave);
		
		add(new JLabel("Band actions"));
		
		butAddBand.setActionCommand(ACTION_ADD_BAND);
		butAddBand.addActionListener(this);
		add(butAddBand);
		
		butDeleteBand.setActionCommand(ACTION_DELETE_BAND);
		butDeleteBand.addActionListener(this);
		add(butDeleteBand);
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals(ACTION_LOAD)) {
			parent.load();
		}
		if(command.equals(ACTION_CALCULATE)) {
			parent.calculate();
		}
		if(command.equals(ACTION_CLUSTER)) {
			parent.clusterBands();
		}
		if(command.equals(ACTION_SAVE)) {
			parent.save();
		}
		if(command.equals(ACTION_ADD_BAND)) {
			parent.addBand();
		}
		if(command.equals(ACTION_DELETE_BAND)) {
			parent.deleteSelectedBand();
		}
		
	}

}
