package org.finra.dave;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ConnectionPanel extends JPanel{

	static ArrayList<String> sqlHist;
	static SavedConn[] conns;
	int sqlnum;
	JComboBox<SavedConn> con_names;
	JComboBox<String> db_names;
	BorderTextBox name;
	BorderTextBox url;
	BorderTextBox port;
	BorderTextBox schema;
	BorderTextBox user;
	BorderTextBox sql_name;
	JPasswordField pass;
	JTextArea sql;
	JLabel error;
	JButton run;
	JButton browse;
	JLabel count;
	JButton save;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6944251588741511851L;

	public ConnectionPanel(){
		TitledBorder title = BorderFactory.createTitledBorder("Connection");
		this.setBorder(title);
		JLabel con = new JLabel("Saved Connections: ", SwingConstants.RIGHT);
		GridBagLayout lay = new GridBagLayout();
		this.setLayout(lay);
		name = createTextBox("Name");
		url = createTextBox("URL or File Name");
		port = createTextBox("Port");
		schema = createTextBox("Schema");
		user = createTextBox("User");
		TitledBorder pass_t = BorderFactory.createTitledBorder("Password");
		pass = new JPasswordField();
		pass.setSize(100, JPasswordField.HEIGHT);
		pass.setBorder(pass_t);
		save = new JButton("Save");
		TitledBorder query_t = BorderFactory.createTitledBorder("SQL Query");
		sql = new JTextArea();
		JScrollPane scroll = new JScrollPane(sql);
		scroll.setBorder(query_t);
		error = new JLabel();
		browse = new JButton("Browse");
		run = new JButton("Run");
		sql_name = createTextBox("SQL Name(Optional)");
		TitledBorder count_t = BorderFactory.createTitledBorder("Row Count");
		count = new JLabel("NULL");
		count.setBorder(count_t);

		browse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fc = new JFileChooser();
				AccessFilter af = new AccessFilter();
				ExcelFilter xf = new ExcelFilter();
				TextFilter text = new TextFilter();
				fc.addChoosableFileFilter(af);
				fc.addChoosableFileFilter(xf);
				fc.addChoosableFileFilter(text);
				if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Access")){
					fc.setFileFilter(af);
				}
				if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Excel")){
					fc.setFileFilter(xf);
				}
				if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Delimited File")){
					fc.setFileFilter(text);
				}
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showOpenDialog(ConnectionPanel.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					url.setText(file.getAbsolutePath());
				} 
			}
		}); 

		sqlnum = 0;
		if(sqlHist == null){
			sqlHist = getSQLHist();
		}
		if(sqlHist.size() > 0){
			sqlnum = sqlHist.size() - 1;
			display(sqlHist.get(sqlnum));
		}
		sql.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_F2){
					if(sqlHist.size() > 0){
						sqlnum = (sqlnum + 1) % sqlHist.size();
						display(sqlHist.get(sqlnum));
					}
				}
				else if(arg0.getKeyCode() == KeyEvent.VK_F1){
					if(sqlHist.size() > 0){
						sqlnum = (sqlnum - 1 + sqlHist.size()) % sqlHist.size();
						display(sqlHist.get(sqlnum));
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

		});

		db_names = setConnectionTypes();

		if(conns == null){
			getSavedConnections();
		}
		if(conns != null){
			con_names = new JComboBox<SavedConn>(conns);
		}
		else {
			con_names = new JComboBox<SavedConn>();
		}

		con_names.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				SavedConn s = (SavedConn) con_names.getSelectedItem();
				display(s);
			}
		}); 
		db_names.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				display();
			}
		}); 
		SavedConn s = (SavedConn) con_names.getSelectedItem();
		if(s != null){
			display(s);
		}

		this.add(con, spot(.25, 0, 0, 1, 0));
		this.add(con_names, spot(.25, 1, 0, 1, 0));
		this.add(db_names, spot(.25, 2, 0, 1, 0));
		this.add(name, spot(.25, 3, 0, 1, 0));
		this.add(url, spot(.25, 0, 1, 2, 0));
		this.add(port, spot(.25, 2, 1, 1, 0));
		this.add(schema, spot(.25, 3, 1, 1, 0));
		this.add(user, spot(.25, 0, 2, 1, 0));
		this.add(pass, spot(.25, 1, 2, 1, 0));
		this.add(browse, spot(.25, 2, 2, 1, 0));
		this.add(save, spot(.25, 3, 2, 1, 0));
		this.add(scroll, spot(1, 0, 3, 4, 100));
		this.add(sql_name, spot(.25, 0, 4, 3, 0));
		this.add(count, spot(.25, 3, 4, 1, 0));
		this.add(error, spot(.25, 0, 5, 3, 0));
		this.add(run, spot(.25, 3, 5, 1, 0));
	}

	public void display(){
		if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Access")){
			name.setText("");
			url.setText("");
			port.setTitle("Port");
			port.setText("");
			schema.setTitle("Schema");
			schema.setText("");
			schema.setEnabled(false);
			port.setEnabled(false);
			user.setText("");
			pass.setText("");
			browse.setEnabled(true);
			this.repaint();
		}
		else if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Excel")){
			name.setText("");
			url.setText("");
			port.setTitle("Port");
			port.setText("");
			schema.setTitle("Sheet");
			schema.setText("");
			port.setEnabled(false);
			user.setText("");
			pass.setText("");
			browse.setEnabled(true);
			sql.setText("");
			sql_name.setText("");
			this.repaint();
		}
		else if(((String)db_names.getSelectedItem()).equalsIgnoreCase("Delimited File")){
			db_names.setSelectedItem("");
			name.setText("");
			url.setText("");
			port.setTitle("Delimitor");
			port.setText("");
			schema.setTitle("Schema");
			schema.setText("");
			schema.setEnabled(false);
			user.setText("");
			pass.setText("");
			browse.setEnabled(true);
			sql.setText("");
			sql_name.setText("");
			this.repaint();
		}
		else {
			schema.setEnabled(true);
			port.setEnabled(true);
			name.setText("");
			url.setText("");
			port.setTitle("Port");
			port.setText("");
			schema.setTitle("Schema");
			schema.setText("");
			user.setText("");
			pass.setText("");
			browse.setEnabled(false);
			this.repaint();
		}
	}

	public void display(SavedConn s){
		if(s.db_type.equalsIgnoreCase("Access")){
			db_names.setSelectedItem(s.db_type);
			name.setText(s.name);
			url.setText(s.url);
			schema.setTitle("Schema");
			schema.setEnabled(false);
			port.setEnabled(false);
			port.setTitle("Port");
			this.repaint();
			user.setText(s.user);
			pass.setText(s.pass);
			browse.setEnabled(true);
		}
		else if(s.db_type.equalsIgnoreCase("Excel")){
			db_names.setSelectedItem(s.db_type);
			name.setText(s.name);
			url.setText(s.url);
			port.setTitle("Port");
			port.setEnabled(false);
			this.repaint();
			schema.setText(s.schema);
			user.setText(s.user);
			pass.setText(s.pass);
			browse.setEnabled(true);
		}
		else if(s.db_type.equalsIgnoreCase("Delimited File")){
			System.out.println("Here");
			db_names.setSelectedItem(s.db_type);
			name.setText(s.name);
			url.setText(s.url);
			port.setEnabled(true);
			port.setTitle("Delimitor");
			port.setText(s.port);
			this.repaint();
			schema.setTitle("Schema");
			schema.setText(s.schema);
			schema.setEnabled(false);
			user.setText(s.user);
			pass.setText(s.pass);
			browse.setEnabled(true);
		}
		else {
			port.setEnabled(true);
			schema.setEnabled(true);
			db_names.setSelectedItem(s.db_type);
			name.setText(s.name);
			url.setText(s.url);
			port.setTitle("Port");
			port.setText(s.port);
			this.repaint();
			schema.setTitle("Schema");
			schema.setText(s.schema);
			user.setText(s.user);
			pass.setText(s.pass);
			browse.setEnabled(false);
		}
	}

	public void display(String s){
		String query = s.replaceAll("\\<#.*\\>", "");
		int index = s.indexOf("<#");
		String nm = s.substring(index + 2, s.length() - 1);
		sql.setText(query);
		sql_name.setText(nm);
	}

	public void setCount(String cnt){
		count.setText(cnt);
	}

	public Connection getConn() throws SQLException{
		String name = (String) db_names.getSelectedItem();
		if(name.equalsIgnoreCase("Oracle")){
			return getOracleConn();
		}
		else if(name.equalsIgnoreCase("Netezza")){
			return getNetezzaConn();
		}
		else if(name.equalsIgnoreCase("PostgreSql")){
			return getPostgreSqlConn();
		}
		else if(name.equalsIgnoreCase("MySQL")){
			return getMySQLConn();
		}
		else if(name.equalsIgnoreCase("MSFT SQL Server")){
			return getMSFTSQLConn();
		}
		else if(name.equalsIgnoreCase("Access")){
			return getAccessConn();
		}
		else if(name.equalsIgnoreCase("Excel")){
			return getExcelConn();
		}
		else {
			return null;
		}
	}

	public String getSQL(){
		String[] spl = sql.getText().split(";");
		if(!spl[0].isEmpty()){
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter("Data/SQL/SQLhist.list", true));
			} catch (IOException e) {
				setError(e.getMessage());
				return null;
			}
			String nm = null;
			if(!sqlHist.contains(spl[0] + "<#" + (nm = (sql_name.getText() == null ? "" : sql_name.getText())) + ">")){
				out.println(spl[0] + "<#" + nm + ">");
				sqlHist.add(spl[0] + "<#" + nm + ">");
			}
			out.close();
			return spl[0];
		}
		else {
			String nm = "<#" + sql_name.getText() + ">";
			for(String s: sqlHist){
				if(s.endsWith(nm)){
					display(s);
					return getSQL();
				}
			}
			setError("Can't find named SQL");
			return null;
		}
	}

	public boolean isExcelConnection(){
		String name = (String) db_names.getSelectedItem();
		if(name.equalsIgnoreCase("Excel")){
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isFileConnection(){
		String name = (String) db_names.getSelectedItem();
		if(name.equalsIgnoreCase("Delimited File")){
			return true;
		}
		else {
			return false;
		}
	}
	
	public String getDelimitor(){
		if(isFileConnection()){
			return port.getText();
		}
		else {
			return null;
		}
	}

	public File getFile(){
		if(isExcelConnection()){
			String name = url.getText();
			return new File(name);
		}
		if(isFileConnection()){
			String name = url.getText();
			return new File(name);
		}
		return null;
	}

	public String getTab(){
		if(isExcelConnection()){
			return schema.getText();
		}
		return null;
	}

	public void setError(String err){
		error.setText(err);
		error.setForeground(Color.red);
	}

	public void setConn(String con){
		for(SavedConn s: conns){
			if(s.name.equalsIgnoreCase(con)){
				display(s);
				con_names.setSelectedItem(s);
				return;
			}
		}
	}

	public void setSQL(String name){
		String nm = "<#" + name + ">";
		for(String s: sqlHist){
			if(s.endsWith(nm)){
				display(s);
			}
		}
	}

	public SavedConn writeConn(){
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter("Data/Conn/connections.xml", true));
		} catch (IOException e) {
			this.setError(e.getMessage());
			return null;
		}

		SavedConn s = new SavedConn(name.getText(), (String)db_names.getSelectedItem(),
				url.getText(), port.getText(), schema.getText(), user.getText(),
				new String(pass.getPassword()));
		int count = con_names.getItemCount();
		for(int i = 0; i < count; i++){
			if(s.equals(con_names.getItemAt(i))){
				setError("Connection Already Exists");
				out.close();
				return null;
			}
		}

		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<Connection name=\"" + s.name + "\">");
		out.println("<Database>" + s.db_type + "</Database>");
		out.println("<Url>" + s.url + "</Url>");
		out.println("<Port>" + s.port + "</Port>");
		out.println("<Schema>" + s.schema + "</Schema>");
		out.println("<User>" + s.user + "</User>");
		s.writePwd(out);
		out.println("</Connection>");
		con_names.addItem(s);
		out.close();
		return s;
	}

	public void addConn(SavedConn s){
		if(s == null){
			return;
		}
		con_names.addItem(s);
	}

	private JComboBox<String> setConnectionTypes(){
		JComboBox<String> dbTypes = new JComboBox<String>();
		dbTypes.addItem("Oracle");
		dbTypes.addItem("MySQL");
		dbTypes.addItem("PostgreSql");
		dbTypes.addItem("MSFT SQL Server");
		dbTypes.addItem("Netezza");
		dbTypes.addItem("Excel");
		dbTypes.addItem("Delimited File");
		return dbTypes;
	}

	private Connection getAccessConn() throws SQLException{
		String hostname = url.getText();
		String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + hostname + ";";
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getExcelConn() throws SQLException{
		if(sql.getText().isEmpty() && sql_name.getText().isEmpty()){
			return null;
		}
		String hostname = url.getText();
		String url = "jdbc:odbc:Driver={Microsoft Excel Driver(*.xls)};DBQ=" + hostname + ";";
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getMySQLConn() throws SQLException{
		String hostname = url.getText();
		String prt = port.getText();
		String database = schema.getText();
		String url = "jdbc:mysql" +  "://" + hostname + ":" + prt + "/" + database;
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getMSFTSQLConn() throws SQLException{
		String hostname = url.getText();
		String prt = port.getText();
		String database = schema.getText();
		String url = "jdbc:sqlserver" +  "://" + hostname + ":" + prt + ";databaseName=" + database + ";integratedSecurity=true;";
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getNetezzaConn() throws SQLException{
		try {
			Class.forName("org.netezza.Driver");
		} catch (ClassNotFoundException e) {
			setError("Netezza Driver not found");
			e.printStackTrace();
		}
		String hostname = url.getText();
		String prt = port.getText();
		String database = schema.getText();
		String url = "jdbc:netezza" +  "://" + hostname + ":" + prt + "/" + database;
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getPostgreSqlConn() throws SQLException{
		String hostname = url.getText();
		String prt = port.getText();
		String database = schema.getText();
		String url = "jdbc:postgresql" +  "://" + hostname + ":" + prt + "/" + database;
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private Connection getOracleConn() throws SQLException{
		String hostname = url.getText();
		String prt = port.getText();
		String database = schema.getText();
		String url = "jdbc:oracle:thin" +  ":@" + hostname + ":" + prt + ":" + database;
		return DriverManager.getConnection(url, user.getText(), new String(pass.getPassword()));
	}

	private ArrayList<String> getSQLHist(){
		ArrayList<String> ans = new ArrayList<String>();
		File f = new File("Data/SQL/");
		if(!f.exists()){
			f.mkdirs();
		}
		f = new File("Data/SQL/SQLhist.list");
		if(f.exists()){
			try {
				return getSQLHist(f);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
				setError(e.getMessage());
				return ans;
			}
		}
		else {
			this.setError("Could not find saved SQL History");
			return ans;
		}
	}

	private ArrayList<String> getSQLHist(File f) throws ParserConfigurationException, SAXException, IOException{
		ArrayList<String> ans = new ArrayList<String>();
		Scanner scan = new Scanner(f);
		scan.useDelimiter("(?<=\\<#[A-Za-z0-9_]{0,15}\\>)\r\n");
		while(scan.hasNext()){
			ans.add(scan.next());
		}
		scan.close();
		return ans;
	}

	private GridBagConstraints spot(double w, int x, int y, int span, int tall){
		GridBagConstraints co = new GridBagConstraints();
		co.fill = GridBagConstraints.HORIZONTAL;
		co.weightx = w;
		co.gridx = x;
		co.gridy = y;
		co.gridwidth = span;
		co.ipady = tall;
		return co;
	}

	private BorderTextBox createTextBox(String name){
		return new BorderTextBox(name);
	}

	private void getSavedConnections(){
		File f = new File("Data/Conn/");
		if(!f.exists()){
			f.mkdirs();
		}
		f = new File("Data/Conn/connections.xml");
		if(f.exists()){
			try {
				conns = getSavedConns(f);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				this.setError(e.getMessage());
			}
		}
		else {
			this.setError("Could not find saved Connections");
		}
	}

	private SavedConn[] getSavedConns(File f) throws ParserConfigurationException, SAXException, IOException{
		LinkedList<Document> xml = new LinkedList<Document>();
		Scanner scan = new Scanner(f);
		scan.useDelimiter("(?=\\<\\?)");
		while(scan.hasNext()){
			xml.add(loadXMLFromString(scan.next()));
		}
		scan.close();
		SavedConn[] ans = new SavedConn[xml.size()];
		int count = 0;
		for(Document d: xml){
			NodeList list = d.getChildNodes();
			for(int i = 0; i < list.getLength(); i++){
				Node ch = list.item(i);
				if(ch.getNodeName().equalsIgnoreCase("Connection")){
					ans[count] = new SavedConn(ch);
					count++;
				}
			}
		}
		return ans;
	}

	private static Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}
}
class AccessFilter extends FileFilter {

	TreeSet<String> extentions;

	protected AccessFilter(){
		extentions = new TreeSet<String>();
		extentions.add("mdb");
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String ext = getExtension(f);
		if(ext == null){
			return false;
		}
		else if(extentions.contains(ext)){
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "Access Database (*.mdb)";
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}
class ExcelFilter extends FileFilter {

	TreeSet<String> extentions;

	protected ExcelFilter(){
		extentions = new TreeSet<String>();
		extentions.add("xls");
		extentions.add("xlsx");
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String ext = getExtension(f);
		if(ext == null){
			return false;
		}
		else if(extentions.contains(ext)){
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "Excel File (*.xls, *.xlsx)";
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}
class TextFilter extends FileFilter {

	TreeSet<String> extentions;

	protected TextFilter(){
		extentions = new TreeSet<String>();
		extentions.add("txt");
		extentions.add("tsv");
		extentions.add("csv");
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String ext = getExtension(f);
		if(ext == null){
			return false;
		}
		else if(extentions.contains(ext)){
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "Text Files (*.txt, *.csv)";
	}

	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}

