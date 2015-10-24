package org.finra.dave;
import java.io.PrintWriter;
import java.math.BigInteger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SavedConn {

	String db_type;
	String name;
	String url;
	String port;
	String schema;
	String user;
	String pass;
	
	public SavedConn(Node n){
		name = n.getAttributes().getNamedItem("name").getNodeValue();
		NodeList list = n.getChildNodes();
		for(int i = 0; i < list.getLength(); i++){
			Node ch = list.item(i);
			if(ch.getNodeName().equalsIgnoreCase("URL")){
				url = ch.getTextContent().trim();
			}
			else if(ch.getNodeName().equalsIgnoreCase("PORT")){
				port = ch.getTextContent().trim();
			}
			else if(ch.getNodeName().equalsIgnoreCase("DATABASE")){
				db_type = ch.getTextContent().trim();
			}
			else if(ch.getNodeName().equalsIgnoreCase("SCHEMA")){
				schema = ch.getTextContent().trim();
			}
			else if(ch.getNodeName().equalsIgnoreCase("USER")){
				user = ch.getTextContent().trim();
			}
			if(ch.getNodeName().equalsIgnoreCase("PASSWORD")){
				String enc = ch.getTextContent().trim();
				String pwd = ch.getAttributes().getNamedItem("pwd").getNodeValue();
				if(enc.isEmpty()){
					pass = "";
				}
				else {
					pass = decrypt(enc, flip(pwd));
				}
			}
		}
	}
	public SavedConn(String n, String d, String u, String p, String s, String use, String pas){
		name = n.trim();
		db_type = d.trim();
		url = u.trim();
		port = p.trim();
		schema = s.trim();
		user = use.trim();
		pass = pas.trim();
	}
	
	public String toString(){
		return name;
	}
	
	public void writePwd(PrintWriter out){
		String p = generate(100);
		String enc = encrypt(pass.toCharArray(), p);
		out.println("<Password pwd=\""+ flip(p) + "\">" + enc + "</Password>");
	}
	
	public boolean equals(SavedConn s){
		if(!name.equals(s.name))
			return false;
		else if(!url.equals(s.url))
			return false;
		else if(!port.equals(s.port))
			return false;
		else if(!schema.equals(s.schema))
			return false;
		else if(!user.equals(s.user))
			return false;
		else if(!pass.equals(s.pass))
			return false;
		return true;
	}
	
	private static String generate(int digits){
		String ans = "";
		for(int i = 0; i < digits; i++){
			int c = (int) (Math.random() * 128);
			if(c >= 32 && c != 60 && c != 62 && c != 34 && c != 38){
				ans += (char)c;
			}
		}
		return ans;
	}
	
	private static int toInt(char first, char second){
		int a = (int)(first);
		a = a << 8;
		a = a + (int)(second);
		return a;
	}
	
	private static String encrypt(char[] pln, String pass){
		if(pass.length() <= 0){
			return "";
		}
		if(pln.length == 0){
			return "";
		}
		
		String[] pwd = getObscuredPwd(pass.toCharArray());
		String pass1 = runEncrypt(pln, pwd[0].toCharArray());
		String pass2 = runEncrypt(pwd[1].toCharArray(), pwd[0].toCharArray());
		BigInteger x = new BigInteger(pass1, 16);
		BigInteger y = new BigInteger(pass2, 16);
		return x.multiply(y).toString(16);
	}
	
	private static String runEncrypt(char[] pln, char[] pwd){
		String cyp = "";
		int index = 0;
		for(char c: pln){
			int temp = c + pwd[index];
			String hold = Integer.toHexString(temp);
			if(hold.length() == 1){
				cyp += "0" + hold;
			}
			else {
				cyp += hold;
			}
			index = (index + 1) % pwd.length;
		}
		return cyp;
	}
	
	private static String[] getObscuredPwd(char[] pwd){
		char one = pwd[0];
		char two = pwd[1];
		char first = pwd[((int)one) % pwd.length];
		char second = pwd[((int)two) % pwd.length];
		int mask = toInt(first, second);
		return mask(pwd, mask);
	}
	
	private static String[] mask(char[] pass, int mask){
		int x = 1;
		String[] pwds = new String[2];
		for(char c: pass){
			if((x & mask) > 0){
				pwds[0] += c;
			}
			else {
				pwds[1] += c;
			}
			x = x << 1;
			if(x == 0){
				x = 1;
			}
		}
		return pwds;
	}
	
	private static String decrypt(String num, String pass){
		if(pass.length() <= 0){
			return "";
		}
		String[] pwd = getObscuredPwd(pass.toCharArray());
		String pass2 = runEncrypt(pwd[1].toCharArray(), pwd[0].toCharArray());
		BigInteger y = new BigInteger(pass2, 16);
		BigInteger x = new BigInteger(num, 16);
		
		BigInteger cyp2 = x.divide(y);
		String cyp = cyp2.toString(16);
		
		return runDecrypt(cyp, pwd[0].toCharArray());
	}
	
	private static String runDecrypt(String temp, char[] pwd){
		String plain = "";
		int index = 0;
		while(temp.length() > 1){
			String hold = temp.substring(0, 2);
			char pln = (char) Integer.parseInt(hold, 16);
			plain += (char)(pln - pwd[index]);
			index = (index + 1) % pwd.length;
			temp = temp.substring(2);
		}
		return plain;
	}
	
	private static String flip(String s){
		char[] hold = s.toCharArray();
		String ans = "";
		for(char c: hold){
			ans = c + ans;
		}
		return ans;
	}
}
