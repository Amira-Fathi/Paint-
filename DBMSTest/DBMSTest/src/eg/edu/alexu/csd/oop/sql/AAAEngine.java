package eg.edu.alexu.csd.oop.sql;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import eg.edu.alexu.csd.oop.db.DataBase;
import eg.edu.alexu.csd.oop.db.Parser;

public class AAAEngine implements DataBase{
	private String curdb=null;
	@Override
	// open a data base
	public String createDatabase(String databaseName, boolean dropIfExists) {
		try {
			if (dropIfExists)
				executeStructureQuery("DROP DATABASE "+databaseName);
			if (executeStructureQuery("CREATE DATABASE "+databaseName)){
				curdb=databaseName;
				File f = new File (databaseName);
				f.mkdir();
				return f.getAbsolutePath();
			}
		}
		catch(SQLException ex){}
		return null;
	}
	@Override
	public boolean executeStructureQuery(String query) throws SQLException {
		if (query==null)
				throw new SQLException("Null Query");
		if (curdb==null) throw new SQLException("No Such DataBase is created");
		return (boolean) (new StructureQueryParser(curdb)).parse(query);
	}
	@Override
	public Object[][] executeQuery(String query) throws SQLException {
		if (query==null)
			throw new SQLException("Null Query");
		if (curdb==null) throw new SQLException("No Such DataBase is created");
		return (String[][]) (new SelectParser(curdb)).parse(query);
	}
	@Override
	// insert 
	// delete 
	// update 
	public int executeUpdateQuery(String query) throws SQLException {
		if (query==null)
			throw new SQLException("Null Query");
		if (curdb==null) throw new SQLException("No Such DataBase is created");
		try{
			String s = query.split("\\s+")[0].toLowerCase();
			s = (s.charAt(0)+"").toUpperCase()+s.substring(1,s.length());
				return (int)((Parser)(Class.forName("eg.edu.alexu.csd.oop.sql."+s+"Parser")).
						getConstructor(String.class).newInstance(curdb)).parse(query);
		}
		catch(ClassNotFoundException|InstantiationException|IllegalAccessException|StringIndexOutOfBoundsException|
		IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException ex){
			throw new SQLException("update reflection");
		}
	}
}
