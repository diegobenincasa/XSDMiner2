package br.inpe.XSDMiner;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;


public class XSDExtractor {

	public static void wsdlToXSD(InputStream is, OutputStream os) {
		try{
			String input = IOUtils.toString(is);
			int index1 = input.indexOf("<wsdl:types>");
			int index2 = input.lastIndexOf("</wsdl:types");
			String xsd = "";
			
			if(index1 > -1 && index2 > -1)
				xsd = input.substring(index1+12, index2);
			
			else
			{
				index1 = input.indexOf("<types>");
				index2 = input.indexOf("</types>");
				if(index1 > -1 && index2 > -1)
				{
					xsd = input.substring(index1+7, index2);
				}
			}

//			System.out.println(xsd);
			os.write(xsd.getBytes());
		}
		catch(Exception e){
			System.out.println("No types found!");
		}
	}
	
	public static String[] splitXSD(String input)
	{
		String[] output;
		String intInput = input.replaceAll("\n", "");
		String int2Input = intInput.replaceAll("\\s+", " ");
		String newInput = int2Input.replaceAll("(?m)^\\s+$", "");
		
		int fpos = newInput.indexOf("<");
		int lpos = newInput.indexOf(">");
		
		String test = newInput.substring(fpos, lpos);
		String[] test2 = test.split(" ");
		String schTagName = test2[0];
		String schemaTagName = schTagName.substring(1);
		
		String closeTag = "</" + schemaTagName + ">";
		String regex = "(?<=" + closeTag + ")";
		output = newInput.split(regex);
		
		return prepareXSD(output);
	}
	private static String[] prepareXSD(String[] input)
	{
		String[] preparedXSD = new String[input.length-1];
		int counter = 0;
		for(String xsd : input)
		{
			if(counter == input.length-1)
				continue;
			String newXSD = "";
			int fpos = xsd.indexOf("<");
			int lpos = xsd.indexOf(">");
			String firstline = xsd.substring(fpos, lpos+1);
			String restOfFile = xsd.substring(lpos+2);
			String[] firstlineSplitted = firstline.split(" ");
			String[] newFirstlineSplitted = new String[firstlineSplitted.length];
			String newFirstLine = "";
			boolean namespaceDef = false;
			for(int i = 0; i < firstlineSplitted.length; i++)
			{
				if(firstlineSplitted[i].contains("http://www.w3.org/2001/XMLSchema"))
				{
					namespaceDef = true;
//					preparedXSD = input;
					break;
				}
			}
			if(!namespaceDef)
			{
				String schemaNS = firstlineSplitted[0].substring(1);
				String[] namespace = schemaNS.split(":");
				
				if(!namespace[0].isEmpty())
				{
					String ns = namespace[0];
					newFirstlineSplitted[0] = "<" + schemaNS 
							+ " xmlns:" + ns + "=" + "\"http://www.w3.org/2001/XMLSchema\"";
				}
				for(int i = 1; i < firstlineSplitted.length; i++)
				{
					newFirstlineSplitted[i] = firstlineSplitted[i];
				}
			
				for(int i = 0; i < newFirstlineSplitted.length; i++)
				{
					if(i != newFirstlineSplitted.length - 1)
						newFirstLine += newFirstlineSplitted[i] + " ";
					else
						newFirstLine += newFirstlineSplitted[i];
				}
				
				newXSD = newFirstLine + restOfFile;
				preparedXSD[counter] = newXSD;
			}
			else
			{
				preparedXSD[counter] = xsd;
			}


			counter++;
		}
		
		return preparedXSD;
	}
}