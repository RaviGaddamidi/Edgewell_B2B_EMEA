package com.energizer.core.datafeed.processor.order;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.services.email.EnergizerGenericEmailGenerationService;
import com.energizer.services.order.EnergizerOrderService;
import com.energizer.services.product.EnergizerProductService;


/**
 * 
 * This processors imports the order update .
 * 
 * Sample file will look like
 * 
 * SAPOrderNo,HybrisOrderNo,PONo,ReqDeliveryDate, TotalValue,TotalTax,
 * TotalShipment,TotalDiscount,Status,ContainerID,SealNumber
 * ,VesselNumber,InvoicePDF,ArchiveID,ERPMaterialID,OrderEntryQty
 * ,UOM,RejectionReason,ItemTotalPrice,LineItemTotalPrice,ItemTotalShipment,ItemTotalDiscount,ItemTax 54324,
 * 1427454247389,XYZ, 11-5-2014 23:11:51 EEST, , , 100, 250, ,XYZ, XYZ, XYZ, ,XYZ, PRD004, 100, 2A, XYZ,10, 1000, 50,
 * 50, Total column count : 23
 */

public class EnergizerOrderUpdateCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;
	@Resource
	private CatalogVersionService catalogVersionService;
	@Resource
	private ProductService productService;
	@Resource
	private UnitService unitService;
	@Resource
	private EnergizerOrderService energizerOrderService;
	@Resource
	EnergizerProductService energizerProductService;
	@Resource
	EnergizerGenericEmailGenerationService energizerGenericEmailGenerationService;

	private Converter<OrderModel, OrderData> orderConverter;
	private OrderData orderData;

	@Resource
	private UserService userService;

	@Resource
	EmailService emailService;



	//Constant declarations for CSV header names
	private static final String SAP_ORDER_NO = "SAPOrderNo";
	private static final String HYBRIS_ORDER_NO = "HybrisOrderNo";
	private static final String PO_NO = "POnumber";
	private static final String PriceUoM = "PriceUoM";
	private static final String REQ_DELIVERY_DATE = "Requesteddeliverydate";
	private static final String TOTAL_VALUE = "TotalValue";
	private static final String TOTAL_TAX = "TotalTax";
	private static final String TOTAL_SHIPMENT = "TotalShipment";
	private static final String TOTAL_DISCOUNT = "TotalDiscount";
	private static final String STATUS = "Status";
	private static final String CONTAINER_ID = "ContainerID";
	private static final String SEAL_NUMBER = "SealNumber";
	private static final String VESSAL_NUMBER = "VesselNumber";
	private static final String INVOICE_PDF = "InvoicePDF";
	private static final String ARCHIVE_ID = "ArchiveID";
	private static final String ERP_MATERIAL_ID = "ERPMaterialID";
	private static final String ORDER_ENTRY_QTY = "OrderEntryQty";
	private static final String UOM = "UOM";
	private static final String REJECTION_REASON = "Rejectionreason";
	private static final String ITEM_TOTAL_PRICE = "ItemTotalPrice";
	private static final String LINE_ITEM_TOTAL_PRICE = "LineItemTotalPrice";
	private static final String ITEM_TOTAL_SHIPMENT = "ItemTotalShipment";
	private static final String ITEM_TOTAL_DISCOUNT = "ItemTotalDiscount";
	private static final String ITEM_TAX = "ItemTax";
	private static final String BDS_DOCID = "BDSDocId";
	private static final String BDS_CONTREP = "BDSContRep";
	private static final String BDS_DOCUCLASS = "BDSDocuClass";

	private static final String FROM_EMAIL_ADDRESS = Config.getParameter("fromEmailAddress.orderEmailSender");
	private static final String FROM_EMAIL_DISPLAY_NAME = Config.getParameter("fromEmailDisplayName.orderEmailSender");
	private static final String SEND_EMAIL_FOR_ORDER_STATUS = Config.getParameter("sendEmailForOrderStatus");
	private static final String ENERGIZER_ORDER_UPDATE_FEED_MANDATORY_KEY = "feedprocessor.energizerOrderUpdateFeed.mandatory";
	private static final String ENERGIZER_POSSIBLE_ORDER_STATUS_KEY = Config.getParameter("possibleOrderStatus");
	private static final HashMap<String, String> ENERGIZER_POSSIBLE_ORDER_STATUS_MAP = new HashMap<String, String>()
	{
		{
			put("1", "PENDING");
			put("2", "IN_PROCESS");
			put("3", "SHIPPED");
			put("4", "INVOICED");
			put("5", "CANCELLED");
		}
	};
	private static final String ENERGIZER_DATE_FORMAT_KEY = Config.getParameter("dateFormat");
	private static final SimpleDateFormat ORDER_DATE_FORMATTER = new SimpleDateFormat(ENERGIZER_DATE_FORMAT_KEY);
	private static final Logger LOG = Logger.getLogger(EnergizerOrderUpdateCSVProcessor.class);

	//Instance variables declaration for holding CSV record
	private CSVRecord record = null;

	//Instance variables declarations for holding values of CSV records
	//ForOrder
	private String sapOrderNo = null;
	private String hybrisOrderNo = null;
	private String poNo = null;
	private Date reqDeliveryDate = null;
	private Double totalValue = null;
	private Double totalTax = null;
	private Double totalShipment = null;
	private Double totalDiscount = null;
	private String status = null;
	private String containerID = null;
	private String sealNumber = null;
	private String vesselNumber = null;
	private String invoicePDF = null;

	private String bdsDocId = null;
	private String bdsContrEp = null;
	private String bdsCocuClass = null;
	//private String archiveID = null;
	//For Order Entry
	private String erpMaterialID = null;
	private Long orderEntryQty = null;
	private String uom = null;
	private String rejectionReason = null;
	private String rejectedStatus = "Yes";
	private Double itemTotalPrice = null;
	private Double lineItemTotalPrice = null;
	private Double itemTotalShipment = null;
	private Double itemTotalDiscount = null;
	private Double itemTax = null;

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		try
		{
			LOG.info("EnergizerOrderUpdateCSVProcessor:process:Start");
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord tempRecord : records)
			{
				record = tempRecord;
				validate(record);
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					continue;
				}
				if (!readCSVRecord(record))
				{
					continue;
				}

				//Validate status code
				if (energizerOrderService.getEnergizerOrderStatus(status) == null)
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + STATUS + " " + status + " is not valid");
					LOG.info("Valid status codes are " + ENERGIZER_POSSIBLE_ORDER_STATUS_KEY);
					continue;
				}

				OrderModel existEnergizerOrder = energizerOrderService.getExistingOrder(sapOrderNo, hybrisOrderNo);

				if (existEnergizerOrder == null)
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + SAP_ORDER_NO + " " + sapOrderNo + " and "
							+ HYBRIS_ORDER_NO + " " + hybrisOrderNo + " is not exist");
					continue;
				}

				//check for offline order
				//if it is offline order then don't proceed to update
				if (!(StringUtils.isEmpty(existEnergizerOrder.getErpOrderCreator())))
				{
					LOG.info("In Record Number " + record.getRecordNumber() + " " + SAP_ORDER_NO + " " + sapOrderNo + " "
							+ HYBRIS_ORDER_NO + " " + hybrisOrderNo + " is offline order and offline order can not be updated");
					//continue;
				}

				final OrderEntryModel energizerOrderEntryModel = createOrUpdateOrderItem(existEnergizerOrder);
				if (energizerOrderEntryModel != null)
				{
					//Update order if entry is valid for update
					existEnergizerOrder = updateOrder(existEnergizerOrder);
					modelService.save(existEnergizerOrder);
					modelService.save(energizerOrderEntryModel);
					LOG.info(" Energizer order and associated items are saved successfully ");
					existEnergizerOrder
							.setAdjustedTotalPrice(energizerOrderService.getAdjustedTotalPriceForOrder(existEnergizerOrder));
					modelService.save(existEnergizerOrder);
					prepareEmail(existEnergizerOrder);
				}
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}//end of for loop
			LOG.info("EnergizerOrderUpdateCSVProcessor:process:End");
		}//end of try block
		catch (final Exception e)
		{
			LOG.error("EnergizerOrderUpdateCSVProcessor ", e);
		}
		return getCsvFeedErrorRecords();
	}


	private void prepareEmail(final OrderModel existEnergizerOrder)
	{
		final List<String> possibleOrderStatusValues = Arrays.asList(SEND_EMAIL_FOR_ORDER_STATUS.split(new Character(',')
				.toString()));
		final String orderStatusCode = status.toUpperCase();
		final boolean result = possibleOrderStatusValues.contains(orderStatusCode);

		if (result)
		{

			final UserModel user = existEnergizerOrder.getUser();
			final B2BCustomerModel b2bOrderCreator = (B2BCustomerModel) user;
			final Set<B2BCustomerModel> b2bOrderApprovers = existEnergizerOrder.getB2bUnit().getApprovers();

			if (b2bOrderCreator.getActive() && !StringUtils.isEmpty(b2bOrderCreator.getEmail()))
			{
				final String orderCreatorEmail = b2bOrderCreator.getEmail();
				final EmailAddressModel fromEmail = getEmailService().getOrCreateEmailAddressForEmail(FROM_EMAIL_ADDRESS,
						FROM_EMAIL_DISPLAY_NAME);
				EmailAddressModel toEmail = getEmailService().getOrCreateEmailAddressForEmail(orderCreatorEmail, orderCreatorEmail);
				final List<EmailAddressModel> toAddress = new ArrayList<EmailAddressModel>();
				toAddress.add(toEmail);
				final List<EmailAddressModel> orderApproversEmailList = new ArrayList<EmailAddressModel>();
				if (null != b2bOrderApprovers && !b2bOrderApprovers.isEmpty())
				{
					for (final B2BCustomerModel b2bOrderApprover : b2bOrderApprovers)
					{
						if (b2bOrderApprover.getActive() && !StringUtils.isEmpty(b2bOrderApprover.getEmail()))
						{
							toEmail = getEmailService().getOrCreateEmailAddressForEmail(b2bOrderApprover.getEmail(),
									b2bOrderApprover.getEmail());
							orderApproversEmailList.add(toEmail);
						}
					}
				}
				final Map<String, Object> contextmap = new HashMap<String, Object>();
				userService.setCurrentUser(existEnergizerOrder.getUser());
				orderData = getOrderConverter().convert(existEnergizerOrder);
				final BaseSiteModel baseSite = existEnergizerOrder.getSite();
				contextmap.put("orderData", orderData);
				contextmap.put("baseSite", baseSite);
				if (status.equalsIgnoreCase("SHIPPED"))
				{
					energizerGenericEmailGenerationService.generateAndSendEmail("DeliverySentEmailTemplate", toAddress, fromEmail,
							orderApproversEmailList, existEnergizerOrder.getUser().getSessionLanguage(), contextmap);
				}
				if (status.equalsIgnoreCase("CANCELLED"))
				{
					energizerGenericEmailGenerationService.generateAndSendEmail("OrderCancelledEmailTemplate", toAddress, fromEmail,
							orderApproversEmailList, existEnergizerOrder.getUser().getSessionLanguage(), contextmap);
				}
				if (status.equalsIgnoreCase("IN_PROCESS"))
				{
					energizerGenericEmailGenerationService.generateAndSendEmail("OrderInProcessEmailTemplate", toAddress, fromEmail,
							orderApproversEmailList, existEnergizerOrder.getUser().getSessionLanguage(), contextmap);
				}
			}

		}
	}

	/**
	 * This function will update order and return updated order object
	 */
	private OrderModel updateOrder(final OrderModel energizerOrderModel)
	{
		energizerOrderModel.setPurchaseOrderNumber(poNo);
		energizerOrderModel.setRequestedDeliveryDate(reqDeliveryDate);
		energizerOrderModel.setTotalPrice(totalValue);
		energizerOrderModel.setTotalTax(totalTax);
		energizerOrderModel.setDeliveryCost(totalShipment);
		energizerOrderModel.setTotalDiscounts(totalDiscount);
		energizerOrderModel.setStatus(energizerOrderService.getEnergizerOrderStatus(status));
		energizerOrderModel.setContainerId(containerID);
		energizerOrderModel.setSealNumber(sealNumber);
		energizerOrderModel.setVesselNumber(vesselNumber);
		energizerOrderModel.setInvoicePDF(invoicePDF);
		energizerOrderModel.setDocumentID(bdsDocId);
		energizerOrderModel.setDocumentClass(bdsCocuClass);
		energizerOrderModel.setContrEP(bdsContrEp);


		return energizerOrderModel;
	}

	/**
	 * This function will create or update OrderEntry for a given Order
	 * 
	 * @param energizerOrderModel
	 * @return OrderEntryModel
	 */
	private OrderEntryModel createOrUpdateOrderItem(final OrderModel energizerOrderModel)
	{
		OrderEntryModel energizerOrderEntry = null;

		final EnergizerProductModel existEnergizerProduct = (EnergizerProductModel) productService.getProductForCode(erpMaterialID);

		UnitModel existUnit;
		try
		{
			existUnit = unitService.getUnitForCode("EA");
		}
		catch (final Exception e)
		{
			existUnit = null;
		}

		if (existEnergizerProduct == null)
		{
			LOG.info("In Record Number " + record.getRecordNumber() + " " + ERP_MATERIAL_ID + " " + erpMaterialID);
		}
		else
		{
			final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialID,
					energizerOrderModel.getB2bUnit().getUid());
			final String customerUOM = energizerCMIRModel.getUom();
			Integer customerUOMMultiplier = 0;
			Integer salesUOMMultiplier = 0;
			Integer finalConversionFactor = 0;
			/*
			 * if (energizerCMIRModel == null) { LOG.info("No CMIR exist for Record Number " + record.getRecordNumber() +
			 * " " + ERP_MATERIAL_ID + " " + erpMaterialID + " and B2B unit " + energizerOrderModel.getB2bUnit().getUid());
			 * } else {
			 */
			energizerOrderEntry = energizerOrderService.getExistingOrderItem(energizerOrderModel, existEnergizerProduct);
			// e.g customerUOM = Pallet, uom from SAP order update = cases
			for (final EnergizerProductConversionFactorModel convertion : existEnergizerProduct.getProductConversionFactors())
			{
				if (customerUOM.equalsIgnoreCase(convertion.getAlternateUOM()))
				{
					customerUOMMultiplier = convertion.getConversionMultiplier();
				}
				if (uom.equalsIgnoreCase(convertion.getAlternateUOM()))
				{
					salesUOMMultiplier = convertion.getConversionMultiplier();
				}
			}
			if (energizerOrderEntry == null)
			{
				energizerOrderEntry = modelService.create(OrderEntryModel.class);
				energizerOrderEntry.setOrder(energizerOrderModel);
				energizerOrderEntry.setProduct(existEnergizerProduct);
				energizerOrderEntry.setQuantity(orderEntryQty);
				energizerOrderEntry.setTotalPrice(lineItemTotalPrice);
				if (!customerUOM.equalsIgnoreCase(uom))
				{
					energizerOrderEntry.setBasePrice(itemTotalPrice * customerUOMMultiplier * finalConversionFactor);
				}
				else
				{
					energizerOrderEntry.setBasePrice(itemTotalPrice * customerUOMMultiplier);
				}
			}
			else
			{
				if (!customerUOM.equalsIgnoreCase(uom))
				{
					// for pilot project we are only suppose to get final conversions for PAL and LAY UOM's
					// and they are always bigger than incoming UOM(sales uom)
					finalConversionFactor = customerUOMMultiplier / salesUOMMultiplier;
					energizerOrderEntry.setAdjustedQty(new Integer(0));
					energizerOrderEntry.setAdjustedLinePrice(new BigDecimal("0.00"));
					// update the value with incoming value
					energizerOrderEntry.setAdjustedQty(orderEntryQty.intValue() / finalConversionFactor);
					//since the price UOM is always maintained at each (as per confirmation from Hitesh Wadhwa )
					energizerOrderEntry.setAdjustedLinePrice(BigDecimal.valueOf(lineItemTotalPrice));
					energizerOrderEntry.setAdjustedItemPrice(new BigDecimal(itemTotalPrice * customerUOMMultiplier
							* finalConversionFactor));
				}
				else
				{
					//clear the previous value before updating 
					energizerOrderEntry.setAdjustedQty(new Integer(0));
					energizerOrderEntry.setAdjustedLinePrice(new BigDecimal("0.00"));
					// update the value with incoming value
					energizerOrderEntry.setAdjustedQty(orderEntryQty.intValue());
					energizerOrderEntry.setAdjustedItemPrice(new BigDecimal(itemTotalPrice * customerUOMMultiplier));
					energizerOrderEntry.setAdjustedLinePrice(BigDecimal.valueOf(lineItemTotalPrice));
				}
			}
			energizerOrderEntry.setUnit(existUnit);
			energizerOrderEntry.setRejectionReason(rejectionReason);
			energizerOrderEntry.setRejectedStatus(rejectedStatus);
			energizerOrderEntry.setItemTotalShipment(itemTotalShipment);

			if (itemTotalPrice != null && itemTotalDiscount != null)
			{
				//Create Discount value list and attach with order entry
				final DiscountValue discountValue1 = new DiscountValue("SAP Discount", itemTotalPrice, true, itemTotalDiscount,
						energizerOrderModel.getCurrency().getIsocode());
				final List<DiscountValue> discountValuesList = new ArrayList<DiscountValue>();
				discountValuesList.add(discountValue1);
				energizerOrderEntry.setDiscountValues(discountValuesList);

			}
			if (itemTotalPrice != null && itemTax != null)
			{
				//Create Tax value list and attach with order entry
				final TaxValue taxValue1 = new TaxValue("SAP TAX", itemTotalPrice, true, itemTax, energizerOrderModel.getCurrency()
						.getIsocode());
				final List<TaxValue> taxValuesList = new ArrayList<TaxValue>();
				taxValuesList.add(taxValue1);
				energizerOrderEntry.setTaxValues(taxValuesList);
			}
			//}
		}
		return energizerOrderEntry;
	}

	/**
	 * This function will read CSV Record and update instance variable related to CSV Processor this function will true
	 * if record is successfully read and if record is having some errors then it will return false
	 * 
	 * @param record
	 * @return boolean
	 */
	private boolean readCSVRecord(final CSVRecord record)
	{
		boolean isValid = true;
		final StringBuffer errBuffer = new StringBuffer();
		errBuffer.append("Record Number " + record.getRecordNumber() + " has invalid field");
		final Map<String, String> csvValuesMap = record.toMap();

		//NumberUtils.isNumber function will check for null, empty and number
		//StringUtils.isEmpty function will check null and empty
		//Read all CSV feeds

		if (!StringUtils.isEmpty(csvValuesMap.get(SAP_ORDER_NO)))
		{
			sapOrderNo = csvValuesMap.get(SAP_ORDER_NO);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(HYBRIS_ORDER_NO)))
		{
			hybrisOrderNo = csvValuesMap.get(HYBRIS_ORDER_NO);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(PO_NO)))
		{
			poNo = csvValuesMap.get(PO_NO);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(REQ_DELIVERY_DATE)))
		{
			try
			{
				reqDeliveryDate = ORDER_DATE_FORMATTER.parse(csvValuesMap.get(REQ_DELIVERY_DATE));
			}
			catch (final ParseException e)
			{
				isValid = false;
				errBuffer.append(" || " + REQ_DELIVERY_DATE + " = " + csvValuesMap.get(REQ_DELIVERY_DATE) + " has invalid format: "
						+ e.getMessage());
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(TOTAL_VALUE)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(TOTAL_VALUE)))
			{
				totalValue = NumberUtils.createDouble(csvValuesMap.get(TOTAL_VALUE));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + TOTAL_VALUE + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(TOTAL_TAX)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(TOTAL_TAX)))
			{
				totalTax = NumberUtils.createDouble(csvValuesMap.get(TOTAL_TAX));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + TOTAL_TAX + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(TOTAL_SHIPMENT)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(TOTAL_SHIPMENT)))
			{
				totalShipment = NumberUtils.createDouble(csvValuesMap.get(TOTAL_SHIPMENT));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + TOTAL_SHIPMENT + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(TOTAL_DISCOUNT)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(TOTAL_DISCOUNT)))
			{
				totalDiscount = NumberUtils.createDouble(csvValuesMap.get(TOTAL_DISCOUNT));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + TOTAL_DISCOUNT + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(STATUS)))
		{
			status = ENERGIZER_POSSIBLE_ORDER_STATUS_MAP.get(csvValuesMap.get(STATUS));
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(CONTAINER_ID)))
		{
			containerID = csvValuesMap.get(CONTAINER_ID);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(SEAL_NUMBER)))
		{
			sealNumber = csvValuesMap.get(SEAL_NUMBER);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(VESSAL_NUMBER)))
		{
			vesselNumber = csvValuesMap.get(VESSAL_NUMBER);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(INVOICE_PDF)))
		{
			invoicePDF = csvValuesMap.get(INVOICE_PDF);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ARCHIVE_ID)))
		{
			//archiveID = csvValuesMap.get(ARCHIVE_ID);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ERP_MATERIAL_ID)))
		{
			erpMaterialID = csvValuesMap.get(ERP_MATERIAL_ID);
		}
		if (!StringUtils.isEmpty(csvValuesMap.get(BDS_DOCID)))
		{
			bdsDocId = csvValuesMap.get(BDS_DOCID);
		}
		if (!StringUtils.isEmpty(csvValuesMap.get(BDS_CONTREP)))
		{
			bdsContrEp = csvValuesMap.get(BDS_CONTREP);
		}
		if (!StringUtils.isEmpty(csvValuesMap.get(BDS_DOCUCLASS)))
		{
			bdsCocuClass = csvValuesMap.get(BDS_DOCUCLASS);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ORDER_ENTRY_QTY)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ORDER_ENTRY_QTY)))
			{
				try
				{
					orderEntryQty = NumberUtils.createLong(csvValuesMap.get(ORDER_ENTRY_QTY));
				}
				catch (final NumberFormatException e)
				{
					isValid = false;
					errBuffer.append(" || " + ORDER_ENTRY_QTY + " is not a integer " + e.getMessage());
				}
			}

		}

		if (!StringUtils.isEmpty(csvValuesMap.get(UOM)))
		{
			uom = csvValuesMap.get(UOM);
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(REJECTION_REASON)))
		{
			rejectionReason = csvValuesMap.get(REJECTION_REASON);
			if (rejectionReason.trim().equals("0"))
			{
				rejectedStatus = "No";
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ITEM_TOTAL_PRICE)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ITEM_TOTAL_PRICE)))
			{
				itemTotalPrice = NumberUtils.createDouble(csvValuesMap.get(ITEM_TOTAL_PRICE));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + ITEM_TOTAL_PRICE + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(LINE_ITEM_TOTAL_PRICE)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(LINE_ITEM_TOTAL_PRICE)))
			{
				lineItemTotalPrice = NumberUtils.createDouble(csvValuesMap.get(LINE_ITEM_TOTAL_PRICE));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + LINE_ITEM_TOTAL_PRICE + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ITEM_TOTAL_SHIPMENT)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ITEM_TOTAL_SHIPMENT)))
			{
				itemTotalShipment = NumberUtils.createDouble(csvValuesMap.get(ITEM_TOTAL_SHIPMENT));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + ITEM_TOTAL_SHIPMENT + " is not an number ");
			}
		}

		if (!StringUtils.isEmpty(csvValuesMap.get(ITEM_TOTAL_DISCOUNT)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ITEM_TOTAL_DISCOUNT)))
			{
				itemTotalDiscount = NumberUtils.createDouble(csvValuesMap.get(ITEM_TOTAL_DISCOUNT));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + ITEM_TOTAL_DISCOUNT + " is not an number ");
			}
		}


		if (!StringUtils.isEmpty(csvValuesMap.get(ITEM_TAX)))
		{
			if (NumberUtils.isNumber(csvValuesMap.get(ITEM_TAX)))
			{
				itemTax = NumberUtils.createDouble(csvValuesMap.get(ITEM_TAX));
			}
			else
			{
				isValid = false;
				errBuffer.append(" || " + ITEM_TAX + " is not an number ");
			}
		}

		//Generate Log if record is not readable
		if (!isValid)
		{
			LOG.info(errBuffer);
		}

		return isValid;
	}

	/**
	 * @param record
	 * @return EnergizerCSVFeedError
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;

		if (!hasMandatoryFields(record, getHeadersForFeed(ENERGIZER_ORDER_UPDATE_FEED_MANDATORY_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(Config.getParameter(ENERGIZER_ORDER_UPDATE_FEED_MANDATORY_KEY).split(
					new Character(DELIMETER).toString()));
			error = new EnergizerCSVFeedError();
			final List<String> columnNames = new ArrayList<String>();
			final List<Integer> columnNumbers = new ArrayList<Integer>();
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			long recordFailed = getRecordFailed();
			for (final String columnHeader : map.keySet())
			{
				columnNumber++;
				if (mandatoryFields.contains(columnHeader))
				{

					final String value = map.get(columnHeader);
					if (StringUtils.isEmpty(value))
					{
						columnNames.add(columnHeader);
						columnNumbers.add(columnNumber);
					}
				}
			}
			error.setLineNumber(record.getRecordNumber());
			error.setColumnName(columnNames);
			error.setColumnNumber(columnNumbers);
			error.setMessage(" column should not be empty");
			error.setUserType(BUSINESS_USER);
			getTechnicalFeedErrors().add(error);
			setTechRecordError(getTechnicalFeedErrors().size());
			recordFailed++;
			setRecordFailed(recordFailed);
		}
		return error;
	}

	protected Converter<OrderModel, OrderData> getOrderConverter()
	{
		return orderConverter;
	}

	@Required
	public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter)
	{
		this.orderConverter = orderConverter;
	}

	/**
	 * @return the emailService
	 */
	public EmailService getEmailService()
	{
		return emailService;
	}


	/**
	 * @param emailService
	 *           the emailService to set
	 */
	public void setEmailService(final EmailService emailService)
	{
		this.emailService = emailService;
	}


}
