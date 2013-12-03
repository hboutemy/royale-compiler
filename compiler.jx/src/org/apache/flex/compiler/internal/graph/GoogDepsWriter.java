package org.apache.flex.compiler.internal.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.problems.FileNotFoundProblem;

import com.google.common.io.Files;

public class GoogDepsWriter {

	public GoogDepsWriter(File outputFolder, String mainClassName, JSGoogConfiguration config)
	{
		this.outputFolderPath = outputFolder.getAbsolutePath();
		this.mainName = mainClassName;
		otherPaths = config.getSDKJSLib();
	}
	
	private ProblemQuery problems;
	private String outputFolderPath;
	private String mainName;
	private List<String> otherPaths;
	private boolean problemsFound = false;
	
	private HashMap<String,GoogDep> depMap = new HashMap<String,GoogDep>();
	
	public ArrayList<String> getListOfFiles() throws InterruptedException
	{
		buildDB();
		ArrayList<GoogDep> dps = sort(mainName);
		ArrayList<String> files = new ArrayList<String>();
		for (GoogDep gd : dps)
		{
			files.add(gd.filePath);
		}
		return files;
	}
	
	public boolean generateDeps(ProblemQuery problems, StringBuilder depsFileData) throws InterruptedException, FileNotFoundException
	{
	    problemsFound = false;
	    this.problems = problems;
		buildDB();
		ArrayList<GoogDep> dps = sort(mainName);
		String outString = "// generated by FalconJS" + "\n";
		int n = dps.size();
		for (int i = n - 1; i >= 0; i--)
		{
			GoogDep gd = dps.get(i);
			if (!isGoogClass(gd.className)) 
			{
			    String s = "goog.addDependency('";
	            s += relativePath(gd.filePath);
	            s += "', ['";
	            s += gd.className;
	            s += "'], [";
	            s += getDependencies(gd.deps);
	            s += "]);\n";
	            outString += s;
			}
		}
		depsFileData.append(outString);
		return !problemsFound; 
	}
	
	private boolean isGoogClass(String className)
	{
	    return className.startsWith("goog.");
	}
	
	private void buildDB()
	{
		addDeps(mainName);
	}
	
    public ArrayList<String> filePathsInOrder = new ArrayList<String>();
    
    private HashMap<String, GoogDep> visited = new HashMap<String, GoogDep>();
    
	private ArrayList<GoogDep> sort(String rootClassName)
	{
		ArrayList<GoogDep> arr = new ArrayList<GoogDep>();
		GoogDep current = depMap.get(rootClassName);
		sortFunction(current, arr);
		return arr;
	}
	
	private void sortFunction(GoogDep current, ArrayList<GoogDep> arr)
	{
		visited.put(current.className, current);
		
		filePathsInOrder.add(current.filePath);
        System.out.println("Dependencies calculated for '" + current.filePath + "'");

		ArrayList<String> deps = current.deps;
		for (String className : deps)
		{
			if (!visited.containsKey(className) && !isGoogClass(className))
			{
				GoogDep gd = depMap.get(className);
				sortFunction(gd, arr);
			}
		}
		arr.add(current);
	}
	
	private void addDeps(String className)
	{
		if (depMap.containsKey(className) || isGoogClass(className))
			return;
		
		// build goog dependency list
		GoogDep gd = new GoogDep();
		gd.className = className;
		gd.filePath = getFilePath(className);
		depMap.put(gd.className, gd);
		ArrayList<String> deps = getDirectDependencies(gd.filePath);
		
		gd.deps = new ArrayList<String>();
		ArrayList<String> circulars = new ArrayList<String>();
		for (String dep : deps)
		{
		    if (depMap.containsKey(dep) && !isGoogClass(dep))
		    {
		        circulars.add(dep);
		        continue;
		    }
			gd.deps.add(dep);
		}
        for (String dep : deps)
        {
            addDeps(dep);
        }
		if (circulars.size() > 0)
		{
		    // remove requires that would cause circularity
		    try
            {
                List<String> fileLines = Files.readLines(new File(gd.filePath), Charset.defaultCharset());
                ArrayList<String> finalLines = new ArrayList<String>();
                
                String inherits = getBaseClass(fileLines, className);
                
                for (String line : fileLines)
                {
                    int c = line.indexOf("goog.require");
                    if (c > -1)
                    {
                        int c2 = line.indexOf(")");
                        String s = line.substring(c + 14, c2 - 1);
                        if (circulars.contains(s) && !s.equals(inherits))
                            continue;
                    }
                    finalLines.add(line);
                }
                File file = new File(gd.filePath);  
                PrintWriter out = new PrintWriter(new FileWriter(file));  
                for (String s : finalLines)
                {
                    out.println(s);
                }
                out.close();
                    
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
		    
		}
	}
	
	String getBaseClass(List<String> lines, String className)
	{
	    int n = lines.size();
	    for (int i = 0; i < n; i++)
	    {
	        String line = lines.get(i);
	        int c2;
	        int c = line.indexOf("goog.inherits");
	        if (c > -1)
	        {
	            String inheritLine = ""; 
                while (true)
                {
                    inheritLine += line;
                    c2 = line.indexOf(")");
                    if (c2 > -1)
                        break;
                    else
                    {
                        i++;
                        line = lines.get(i);
                    }
                }
	            c = inheritLine.indexOf(",");
                c2 = inheritLine.indexOf(")");
                return inheritLine.substring(c + 1, c2).trim();            
	        }
	    }
	    return null;
	}
	
	String getFilePath(String className)
	{
	    String fn;
	    File destFile;
	    File f;
	    
		String classPath = className.replace(".", File.separator);
		
        fn = outputFolderPath + File.separator + classPath + ".js";
        f = new File(fn);
        if (f.exists())
        {
            return fn;
        }
        
        for (String otherPath : otherPaths)
        {
    		fn = otherPath + File.separator + classPath + ".js";
    		f = new File(fn);
    		if (f.exists())
    		{
    			fn = outputFolderPath + File.separator + classPath + ".js";
    			destFile = new File(fn);
    			// copy source to output
    			try {
    				FileUtils.copyFile(f, destFile);
    			} catch (IOException e) {
    				System.out.println("Error copying file for class: " + className);
    			}
    			return fn;
    		}
        }
        
		System.out.println("Could not find file for class: " + className);
		problems.add(new FileNotFoundProblem(className));
		problemsFound = true;
		return "";
	}
	
	private ArrayList<String> getDirectDependencies(String fn)
	{
		ArrayList<String> deps = new ArrayList<String>();
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(fn);
			Scanner scanner = new Scanner(fis, "UTF-8");
			while (scanner.hasNextLine())
			{
				String s = scanner.nextLine();
				if (s.indexOf("goog.inherits") > -1)
					break;
				int c = s.indexOf("goog.require");
				if (c > -1)
				{
					int c2 = s.indexOf(")");
					s = s.substring(c + 14, c2 - 1);
					deps.add(s);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return deps;
	}
	
	private String getDependencies(ArrayList<String> deps)
	{
		String s = "";
		for (String dep : deps)
		{
			if (s.length() > 0)
			{
				s += ", ";
			}
			s += "'" + dep + "'";			
		}
		return s;
	}

	String relativePath(String path)
	{
        if (path.indexOf(outputFolderPath) == 0)
        {
            path = path.replace(outputFolderPath, "../../..");
        }
        else
        {
    	    for (String otherPath : otherPaths)
    	    {
        		if (path.indexOf(otherPath) == 0)
        		{
        			path = path.replace(otherPath, "../../..");
        			
        		}
    	    }
        }
		// paths are actually URIs and always have forward slashes
		path = path.replace('\\', '/');
		return path;
	}
	private class GoogDep
	{
		public String filePath;
		public String className;
		public ArrayList<String> deps;
		
	}
}
