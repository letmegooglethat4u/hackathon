package org.finra.dave;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ParentPanel extends JPanel{

	JTextField name;
	JButton export1;
	JButton export1_join;
	JButton export2;
	JButton export3;
	JButton delete;
	/**
	 * 
	 */
	private static final long serialVersionUID = -205510213300983374L;
	SiblingPanel sib1;
	SiblingPanel sib2;

	public ParentPanel(){
		this.setLayout(new BorderLayout());
		JPanel hold = new JPanel();
		hold.setLayout(new GridLayout(1, 2));
		sib1 = new SiblingPanel();
		sib2 = new SiblingPanel();
		export1 = new JButton("1 Sheet Export");
		export1.setEnabled(false);
		export2 = new JButton("2 Sheet Export");
		export2.setEnabled(false);
		export3 = new JButton("3 Sheet Export");
		export3.setEnabled(false);
		export1_join = new JButton("Join Export");
		export1_join.setEnabled(false);
		delete = new JButton("Remove Matches");
		delete.setEnabled(false);
		name = new JTextField("export");

		sib1.setTableRender(new Renderer(sib2.table));
		sib2.setTableRender(new Renderer(sib1.table));

		export1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					export(1);
				} catch (IOException e) {
					e.printStackTrace();
					sib1.setError("Problem Writing to File");
					sib2.setError("Problem Writing to File");
				}
			}

		});

		export2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					export(2);
				} catch (IOException e) {
					e.printStackTrace();
					sib1.setError("Problem Writing to File");
					sib2.setError("Problem Writing to File");
				}
			}

		});

		export3.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					export(3);
				} catch (IOException e) {
					e.printStackTrace();
					sib1.setError("Problem Writing to File");
					sib2.setError("Problem Writing to File");
				}
			}
		});

		export1_join.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					export(4);
				} catch (IOException e) {
					e.printStackTrace();
					sib1.setError("Problem Writing to File");
					sib2.setError("Problem Writing to File");
				}
			}

		});

		sib1.conn.run.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		sib2.conn.run.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		sib1.conn.save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				SavedConn s = sib1.conn.writeConn();
				sib2.conn.addConn(s);
			}
		});
		sib2.conn.save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				SavedConn s = sib2.conn.writeConn();
				sib1.conn.addConn(s);
			}
		}); 
		addedJoin();

		hold.add(sib1);
		hold.add(sib2);
		this.add(hold, BorderLayout.CENTER);
		JPanel bot = new JPanel();
		bot.setLayout(new GridLayout(1,6));
		bot.add(name);
		bot.add(export1);
		bot.add(export2);
		bot.add(export3);
		bot.add(export1_join);
		bot.add(delete);
		this.add(bot, BorderLayout.SOUTH);
	}

	protected void run(){
		sib1.pull();
		sib2.pull();
		sib1.dataModel.fireTableDataChanged();
		sib2.dataModel.fireTableDataChanged();
		export1.setEnabled(true);
		export2.setEnabled(true);
		export3.setEnabled(false);
		export1_join.setEnabled(false);
		delete.setEnabled(false);
		if(MasterPanel.join1 != null){
			setJoin(1, MasterPanel.join1);
		}
		if(MasterPanel.join2 != null){
			setJoin(2, MasterPanel.join2);
		}
	}

	protected void join(final int x){
		join(sib1.data, sib2.data, x);
		sib1.dataModel.fireTableDataChanged();
		sib2.dataModel.fireTableDataChanged();
		export3.setEnabled(true);

		delete.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeMatches(sib1.data, sib2.data, x);
				sib1.dataModel.fireTableDataChanged();
				sib2.dataModel.fireTableDataChanged();
			}

		});
		delete.setEnabled(true);
		export1_join.setEnabled(true);
	}

	protected void export(int i) throws IOException{
		if(name.getText().isEmpty()){
			sib1.setError("Please Name File");
			sib2.setError("Please Name File");
			return;
		}

		ProgressMonitor progressMonitor = new ProgressMonitor(this,
				"Running a Long Task",
				"", 0, sib1.data.size() + sib2.data.size());

		if(i == 1){
			if(export1.isEnabled()){
				export(name.getText(), progressMonitor);
			}
		}
		else if (i == 3) {
			if(export3.isEnabled()){
				split(name.getText(), progressMonitor);
			}
		}
		else if (i == 4){
			if(export1_join.isEnabled()){
				export(sib1, sib2, progressMonitor);
			}
		}
		else {
			if(export2.isEnabled()){
				export(progressMonitor);
			}
		}
	}

	public void setFileName(String name){
		this.name.setText(name);
	}

	public void setConn(int num, String con){
		if(num == 1){
			sib1.setConn(con);
		}
		else if(num == 2){
			sib2.setConn(con);
		}
		else {
			sib1.setConn(con);
			sib2.setConn(con);
		}
	}

	public void setSQL(int num, String sql){
		if(num == 1){
			sib1.setSQL(sql);
		}
		else if(num == 2){
			sib2.setSQL(sql);
		}
		else {
			sib1.setSQL(sql);
			sib2.setSQL(sql);
		}
	}

	public void setJoin(int num, List<String> join_list){
		if(num == 1){
			int n = sib1.setJoin(join_list);
			if(n != 0){
				newJoinRow(n);
				sib1.setJoin(join_list);
			}
		}
		else if(num == 2){
			int n = sib2.setJoin(join_list);
			if(n != 0){
				newJoinRow(n);
				sib2.setJoin(join_list);
			}
		}
		else {
			int n = sib1.setJoin(join_list);
			if(n != 0){
				newJoinRow(n);
				sib1.setJoin(join_list);
			}
			sib2.setJoin(join_list);
		}
	}

	public boolean checkCounts(){
		String count1 = sib1.conn.count.getText();
		String count2 = sib2.conn.count.getText();
		return count1.equalsIgnoreCase(count2);
	}

	public boolean checkData(){
		if(!checkCounts()){
			return false;
		}
		if(sib1.header.size() != sib2.header.size()){
			return false;
		}
		for(int row = 1; row <= sib1.table.getRowCount(); row++){
			for(int cell = 0; cell < sib1.table.getColumnCount(); cell++){
				TableCellRenderer renderer = sib1.table.getCellRenderer(row, cell);
				Component component = sib1.table.prepareRenderer(renderer, row, cell);    
				Color color = component.getBackground();
				if(color != null){
					return false;
				}
			}
		}
		return true;
	}

	private void newJoinRow(int n){
		for(int i = 0; i < n; i++){
			sib1.addJoin();
			sib2.addJoin();
			addedJoin();
		}
	}

	private void addedJoin(){
		final int x = sib1.j.size();
		sib1.j.get(x - 1).join.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				join(x);
			}

		});
		sib1.j.get(x - 1).sort.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sort(sib1, x);
				sib1.dataModel.fireTableDataChanged();
				sib2.dataModel.fireTableDataChanged();
			}

		});
		sib1.j.get(x - 1).add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newJoinRow(1);
			}

		});
		sib2.j.get(x - 1).join.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				join(x);
			}

		});
		sib2.j.get(x - 1).sort.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sort(sib2, x);
				sib2.dataModel.fireTableDataChanged();
				sib1.dataModel.fireTableDataChanged();
			}

		});
		sib2.j.get(x - 1).add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newJoinRow(1);
			}

		});

	}

	private void export(String name, ProgressMonitor prog) throws IOException{
		JTable d1 = sib1.table;
		JTable d2 = sib2.table;
		TableModel t1 = d1.getModel();
		TableModel t2 = d2.getModel();
		String from1 = sib1.conn.name.getText();
		String from2 = sib2.conn.name.getText();
		int max = Math.max(d1.getRowCount(), d2.getRowCount());
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet temp = wb.createSheet(from1 + "_Union_" + from2);
		XSSFRow head = temp.createRow(0);
		int header = 1;
		for(String s: sib1.header){
			XSSFCell h = head.createCell(header);
			h.setCellValue(s);
			header++;
		}
		int count = 1;
		for(int row = 0; row < max; row++){
			if(d1.getRowCount() > row && t1.getValueAt(row, 0) != null){
				XSSFRow r = temp.createRow(count);
				XSSFCell from = r.createCell(0);
				from.setCellValue(from1);
				for(int cell = 0; cell < d1.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, d1, row, cell);
				}
				count++;
			}
			if(d2.getRowCount() > row && t2.getValueAt(row, 0) != null){
				XSSFRow r = temp.createRow(count);
				XSSFCell from = r.createCell(0);
				from.setCellValue(from2);
				for(int cell = 0; cell < d2.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, d2, row, cell);
				}
				count++;
			}
			prog.setProgress(count);
		}
		File file = new File("Data/" + name + ".xlsx");
		FileOutputStream f = new FileOutputStream(file);
		wb.write(f);
		f.close();
		Desktop.getDesktop().open(file);
	}

	private void export(ProgressMonitor prog) throws IOException{
		XSSFWorkbook wb = new XSSFWorkbook();
		if(name.getText().isEmpty()){
			sib1.setError("Please Name File");
			sib2.setError("Please Name File");
			wb.close();
			return;
		}
		String tab1 = sib1.conn.name.getText();
		String tab2 = sib2.conn.name.getText();
		if(tab1.equals(tab2)){
			tab2 = tab2 + "_2";
		}
		export(wb, sib1.header, sib1.table, tab1, prog);
		export(wb, sib2.header, sib2.table, tab2, prog);
		File file = new File("Data/" + name.getText() + ".xlsx");
		FileOutputStream f = new FileOutputStream(file);
		wb.write(f);
		f.close();
		Desktop.getDesktop().open(file);
	}

	private void export(SiblingPanel p1, SiblingPanel p2,  ProgressMonitor prog) throws IOException{
		XSSFWorkbook wb = new XSSFWorkbook();
		if(name.getText().isEmpty()){
			p1.setError("Please Name File");
			p2.setError("Please Name File");
			wb.close();
			return;
		}
		export(wb, p1.header, p1.table, p2.header, p2.table, prog);
		File file = new File("Data/" + name.getText() + ".xlsx");
		FileOutputStream f = new FileOutputStream(file);
		wb.write(f);
		f.close();
		Desktop.getDesktop().open(file);
	}

	private void export(XSSFWorkbook wb, LinkedList<String> h1, JTable d1, LinkedList<String> h2, JTable d2,  ProgressMonitor prog) throws IOException{
		XSSFSheet temp = wb.createSheet("JoinedData");
		XSSFRow head = temp.createRow(0);
		int header = 0;
		for(String s: h1){
			XSSFCell hold = head.createCell(header);
			hold.setCellValue(s);
			header++;
		}
		for(String s: h2){
			XSSFCell hold = head.createCell(header);
			hold.setCellValue(s);
			header++;
		}
		for(int row = 1; row <= d1.getRowCount(); row++){
			XSSFRow r = temp.createRow(row);
			for(int cell = 0; cell < d1.getColumnCount(); cell++){
				XSSFCell c = r.createCell(cell);
				prepareCell(wb, c, d1, row - 1, cell);
			}
			int len = d2.getColumnCount();
			for(int cell = len; cell < d2.getColumnCount() + len; cell++){
				XSSFCell c = r.createCell(cell);
				prepareCell(wb, c, d2, row - 1, cell - len);
			}
			prog.setProgress(row);
		}
	}

	private void export(XSSFWorkbook wb, LinkedList<String> h, JTable d, String from, ProgressMonitor prog) throws IOException{
		XSSFSheet temp = wb.createSheet(from);
		XSSFRow head = temp.createRow(0);
		int header = 1;
		for(String s: h){
			XSSFCell hold = head.createCell(header);
			hold.setCellValue(s);
			header++;
		}
		for(int row = 1; row <= d.getRowCount(); row++){
			XSSFRow r = temp.createRow(row);
			XSSFCell f = r.createCell(0);
			f.setCellValue(from);
			for(int cell = 0; cell < d.getColumnCount(); cell++){
				XSSFCell c = r.createCell(cell + 1);
				prepareCell(wb, c, d, row - 1, cell);
			}
			prog.setProgress(row);
		}
	}

	private void split(String name, ProgressMonitor prog) throws IOException{
		JTable t1 = sib1.table;
		JTable t2 = sib2.table;
		TableModel d1 = t1.getModel();
		TableModel d2 = t2.getModel();
		String from1 = sib1.conn.name.getText();
		String from2 = sib2.conn.name.getText();
		if(from1.equals(from2)){
			from2 = from2 + "_2";
		}
		int max = Math.max(d1.getRowCount(), d2.getRowCount());
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet[] all = new XSSFSheet[3];
		all[0] = wb.createSheet(from1 + "_Only");
		all[1] = wb.createSheet(from1 + "_Intsct_" + from2);
		all[2] = wb.createSheet(from2 + "_Only");
		for(XSSFSheet temp: all){
			XSSFRow head = temp.createRow(0);
			int header = 1;
			for(String s: sib1.header){
				XSSFCell h = head.createCell(header);
				h.setCellValue(s);
				header++;
			}
		}
		int[] count = new int[3];
		count[0] = 1;
		count[1] = 1;
		count[2] = 1;
		for(int row = 0; row < max; row++){
			if(d1.getValueAt(row, 0) != null &&
					d2.getValueAt(row, 0) != null){
				XSSFRow r = all[1].createRow(count[1]);
				XSSFCell from = r.createCell(0);
				from.setCellValue(from1);
				for(int cell = 0; cell < d1.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, t1, row, cell);
				}
				count[1]++;
				r = all[1].createRow(count[1]);
				from = r.createCell(0);
				from.setCellValue(from2);
				for(int cell = 0; cell < d2.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, t2, row, cell);
				}
				count[1]++;
			}
			else if(d1.getValueAt(row, 0) != null &&
					d2.getValueAt(row, 0) == null){
				XSSFRow r = all[0].createRow(count[0]);
				XSSFCell from = r.createCell(0);
				from.setCellValue(from1);
				for(int cell = 0; cell < d1.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, t1, row, cell);
				}
				count[0]++;
			}
			else if(d1.getValueAt(row, 0) == null &&
					d2.getValueAt(row, 0) != null){
				XSSFRow r = all[2].createRow(count[2]);
				XSSFCell from = r.createCell(0);
				from.setCellValue(from2);
				for(int cell = 0; cell < d2.getColumnCount(); cell++){
					XSSFCell c = r.createCell(cell + 1);
					prepareCell(wb, c, t2, row, cell);
				}
				count[2]++;
			}
			prog.setProgress(row);
		}
		File file = new File("Data/" + name + ".xlsx");
		FileOutputStream f = new FileOutputStream(file);
		wb.write(f);
		f.close();
		Desktop.getDesktop().open(file);
	}

	private void prepareCell(XSSFWorkbook wb, XSSFCell c, JTable table, int row, int col){
		TableModel dtm = table.getModel();
		TableCellRenderer renderer = table.getCellRenderer(row, col);
		Component component = table.prepareRenderer(renderer, row, col);    
		Color color = component.getBackground();
		c.setCellValue((String) dtm.getValueAt(row, col));
		XSSFCellStyle style = wb.createCellStyle();
		if(color != null){
			style.setFillForegroundColor(new XSSFColor(color));
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
		c.setCellStyle(style);
	}

	private void sort(SiblingPanel sib, int num){
		clean(sib.data);
		int[] sort = new int[num];
		for(int i = 0; i < num; i++){
			sort[i] = sib.header.indexOf(sib.j.get(i).getJoinCol());
		}
		Collections.sort(sib.data, new StringArrayCompare(sort));
	}

	private void join(ArrayList<String[]> d1, ArrayList<String[]> d2, int num){
		clean(d1);
		clean(d2);
		int[] join1 = new int[num];
		int[] join2 = new int[num];
		for(int i = 0; i < num; i++){
			join1[i] = sib1.header.indexOf(sib1.j.get(i).getJoinCol());
		}
		for(int i = 0; i < num; i++){
			join2[i] = sib2.header.indexOf(sib2.j.get(i).getJoinCol());
		}
		LinkedList<String[]> hold = new LinkedList<String[]>();
		int count = 0;
		for(int x = 0; x < d1.size(); x++){
			boolean found = false;
			int matches = 0;
			for(int i = x; i < d2.size(); i++){
				boolean all = true;
				for(int j = 0; j < join1.length; j++){
					if(Renderer.compareThings(d1.get(x)[join1[j]], d2.get(i)[join2[j]]) != 0){
						all = false;
						break;
					}
				}
				if(all){
					//System.out.println("Match " + x + "|" + i + " found");
					matches++;
					String[] temp = d2.remove(i);
					d2.add(x, temp);
					hold.add(temp);
					found = true;
					count++;
					if(matches > 1){
						String[] point = d1.get(x);
						d1.add(x, point);
						x++;
					}
				}
			}
			if(!found){
				for(String[] str: hold){
					boolean all = true;
					for(int j = 0; j < join1.length; j++){
						if(Renderer.compareThings(d1.get(x)[join1[j]], str[join2[j]]) != 0){
							all = false;
							break;
						}
					}
					if(all){
						matches++;
						d2.add(x, str);
						found = true;
						count++;
						if(matches > 1){
							String[] point = d1.get(x);
							d1.add(x, point);
							x++;
						}
					}
				}
			}
			if(!found){
				try {
					d2.add(x, new String[d2.get(0).length]);
				}
				catch (IndexOutOfBoundsException e){
					sib1.setError("Right Table is Empty");
					sib2.setError("Right Table is Empty");
				}
			}
		}
		int add = d2.size() - d1.size();
		try {
			for(int i = 0; i < add; i++){
				d1.add(new String[d1.get(0).length]);
			}
		}
		catch (IndexOutOfBoundsException e){
			sib1.setError("Left Table is Empty");
			sib2.setError("Left Table is Empty");
		}
		sib1.j.get(num - 1).setMatches(count + " / " + d1.size());
		sib2.j.get(num - 1).setMatches(count + " / " + d2.size());
	}

	private void removeMatches(ArrayList<String[]> d1, ArrayList<String[]> d2, int num){
		clean(d1);
		clean(d2);
		int[] join1 = new int[num];
		int[] join2 = new int[num];
		for(int i = 0; i < num; i++){
			join1[i] = sib1.header.indexOf(sib1.j.get(i).getJoinCol());
		}
		for(int i = 0; i < num; i++){
			join2[i] = sib2.header.indexOf(sib2.j.get(i).getJoinCol());
		}
		for(int x = 0; x < d1.size(); x++){
			for(int i = x; i < d2.size(); i++){
				boolean all = true;
				for(int j = 0; j < join1.length; j++){
					if(Renderer.compareThings(d1.get(x)[join1[j]], d2.get(i)[join2[j]]) != 0){
						all = false;
						break;
					}
				}
				if(all){
					d2.remove(i);
					d1.remove(x);
					x--;
					break;
				}
			}
		}
	}

	private void clean(ArrayList<String[]> d){
		for(int i = 0; i < d.size(); i++){
			if(d.get(i)[0] == null){
				d.remove(i);
				i--;
			}
		}
	}

	private static class Renderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Color lowColor = new Color(255, 120, 120);
		Color highColor = new Color(120, 255, 120);
		Color backgroundColor = getBackground();
		JTable other;

		public Renderer(JTable o){
			other = o;
		}

		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);

			int rows1 = table.getRowCount();
			int cols1 = table.getColumnCount();
			int rows2 = other.getRowCount();
			int cols2 = other.getColumnCount();
			if(cols1 == cols2){
				if(row < Math.min(rows1, rows2)){
					String value1 = (String)table.getValueAt(row,column);
					String value2 = (String)other.getValueAt(row,column);

					int x = compareThings(value1, value2);
					if(x < 0){
						setBackground(lowColor);
						return c;
					}
					else if(x > 0){
						setBackground(highColor);
						return c;
					}
					else {
						setBackground(backgroundColor);
						return c;
					}
				}
				else {
					setBackground(backgroundColor);
					return c;
				}
			}
			else {
				setBackground(backgroundColor);
				return c;
			}
		}

		private static int compareThings(String value1, String value2){
			if(value1 == null && value2 == null){
				return 0;
			}
			else if (value1 == null){
				return -1;
			}
			else if (value2 == null){
				return 1;
			}

			Double doub1 = null;
			Double doub2 = null;
			if(value1.matches("[0-9.E]+") && value2.matches("[0-9.E]+")){
				doub1 = Double.parseDouble(value1);
				doub2 = Double.parseDouble(value2);
				if(doub1.compareTo(doub2) < 0) {
					return -1;
				}
				else if(doub1.compareTo(doub2) > 0) {
					return 1;
				}
				else {
					return 0;
				}
			}
			Date d1 = tryDateFormat(value1);
			Date d2 = tryDateFormat(value2);
			if(d1 != null && d2 != null){
				if(d1.compareTo(d2) < 0) {
					return -1;
				}
				else if(d1.compareTo(d2) > 0) {
					return 1;
				}
				else {
					return 0;
				}
			}
			else {
				if(value1.equals("<NULL>")){
					if(value2.equals("<NULL>") || value2.isEmpty()){
						return 0;
					}
					else if(value1.compareTo(value2) < 0) {
						return -1;
					}
					else if(value1.compareTo(value2) > 0) {
						return 1;
					}
					else {
						return 0;
					}
				}
				else if(value2.equals("<NULL>")){
					if(value1.equals("<NULL>") || value1.isEmpty()){
						return 0;
					}
					else if(value1.compareTo(value2) < 0) {
						return -1;
					}
					else if(value1.compareTo(value2) > 0) {
						return 1;
					}
					else {
						return 0;
					}
				}
				else if(value1.compareTo(value2) < 0) {
					return -1;
				}
				else if(value1.compareTo(value2) > 0) {
					return 1;
				}
				else {
					return 0;
				}
			}
		}

		private static Date tryDateFormat(String value) {
			ArrayList<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>() {
				private static final long serialVersionUID = 1L;
				{
					add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
					add(new SimpleDateFormat("dd.M.yyyy hh:mm:ss a"));
					add(new SimpleDateFormat("dd-MMM-yy hh:mm:ss a"));
					add(new SimpleDateFormat("dd.MMM.yyyy hh:mm:ss a"));
					add(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a"));
					add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a"));

					add(new SimpleDateFormat("M/dd/yyyy HH:mm:ss"));
					add(new SimpleDateFormat("dd.M.yyyy HH:mm:ss"));
					add(new SimpleDateFormat("dd-MMM-yy HH:mm:ss"));
					add(new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss"));
					add(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"));
					add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

					add(new SimpleDateFormat("M/dd/yyyy HH:mm"));
					add(new SimpleDateFormat("dd.M.yyyy HH:mm"));
					add(new SimpleDateFormat("dd-MMM-yy HH:mm"));
					add(new SimpleDateFormat("dd.MMM.yyyy HH:mm"));
					add(new SimpleDateFormat("dd-MMM-yyyy HH:mm"));
					add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

					add(new SimpleDateFormat("M/dd/yyyy"));
					add(new SimpleDateFormat("dd.M.yyyy"));
					add(new SimpleDateFormat("dd-MMM-yy"));
					add(new SimpleDateFormat("dd.MMM.yyyy"));
					add(new SimpleDateFormat("dd-MMM-yyyy"));
					add(new SimpleDateFormat("yyyy-MM-dd"));
				}};

				for(SimpleDateFormat form: dateFormats){
					try {
						form.format(new Date());
						Date d = form.parse(value);
						return d;
					} catch (ParseException e) {}
				}
				return null;
		}
	}
}

class StringArrayCompare implements Comparator<String[]>{

	private int[] cols;

	StringArrayCompare(int[] cols){
		this.cols = cols;
	}

	@Override
	public int compare(String[] strings, String[] otherStrings) {
		int ans = 0;
		for(int x: cols){
			ans = strings[x].compareTo(otherStrings[x]);
			if(ans == 0){
				continue;
			}
			return ans;
		}
		return 0;
	}

}
