package eg.edu.alexu.csd.oop.db;
import java.io.File;
import java.sql.SQLException;
public class InsertParser extends MyParser{
	private String curDb;
	private String table_name;
	private String[]cols=null,values,attr;
	private String[][]result;
	public InsertParser (String db){
		curDb = db;
	}
	private String[][] check1() throws SQLException{
		if (values.length!=attr.length) 
			throw new SQLException("Error :table "+table_name+" has "+attr.length+" columns but "+values.length+" are supplied");
		for (int i=0;i<values.length;i++){
			String[]p=attr[i].split("\\;");
			char c1=values[i].charAt(0);
			char c2=values[i].charAt(values[i].length()-1);
			boolean stringerror=true;
			if (c1=='"'&&c2=='"' || c1=='\''&&c2=='\'' && values.length>=3){
				stringerror=false;
				values[i]=values[i].substring(1,values[i].length()-1);
			}
			if (p[1].toLowerCase().equals("int")){
				try{
					Integer.parseInt(values[i]);
				}catch(NumberFormatException ex){
					throw new SQLException("Error: Not Valid INT Field");
				}
			}else if (stringerror){
				throw new SQLException("Error: Not Valid VarChar Field");
			}
		}
		String[][]v=new String[1][values.length];
		v[0]=values;
		return v;
	}
	private String[][] check2 () throws SQLException{
		if (!(cols.length==values.length&&cols.length==attr.length))
			throw new SQLException("Error: number of columns or values isn't correct");
		String[][]v=new String[1][attr.length];
		for (int i=0;i<cols.length;i++){
			boolean stringerror=true;
			char c1=values[i].charAt(0);
			char c2=values[i].charAt(values[i].length()-1);
			if (c1=='"'&&c2=='"' || c1=='\''&&c2=='\'' && values[i].length()>=3){
				stringerror=false;
				values[i]=values[i].substring(1,values[i].length()-1);
			}
			boolean found = false;
			for (int j=0;j<attr.length&&!found;j++){
				String[]p=attr[j].split("\\;");
				if (cols[i].toLowerCase().equals(p[0].toLowerCase())){
					if (p[1].toLowerCase().equals("int")){
						try {
							Integer.parseInt(values[i]);
						}
						catch(NumberFormatException ex){
							throw new SQLException("Error: Not Valid INT Field");
						}
						// varchar
					}else if(stringerror){
						throw new SQLException("Error: Not Valid VarChar Field");
					}
					found=true;
					v[0][j]=values[i];
				}
			}
			if (!found){
				throw new SQLException("Error: No Such Column "+cols[i]);
			}
		}
		return v;
	}
	@Override
	public Object parse(String query)throws SQLException{
		String reg1 = "(\\s*[Ii][Nn][Ss][Ee][Rr][Tt]\\s+[Ii][Nn][Tt][Oo]\\s+)" // INSERT INTO 1
				   + "(.*\\S)"  // table name 2
			       + "(\\s+[Vv][Aa][Ll][Uu][Ee][Ss]\\s*)" // VALUES 3
			       + "(\\(.+\\))" // values 4
			       + "(\\s*)" // space 5
				;
		String reg2= "(\\s*[Ii][Nn][Ss][Ee][Rr][Tt]\\s+[Ii][Nn][Tt][Oo]\\s+)" // INSERT INTO 1
				   + "(.*\\S)"  // table name 2
				   + "(\\s*)" // space 3 
			       + "(\\(.+\\))" // columns name (option)4
			       + "(\\s*[Vv][Aa][Ll][Uu][Ee][Ss]\\s*)" // VALUES 5
			       + "(\\(.+\\))" // values 6
			       + "(\\s*)" // space 7
				;
		if (regexChecker(reg2,query,query.length())){
			table_name = query.replaceAll(reg2,"$2");
			String colsNames = query.replaceAll(reg2,"$4").replaceAll("^(\\()|(\\))$","").
					replaceAll("^(\\s*)|(\\s*)$","").replaceAll("\\s*,\\s*",",");
			cols = colsNames.split("\\,");
			String valuesNames = query.replaceAll(reg2,"$6").replaceAll("^(\\()|(\\))$","").
					replaceAll("^(\\s*)|(\\s*)$","").replaceAll("\\s*,\\s*",",");
			values = valuesNames.split("\\,");
			// validation of values ' ' or " " or nothing
		}
		else if (regexChecker(reg1,query,query.length())){
			table_name = query.replaceAll(reg1,"$2");
			String valuesNames = query.replaceAll(reg1,"$4").replaceAll("^(\\()|(\\))$","").
					replaceAll("^(\\s*)|(\\s*)$","").replaceAll("\\s*,\\s*",",");
			values = valuesNames.split("\\,");
		}
		else {
			throw new SQLException("SQL Syntax Error: Not Valid SQL");
		}
		XmlReader xmlr=null;
		try{
			xmlr = new XmlReader(curDb+File.separator+table_name+".xml");
		}catch(RuntimeException ex){
			throw new SQLException("Error :Not Found Such Table "+table_name);
		}
		attr=xmlr.getAtrr().split("\\,");
		String[][]v;
		if (cols==null){
			v=check1();
		}
		else {
			v=check2();
		}
		result = new String [xmlr.getEntries().length+1][];
        System.arraycopy(xmlr.getEntries() , 0, result, 0, xmlr.getEntries().length);
        System.arraycopy(v, 0, result, xmlr.getEntries().length, v.length);
        new XmlWriter(new File(curDb+File.separator+table_name+".xml"),result,xmlr.getAtrr(),table_name);
        return 1;
	}
}
