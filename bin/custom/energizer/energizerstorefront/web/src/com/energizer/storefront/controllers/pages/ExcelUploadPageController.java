/**
 *
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.product.data.EnergizerFileUploadData;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.facades.order.EnergizerExcelUploadFacade;
import com.energizer.facades.quickorder.EnergizerQuickOrderFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.ControllerConstants.Views;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ContainerUtilizationForm;
import com.energizer.storefront.forms.ExcelUploadForm;
import com.energizer.storefront.forms.UpdateQuantityForm;


/**
 * @author M9005674
 *
 */

@Controller
@Scope("tenant")
@RequestMapping("/my-cart")
public class ExcelUploadPageController extends AbstractSearchPageController
{

	protected static final Logger LOG = Logger.getLogger(ExcelUploadPageController.class);

	private static final String PRODUCT_ENTRIES_PAGE = "productentries";
	private static final String CART_CMS_PAGE = "cartPage";
	private static final String CONTINUE_URL = "continueUrl";
	private static final String EXCEL_ORDER_AJAX_CALL = "/excelUpload/updateOrderQuantity";
	private static final String CART = "/cart";

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	@Resource
	private EnergizerExcelUploadFacade energizerExcelRowtoModelFacade;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService shippingPointBusinessRulesService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService cartEntryBusinessRulesService;

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "quickOrderFacade")
	private EnergizerQuickOrderFacade quickOrderFacade;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerProductService energizerProductService;

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	ContainerUtilizationForm contUtilForm = new ContainerUtilizationForm();

	String containerHeight, packingOption;

	boolean enableButton = false;

	boolean enableForB2BUnit = false;

	@Value("${excelFileSize}")
	private String excelFileSize;

	Map<String, List<EnergizerFileUploadData>> shipmentMap = new HashMap<String, List<EnergizerFileUploadData>>();
	Map<String, String> shipmentNameMap = new HashMap<String, String>();

	@RequestMapping(value = "/excelFileToUpload", method = RequestMethod.POST)
	@RequireHardLogIn
	public String uploadExcelFile(final Model model, @RequestParam("file") final CommonsMultipartFile file)
			throws CMSItemNotFoundException
	{

		storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.excelFileUpload"));
		model.addAttribute("metaRobots", "no-index,no-follow");

		List<EnergizerFileUploadData> energizerExcelUploadModels = new ArrayList<EnergizerFileUploadData>();

		if (shipmentMap != null && !shipmentMap.keySet().isEmpty())
		{
			shipmentMap.clear();
		}

		if (file == null || file.isEmpty())
		{
			GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadEmpty");
			return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
		}
		else if (file.getSize() > Double.valueOf(excelFileSize))
		{
			GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadSize");
			return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
		}
		else if (!file.isEmpty())
		{
			Iterator<Row> rowIterator = null;

			LOG.info(" Name of the file " + file.getOriginalFilename());

			try
			{
				EnergizerFileUploadData uploadData = null;
				final List<EnergizerFileUploadData> energizerFileUploadDatas = new ArrayList<EnergizerFileUploadData>();
				if (file.getOriginalFilename().endsWith(".xls"))
				{
					final HSSFWorkbook hworkbook = new HSSFWorkbook(file.getInputStream());
					final HSSFSheet hsheet = hworkbook.getSheetAt(0);
					rowIterator = hsheet.rowIterator();
				}
				else if (file.getOriginalFilename().endsWith(".xlsx"))
				{
					final XSSFWorkbook xworkbook = new XSSFWorkbook(file.getInputStream());
					final XSSFSheet xsheet = xworkbook.getSheetAt(0);
					rowIterator = xsheet.rowIterator();
				}
				else
				{
					LOG.warn("Format doesn't support, upload only xls and xlsx types");
					GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileUploadFormat");
					return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
				}
				while (rowIterator.hasNext())
				{
					final Row row = rowIterator.next();

					if (!(row.getRowNum() == 0))
					{

						final String materialId = validateAndGetString(row.getCell(0));
						final String customerMaterailId = validateAndGetString(row.getCell(1));

						if (materialId != null || customerMaterailId != null)
						{
							uploadData = new EnergizerFileUploadData();



							if (row.getCell(3) != null)
							{
								try
								{

									final String val = row.getCell(3).toString().trim();
									final Long quantity = new Double(val).longValue();
									if (quantity > 0)
									{
										uploadData.setCustomerMaterialId(customerMaterailId);
										uploadData.setMaterialId(materialId);
										uploadData.setQuantity(quantity);

									}


								}
								catch (final Exception ise)
								{
									LOG.error("cannot convert " + row.getCell(2).getStringCellValue() + " into number");
									GlobalMessages.addErrorMessage(model, "text.account.excelUpload.badDataForQuantity");
								}
							}
							energizerFileUploadDatas.add(uploadData);
						}
					}

				}
				if (energizerFileUploadDatas.size() == 0)
				{
					GlobalMessages.addErrorMessage(model, "text.account.excelUpload.noRowsFound");
					return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
				}

				energizerExcelUploadModels = energizerExcelRowtoModelFacade.convertExcelRowtoBean(energizerFileUploadDatas);
				for (final EnergizerFileUploadData energizerCMIRModel : energizerExcelUploadModels)
				{
					if (energizerCMIRModel.isHasError())
					{
						GlobalMessages.addMessage(model, "accErrorMsgs", "text.account.excelUpload.productnotfound", new Object[]
						{ energizerCMIRModel.getMessage() });
					}
					else
					{
						final String shipmentPointId = (energizerCMIRModel.getShippingPoint());
						shipmentNameMap.put(shipmentPointId, energizerProductService.getShippingPointName(shipmentPointId));
						if (shipmentMap.containsKey(shipmentPointId))
						{
							final List<EnergizerFileUploadData> tempList = shipmentMap.get(shipmentPointId);
							tempList.add(energizerCMIRModel);
						}
						else
						{
							final List<EnergizerFileUploadData> uploadDataCMIRList = new ArrayList<EnergizerFileUploadData>();
							uploadDataCMIRList.add(energizerCMIRModel);
							shipmentMap.put(shipmentPointId, uploadDataCMIRList);

						}
					}
				}
				model.addAttribute("shipmentData", shipmentMap);
				model.addAttribute("shipmentName", shipmentNameMap);

			}
			catch (final FileNotFoundException fne)
			{
				LOG.error("File Not Found");
				GlobalMessages.addErrorMessage(model, "text.account.excelUpload.fileNotFound");
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
			catch (final IOException e)
			{
				LOG.error("Unable to convert the file into stream");
				GlobalMessages.addErrorMessage(model, "text.account.excelUpload.unableToConvert");
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
			catch (final Exception e)
			{
				LOG.error(e.getMessage());
				return ControllerConstants.Views.Pages.Account.AccountExcelUpload;
			}
		}

		model.addAttribute("cartData", quickOrderFacade.getCurrentSessionCart());
		return ControllerConstants.Views.Pages.Account.AccountExcelUploadEntries;
	}

	@RequestMapping(value = "/addtocart", method = RequestMethod.POST)
	@RequireHardLogIn
	public String addtocart(@ModelAttribute("excelUploadForm") final ExcelUploadForm excelUploadForm, final Model model,
			final HttpSession session) throws CMSItemNotFoundException
	{

		final String shipmentPoint = excelUploadForm.getShippingPoint();
		final List<EnergizerFileUploadData> orderEntryList = new ArrayList<EnergizerFileUploadData>();
		final List<BusinessRuleError> orderEntryErrors = new ArrayList<BusinessRuleError>();

		if (shippingPointBusinessRulesService.getErrors() != null && !shippingPointBusinessRulesService.getErrors().isEmpty())
		{
			shippingPointBusinessRulesService.getErrors().clear();
		}
		if (cartEntryBusinessRulesService.getErrors() != null && !cartEntryBusinessRulesService.getErrors().isEmpty())
		{
			cartEntryBusinessRulesService.getErrors().clear();
		}

		if (shipmentMap.containsKey(shipmentPoint))
		{
			final List<EnergizerFileUploadData> productsList = shipmentMap.get(shipmentPoint);
			for (final EnergizerFileUploadData energizerFileUploadData : productsList)
			{
				final EnergizerCMIRModel cmir = quickOrderFacade.getCMIRForProductCodeOrCustomerMaterialID(
						energizerFileUploadData.getMaterialId(), energizerFileUploadData.getCustomerMaterialId());

				final OrderEntryData orderEntry = quickOrderFacade.getProductData(energizerFileUploadData.getMaterialId(),
						energizerFileUploadData.getCustomerMaterialId(), cmir);

				model.addAttribute("shipmentData", shipmentMap);
				model.addAttribute("shipmentName", shipmentNameMap);


				if (orderEntry != null)
				{
					orderEntry.setQuantity(energizerFileUploadData.getQuantity());
					model.addAttribute("cartShippingPoint",
							orderEntry.getReferenceShippingPoint() != null ? orderEntry.getReferenceShippingPoint() : "");

					shippingPointBusinessRulesService.validateBusinessRules(orderEntry);
					cartEntryBusinessRulesService.validateBusinessRules(orderEntry);

					if (!shippingPointBusinessRulesService.hasErrors() && !cartEntryBusinessRulesService.hasErrors())
					{
						orderEntryList.add(energizerFileUploadData);
					}
					if (shippingPointBusinessRulesService.hasErrors())
					{
						orderEntryErrors.addAll(shippingPointBusinessRulesService.getErrors());
						shippingPointBusinessRulesService.getTempErrors().clear();
					}
					if (cartEntryBusinessRulesService.hasErrors())
					{
						orderEntryErrors.addAll(cartEntryBusinessRulesService.getErrors());
						cartEntryBusinessRulesService.getTempErrors().clear();
					}
				}
				else
				{
					GlobalMessages.addErrorMessage(model, "quickorder.addtocart.cmir.badData");
				}
			}
		}

		if (orderEntryErrors != null && orderEntryErrors.size() > 0)
		{
			storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_ENTRIES_PAGE));
			model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.excelFileUpload"));
			model.addAttribute("metaRobots", "no-index,no-follow");

			for (final BusinessRuleError error : orderEntryErrors)
			{
				LOG.info("The error message is " + error.getMessage());
				GlobalMessages.addBusinessRuleMessage(model, error.getMessage());
			}
			return ControllerConstants.Views.Pages.Account.AccountExcelUploadEntries;
		}
		else
		{
			for (final EnergizerFileUploadData entry : orderEntryList)
			{
				try
				{
					cartFacade.addToCart(entry.getMaterialId(), entry.getQuantity());
				}
				catch (final CommerceCartModificationException e)
				{
					LOG.error("Problem in adding items to Cart");
				}
			}
		}

		/*
		 * if (session.getAttribute("containerHeight") != null && session.getAttribute("packingOption") != null) {
		 * contUtilForm.setContainerHeight((String) session.getAttribute("containerHeight"));
		 * contUtilForm.setPackingType((String) session.getAttribute("packingOption")); }
		 */

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		//final boolean enableButton = b2bUnit.getEnableContainerOptimization();
		final boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization();
		if (b2bUnit.getEnableContainerOptimization() == false)
		{
			enableButton = b2bUnit.getEnableContainerOptimization();
		}
		else if (session.getAttribute("enableButton") != null)
		{
			enableButton = (boolean) session.getAttribute("enableButton");
		}
		prepareDataForPage(model);
		model.addAttribute("enableButton", enableButton);
		model.addAttribute("enableForB2BUnit", enableForB2BUnit);
		shipmentMap.clear();
		return ControllerConstants.Views.Pages.Cart.CartPage;
	}

	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		final String continueUrl = (String) getSessionService().getAttribute(WebConstants.CONTINUE_URL);
		model.addAttribute(CONTINUE_URL, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);

		if (sessionService.getAttribute(WebConstants.CART_RESTORATION) instanceof CartRestorationData)
		{
			final CartRestorationData restorationData = (CartRestorationData) sessionService
					.getAttribute(WebConstants.CART_RESTORATION);
			model.addAttribute("restorationData", restorationData);
		}
		createProductList(model);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
	}

	protected void createProductList(final Model model) throws CMSItemNotFoundException
	{
		final CartData cartData = cartFacade.getSessionCart();
		final List<String> businessRuleErrors = new ArrayList<String>();
		boolean errorMessages = false;

		reverseCartProductsOrder(cartData.getEntries());
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			boolean flag = false;
			String productWithCmirInActive = "";
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateQuantityForm uqf = new UpdateQuantityForm();
				uqf.setQuantity(entry.getQuantity());
				model.addAttribute("updateQuantityForm" + entry.getEntryNumber(), uqf);
				if (entry.getProduct().isIsActive() == false)
				{
					productWithCmirInActive += entry.getProduct().getErpMaterialID() + "  ";
					flag = true;

				}
			}
			if (flag == true)
			{
				GlobalMessages.addMessage(model, "accErrorMsgs", "cart.cmirinactive", new Object[]
				{ productWithCmirInActive });
				//return FORWARD_PREFIX + "/cart";
			}

		}

		if (contUtilForm.getContainerHeight() != null || contUtilForm.getPackingType() != null)
		{
			containerHeight = contUtilForm.getContainerHeight();
			packingOption = contUtilForm.getPackingType();
		}
		else
		{
			containerHeight = Config.getParameter("energizer.default.containerHeight");

			packingOption = Config.getParameter("energizer.default.packingOption");
		}

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		//final boolean enableContOptimization = b2bUnit.getEnableContainerOptimization();

		LOG.info(" Enable Container Optimization value: " + enableButton);


		final CartData cartDataUpdationforContainer = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart(),
				containerHeight, packingOption, enableButton);
		//final CartData cartDataUpdationforContainer = null;

		if (cartData.isIsFloorSpaceFull() && cartData.getContainerPackingType().equalsIgnoreCase("2 SLIP SHEETS") && enableButton)
		{
			GlobalMessages.addErrorMessage(model, "errorMessages.enable.2slipsheet");
		}

		final List<String> message = energizerCartService.getMessages();
		if (message != null && message.size() > 0)
		{
			for (final String messages : message)
			{
				if (messages.contains("20"))
				{
					GlobalMessages.addMessage(model, "accErrorMsgs", "errormessage.greaterthan.totalpalletcount", new Object[]
					{ "20" });
				}
				else if (messages.contains("40"))
				{
					GlobalMessages.addMessage(model, "accErrorMsgs", "errormessage.greaterthan.totalpalletcount", new Object[]
					{ "40" });
				}
				else if (message.contains("2 wooden base packing material"))
				{
					GlobalMessages.addErrorMessage(model, "errormessage.partialpallet");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, messages);
				}
			}
			errorMessages = true;
		}

		cartDataUpdationforContainer.setBusinesRuleErrors(businessRuleErrors);


		final List<String> containerHeightList = Arrays.asList(Config.getParameter("possibleContainerHeights").split(
				new Character(',').toString()));

		final List<String> packingOptionsList;
		if (containerHeight.equals("20FT"))
		{
			packingOptionsList = Arrays.asList(Config.getParameter("possiblePackingOptions.20FT").split(
					new Character(',').toString()));
		}
		else
		{

			packingOptionsList = Arrays.asList(Config.getParameter("possiblePackingOptions").split(new Character(',').toString()));
		}


		contUtilForm.setContainerHeight(containerHeight);
		contUtilForm.setPackingType(packingOption);
		cartDataUpdationforContainer.setFloorSpaceProductsMap(energizerCartService.getFloorSpaceProductsMap());
		cartDataUpdationforContainer.setNonPalletFloorSpaceProductsMap(energizerCartService.getNonPalletFloorSpaceProductsMap());
		cartDataUpdationforContainer.setProductsNotAddedToCart(energizerCartService.getProductNotAddedToCart());
		cartDataUpdationforContainer.setProductsNotDoubleStacked(energizerCartService.getProductsNotDoublestacked());

		energizerB2BCheckoutFlowFacade.setContainerAttributes(cartDataUpdationforContainer);

		model.addAttribute("containerHeightList", containerHeightList);
		model.addAttribute("packingOptionList", packingOptionsList);
		model.addAttribute("errorMessages", errorMessages);


		//model.addAttribute("productList", productList);
		//model.addAttribute("productsNotDoubleStacked", productsNotDoubleStacked);
		model.addAttribute("containerUtilizationForm", contUtilForm);
		model.addAttribute("enableButton", enableButton);


		model.addAttribute("cartData", cartDataUpdationforContainer);

		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CART_CMS_PAGE));
	}

	protected void reverseCartProductsOrder(final List<OrderEntryData> entries)
	{
		if (entries != null)
		{
			Collections.reverse(entries);
		}
	}

	@RequestMapping(value = EXCEL_ORDER_AJAX_CALL, method = RequestMethod.GET)
	@RequireHardLogIn
	public @ResponseBody Map<String, List<EnergizerFileUploadData>> excelUploadQuantityUpdate(final Model model,
			@RequestParam("quantity") final Long quantity, @RequestParam("erpMaterialCode") final String erpMaterialCode)
			throws CMSItemNotFoundException
	{
		List<EnergizerFileUploadData> excelDataList = null;

		for (final Entry<String, List<EnergizerFileUploadData>> entry : shipmentMap.entrySet())
		{
			excelDataList = new ArrayList<EnergizerFileUploadData>();
			excelDataList = entry.getValue();
			for (final EnergizerFileUploadData excelDataRow : excelDataList)
			{
				if (excelDataRow.getMaterialId().equals(erpMaterialCode))
				{
					excelDataRow.setQuantity(quantity);
					break;
				}
			}
		}
		return shipmentMap;
	}

	private String validateAndGetString(final Cell cell)
	{
		final DataFormatter formatter = new DataFormatter(Locale.US);
		final org.apache.poi.ss.util.CellReference ref = new org.apache.poi.ss.util.CellReference(cell);
		if (cell != null && !(StringUtils.isBlank(formatter.formatCellValue(cell))))
		{
			LOG.info("The value of " + ref.formatAsString() + " is " + formatter.formatCellValue(cell));
		}

		return cell == null ? null : StringUtils.isBlank(formatter.formatCellValue(cell)) ? null : formatter.formatCellValue(cell);

	}


	/*
	 * Redirect to /my-cart/cart page ,once action is performed on /my-cart/addtoCart Page...
	 */

	@RequestMapping(value = CART, method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateContainerUtil(@Valid final ContainerUtilizationForm containerUtilizationForm, final Model model,
			final BindingResult bindingErrors, final RedirectAttributes redirectAttributes, final HttpServletRequest request,
			final HttpSession session) throws CMSItemNotFoundException
	{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final boolean enableForB2BUnit = b2bUnit.getEnableContainerOptimization();
		final String str = request.getParameter("choice");
		if (str != null && str.equals("Yes"))
		{
			LOG.info("Enable radio button :");
			enableButton = true;
		}

		if (str != null && str.equals("No"))
		{
			LOG.info("radio button value:");
			enableButton = false;
		}
		if (b2bUnit.getEnableContainerOptimization() == false)
		{
			enableButton = b2bUnit.getEnableContainerOptimization();
		}
		cartEntryBusinessRulesService.clearErrors();


		contUtilForm.setContainerHeight(containerUtilizationForm.getContainerHeight());
		contUtilForm.setPackingType(containerUtilizationForm.getPackingType());
		session.setAttribute("enableButton", enableButton);


		prepareDataForPage(model);
		model.addAttribute("enableForB2BUnit", enableForB2BUnit);
		return Views.Pages.Cart.CartPage;
	}

}
