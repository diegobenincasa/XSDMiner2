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
import java.util.Properties;
import java.sql.*;

import org.apache.commons.io.IOUtils;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author diego
 */
public class MineXSD implements CommitVisitor{
    
    /**
     *
     * @param repo
     * @param commit
     * @param writer
     */
    
    public MineXSD() {

    }
    
    @Override
    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
    	
    	String projectName = "";
    	String fullName = "";
    	
        try {
        		if(!commit.getBranches().contains("master"))
        			return;

        		if(commit.isMerge())
        			return;
        		
                Class.forName("org.postgresql.Driver");
                projectName = repo.getPath().substring(repo.getPath().lastIndexOf("/")+1);
                
                for(Modification m : commit.getModifications())
                {
                    String fName = m.getFileName();
                    String addrem = "";
                    String fullNameNew = m.getNewPath();
                    String fullNameOld = m.getOldPath();
                    if(fullNameNew.equals("/dev/null"))
                    {
                    	addrem = "r";
                    	fullName = fullNameOld;
                    }
                    else
                    {
                    	fullName = fullNameNew;
                    }
                    
                    if(fullNameOld.equals("/dev/null"))
                    {
                    	 addrem = "a";
                    }

                    String fileExtension = fName.substring(fName.lastIndexOf(".")+1);

                    boolean isWSDL = fileExtension.equals("wsdl");
                    boolean isXSD  = fileExtension.equals("xsd");
                    
                    if(!(isWSDL || isXSD))
                    	continue;
                                        
                    InputStream input = new ByteArrayInputStream(m.getSourceCode().getBytes(StandardCharsets.UTF_8));
                    ByteArrayOutputStream outputXSD = new ByteArrayOutputStream();
                    
                    String[] schemas;
                    if(fileExtension.equals("wsdl"))
                    {
                    	XSDExtractor.wsdlToXSD(input, outputXSD);
                    	schemas = XSDExtractor.splitXSD(outputXSD.toString());
                    	
                    }
                    else
                    {
                    	outputXSD.write(IOUtils.toString(input).getBytes());
                    	schemas = new String[1];
                    	schemas[0] = outputXSD.toString();
                    }
                    
                    String url = "jdbc:postgresql://localhost/xsdminer";
                    Properties props = new Properties();
                    props.setProperty("user","postgres");
                    props.setProperty("password","070910");
                    Connection conn = DriverManager.getConnection(url, props);

                    int schemaCount = schemas.length;
                    
                    for(int i = 0; i < schemaCount; i++)
                    {
                        String query = "INSERT INTO files VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    	PreparedStatement st = conn.prepareStatement(query);
                        st.setInt(1, i+1);
                        st.setString(2, fullName);
                        st.setString(3, projectName);
                        st.setString(4, commit.getHash());
                        st.setTimestamp(5, new java.sql.Timestamp(commit.getDate().getTimeInMillis()));
                        st.setString(6, schemas[i]);
                        st.setString(7, addrem);
                        st.setBoolean(8, false);
                        st.executeUpdate();
                        st.close();
                    }
                    conn.close();
                }
        } catch (IOException ex) {
            Logger.getLogger(MineXSD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Failure - Project " + projectName + "; file: " + fullName);
			//e.printStackTrace();
		} finally {
            repo.getScm().reset();
        }
    }

    public String name() {
        return "files";
    }
}