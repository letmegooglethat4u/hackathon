package org.finra.dave;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class SiblingPanel extends JPanel{

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Pattern vars = Pattern.compile(":[A-Za-z0-9]+");
	AbstractTableModel dataModel;
	JTable table;
	ArrayList<String[]> data;
	LinkedList<String> header;
	JPanel hold;
	List<JoinRow> j;
	static FormulaEvaluator formEval;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6141495106641850995L;
	ConnectionPanel conn;

	public SiblingPanel(){
		header = new LinkedList<String>();
		data = new ArrayList<String[]>();
		for(int i = 0; i < 50; i++){
			String[] empty = new String[10];
			for(int x = 0; x < empty.length; x++){
				empty[x] = "";
			}
			data.add(empty);
		}
		conn = new ConnectionPanel();
		GridBagLayout lay = new GridBagLayout();
		this.setLayout(lay);
		this.add(conn, spot(.25, 0, 0, 0, 0));
		j = new LinkedList<JoinRow>();
		hold = new JPanel();
		addJoin();
		this.add(hold, spot(0, 0, 0, 1, 0));

		dataModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			public int getColumnCount() { return data.size() > 0 ? data.get(0).length : 0; }
			public int getRowCount() { return data.size();}
			public Object getValueAt(int row, int col) { return data.get(row).length <= col ? "" : data.get(row)[col];}
		};
		table = new JTable(dataModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane cont = new JScrollPane(table);
		for(int i = 0; i < 10; i++){
			table.getColumnModel().getColumn(i).setHeaderValue("Col" + i);
		}
		this.add(cont, spot(1, 1, 0, 2, 0));
		this.setSize(getMaximumSize());
	}

	private GridBagConstraints spot(double w, double h, int x, int y, int tall){
		GridBagConstraints co = new GridBagConstraints();
		co.fill = GridBagConstraints.BOTH;
		co.weightx = w;
		co.weighty = h;
		co.gridx = x;
		co.gridy = y;
		co.ipady = tall;
		return co;
	}

	public void setError(String err){
		conn.setError(err);
	}

	public void addJoin(){
		hold.setLayout(new GridLayout(j.size() + 1, 1));
		JoinRow temp = new JoinRow();
		j.add(temp);
		if(!header.isEmpty()){
			temp.addAll(header);
		}
		hold.add(temp);
		hold.repaint();
		hold.revalidate();
		this.repaint();
	}

	public void setTableRender(TableCellRenderer r){
		table.setDefaultRenderer(Object.class, r);
	}

	public Workbook getWorkbook(File f){
		try {
			if(f.getName().toLowerCase().endsWith("xls")){
				HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(f));
				formEval = new HSSFFormulaEvaluator(wb);
				return wb;
			}
			else if(f.getName().toLowerCase().endsWith("xlsx")){
				XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(f));
				formEval = new XSSFFormulaEvaluator(wb);
				return wb;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			conn.setError(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			conn.setError(e.getMessage());
		}
		return null;
	}

	public void pullExcelData(){
		File f = conn.getFile();
		Workbook wb = getWorkbook(f);
		Sheet s = wb.getSheet(conn.getTab());
		int sum = 0;
		data.clear();
		header.clear();
		for(JoinRow jo: j){
			jo.clear();
		}
		ArrayList<String[]> hold = Results(s);
		String[] head = hold.remove(0);
		data.addAll(hold);
		int numcols = data.get(0).length;
		conn.setCount(data.size() + "");
		dataModel.fireTableStructureChanged();
		for(int x = 0; x < numcols; x++){
			if(x > table.getColumnCount()){
				TableColumn tab = new TableColumn();
				table.addColumn(tab);
			}
			String name = head[x];
			header.add(name);
			TableColumn col = table.getColumnModel().getColumn(x);
			sum += name.length();
			col.setWidth(name.length());
			col.setHeaderValue(name);
		}
		table.setSize(sum, table.getHeight());
		for(JoinRow jo: j){
			jo.addAll(header);
			jo.col_names.revalidate();
		}
		dataModel.fireTableDataChanged();
		table.repaint();
	}

	public void pullFileData(){
		try {
			File f = conn.getFile();
			Scanner scan = new Scanner(f);
			data.clear();
			header.clear();
			int sum = 0;
			for(JoinRow jo: j){
				jo.clear();
			}
			ArrayList<String[]> hold = Results(scan, conn.getDelimitor());
			String[] head = hold.remove(0);
			data.addAll(hold);
			int numcols = data.get(0).length;
			conn.setCount(data.size() + "");
			dataModel.fireTableStructureChanged();
			for(int x = 0; x < numcols; x++){
				if(x > table.getColumnCount()){
					TableColumn tab = new TableColumn();
					table.addColumn(tab);
				}
				String name = head[x];
				header.add(name);
				TableColumn col = table.getColumnModel().getColumn(x);
				sum += name.length();
				col.setWidth(name.length());
				col.setHeaderValue(name);
			}
			table.setSize(sum, table.getHeight());
			for(JoinRow jo: j){
				jo.addAll(header);
				jo.col_names.revalidate();
			}
			dataModel.fireTableDataChanged();
			table.repaint();
		} catch(Exception e){
			conn.setError(e.getMessage());
		}
	}

	public void pull(){
		Connection con = null;
		try {
			con = conn.getConn();
			if(con == null){
				if(conn.isExcelConnection()){
					pullExcelData();
					return;
				}
				if(conn.isFileConnection()){
					pullFileData();
					return;
				}
				setError("Database Type Not Selected");
				return;
			}
			Statement stmt = con.createStatement();
			String sql = conn.getSQL();
			if(sql == null){
				con.close();
				return;
			}
			Set<String> var = checkVariables(sql);
			if(MasterPanel.user){
				if(var.size() > 0){
					for(String s: var){
						String ans = null;
						if(MasterPanel.arg != null && MasterPanel.arg.containsKey(s)){
							String temp = MasterPanel.arg.get(s);
							if(MasterPanel.prompt){
								ans = JOptionPane.showInputDialog(conn,
										"Please define variable: " + s, temp);
							}
							else {
								ans = temp;
							}
						}
						else {
							ans = JOptionPane.showInputDialog(conn,
									"Please define variable: " + s);
						}
						sql = sql.replaceAll(":" + s,  ans);
					}
				}
			}
			else {
				if(var.size() > 0){
					for(String s: var){
						String ans = MasterPanel.arg.get(var);
						sql.replaceAll(":" + s,  ans);
					}
				}
			}
			ResultSet result = stmt.executeQuery(sql);
			ResultSetMetaData d = result.getMetaData();
			int numcols = d.getColumnCount();
			int sum = 0;
			data.clear();
			header.clear();
			for(JoinRow jo: j){
				jo.clear();
			}
			data.addAll(Results(result));
			conn.setCount(data.size() + "");
			dataModel.fireTableStructureChanged();
			for(int x = 1; x <= numcols; x++){
				if(x > table.getColumnCount()){
					TableColumn tab = new TableColumn();
					table.addColumn(tab);
				}
				String name = d.getColumnName(x);
				header.add(name);
				TableColumn col = table.getColumnModel().getColumn(x-1);
				sum += name.length();
				col.setWidth(name.length());
				col.setHeaderValue(d.getColumnName(x));
			}
			table.setSize(sum, table.getHeight());
			for(JoinRow jo: j){
				jo.addAll(header);
				jo.col_names.revalidate();
			}
			dataModel.fireTableDataChanged();
			table.repaint();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			conn.setError(e.getMessage());
		} finally {
			try {
				if(conn.isExcelConnection() || conn.isFileConnection());
				else{
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				conn.setError("Could Not Close Connection");
			}
		}
	}

	public void setConn(String con){
		conn.setConn(con);
	}
	public void setSQL(String con){
		conn.setSQL(con);
	}
	public int setJoin(List<String> con){
		int count = 0;
		for(JoinRow join: j){
			if(count >= con.size()){
				break;
			}
			join.setJoinCol(con.get(count));
			count++;
		}
		if(count < con.size()){
			return con.size() - count;
		}
		return 0;
	}

	private Set<String> checkVariables(String sql){
		TreeSet<String> ans = new TreeSet<String>();
		Matcher matcher = vars.matcher(sql);
		while(matcher.find())
		{
			String variable = matcher.group();
			ans.add(variable.substring(1));
		}
		return ans;
	}

	private ArrayList<String[]> Results(Sheet s){
		ArrayList<String[]> file = new ArrayList<String[]>();

		Iterator<Row> rows  = s.rowIterator();

		ArrayList<String> header = new ArrayList<String>();
		if(rows.hasNext()){
			Row r = rows.next();
			for(Cell c: r){
				header.add(toString(c, c.getCellType()));
			}
		}
		int numcols = header.size();
		String[] head = new String[numcols];
		head = header.toArray(head);
		file.add(head);

		while(rows.hasNext()){
			String[] line = new String[numcols];
			Row r = rows.next();
			for(int i = 0; i < numcols; i++){
				Cell c = r.getCell(i);
				if(c != null){
					line[i] = toString(c, c.getCellType());
				}
				else {
					line[i] = "<NULL>";
				}
			}
			file.add(line);
		}
		return file;

	}

	private ArrayList<String[]> Results(Scanner s, String delimiter){
		ArrayList<String[]> file = new ArrayList<String[]>();

		String[] head = new String[1];
		if(s.hasNextLine()){
			String line = s.nextLine();
			head = line.split(delimiter);
		}
		for(int i = 0; i < head.length; i++){
			head[i] = head[i].matches("\".*\"") ? head[i].substring(1, head[i].length() - 1): head[i];
		}
		file.add(head);

		while(s.hasNextLine()){
			String line = s.nextLine();
			String[] str = line.split(delimiter);
			for(int i = 0; i < str.length; i++){
				str[i] = str[i].matches("\".*\"") ? str[i].substring(1, str[i].length() - 1): str[i];
			}
			file.add(str);
		}
		return file;
	}

	private ArrayList<String[]> Results(ResultSet result) throws SQLException{
		ResultSetMetaData data = result.getMetaData();
		int numcols = data.getColumnCount();

		ArrayList<String[]> file = new ArrayList<String[]>();

		while(result.next()){
			String[] line = new String[numcols];
			for(int x = 1; x <= numcols; x++){
				line[x-1] = toString(x, result, data.getColumnType(x));
			}
			file.add(line);
		}
		return file;

	}

	private static String toString(int colnum, ResultSet result, int type) throws SQLException{
		if(type == Types.DATE){
			Date d = result.getDate(colnum);
			if(d != null){
				return format.format(d);
			}
			return "<NULL>";
		}
		else if(type == Types.TIME){
			Date d = result.getDate(colnum);
			if(d != null){
				return format.format(d);
			}
			return "<NULL>";
		}
		else if(type == Types.TIMESTAMP){
			Timestamp t = result.getTimestamp(colnum);
			if(t != null){
				return format.format(t);
			}
			return "<NULL>";
		}
		else {
			String s = result.getString(colnum);
			if(s != null){
				return s;
			}
			return "<NULL>";
		}
	}

	private static String toString(Cell result, int type){
		if(result == null){
			return "<NULL>";
		}
		else if(type == Cell.CELL_TYPE_NUMERIC){
			if(DateUtil.isCellDateFormatted(result)){
				java.util.Date d = DateUtil.getJavaDate(result.getNumericCellValue());
				if(d != null){
					return format.format(d);
				}
				return "<NULL>";
			}
			else {
				return Double.toString(result.getNumericCellValue());
			}
		}
		else if(type == Cell.CELL_TYPE_ERROR){
			return "<NULL>";
		}
		else if(type == Cell.CELL_TYPE_BLANK){
			return "<NULL>";
		}
		else if(type == Cell.CELL_TYPE_BOOLEAN){
			return Boolean.toString(result.getBooleanCellValue());
		}
		else if(type == Cell.CELL_TYPE_FORMULA){
			formEval.evaluateInCell(result);
			return toString(result, result.getCellType());
		}
		else {
			String s = result.getStringCellValue();
			if(s != null){
				return s;
			}
			return "<NULL>";
		}
	}
}
