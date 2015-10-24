package org.finra.dave;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.ImageObserver;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class JoinRow extends JPanel{
	
	JButton add;
	JButton join;
	JButton sort;
	JComboBox<String> col_names;
	ConnectionPanel con;
	JLabel count;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207423431062356314L;

	public JoinRow(){
		add = new JButton("+");
		col_names = new JComboBox<String>();
		col_names.setSize(ImageObserver.WIDTH, 200);
		sort = new JButton("Sort");
		join = new JButton("Join");
		TitledBorder count_t = BorderFactory.createTitledBorder("Match Count");
		count = new JLabel("NULL");
		count.setBorder(count_t);
		this.setLayout(new GridBagLayout());
		this.add(add, spot(0, 0, 0, 0, 1));
		this.add(col_names, spot(.5, 0, 1, 0, 2));
		this.add(count, spot(.5, 0, 3, 0, 2));
		this.add(join, spot(0, 0, 5, 0, 2));
		this.add(sort, spot(0, 0, 7, 0, 2));
	}
	
	public void clear(){
		col_names.removeAllItems();
	}
	
	public String getJoinCol(){
		return (String) col_names.getSelectedItem();
	}
	
	public void setJoinCol(String col){
		col_names.setSelectedItem(col);
	}
	
	public void addAll(List<String> list){
		for(String s: list){
			col_names.addItem(s);
		}
	}
	
	public void setMatches(String cnt){
		count.setText(cnt);
	}
	
	private GridBagConstraints spot(double w, double h, int x, int y, int span){
		GridBagConstraints co = new GridBagConstraints();
		co.fill = GridBagConstraints.HORIZONTAL;
		co.weightx = w;
		co.weighty = h;
		co.gridx = x;
		co.gridy = y;
		co.gridwidth = span; 
		return co;
	}
	
}
