/**
 * 
 */
package com.energizer.services.order.impl;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.energizer.core.createorder.jaxb.xsd.objects.ArrayOfBAPIINCOMP_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ArrayOfBAPIRET2_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ArrayOfZSD_TSOCONDITIONS_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ArrayOfZSD_TSOITEM_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ArrayOfZSD_TSOPART_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.BAPIRET2_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_BAPI_SALESORDER_CREATE;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_BAPI_SALESORDER_CREATEResponse;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_ISOHEAD_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_TSOCONDITIONS_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_TSOITEM_D31E8C;
import com.energizer.core.createorder.jaxb.xsd.objects.ZSD_TSOPART_D31E8C;
import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.jaxb.xsd.objects.ArrayOfBAPIINCOMP_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ArrayOfBAPIRET2_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ArrayOfZSD_TSOCONDITIONS_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ArrayOfZSD_TSOITEM_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ArrayOfZSD_TSOPART_Fa2309;
import com.energizer.core.jaxb.xsd.objects.BAPIRET2_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ObjectFactory;
import com.energizer.core.jaxb.xsd.objects.ZSD_BAPI_SALESORDER_SIMULATE;
import com.energizer.core.jaxb.xsd.objects.ZSD_BAPI_SALESORDER_SIMULATEResponse;
import com.energizer.core.jaxb.xsd.objects.ZSD_ISOHEAD_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ZSD_TSOCONDITIONS_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ZSD_TSOITEM_Fa2309;
import com.energizer.core.jaxb.xsd.objects.ZSD_TSOPART_Fa2309;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.EnergizerB2BOrderService;
import com.energizer.services.order.dao.EnergizerB2BOrderDAO;


/**
 * @author M1023097
 * 
 */
public class DefaultEnergizerB2BOrderService implements EnergizerB2BOrderService
{

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BOrderService.class);

	@Resource(name = "energizerB2BOrderDAO")
	EnergizerB2BOrderDAO energizerB2BOrderDAO;

	@Resource(name = "modelService")
	ModelService modelService;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;
	@Resource(name = "productService")
	ProductService productService;

	@Resource
	private EmailService emailService;
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private static final int SUCCESS = 1;
	private static final int FAILURE = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.order.EnergizerB2BOrderService#getLeadTimeData()
	 */
	@Override
	public List<EnergizerB2BUnitLeadTimeModel> getLeadTimeData(final EnergizerB2BUnitModel b2bUnitModel,
			final String shippingPointId, final String soldToAddressId)
	{
		return energizerB2BOrderDAO.getLeadTimeData(b2bUnitModel, shippingPointId, soldToAddressId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.order.EnergizerB2BOrderService#getDeliveryAddress(java.lang.String)
	 */
	@Override
	public List<String> getsoldToAddressIds(final EnergizerB2BUnitModel b2bUnitModel, final String shippingPointId)
	{
		return energizerB2BOrderDAO.getsoldToAddressIds(b2bUnitModel, shippingPointId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.order.EnergizerB2BOrderService#simulateOrder(de.hybris.platform.commercefacades.order.data
	 * .CartData)
	 */
	@Override
	public CartData simulateOrder(final CartData cartData) throws Exception
	{
		final Long startTime = System.currentTimeMillis();
		LOG.info("Before marshall " + startTime);
		final String orderSimulateXML = simulateOrderMarshall(cartData);
		final Long marshallTime = System.currentTimeMillis();
		LOG.info("Marshall took " + (marshallTime - startTime) + " milliseconds");
		final String restCallResponse = invokeRESTCall(orderSimulateXML, "simulate");
		final Long unmarshallTime = System.currentTimeMillis();
		LOG.info("REST Call took " + (unmarshallTime - marshallTime) + " milliseconds");
		final AbstractOrderData orderData = simulateOrderUnMarshall(restCallResponse, cartData);
		LOG.info("UnMarshall took " + (System.currentTimeMillis() - unmarshallTime) + " milliseconds");
		return (CartData) orderData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.order.EnergizerB2BOrderService#createOrder(de.hybris.platform.core.model.order.OrderModel)
	 */
	@Override
	public int createOrder(final OrderModel orderModel) throws Exception
	{
		String OrderCreationXML = "";
		final Long startTime = System.currentTimeMillis();
		LOG.info("Before create order marshall " + startTime);
		try
		{
			OrderCreationXML = createOrderMarshall(orderModel);
		}
		catch (final Exception e)
		{
			throw e;
		}
		final Long marshallTime = System.currentTimeMillis();
		LOG.info("Create order Marshall took " + (marshallTime - startTime) + " milliseconds");
		final String restCallResponse = invokeRESTCall(OrderCreationXML, "createOrder");

		final Long unmarshallTime = System.currentTimeMillis();
		LOG.info("Create order REST Call took " + (unmarshallTime - marshallTime) + " milliseconds");
		try
		{
			simulateOrderforIDUnMarshall(restCallResponse, orderModel);
			LOG.info("Create order UnMarshall took " + (System.currentTimeMillis() - unmarshallTime) + " milliseconds");
			return SUCCESS;
		}
		catch (final Exception e)
		{
			LOG.info("Caught Eception during UnMashall " + e.getMessage());
			return FAILURE;
		}

	}

	private String simulateOrderMarshall(final AbstractOrderData orderData) throws Exception
	{
		final ObjectFactory objectFactory = new ObjectFactory();
		StringWriter stringWriter = new StringWriter();
		String parsedXML = null;
		JAXBContext context;
		try
		{
			context = JAXBContext.newInstance("com.energizer.core.jaxb.xsd.objects");
			final ZSD_BAPI_SALESORDER_SIMULATE xmlRoot = objectFactory.createZSD_BAPI_SALESORDER_SIMULATE();
			final EnergizerB2BUnitData b2bUnitData = orderData.getB2bUnit();
			final ZSD_ISOHEAD_Fa2309 xmlHead = objectFactory.createZSD_ISOHEAD_Fa2309();
			final JAXBElement<String> purchaseno = objectFactory.createZSD_ISOHEAD_Fa2309PURCH_NO(orderData.getCode());
			final JAXBElement<String> purchasenos = objectFactory.createZSD_ISOHEAD_Fa2309PURCH_NO_S(orderData.getCode());
			final JAXBElement<String> salesOrg = objectFactory.createZSD_ISOHEAD_Fa2309SALES_ORG(b2bUnitData.getSalesOrganisation());
			final JAXBElement<String> salesDiv = objectFactory.createZSD_ISOHEAD_Fa2309DIVISION(b2bUnitData.getDivision());
			final JAXBElement<String> dsitrChanel = objectFactory.createZSD_ISOHEAD_Fa2309DISTR_CHAN(b2bUnitData
					.getDistributionChannel());
			final JAXBElement<String> documentType = objectFactory
					.createZSD_ISOHEAD_Fa2309DOC_TYPE(b2bUnitData.getErpOrderingType());
			// SAP dont need net value to be sent
			final JAXBElement<String> netvalue = objectFactory.createZSD_ISOHEAD_Fa2309NET_VALUE("");
			final JAXBElement<String> currency = objectFactory.createZSD_ISOHEAD_Fa2309CURRENCY(orderData.getSubTotal()
					.getCurrencyIso());
			final GregorianCalendar c = new GregorianCalendar();
			c.setTime(orderData.getRequestedDeliveryDate());
			XMLGregorianCalendar date2 = null;
			try
			{
				date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(sdf.format(c.getTime()));
			}
			catch (final DatatypeConfigurationException e)
			{
				LOG.error(e.getMessage());
			}
			xmlHead.setDOC_TYPE(documentType);
			xmlHead.setSALES_ORG(salesOrg);
			xmlHead.setDISTR_CHAN(dsitrChanel);
			xmlHead.setDIVISION(salesDiv);
			xmlHead.setPURCH_NO(purchaseno);
			xmlHead.setPURCH_NO_S(purchasenos);
			xmlHead.setNET_VALUE(netvalue);
			final JAXBElement<XMLGregorianCalendar> reqestedDate = objectFactory.createZSD_ISOHEAD_Fa2309REQ_DATE_H(date2);
			xmlHead.setREQ_DATE_H(reqestedDate);
			xmlHead.setCURRENCY(currency);


			final JAXBElement<ZSD_ISOHEAD_Fa2309> headObj = objectFactory.createZSD_BAPI_SALESORDER_SIMULATEI_SOHEAD(xmlHead);
			final ArrayOfZSD_TSOITEM_Fa2309 itemArray = new ArrayOfZSD_TSOITEM_Fa2309();
			for (final OrderEntryData entry : orderData.getEntries())
			{
				final ProductData productData = entry.getProduct();
				final String material = productData.getErpMaterialID();
				final String prodCode = productData.getCode();
				final String plant = productData.getShippingPoint();
				Long quantity = entry.getQuantity();
				final ZSD_TSOITEM_Fa2309 orderEntry = objectFactory.createZSD_TSOITEM_Fa2309();
				// SAP need item number to be sent in multiple of 10's
				orderEntry.setITM_NUMBER(objectFactory.createZSD_TSOITEM_Fa2309ITM_NUMBER((entry.getEntryNumber() + 1) * 10));
				orderEntry.setMATERIAL(objectFactory.createZSD_TSOITEM_Fa2309MATERIAL(prodCode));
				String uom = productData.getUom();
				if (uom.equalsIgnoreCase("EA"))
				{
					LOG.error("We can not simulate the Order for UOM in EA");
					throw new Exception("We can not simulate the Order for UOM in EA");
				}
				if (!uom.equalsIgnoreCase("CS") && !uom.equalsIgnoreCase("IP"))
				{
					final List<EnergizerProductConversionFactorModel> conversionList = getConversionModelList(prodCode);
					for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
					{
						if (uom.equalsIgnoreCase("PAL"))
						{
							final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "PAL");
							final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
							Integer quantityInInt = 0;
							if (conversionMultiplier != null)
							{
								quantityInInt = conversionMultiplier / conversionMultiplierForCase;
							}
							else
							{
								sendEmail(orderData, material, prodCode, "PAL");
							}
							quantity = quantityInInt.longValue() * quantity;
							uom = "CS";
						}
						if (uom.equalsIgnoreCase("LAY"))
						{
							final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "LAY");
							final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
							Integer quantityinInt = 0;
							//quantityinInt = conversionMultiplier / conversionMultiplierForCase;
							if (conversionMultiplier != null)
							{
								quantityinInt = conversionMultiplier / conversionMultiplierForCase;
							}
							else
							{
								sendEmail(orderData, material, prodCode, "LAY");
							}
							quantity = quantityinInt.longValue() * quantity;
							uom = "CS";
						}
					}
				}
				orderEntry.setTARGET_QTY(objectFactory.createZSD_TSOITEM_Fa2309TARGET_QTY(quantity));
				orderEntry.setTARGET_QU(objectFactory.createZSD_TSOITEM_Fa2309TARGET_QU(uom));
				orderEntry.setPLANT(objectFactory.createZSD_TSOITEM_Fa2309PLANT(plant));
				// SAP dont need net value to be sent
				orderEntry.setNET_VALUE(objectFactory.createZSD_TSOITEM_Fa2309NET_VALUE(""));


				itemArray.getZSD_TSOITEM().add(orderEntry);
			}

			final JAXBElement<ArrayOfZSD_TSOITEM_Fa2309> simulateitems = objectFactory
					.createZSD_BAPI_SALESORDER_SIMULATET_SOITEM(itemArray);

			final ArrayOfZSD_TSOPART_Fa2309 prtnerArray = new ArrayOfZSD_TSOPART_Fa2309();
			ZSD_TSOPART_Fa2309 partner = objectFactory.createZSD_TSOPART_Fa2309();
			partner.setPARTN_ROLE(objectFactory.createZSD_TSOPART_Fa2309PARTN_ROLE("SP"));
			partner
					.setPARTN_NUMB(objectFactory.createZSD_TSOPART_Fa2309PARTN_NUMB(orderData.getDeliveryAddress().getErpAddressId()));
			prtnerArray.getZSD_TSOPART().add(partner);
			partner = objectFactory.createZSD_TSOPART_Fa2309();
			//	partner.setPARTN_ROLE(objectFactory.createZSD_TSOPART_Fa2309PARTN_ROLE("PY"));
			// partner.setPARTN_NUMB(objectFactory.createZSD_TSOPART_Fa2309PARTN_NUMB(orderData.getDeliveryAddress().getErpAddressId()));
			// prtnerArray.getZSD_TSOPART().add(partner);
			final JAXBElement<ArrayOfZSD_TSOPART_Fa2309> partners = objectFactory
					.createZSD_BAPI_SALESORDER_SIMULATET_SOPARTNER(prtnerArray);
			final ArrayOfZSD_TSOCONDITIONS_Fa2309 conditionsArray = new ArrayOfZSD_TSOCONDITIONS_Fa2309();
			final JAXBElement<ArrayOfZSD_TSOCONDITIONS_Fa2309> conditions = objectFactory
					.createZSD_BAPI_SALESORDER_SIMULATET_TSOCONDITIONS(conditionsArray);
			final ArrayOfBAPIRET2_Fa2309 messageArray = new ArrayOfBAPIRET2_Fa2309();
			final JAXBElement<ArrayOfBAPIRET2_Fa2309> messages = objectFactory
					.createZSD_BAPI_SALESORDER_SIMULATEMESSAGETABLE(messageArray);
			final ArrayOfBAPIINCOMP_Fa2309 orderIncompArray = new ArrayOfBAPIINCOMP_Fa2309();
			final JAXBElement<ArrayOfBAPIINCOMP_Fa2309> orderincomplete = objectFactory
					.createZSD_BAPI_SALESORDER_SIMULATEORDER_INCOMPLETE(orderIncompArray);

			xmlRoot.setI_SOHEAD(headObj);
			xmlRoot.setMESSAGETABLE(messages);
			xmlRoot.setORDER_INCOMPLETE(orderincomplete);
			xmlRoot.setT_SOITEM(simulateitems);
			xmlRoot.setT_SOPARTNER(partners);
			xmlRoot.setT_TSOCONDITIONS(conditions);

			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			stringWriter = new StringWriter();
			marshaller.marshal(xmlRoot, stringWriter);
			parsedXML = stringWriter.toString();
		}
		catch (final JAXBException jaxbException)
		{
			LOG.error(jaxbException.getMessage());
		}
		return parsedXML;
	}

	private void sendEmail(final AbstractOrderData orderData, final String material, final String prodCode, final String uom)
			throws Exception
	{
		{
			LOG.info("Could not find the conversion factor in cases(CS)" + material);
			String supportEmail = Config.getString("energizer.customer.support.to.email", "test@test.com");
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			supportEmail = Config.getString("energizer.customer.support.from.email", "test@test.com");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			emailSubject.append("Simulation Failed ");
			emailBody.append("Hi <br/>");
			emailBody.append("While creating data for simulating order in hybris " + "<br/>");
			emailBody.append("we could not find the conversions for the sales UOM " + uom + "for the material id " + prodCode
					+ "<br/>");
			emailBody.append("This is an automatically generated email. Please do not reply to this mail");
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString(), null);
			LOG.error("Failed to simulate order \n ");
			emailService.send(message);
			throw new Exception("No converion found in Cases for material " + material);
		}
	}

	/**
	 * @param code
	 * @return
	 */
	private List<EnergizerProductConversionFactorModel> getConversionModelList(final String prodCode)
	{
		final EnergizerProductModel enrProdModel = (EnergizerProductModel) productService.getProductForCode(prodCode);
		final List<EnergizerProductConversionFactorModel> conversionList = enrProdModel.getProductConversionFactors();
		return conversionList;
	}

	private String createOrderMarshall(final OrderModel order) throws Exception
	{
		final com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory objectFactory = new com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory();
		StringWriter stringWriter = new StringWriter();
		String parsedXML = null;
		JAXBContext context;
		try
		{
			context = JAXBContext.newInstance("com.energizer.core.createorder.jaxb.xsd.objects");
			final ZSD_BAPI_SALESORDER_CREATE xmlRoot = objectFactory.createZSD_BAPI_SALESORDER_CREATE();
			final EnergizerB2BUnitModel b2bUnitData = order.getB2bUnit();
			final ZSD_ISOHEAD_D31E8C xmlHead = objectFactory.createZSD_ISOHEAD_D31E8C();
			final JAXBElement<String> purchaseno = objectFactory.createZSD_ISOHEAD_D31E8CPURCH_NO(order.getPurchaseOrderNumber());
			final JAXBElement<String> purchasenos = objectFactory.createZSD_ISOHEAD_D31E8CPURCH_NO_S(order.getCode());
			final JAXBElement<String> salesOrg = objectFactory.createZSD_ISOHEAD_D31E8CSALES_ORG(b2bUnitData.getSalesOrganisation());
			final JAXBElement<String> salesDiv = objectFactory.createZSD_ISOHEAD_D31E8CDIVISION(b2bUnitData.getDivision());
			final JAXBElement<String> dsitrChanel = objectFactory.createZSD_ISOHEAD_D31E8CDISTR_CHAN(b2bUnitData
					.getDistributionChannel());
			final JAXBElement<String> documentType = objectFactory
					.createZSD_ISOHEAD_D31E8CDOC_TYPE(b2bUnitData.getErpOrderingType());
			// SAP dont need net value to be sent
			final JAXBElement<String> netvalue = objectFactory.createZSD_ISOHEAD_D31E8CNET_VALUE("");
			final JAXBElement<String> currency = objectFactory.createZSD_ISOHEAD_D31E8CCURRENCY(order.getCurrency().getIsocode());
			final GregorianCalendar c = new GregorianCalendar();

			c.setTime(order.getRequestedDeliveryDate());
			XMLGregorianCalendar date2 = null;
			try
			{
				date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(sdf.format(c.getTime()));
			}
			catch (final DatatypeConfigurationException e)
			{
				LOG.error(e.getMessage());
			}
			xmlHead.setDOC_TYPE(documentType);
			xmlHead.setSALES_ORG(salesOrg);
			xmlHead.setDISTR_CHAN(dsitrChanel);
			xmlHead.setDIVISION(salesDiv);
			xmlHead.setPURCH_NO(purchaseno);
			xmlHead.setPURCH_NO_S(purchasenos);
			xmlHead.setNET_VALUE(netvalue);
			final JAXBElement<XMLGregorianCalendar> reqestedDate = objectFactory.createZSD_ISOHEAD_D31E8CREQ_DATE_H(date2);
			xmlHead.setREQ_DATE_H(reqestedDate);
			xmlHead.setCURRENCY(currency);

			final JAXBElement<ZSD_ISOHEAD_D31E8C> headObject = objectFactory.createZSD_BAPI_SALESORDER_CREATEI_SOHEAD(xmlHead);
			final ArrayOfZSD_TSOITEM_D31E8C itemArray = new ArrayOfZSD_TSOITEM_D31E8C();
			for (final AbstractOrderEntryModel orderEntry : order.getEntries())
			{
				final EnergizerProductModel productData = (EnergizerProductModel) orderEntry.getProduct();
				final String material = productData.getCode();
				final String code = productData.getCode();
				final String plant = productData.getProductCMIR().get(0).getShippingPoint();
				Long quantity = orderEntry.getQuantity();
				final ZSD_TSOITEM_D31E8C orderEntries = objectFactory.createZSD_TSOITEM_D31E8C();
				// SAP need item number to be sent in multiple of 10's
				orderEntries
						.setITM_NUMBER(objectFactory.createZSD_TSOCONDITIONS_D31E8CITM_NUMBER((orderEntry.getEntryNumber() + 1) * 10));
				orderEntries.setMATERIAL(objectFactory.createZSD_TSOCONDITIONS_D31E8CMATERIAL(code));

				final List<EnergizerCMIRModel> CMIRModelList = productData.getProductCMIR();
				String uom = "";
				for (final EnergizerCMIRModel CMIRModel : CMIRModelList)
				{
					if (CMIRModel.getB2bUnit().getUid().equalsIgnoreCase(b2bUnitData.getUid()))
					{
						uom = CMIRModel.getUom();
					}
				}
				if (uom.equalsIgnoreCase("EA"))
				{
					LOG.error("We can not simulate the Order for UOM in EA");
					throw new Exception("We can not simulate the Order for UOM in EA");
				}
				if (!uom.equalsIgnoreCase("CS") && !uom.equalsIgnoreCase("IP"))
				{
					final List<EnergizerProductConversionFactorModel> conversionList = productData.getProductConversionFactors();
					for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
					{
						if (uom.equalsIgnoreCase("PAL"))
						{
							final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "PAL");
							final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
							Integer quantityInInt = 0;
							if (conversionMultiplierForCase != null)
							{
								quantityInInt = conversionMultiplier / conversionMultiplierForCase;
							}
							else
							{
								LOG.info("Could not find the conversion factor in cases(CS)" + material);
								throw new Exception("No converion found in Cases for material " + material);
							}
							quantity = quantityInInt.longValue() * quantity;
							uom = "CS";
						}
						if (uom.equalsIgnoreCase("LAY"))
						{
							final Integer conversionMultiplier = getAlernateConversionMultiplierForUOM(conversionList, "LAY");
							final Integer conversionMultiplierForCase = getAlernateConversionMultiplierForUOM(conversionList, "CS");
							Integer quantityinInt = conversionMultiplier / conversionMultiplierForCase;
							if (conversionMultiplierForCase != null)
							{
								quantityinInt = conversionMultiplier / conversionMultiplierForCase;
							}
							else
							{
								LOG.info("Could not find the conversion factor in cases(CS)" + material);
								throw new Exception("No converion found in Cases for material " + material);
							}
							quantity = quantityinInt.longValue() * quantity;
							uom = "CS";
						}
					}
				}
				orderEntries.setTARGET_QTY(objectFactory.createZSD_TSOITEM_D31E8CTARGET_QTY(quantity));
				orderEntries.setTARGET_QU(objectFactory.createZSD_TSOITEM_D31E8CTARGET_QU(uom));
				orderEntries.setPLANT(objectFactory.createZSD_TSOITEM_D31E8CPLANT(plant));
				// SAP dont need net value to be sent
				orderEntries.setNET_VALUE(objectFactory.createZSD_TSOITEM_D31E8CNET_VALUE(""));


				itemArray.getZSD_TSOITEM().add(orderEntries);
			}
			final JAXBElement<ArrayOfZSD_TSOITEM_D31E8C> items = objectFactory.createZSD_BAPI_SALESORDER_CREATET_SOITEM(itemArray);

			final ArrayOfZSD_TSOPART_D31E8C prtnerArray = new ArrayOfZSD_TSOPART_D31E8C();
			ZSD_TSOPART_D31E8C partner = objectFactory.createZSD_TSOPART_D31E8C();
			partner.setPARTN_ROLE(objectFactory.createZSD_TSOPART_D31E8CPARTN_ROLE("SP"));
			partner.setPARTN_NUMB(objectFactory.createZSD_TSOPART_D31E8CPARTN_NUMB(order.getDeliveryAddress().getErpAddressId()));
			prtnerArray.getZSD_TSOPART().add(partner);
			partner = objectFactory.createZSD_TSOPART_D31E8C();
			//	partner.setPARTN_ROLE(objectFactory.createZSD_TSOPART_Fa2309PARTN_ROLE("PY"));
			// partner.setPARTN_NUMB(objectFactory.createZSD_TSOPART_Fa2309PARTN_NUMB(orderData.getDeliveryAddress().getErpAddressId()));
			// prtnerArray.getZSD_TSOPART().add(partner);
			final JAXBElement<ArrayOfZSD_TSOPART_D31E8C> partners = objectFactory
					.createZSD_BAPI_SALESORDER_CREATET_SOPARTNER(prtnerArray);
			final ArrayOfZSD_TSOCONDITIONS_D31E8C conditionsArray = new ArrayOfZSD_TSOCONDITIONS_D31E8C();
			final JAXBElement<ArrayOfZSD_TSOCONDITIONS_D31E8C> conditions = objectFactory
					.createZSD_BAPI_SALESORDER_CREATET_TSOCONDITIONS(conditionsArray);
			final ArrayOfBAPIRET2_D31E8C messageArray = new ArrayOfBAPIRET2_D31E8C();
			final JAXBElement<ArrayOfBAPIRET2_D31E8C> messages = objectFactory
					.createZSD_BAPI_SALESORDER_CREATEResponseMESSAGETABLE(messageArray);
			final ArrayOfBAPIINCOMP_D31E8C orderIncompArray = new ArrayOfBAPIINCOMP_D31E8C();
			final JAXBElement<ArrayOfBAPIINCOMP_D31E8C> orderincomplete = objectFactory
					.createZSD_BAPI_SALESORDER_CREATEResponseORDER_INCOMPLETE(orderIncompArray);

			xmlRoot.setI_SOHEAD(headObject);
			xmlRoot.setMESSAGETABLE(messages);
			xmlRoot.setORDER_INCOMPLETE(orderincomplete);
			xmlRoot.setT_SOITEM(items);
			xmlRoot.setT_SOPARTNER(partners);
			xmlRoot.setT_TSOCONDITIONS(conditions);

			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			stringWriter = new StringWriter();
			marshaller.marshal(xmlRoot, stringWriter);
			parsedXML = stringWriter.toString();
		}
		catch (final JAXBException jaxbException)
		{
			LOG.error(jaxbException.getMessage());
		}
		return parsedXML;
	}

	private String invokeRESTCall(final String requestXML, final String option)
	{
		try
		{
			final RestTemplate restTemplate = new RestTemplate();
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", MediaType.APPLICATION_XML.toString());
			headers.add("Accept", MediaType.APPLICATION_XML.toString());
			final HttpEntity formEntity = new HttpEntity<>(requestXML, headers);
			final String simulateTimeOutinSeconds = configurationService.getConfiguration().getString("simulateTimeOutinSeconds",
					"30");
			final int simulateTimeOutSeconds = Integer.parseInt(simulateTimeOutinSeconds);
			((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(1000 * simulateTimeOutSeconds);

			if (option.equalsIgnoreCase("simulate"))
			{
				return getResponse(restTemplate, configurationService.getConfiguration().getString("simulateURL"), formEntity);
			}
			else
			{
				return getResponse(restTemplate, configurationService.getConfiguration().getString("orderSubmitURL"), formEntity);
			}
		}
		catch (final HttpClientErrorException clientException)
		{
			String supportEmail = Config.getString("energizer.customer.support.to.email", "test@test.com");
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			supportEmail = Config.getString("energizer.customer.support.from.email", "test@test.com");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			if (option.equalsIgnoreCase("simulate"))
			{
				emailSubject.append("ERROR: Order simulation failed in SAP");
				emailBody.append("Order simulation failed in SAP because of bad format of xml <br/>" + requestXML);
			}
			else if (option.equalsIgnoreCase("createOrder"))
			{
				emailSubject.append("ERROR: Creating order in SAP failed");
				emailBody.append("Creating order in SAP failed because of bad format of xml <br/>" + requestXML);
			}
			emailBody.append("This is an automatically generated email. Please do not reply to this mail");
			//emailBody.append(requestXML);
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString() + "<br/>", null);
			LOG.error("Failed to simulate order \n " + requestXML);
			emailService.send(message);
			throw clientException;
		}
		catch (final RestClientException restException)
		{
			LOG.error("Failed to simulate order" + restException.getMessage());
			String supportEmail = Config.getString("energizer.customer.support.to.email", "test@test.com");
			final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			supportEmail = Config.getString("energizer.customer.support.from.email", "test@test.com");
			final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
			final StringBuilder emailBody = new StringBuilder();
			final StringBuilder emailSubject = new StringBuilder();
			if (option.equalsIgnoreCase("simulate"))
			{
				emailSubject.append("ERROR: Order simulation failed in SAP");
				emailBody.append("Order simulation failed in SAP because of <br/>" + restException.getMessage() + "<br/>");
			}
			else if (option.equalsIgnoreCase("createOrder"))
			{
				emailSubject.append("ERROR: Creating order in SAP failed");
				emailBody.append("Creating order in SAP failed because of <br/>" + restException.getMessage() + "<br/>");
			}
			//emailBody.append(requestXML);
			emailBody.append("This is an automatically generated email. Please do not reply to this mail");
			final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
					emailSubject.toString(), emailBody.toString() + "<br/>", null);
			LOG.error("Failed to simulate order \n " + requestXML);
			emailService.send(message);
			throw restException;
		}
	}

	private AbstractOrderData simulateOrderUnMarshall(final String responce, final CartData orderData) throws Exception
	{
		// YTODO Auto-generated method stub
		final JAXBContext jaxbContext;
		final OrderModel orderModel = null;
		final ZSD_BAPI_SALESORDER_CREATEResponse unmarshalledOrdCreationObject = null;
		try
		{
			jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final InputStream stream = new ByteArrayInputStream(responce.getBytes(StandardCharsets.UTF_8));
			final ZSD_BAPI_SALESORDER_SIMULATEResponse unmarshalledSimulateObject = (ZSD_BAPI_SALESORDER_SIMULATEResponse) unmarshaller
					.unmarshal(stream);
			final ZSD_ISOHEAD_Fa2309 head = unmarshalledSimulateObject.getE_SOHEAD();
			final JAXBElement<String> status = head.getSTATUS();

			if ("E".equalsIgnoreCase(status.getValue()))
			{
				sendEmailonError(unmarshalledSimulateObject, unmarshalledOrdCreationObject, orderModel, orderData);
				throw new Exception("Simulation returned Status is E");
			}
			final List<OrderEntryData> orderDataEntries = orderData.getEntries();
			final JAXBElement<ArrayOfZSD_TSOITEM_Fa2309> T_SOITEM = unmarshalledSimulateObject.getT_SOITEM();
			final List<ZSD_TSOITEM_Fa2309> xmlEntries = T_SOITEM.getValue().getZSD_TSOITEM();
			final OrderEntryData orderEntry = null;
			final JAXBElement<XMLGregorianCalendar> reqDeliveryDate = head.getREQ_DATE_H();
			orderData.setRequestedDeliveryDate(reqDeliveryDate.getValue().toGregorianCalendar().getTime());
			PriceData priceData = new PriceData();
			priceData.setValue(new BigDecimal(head.getNET_VALUE().getValue()));
			orderData.setTotalPrice(priceData);
			priceData = new PriceData();
			priceData.setValue(new BigDecimal(head.getTAX_TOTAL().getValue()));
			orderData.setTotalTax(priceData);
			final JAXBElement<ArrayOfZSD_TSOCONDITIONS_Fa2309> conditionArray = unmarshalledSimulateObject.getT_TSOCONDITIONS();
			for (final OrderEntryData OrdEntry : orderDataEntries)
			{
				updateDataEntries(OrdEntry, xmlEntries, conditionArray.getValue().getZSD_TSOCONDITIONS());
			}

		}
		catch (final JAXBException exception)
		{
			LOG.error(" Failed in getting Orderdata during OrderPlacing " + exception.getMessage(), exception);
		}
		return orderData;
	}


	private void simulateOrderforIDUnMarshall(final String response, final OrderModel orderModel) throws Exception
	{
		final JAXBContext jaxbContext;
		final ZSD_BAPI_SALESORDER_SIMULATEResponse unmarshalledSimulateObject = null;
		final CartData orderData = null;
		try
		{
			jaxbContext = JAXBContext.newInstance(com.energizer.core.createorder.jaxb.xsd.objects.ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
			final ZSD_BAPI_SALESORDER_CREATEResponse unmarshalledOrdCreationObject = (ZSD_BAPI_SALESORDER_CREATEResponse) unmarshaller
					.unmarshal(stream);
			final ZSD_ISOHEAD_D31E8C head = unmarshalledOrdCreationObject.getE_SOHEAD();

			final JAXBElement<String> status = head.getSTATUS();

			if ("E".equalsIgnoreCase(status.getValue()))
			{
				sendEmailonError(unmarshalledSimulateObject, unmarshalledOrdCreationObject, orderModel, orderData);
				throw new Exception("Create order in SAP returned Status is E");
			}

			orderModel.setErpOrderNumber(head.getDOC_NUMBER().getValue());
			orderModel.setTotalTax(Double.parseDouble(head.getTAX_TOTAL().getValue()));
			orderModel.setTotalPrice(Double.parseDouble(head.getNET_VALUE().getValue()));
			final JAXBElement<XMLGregorianCalendar> reqDeliveryDate = head.getREQ_DATE_H();
			orderModel.setRequestedDeliveryDate(reqDeliveryDate.getValue().toGregorianCalendar().getTime());
			final List<AbstractOrderEntryModel> orderModelEntries = orderModel.getEntries();

			final JAXBElement<ArrayOfZSD_TSOCONDITIONS_D31E8C> conditionArray = unmarshalledOrdCreationObject.getT_TSOCONDITIONS();
			final JAXBElement<ArrayOfZSD_TSOITEM_D31E8C> T_SOITEM = unmarshalledOrdCreationObject.getT_SOITEM();
			final List<ZSD_TSOITEM_D31E8C> xmlEntries = T_SOITEM.getValue().getZSD_TSOITEM();

			for (final AbstractOrderEntryModel orderEntryModel : orderModelEntries)
			{
				final String modelProdCode = orderEntryModel.getProduct().getCode();
				Double zDF1BaseUomPrice = Double.parseDouble("0.00");
				Double zDF1BaseUomQuantity = Double.parseDouble("0.00");
				Double zDF1EntryTotla = Double.parseDouble("0.00");

				Double zPR0BaseUomPrice = Double.parseDouble("0.00");
				Double zPR0BaseUomQuantity = Double.parseDouble("0.00");
				Double zPR0EntryTotla = Double.parseDouble("0.00");
				boolean isZDF1PriceAvailable = false;
				boolean isZPR0PriceAvailable = false;

				for (final ZSD_TSOCONDITIONS_D31E8C conditions : conditionArray.getValue().getZSD_TSOCONDITIONS())
				{
					if (conditions.getMATERIAL().getValue().equals(modelProdCode))
					{
						if (conditions.getCOND_TYPE().getValue().equalsIgnoreCase("ZPR0"))
						{
							zPR0BaseUomPrice = Double.parseDouble(conditions.getCOND_VALUE().getValue());
							zPR0BaseUomQuantity = Double.parseDouble(conditions.getCONBASEVAL().getValue());
							zPR0EntryTotla = zPR0BaseUomPrice * zPR0BaseUomQuantity;
							isZPR0PriceAvailable = true;
						}
						if (conditions.getCOND_TYPE().getValue().equalsIgnoreCase("ZDF1"))
						{
							zDF1BaseUomPrice = Double.parseDouble(conditions.getCOND_VALUE().getValue());
							zDF1BaseUomQuantity = Double.parseDouble(conditions.getCONBASEVAL().getValue());
							zDF1EntryTotla = zDF1BaseUomPrice * zDF1BaseUomQuantity;
							isZDF1PriceAvailable = true;
						}
					}
				}
				if (isZDF1PriceAvailable)
				{
					orderEntryModel.setBasePrice(zDF1EntryTotla / orderEntryModel.getQuantity());
					orderEntryModel.setTotalPrice(zDF1EntryTotla);
					orderEntryModel.setRejectedStatus("No");
					modelService.save(orderEntryModel);
				}
				else if (isZPR0PriceAvailable)
				{
					orderEntryModel.setBasePrice(zPR0EntryTotla / orderEntryModel.getQuantity());
					orderEntryModel.setTotalPrice(zPR0EntryTotla);
					orderEntryModel.setRejectedStatus("No");
					modelService.save(orderEntryModel);
				}

			}
			orderModel.setStatus(OrderStatus.PENDING);
			modelService.save(orderModel);
		}
		catch (final Exception exception)
		{
			LOG.error("Failed in Order Placing Process " + exception.getMessage(), exception);
			throw new Exception(exception.getMessage());
		}
	}

	private StringBuilder getOrderCreationEmailBody(final StringBuilder emailBody,
			final ZSD_BAPI_SALESORDER_CREATEResponse unmarshalledOrdCreationObject)
	{
		for (final BAPIRET2_D31E8C message : unmarshalledOrdCreationObject.getMESSAGETABLE().getValue().getBAPIRET2())
		{
			emailBody.append("Message :" + message.toString());
			emailBody.append("<br />");
		}
		return emailBody;
	}


	private StringBuilder getSimulationEmailBody(final StringBuilder emailBody,
			final ZSD_BAPI_SALESORDER_SIMULATEResponse unmarshalledSimulateObject)
	{
		for (final BAPIRET2_Fa2309 message : unmarshalledSimulateObject.getMESSAGETABLE().getValue().getBAPIRET2())
		{
			emailBody.append("Message :" + message.toString());
			emailBody.append("<br />");
		}
		return emailBody;
	}

	private void sendEmailonError(final ZSD_BAPI_SALESORDER_SIMULATEResponse unmarshalledSimulateObject,
			final ZSD_BAPI_SALESORDER_CREATEResponse unmarshalledOrdCreationObject, final OrderModel orderModel,
			final CartData orderData)
	{
		// todo -- handle messagetable and order-incomplete data coming from SAP
		String supportEmail = Config.getString("energizer.customer.support.to.email", "test@test.com");
		final EmailAddressModel toAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
		supportEmail = Config.getString("energizer.customer.support.from.email", "test@test.com");
		final EmailAddressModel fromAddress = emailService.getOrCreateEmailAddressForEmail(supportEmail, "Hybris Test Mail");
		//b2bunit,enduser, time, cartno
		StringBuilder emailBody = new StringBuilder();

		if (orderModel != null && orderData == null)
		{
			emailBody.append("Hybris Ref No :" + orderModel.getCode());
			emailBody.append("\n");
			emailBody.append("user :" + orderModel.getUser());
		}
		else
		{
			emailBody.append("Hybris Ref No :" + orderData.getCode());
			emailBody.append("\n");
			emailBody.append("user :" + orderData.getUser());
		}
		emailBody.append("\n");
		emailBody.append("Date :" + new Date().toString());
		emailBody.append("\n");
		emailBody.append("Error messages from SAP as follows ");
		emailBody.append("\n");

		if (unmarshalledSimulateObject != null)
		{
			emailBody = getSimulationEmailBody(emailBody, unmarshalledSimulateObject);
			unmarshalledSimulateObject.getMESSAGETABLE().getValue().getBAPIRET2().toString();
		}

		if (unmarshalledOrdCreationObject != null)
		{
			emailBody = getOrderCreationEmailBody(emailBody, unmarshalledOrdCreationObject);
			unmarshalledOrdCreationObject.getMESSAGETABLE().getValue().getBAPIRET2().toString();
		}
		emailBody.append("<br/> This is an automatically generated email. Please do not reply to this mail");

		final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toAddress), null, null, fromAddress, "",
				"ERROR: Order simulation failed in SAP", emailBody.toString(), null);

		if (unmarshalledSimulateObject != null)
		{
			LOG.error("Failed to simulate order" + unmarshalledSimulateObject.getMESSAGETABLE().getValue().getBAPIRET2().toString());
		}

		if (unmarshalledOrdCreationObject != null)
		{
			LOG.error("Failed to simulate order"
					+ unmarshalledOrdCreationObject.getMESSAGETABLE().getValue().getBAPIRET2().toString());
		}

		emailService.send(message);
	}


	/**
	 * @param dataEntry
	 * @param xmlEntries
	 * 
	 */
	private void updateDataEntries(final OrderEntryData orderEntry, final List<ZSD_TSOITEM_Fa2309> xmlEntries,
			final List<ZSD_TSOCONDITIONS_Fa2309> condtionList)
	{
		final ProductData productData = orderEntry.getProduct();
		final String prodCode = productData.getCode();
		final PriceData zPR0PriceData = new PriceData();
		final PriceData zPR0TotalPriceData = new PriceData();
		final PriceData zDF1PriceData = new PriceData();
		final PriceData zDF1TotalPriceData = new PriceData();
		boolean isZDF1PriceAvailable = false;
		boolean isZPR0PriceAvailable = false;
		for (final ZSD_TSOCONDITIONS_Fa2309 condtion : condtionList)
		{
			if (prodCode.equalsIgnoreCase(condtion.getMATERIAL().getValue()))
			{
				// ZPRO (product listing price) will give material price at base UOM
				if (condtion.getCOND_TYPE().getValue().equalsIgnoreCase("ZPR0"))
				{
					// COND_VALUE is base UOM price
					final Double eachUnitValue = Double.parseDouble(condtion.getCOND_VALUE().getValue());
					final Double quantityAtBaseUOM = Double.parseDouble(condtion.getCONBASEVAL().getValue());
					final Double totalPriceValue = (eachUnitValue * quantityAtBaseUOM);
					zPR0PriceData.setValue(new BigDecimal(totalPriceValue / orderEntry.getQuantity()));
					zPR0PriceData.setCurrencyIso(condtion.getCURRENCY().getValue());
					zPR0TotalPriceData.setValue(new BigDecimal(totalPriceValue));
					zPR0TotalPriceData.setCurrencyIso(condtion.getCURRENCY().getValue());
					isZPR0PriceAvailable = true;
				}
				if (condtion.getCOND_TYPE().getValue().equalsIgnoreCase("ZDF1"))
				{
					// COND_VALUE is base UOM price
					final Double eachUnitValue = Double.parseDouble(condtion.getCOND_VALUE().getValue());
					final Double quantityAtBaseUOM = Double.parseDouble(condtion.getCONBASEVAL().getValue());
					final Double totalPriceValue = (eachUnitValue * quantityAtBaseUOM);
					zDF1PriceData.setValue(new BigDecimal(totalPriceValue / orderEntry.getQuantity()));
					zDF1PriceData.setCurrencyIso(condtion.getCURRENCY().getValue());
					zDF1TotalPriceData.setValue(new BigDecimal(totalPriceValue));
					zDF1TotalPriceData.setCurrencyIso(condtion.getCURRENCY().getValue());
					isZDF1PriceAvailable = true;
				}
			}
		}
		if (isZDF1PriceAvailable)
		{
			orderEntry.setBasePrice(zDF1PriceData);
			orderEntry.setTotalPrice(zDF1TotalPriceData);
		}
		else if (isZPR0PriceAvailable)
		{
			orderEntry.setBasePrice(zPR0PriceData);
			orderEntry.setTotalPrice(zPR0TotalPriceData);
		}
	}

	private String getResponse(final RestTemplate restTemplate, final String url, final HttpEntity formEntity)
	{
		final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, formEntity, String.class);
		LOG.info("simulate responce!! = " + response.getBody());
		return response.getBody();
	}

	private Integer getAlernateConversionMultiplierForUOM(final List<EnergizerProductConversionFactorModel> conversionList,
			final String uom)
	{
		// YTODO Auto-generated method stub
		for (final EnergizerProductConversionFactorModel enrProdConversion : conversionList)
		{
			if (enrProdConversion.getAlternateUOM().equalsIgnoreCase(uom))
			{
				return enrProdConversion.getConversionMultiplier();
			}
		}

		return null;

	}


}
