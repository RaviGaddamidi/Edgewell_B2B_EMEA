/**
 *
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.b2bacceleratorfacades.company.refactoring.impl.DefaultB2BCostCenterFacade;
import de.hybris.platform.b2bacceleratorfacades.order.B2BCheckoutFacade;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCostCenterData;
import de.hybris.platform.commercefacades.order.converters.populator.CartPopulator;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.data.MetricUnitData;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.MetricUnitModel;



public class EnergizerCartPopulator extends CartPopulator
{
	private Converter<MetricUnitModel, MetricUnitData> metricUnitConverter;
	private Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter;

	private Converter<AddressModel, AddressData> energizerAddressConverter;

	@Resource(name = "DefaultB2BCostCenterFacade")
	private DefaultB2BCostCenterFacade defaultB2BCostCenterFacade;

	@Resource
	private B2BCheckoutFacade b2bCheckoutFacade;


	@Override
	public void populate(final CartModel source, final CartData target)
	{
		Map<Integer, List<String>> palStackData = new TreeMap<Integer, List<String>>();
		final Map<Integer, List<String>> sortedPalStackData = new TreeMap<Integer, List<String>>();
		super.populate(source, target);
		if (source.getB2bUnit() != null)
		{
			target.setB2bUnit(getEnergizerB2BUnitConverter().convert(source.getB2bUnit()));
		}
		target.setOrderType(source.getOrderType());
		target.setShippingPointId(source.getShippingPointId());
		target.setRequestedDeliveryDate(source.getRequestedDeliveryDate());
		if (source.getOrderVolume() != null)
		{
			target.setOrderVolume(getMetricUnitConverter().convert(source.getOrderVolume()));
		}
		if (source.getOrderWeight() != null)
		{
			target.setOrderWeight(getMetricUnitConverter().convert(source.getOrderWeight()));
		}
		if (source.getDeliveryAddress() != null)
		{
			target.setDeliveryAddress(getEnergizerAddressConverter().convert(source.getDeliveryAddress()));
		}
		target.setContainerVolumeUtilization(source.getContainerVolumeUtilization());
		target.setContainerWeightUtilization(source.getContainerWeightUtilization());
		target.setTotalPalletCount(source.getTotalPalletCount());
		target.setVirtualPalletCount(source.getVirtualPalletCount());
		target.setPartialPalletCount(source.getPartialPalletCount());
		target.setLeadTime(source.getLeadTime());

		palStackData = source.getPalStackData();
		final SortedSet<Integer> keys = new TreeSet<Integer>(palStackData.keySet());
		for (final Integer key : keys)
		{
			final List<String> value = palStackData.get(key);
			sortedPalStackData.put(key, value);
		}
		target.setPalStackData(sortedPalStackData);

		//cost center setting as - one b2b unit can have only one cost center.
		final List<? extends B2BCostCenterData> costCenterData = defaultB2BCostCenterFacade.getActiveCostCenters();
		if (costCenterData != null && costCenterData.size() > 0)
		{
			target.setCostCenter(costCenterData.get(0));
		}

		/*
		 * if(source.getContainerHeight()==null && source.getContainerPackingType()==null){
		 * target.setContainerHeight(source.getContainerHeight());
		 * target.setContainerPackingType(source.getContainerPackingType()); } else{
		 */

		target.setContainerHeight(source.getContainerHeight());
		target.setContainerPackingType(source.getContainerPackingType());

		//}
		//target.setContainerVolumeUtilization(source.get)

	}

	public Converter<MetricUnitModel, MetricUnitData> getMetricUnitConverter()
	{
		return metricUnitConverter;
	}

	public void setMetricUnitConverter(final Converter<MetricUnitModel, MetricUnitData> metricUnitConverter)
	{
		this.metricUnitConverter = metricUnitConverter;
	}

	public Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> getEnergizerB2BUnitConverter()
	{
		return energizerB2BUnitConverter;
	}

	public void setEnergizerB2BUnitConverter(final Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter)
	{
		this.energizerB2BUnitConverter = energizerB2BUnitConverter;
	}

	public Converter<AddressModel, AddressData> getEnergizerAddressConverter()
	{
		return energizerAddressConverter;
	}


	public void setEnergizerAddressConverter(final Converter<AddressModel, AddressData> energizerAddressConverter)
	{
		this.energizerAddressConverter = energizerAddressConverter;
	}

}
