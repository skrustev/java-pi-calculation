package main.GUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class DetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6915622549267792262L;

	private JButton calculateBtn;
	private JTextField termsField;
	private JTextField threadsField;
	private JLabel termsLabel;
	private JLabel threadsLabel;
	
	private JProgressBar[] progressBars;
	private JLabel[] barLabels;

	private GridBagConstraints gc;
	private DetailListener detailListener;
	
	private int defaultNumTerms;
	private int defaultNumThreads;
	
	public DetailsPanel( int defaultNumTerms, int defaultNumThreads) {
		this.defaultNumTerms = defaultNumTerms;
		this.defaultNumThreads = defaultNumThreads;
		
		Dimension size = getPreferredSize();
		size.width = 250;
		setPreferredSize(size);

		setBorder(BorderFactory.createTitledBorder("Details"));

		termsLabel = new JLabel("NumTerms: ");
		threadsLabel = new JLabel("NumThreads: ");

		termsField = new JTextField(10);
		threadsField = new JTextField(3);
		termsField.setText(String.valueOf(defaultNumTerms));
		threadsField.setText(String.valueOf(defaultNumThreads));
		
		progressBars = new JProgressBar[8];
		barLabels = new JLabel[8];
		for(int i=0; i<8; i++) {
			progressBars[i] = new JProgressBar(0, 100);
			progressBars[i].setValue(0);
			progressBars[i].setStringPainted(true);
			barLabels[i] = new JLabel("Thread " + i + ":");
		}

		calculateBtn = new JButton("Calculate");

		setLayout(new GridBagLayout());

		gc = new GridBagConstraints();

		// First column
		// Text info
		// ----------------------------------------------
		gc.anchor = GridBagConstraints.LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;

		gc.gridx = 0;
		gc.gridy = 0;
		add(termsLabel, gc);

		gc.gridx = 0;
		gc.gridy = 1;
		add(threadsLabel, gc);
		// -----------------------------------------------

		// Second column
		// Text fields
		// -----------------------------------------------
		gc.anchor = GridBagConstraints.LINE_START;
		gc.gridx = 1;
		gc.gridy = 0;
		add(termsField, gc);

		gc.gridx = 1;
		gc.gridy = 1;
		add(threadsField, gc);
		// -----------------------------------------------

		// Bottom row
		// Calculate Button
		// -----------------------------------------------
		gc.weighty = 10;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.gridx = 1;
		gc.gridy = 2;
		add(calculateBtn, gc);
		// -----------------------------------------------
		
		for(int i=0; i<8; i++) {
			gc.weighty = 1;
			gc.anchor = GridBagConstraints.FIRST_LINE_START;
			gc.gridx = 1;
			gc.gridy = 3+i;
			add(progressBars[i], gc);
			gc.gridx = 0;
			add(barLabels[i], gc);
			
		}

		calculateBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] details = new int[2];
				details[0] = Integer.parseInt(termsField.getText());
				details[1] = Integer.parseInt(threadsField.getText());

				DetailEvent de = new DetailEvent(this, details, progressBars);

				if (detailListener != null) {
					detailListener.detailEventOccured(de);
				}
			}
		});

	}

	public void setDetailListener(DetailListener listener) {
		this.detailListener = listener;
	}
	
	public JProgressBar[] getProgressBars() {
		return progressBars;
	}
}
