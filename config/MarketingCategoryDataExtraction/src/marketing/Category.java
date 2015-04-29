package marketing;

import java.util.Set;
import java.util.TreeSet;

public class Category implements Comparable<Category>{
	
	private Category superCategory;
	private String name;
	private Set<Category> children;
	private String categoryType;
	
	
	
	
	/**
	 * @return the categoryType
	 */
	public String getCategoryType() {
		return categoryType;
	}



	/**
	 * @param categoryType the categoryType to set
	 */
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}



	public Category() {
		super();
		children=new TreeSet<Category>();
	}
	
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * @return the superCategory
	 */
	public Category getSuperCategory() {
		return superCategory;
	}
	/**
	 * @param superCategory the superCategory to set
	 */
	public void setSuperCategory(Category superCategory) {
		this.superCategory = superCategory;
	}
	/**
	 * @return the children
	 */
	public Set<Category> getChildren() {
		return children;
	}
	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<Category> children) {
		this.children = children;
	}



	@Override
	public int compareTo(Category o) {
		// TODO Auto-generated method stub
		if(this.getName().equals(o.getName()))
		return 0;
		else
			return -1;
	}
	
	

}
