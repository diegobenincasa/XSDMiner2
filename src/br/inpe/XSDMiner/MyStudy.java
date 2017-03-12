package br.inpe.XSDMiner;

import br.com.metricminer2.MetricMiner2;
import br.com.metricminer2.RepositoryMining;
import br.com.metricminer2.Study;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.scm.GitRepository;
import br.com.metricminer2.scm.commitrange.Commits;

public class MyStudy implements Study {

    /**
     * @param args the command line arguments
     */
    String project =
    		"Gemma";
            //"bearded-archer";
    		//"animated-batman";
    		//"zimbra_wsdl";
    		//"BPEL-splitting";
            //"carbon-identity";
    		//"battleship";
    		//"CONNECT";
    		//"carbon-identity-framework";
    		//"zend-soap";
    		//"sipXtapi";
    		
    		//"opennms";
            //"opennms-mirror";
            //"SOCIETIES-Platform";
            //"spring-ws";
            //"XeroAPI-Schemas";
            //"xwiki-platform";
    
    String projectDir = "/home/diego/github/" + project;
    String folder = "/home/diego/github";
    //String projectDir = "C:/github/" + project;
    //String projectDir = "c:/" + project;
    
    String output = "/home/diego/Área de Trabalho/metricminer/mm_output/valendo/" + project + ".csv";
    //String output = "c:/Users/Diego/Desktop/" + project + ".csv";
    
    public static void main(String[] args) {
        new MetricMiner2().start(new MyStudy());
    }
    
    @Override
    public void execute() {
        new RepositoryMining()
        		.in(GitRepository.allProjectsIn(folder))
//        		.in(GitRepository.singleProject(projectDir))
                .through(Commits.all())
                .withThreads(8)
                .process(new MineXSD(), new CSVFile(output))
                .mine();
    }
}
