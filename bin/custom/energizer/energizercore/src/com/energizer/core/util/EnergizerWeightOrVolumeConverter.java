/**
 * 
 */
package com.energizer.core.util;

/**
 * @author Bivash Pandit
 * 
 */
public class EnergizerWeightOrVolumeConverter
{
	/**
	 * Cubic cm - CCM = 0.0000001 M3, Meter cube - M3 = 1 M3. Cubic decimeter - CD3 = 0.001 M3, HectoLitre - HL = 0.1 M3,
	 * Cubic ft - FT3 = 0.0283168 M3
	 * 
	 * KG = 1KG, G = 0.001 KG, LB = 0.453592 KG
	 */

	private static String CUBIC_CM = "CCM";
	private static String CUBIC_DECEMETER = "CD3";
	private static String HECTO_LITRE = "HL";
	private static String CUBIC_FEET = "FT3";
	private static String GRAM = "G";
	private static String LB = "LB";
	private static String KG = "KG";
	private static String METER_CUBE = "M3";


	public static double getConversionValue(final String unit, final double value)
	{
		double convertedValue = 1;
		if (unit.equalsIgnoreCase(CUBIC_CM))
		{
			convertedValue = convertCubicCMtoMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(CUBIC_DECEMETER))
		{
			convertedValue = convertCubicDecimeterToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(HECTO_LITRE))
		{
			convertedValue = convertHectoLitreToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(CUBIC_FEET))
		{
			convertedValue = convertCubicFtToMeterCube(value);
		}
		else if (unit.equalsIgnoreCase(GRAM))
		{
			convertedValue = convertGramToKG(value);
		}
		else if (unit.equalsIgnoreCase(LB))
		{
			convertedValue = convertLBtoKG(value);
		}
		else if (unit.equalsIgnoreCase(KG))
		{
			convertedValue = value;
		}
		else if (unit.equalsIgnoreCase(METER_CUBE))
		{
			convertedValue = value;
		}

		return convertedValue;
	}


	private static double convertGramToKG(final double gram)
	{
		final double valueInKg = gram * 0.001;
		return valueInKg;
	}

	private static double convertLBtoKG(final double lb)
	{
		final double valueInKg = lb * 0.453592;
		return valueInKg;

	}

	private static double convertCubicCMtoMeterCube(final double CubicCM)
	{
		final double valueInMeterCube = CubicCM * 0.0000001;
		return valueInMeterCube;

	}

	private static double convertCubicDecimeterToMeterCube(final double cubicDecimeter)
	{
		final double valueInMeterCube = cubicDecimeter * 0.001;
		return valueInMeterCube;
	}

	private static double convertHectoLitreToMeterCube(final double hectoLitre)
	{
		final double valueInMeterCube = hectoLitre * 0.01;
		return valueInMeterCube;
	}

	private static double convertCubicFtToMeterCube(final double cubicFt)
	{
		final double valueInMeterCube = cubicFt * 0.0283168;
		return valueInMeterCube;
	}

}
