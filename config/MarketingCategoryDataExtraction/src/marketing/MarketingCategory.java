package marketing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
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
		energizer.setSuperCategory(null);
		energizer.setName("null");
		//readCategory();
		//printCategories();
		
		createCategories();
		writeCategories();
	}
	
	public static void readCategory()throws Exception
	{
		Reader in = new FileReader(folderPath+"personalcare_extract1.csv");
		//Reader in = new FileReader(folderPath+"household_extract.csv");
		
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			String key=record.get(1)+"#"+record.get(2)+"#"+record.get(3)+"#"+record.get(4);
		    uniqueCategory.add(key);
		}
		System.out.println(uniqueCategory.size());
		
		for(String cat:uniqueCategory)
		{
			categoryMap.put(cat, new TreeSet<String>());
		}
		
		in = new FileReader(folderPath+"personalcare_extract1.csv");
		records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
		    String key=record.get(1)+"#"+record.get(2)+"#"+record.get(3)+"#"+record.get(4);
		    if(categoryMap.get(key)!=null)
		    {
		    	categoryMap.get(key).add(record.get(0));
		    }
		}
		
		for(String key:categoryMap.keySet())
		{
			List<String> sapcat=new ArrayList<String>();
			for(String sapcode:categoryMap.get(key))
			{
				sapcat.add(sapcode);
			}
			System.out.println(key.replaceAll("\\#", " > ")+" <--> "+StringUtils.join(sapcat.toArray(), ","));
		}
		
		
		
	}
	
	public static void printCategories()throws Exception
	{
		FileWriter writer=new FileWriter(new File(folderPath+"categoryReslved.csv"));
		for(String key:categoryMap.keySet())
		{
			List<String> sapcat=new ArrayList<String>();
			for(String sapcode:categoryMap.get(key))
			{
				sapcat.add(sapcode);
			}
			writer.write((key.replaceAll("\\#", ",")+",\""+StringUtils.join(sapcat.toArray(), ","))+"\"\n");
		}
		writer.close();
	}
	
	public static void createCategories()throws Exception
	{
		Reader in = new FileReader(folderPath+"categoryReslved.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			String level1=record.get(0);
			String level2=record.get(1);
			String level3=record.get(2);
			//String level4=record.get(3);
			
			
			if(!level1.isEmpty())
			{
				Category ref=null;
				for(Category c:energizer.getChildren())
				{
					if(c.getName().equals(level1.trim()))
					{
						ref=c;
						
					}
				}
				if(ref==null)
				{
					Category level11=new Category();
					level11.setName(level1.trim());
					level11.setSuperCategory(energizer);
					level11.setCategoryType(LEVEL1_NAME);
					energizer.getChildren().add(level11);
				}
			}
			
			if(!level2.isEmpty())
			{
				Category ref=null;
				for(Category c:energizer.getChildren())
				{
					for(Category cc:c.getChildren())
					{
						if(cc.getName().equals(level2.trim()))
						{
							ref=cc;
							
						}
					}
					
				}
				if(ref==null)
				{
					for(Category c:energizer.getChildren())
					{
						if(c.getName().equals(level1.trim()))
						{
							ref=c;
							
						}
					}
					Category level22=new Category();
					level22.setName(level2.trim());
					level22.setSuperCategory(ref);
					level22.setCategoryType(LEVEL2_NAME);
					ref.getChildren().add(level22);
		
				}
				else if(ref!=null && !ref.getSuperCategory().getName().equals(level1.trim()))
				{
					Category level11=new Category();
					level11.setName(level1.trim());
					level11.setSuperCategory(energizer);
					level11.setCategoryType(LEVEL1_NAME);
					energizer.getChildren().add(level11);
					
					Category level22=new Category();
					level22.setName(level2.trim());
					level22.setSuperCategory(level11);
					level22.setCategoryType(LEVEL2_NAME);
					level11.getChildren().add(level22);
				}
				
			}
			
			
			if(!level3.isEmpty())
			{
				Category ref=null;
				for(Category c:energizer.getChildren())
				{
					for(Category cc:c.getChildren())
					{
						for(Category ccc:cc.getChildren())
						{
							if(ccc.getName().equals(level3.trim()))
							{
								ref=ccc;
								
							}
						}
					}
					
				}
				if(ref==null)
				{
					for(Category c:energizer.getChildren())
					{
						for(Category cc:c.getChildren())
						{
							if(cc.getName().equals(level2.trim()))
							{
								ref=cc;
								
							}
						}
					}
					
					Category level33=new Category();
					level33.setName(level3.trim());
					level33.setSuperCategory(ref);
					level33.setCategoryType(LEVEL3_NAME);
					ref.getChildren().add(level33);
					
				}
				else if(ref!=null && !ref.getSuperCategory().getName().equals(level2.trim()))
				{
					Category fLevel=null;
					for(Category c:energizer.getChildren())
					{
						if(c.getName().equals(level1.trim()))
						{
							fLevel=c;
						}
					}
					Category sLevel=null;
					for(Category c:energizer.getChildren())
					{
						for(Category cc:c.getChildren())
						{
							if(cc.getName().equals(level2.trim()))
							{
								sLevel=cc;
							}
						}
					}
					
					if(fLevel!=null && sLevel!=null)
					{
						
						Category level33=new Category();
						level33.setName(level3.trim());
						level33.setSuperCategory(sLevel);
						level33.setCategoryType(LEVEL3_NAME);
						sLevel.getChildren().add(level33);
					}
					
					else if(fLevel!=null && sLevel==null)
					{
						Category level22=new Category();
						level22.setName(level2.trim());
						level22.setSuperCategory(fLevel);
						level22.setCategoryType(LEVEL2_NAME);
						fLevel.getChildren().add(level22);
						
						Category level33=new Category();
						level33.setName(level3.trim());
						level33.setSuperCategory(level22);
						level33.setCategoryType(LEVEL3_NAME);
						level22.getChildren().add(level33);
					}
					
				}
			}
			
			/*if(!level1.isEmpty())
			{
				//System.out.println(level1);
				Category cat=getCategory(energizer,level1);
				if(cat==null)
				{
					Category level11=new Category();
					level11.setName(level1.trim());
					level11.setSuperCategory(energizer);
					level11.setCategoryType(LEVEL1_NAME);
					energizer.getChildren().add(level11);
				}
				
			}
			if(!level2.isEmpty())
			{
				for(Category c:energizer.getChildren())
				{
					Category cat=getCategory(c,level2);
					if(cat==null)
					{
						Category level22=new Category();
						level22.setName(level2.trim());
						level22.setSuperCategory(c);
						level22.setCategoryType(LEVEL2_NAME);
						c.getChildren().add(level22);
						break;
					}
				}
			}
			
			if(!level3.isEmpty())
			{
				for(Category c:energizer.getChildren())
				{
					for(Category cc:c.getChildren())
					{
						Category cat=getCategory(cc,level3);
						if(cat==null)
						{
							Category level33=new Category();
							level33.setName(level3.trim());
							level33.setSuperCategory(cc);
							level33.setCategoryType(LEVEL3_NAME);
							cc.getChildren().add(level33);
							break;
						}
					}
					
				}
			}*/
			
		/*	if(!level4.isEmpty())
			{
				for(Category c:energizer.getChildren())
				{
					for(Category cc:c.getChildren())
					{
						for(Category ccc:c.getChildren())
						{
							Category cat=getCategory(ccc,level4);
							if(cat==null)
							{
								Category level44=new Category();
								level44.setName(level4);
								level44.setSuperCategory(ccc);
								ccc.getChildren().add(level44);
								break;
							}
						}
						
					}
					
				}
			}*/
			
			
		}
	}
	
	public static Category getCategory(Category cat,String name)
	{
		Category retVal=null;
		if(cat.getName().equals(name))
		{
			return cat;
		}
		else if(cat.getSuperCategory()!=null && cat.getSuperCategory().getName().equals(name))
		{
			return cat.getSuperCategory();
		}
		else
		{
			for(Category c:cat.getChildren())
			{
				if(c.getName().equals(name))
				{
					Category cc=getCategory(c,name);
					if(cc!=null)
					{
						retVal=cc;
						break;
					}
					
				}
			}
		}
		return retVal;
	}
	
	public static void writeCategories()throws Exception
	{
		FileWriter writer=new FileWriter(new File(folderPath+"categoryHierarchy.csv"));
		Set<String> lines=getCategoryLines(energizer);
		for(String line:lines)
		{
			writer.write(line);
			System.out.print(line);
		}
		writer.close();
	}
	
	public static Set<String> getCategoryLines(Category c)
	{
		Set<String> lines=new TreeSet<String>();
		lines.add(getCategoryLine(c));
		for(Category cc:c.getChildren())
		{
			for(String line:getCategoryLines(cc))
			lines.add(line);
		}
		return lines;
	}
	
	public static String getCategoryLine(Category cat)
	{
		//System.out.println(cat.getName());
		String retVal="";
		if(cat.getSuperCategory()==null && cat.getName().equalsIgnoreCase("null"))
		{
			String catName=cat.getName();
			catName=(catName.equalsIgnoreCase("null"))?"":catName;
			retVal=catName+","+catName+",,"+cat.getCategoryType()+"\n";
		}
		else
		{
			
			String supCat=cat.getSuperCategory().getName();
			supCat=(supCat.equalsIgnoreCase("null"))?"":supCat;
			String catName=cat.getName();
			catName=(catName.equalsIgnoreCase("null"))?"":catName;
			retVal=retVal+getSuperCategoryNames(cat)+","+catName+","+getSuperCategoryNames(cat.getSuperCategory())+","+supCat+","+cat.getCategoryType()+"\n";

			
		}
		
		return retVal;
	}
	
	public static String getSuperCategoryNames(Category c)
	{
		
		String retVal=c.getName();
		
		if(c.getSuperCategory()!=null && !c.getSuperCategory().getName().equalsIgnoreCase("null"))
		{
			
			retVal=getSuperCategoryNames(c.getSuperCategory())+"-"+retVal;
		}
		if(retVal.equalsIgnoreCase("null"))
		{
			retVal="";
		}
		return retVal;
	}
	
}
	