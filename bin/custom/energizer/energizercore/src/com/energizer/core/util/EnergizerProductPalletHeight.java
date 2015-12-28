/**
 *
 */
package com.energizer.core.util;

/**
 * @author M1028886
 *
 */
public class EnergizerProductPalletHeight implements Comparable
{

	private String erpMaterialId;
	private double palletHeight;

	/**
	 * @return the erpMaterialId
	 */
	public String getErpMaterialId()
	{
		return erpMaterialId;
	}

	/**
	 * @param erpMaterialId
	 *           the erpMaterialId to set
	 */
	public void setErpMaterialId(final String erpMaterialId)
	{
		this.erpMaterialId = erpMaterialId;
	}

	/**
	 * @return the palletHeight
	 */
	public double getPalletHeight()
	{
		return palletHeight;
	}

	/**
	 * @param palletHeight
	 *           the palletHeight to set
	 */
	public void setPalletHeight(final double palletHeight)
	{
		this.palletHeight = palletHeight;
	}

	@Override
	public int compareTo(final Object o)
	{
		final double comparePalletHeight = ((EnergizerProductPalletHeight) o).getPalletHeight();

		return (int) (comparePalletHeight - this.palletHeight);
	}
}
