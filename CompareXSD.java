/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.inpe.XSDMiner;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.domain.Modification;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
/**
 *
 * @author diego
 */
public class CompareXSD{
    
    /**
     *
     * @param repo
     * @param commit
     * @param writer
     */
    
    static Map<String, Integer> modsElements = new HashMap<String, Integer>();
    static Map<String, Integer> modsAttributes = new HashMap<String, Integer>();
    static Map<String, Integer> modsComplexTypes = new HashMap<String, Integer>();
    static Map<String, Integer> modsImports = new HashMap<String, Integer>();
    
    static long addedCount = 0;
    static long removedCount = 0;
    static long relocatedCount = 0;
    static long refactoredCount = 0;
    static Map<String, byte[]> controlFiles = new HashMap<String, byte[]>();

    public CompareXSD() {
    	
    	try
    	{
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost/xsdminer";
            Properties props = new Properties();
            props.setProperty("user","postgres");
            props.setProperty("password","070910");
            Connection conn = DriverManager.getConnection(url, props);
            
            String query = "SELECT DISTINCT project FROM files ORDER BY project";
        	PreparedStatement st = conn.prepareStatement(query);
        	ResultSet rs = st.executeQuery();
        	
        	while(rs.next())
        	{
        		System.out.println(rs.getString(1));
        	}
    	
    	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
/*	    try {
	    		String projectName = repo.getPath().substring(repo.getPath().lastIndexOf("/")+1);
	            
	            for(Modification m : commit.getModifications())
	            {
	                String fName = m.getFileName();
	                String fullName = m.getNewPath() + "/" + m.getFileName();
	                String fileExtension = fName.substring(fName.lastIndexOf(".")+1);
	
	//					if(!fName.endsWith("wsdl")) continue;
	                boolean isWSDL = fileExtension.equals("wsdl");
	                boolean isXSD  = fileExtension.equals("xsd");
	                if(!(isWSDL || isXSD))
	                	continue;
	                
	                Integer oldMods = modsCount.get(fName);
	                if(oldMods != null)
	                {
	                	modsCount.put(fName, oldMods+1);
	                }
	                else
	                {
	                	modsCount.put(fName, 0);
	                }
	
	                // CREATE PARSER AND FETCH METRICS
	                
	                InputStream input = new ByteArrayInputStream(m.getSourceCode().getBytes(StandardCharsets.UTF_8));
	                ByteArrayOutputStream outputXSD = new ByteArrayOutputStream();
	                
	                if(fileExtension.equals("wsdl"))
	                	XSDExtractor.wsdlToXSD(input, outputXSD);
	                
	                else
	                	outputXSD.write(IOUtils.toString(input).getBytes());
	                
	                String[] schemas = XSDExtractor.splitXSD(outputXSD.toString());
	                
	                XSDParser parser = new XSDParser(outputXSD.toByteArray());
	                
	                XMLUnitCompare xuc = new XMLUnitCompare();
	                
	                if(modsCount.get(fName) == 0)
	                {
	                	controlFiles.put(fName, outputXSD.toByteArray());
	                }
	                else
	                {
	                	xuc.init(controlFiles.get(fName), outputXSD.toByteArray());
	                	controlFiles.put(fName, outputXSD.toByteArray());
	                }
	                
	                if(!xuc.isEmpty())
	                {
	                	addedCount += xuc.getNumAdded();
	                	removedCount += xuc.getNumRemoved();
	                	relocatedCount += xuc.getNumRelocated();
	                	refactoredCount += xuc.getNumRefactored();
	                }
	                
	                int qElements = parser.getQuantityOfElements();
	                int qAttributes = parser.getQuantityOfAttributes();
	                int qComplexTypes = parser.getQuantityOfComplexTypes();
	                int qImports = parser.getQuantityOfImports();
	
	                // INITIALIZE COMPARISON VARIABLES (CV)
	                Integer recentElements = modsElements.get(fName),
	                	recentAttributes = modsAttributes.get(fName),
	                	recentComplexTypes = modsComplexTypes.get(fName),
	                	recentImports = modsImports.get(fName);
	                String updateElements, updateAttributes, updateComplexTypes, updateImports;
	                
	                // CHECK IF THE CURRENT COMMIT HAS THE FIRST VISITED MOD IN CURRENT FILE AND SET "update" values TO 2 IN THIS SCENARIO;
	                // IF NOT, COMPARE IT TO MOST RECENT MODIFICATION
	                
	                // 1) DO IT WITH XS:ELEMENT
	                if(recentElements == null)
	                {
	                    updateElements = "2"; // 2 MEANS FIRST FILE VISIT -- DO NOT TAKE PART IN COMPARISONS
	                }
	                else
	                {
	                	if(qElements > recentElements)
	                    {
	                        updateElements = "-1";
	                    }
	                    else if(qElements == recentElements)
	                        updateElements = "0";
	                    else
	                    {
	                        updateElements = "1";
	                    }
	                }
	                
	                // 2) DO IT WITH XS:ATTRIBUTE
	                if(recentAttributes == null)
	                {
	                    updateAttributes = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
	                }
	                else
	                {
	                	if(qAttributes > recentAttributes)
	                    {
	                        updateAttributes = "-1";
	                    }
	                    else if(qAttributes == recentAttributes)
	                        updateAttributes = "0";
	                    else
	                    {
	                        updateAttributes = "1";
	                    }
	                }
	                
	                // 3) DO IT WITH XS:COMPLEXTYPE
	                if(recentComplexTypes == null)
	                {
	                    updateComplexTypes = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
	                }
	                else
	                {
	                	if(qComplexTypes > recentComplexTypes)
	                    {
	                        updateComplexTypes = "-1";
	                    }
	                    else if(qComplexTypes == recentComplexTypes)
	                        updateComplexTypes = "0";
	                    else
	                    {
	                        updateComplexTypes = "1";
	                    }
	                }
	                
	             // 4) DO IT WITH XS:COMPLEXTYPE
	                if(recentImports == null)
	                {
	                    updateImports = "2"; // 2 MEANS HEAD -- DO NOT TAKE PART IN COMPARISONS
	                }
	                else
	                {
	                	if(qImports > recentImports)
	                    {
	                        updateImports = "-1";
	                    }
	                    else if(qImports == recentImports)
	                        updateImports = "0";
	                    else
	                    {
	                        updateImports = "1";
	                    }
	                }
	                
	                // WRITE CURRENT METRICS TO HASHMAPS
	                modsElements.put(fName, qElements);
	                modsAttributes.put(fName, qAttributes);
	                modsComplexTypes.put(fName, qComplexTypes);
	                modsImports.put(fName, qImports);
	
	                writer.write(projectName,
	                		commit.getHash(),
	                        String.valueOf(commitCount),
	                        fName,
	                        String.valueOf(modsCount.get(fName)),
	                        String.valueOf(qElements),
	                        String.valueOf(qAttributes),
	                        String.valueOf(qComplexTypes),
	                        String.valueOf(qImports),
	                        updateElements,
	                        updateAttributes,
	                        updateComplexTypes,
	                        updateImports,
	                        xuc.getNumAdded().toString(),
	                        xuc.getNumRemoved().toString(),
	                        xuc.getNumRelocated().toString(),
	                        xuc.getNumRefactored().toString()
	                );
	            } commitCount++;
	    } catch (IOException ex) {
	        Logger.getLogger(MineXSD.class.getName()).log(Level.SEVERE, null, ex);
	    } finally {
	        repo.getScm().reset();
	    }*/
    }
}