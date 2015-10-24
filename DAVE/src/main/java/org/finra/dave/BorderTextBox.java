package org.finra.dave;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class BorderTextBox extends JTextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TitledBorder b;

	public BorderTextBox(String title){
		super();
		b = BorderFactory.createTitledBorder(title);
		this.setBorder(b);
	}
	
	public void setTitle(String title){
		b = BorderFactory.createTitledBorder(title);
		this.setBorder(b);
		this.repaint();
	}
}
