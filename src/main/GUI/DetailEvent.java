package main.GUI;
import java.util.EventObject;

import javax.swing.JProgressBar;


public class DetailEvent extends EventObject{
	private int[] details;
	JProgressBar[] progressBars;
	
	public DetailEvent(Object source, int[] details,JProgressBar[] progressBars) {
		super(source);
		this.details = details;
		this.progressBars = progressBars;
	}
	
	public int[] getDetails() {
		return details;
	}
	
	public JProgressBar[] getProgressBars() {
		return progressBars;
	}
}
