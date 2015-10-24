package org.finra.dave;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class MasterPanel {

	public static boolean user = true;
	public static boolean prompt = true;
	public static Map<String, String> arg;
	public static List<String> auto;
	public static List<String> join1;
	public static List<String> join2;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length > 0){
			arg = new TreeMap<String, String>();
			readCommLine(args);
			if(arg.containsKey("gui")){
				user = Boolean.parseBoolean(arg.get("gui"));
			}
			if(arg.containsKey("prompt")){
				user = Boolean.parseBoolean(arg.get("prompt"));
			}
		}
		JFrame CONTAINER = new JFrame();
		ParentPanel p = new ParentPanel();
		presets(p, arg);
		CONTAINER.add(p);
		CONTAINER.setSize(1024,768);
		CONTAINER.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		if(user){
			CONTAINER.setVisible(true);
		}
		boolean passed = true;
		if(auto != null){
			for(String s: auto){
				if(s.equals("RUN")){
					try {
						p.run();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				else if(s.startsWith("JOIN")){
					int i = Math.min(join1.size(), join2.size());
					try {
						p.join(i);
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				else if(s.startsWith("EXPORT")){
					int i = Integer.parseInt(s.replace("EXPORT", "0"));
					try {
						p.export(i);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if(s.startsWith("COMPARE")){
					String[] spl = s.split(" ");
					for(String check: spl){
						if(check.equals("COUNTS")){
							passed = passed & p.checkCounts();
						}
						else if(check.equals("DATA")){
							passed = passed & p.checkData();
						}
					}
				}
			}
		}
		if(!passed){
			System.exit(-1);
		}
	}

	private static void presets(ParentPanel p, Map<String, String> map){
		if(map == null){
			return;
		}
		Set<String> keys = map.keySet();
		for(String s: keys){
			if(s.startsWith("sql")){
				int i = Integer.parseInt(s.replace("sql", "0"));
				p.setSQL(i, map.get(s));
			}
			if(s.startsWith("conn")){
				int i = Integer.parseInt(s.replace("conn", "0"));
				p.setConn(i, map.get(s));
			}
			if(s.startsWith("join")){
				int i = Integer.parseInt(s.replace("join", "0"));
				setJoin(i, map.get(s));
			}
			if(s.equals("auto")){
				auto = new LinkedList<String>();
				String[] comm = map.get(s).split(",");
				for(String str: comm){
					auto.add(str.toUpperCase().trim());
				}
			}
			if(s.equals("file")){
				p.setFileName(map.get(s).trim());
			}
		}

	}

	private static void setJoin(int i, String list){
		String[] spl = list.split(",");
		join1 = new LinkedList<String>();
		join2 = new LinkedList<String>();
		if(i == 1){
			for(String s: spl){
				join1.add(s);
			}
		}
		else if(i == 2){
			for(String s: spl){
				join2.add(s);
			}
		}
		else {
			for(String s: spl){
				join1.add(s);
				join2.add(s);
			}
		}
	}

	private static void readCommLine(String[] args){
		String key = "";
		String value = "";
		for(String s: args){
			if(s.startsWith("-")){
				if(!key.isEmpty() && !value.isEmpty()){
					arg.put(key, value.trim());
				}
				key = s.substring(1);
				value = "";
			}
			else {
				value += s + " ";
			}
		}
		arg.put(key, value.trim());
	}

}
