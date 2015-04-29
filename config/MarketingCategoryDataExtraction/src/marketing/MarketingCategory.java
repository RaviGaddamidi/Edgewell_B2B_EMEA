package marketing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class MarketingCategory {
	
	private static Set<String> uniqueCategory=new TreeSet<String>();
	private static Map<String,Set<String>> categoryMap=new HashMap<String,Set<String>>();
	private static Category energizer=new Category();
	private static final String folderPath="resources\\";
	private static final String LEVEL1_NAME="Product Segment";
	private static final String LEVEL2_NAME="Brand";
	private static final String LEVEL3_NAME="Sub-Brand";
	
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
			readWriteCategories("en");
			readWriteCategories("es");
	}
	
	
	private static void readWriteCategories(String language)throws Exception
	{
		//Reader in = new FileReader(folderPath+"personalcare_extract1.csv");
		Reader in = new FileReader(folderPath+"household_extract.csv");
		
		Set<String> categories=new HashSet<String>();
		
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			
			String level1=record.get(1).trim();
			String level2=record.get(2).trim();
			String level3=record.get(3).trim();
			if(!level1.isEmpty())
			{
				String rec=level1+","+level1+",,"+","+LEVEL1_NAME+","+language+"\n";
				categories.add(rec);
				
			}
			if(!level1.isEmpty() && !level2.isEmpty())
			{
				String rec=level1+"-"+level2+","+level2+","+level1+","+level1+","+LEVEL2_NAME+","+language+"\n";
				categories.add(rec);
				
			}
			if(!level1.isEmpty() && !level2.isEmpty() &&  !level3.isEmpty())
			{
				String rec=level1+"-"+level2+"-"+level3+","+level3+","+level1+"-"+level2+","+level2+","+LEVEL3_NAME+","+language+"\n";
				categories.add(rec);
				
			}
			
			
		}
		
		FileWriter writer=new FileWriter(new File(folderPath+"categoryHierarchy_"+language+".csv"));
		writer.write("CategoryCode,Category,SuperCategoryCode,SuperCategory,CategoryType,Language\n");
		for(String str:categories)
		{
			writer.write(str);
			System.out.print(str);
		}
		
		writer.close();
		in.close();
	}
	
}
	