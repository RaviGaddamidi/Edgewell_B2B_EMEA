/**
 * 
 */
package com.energizer.core.actions;

import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.process.approval.actions.AbstractSimpleB2BApproveOrderDecisionAction;
import de.hybris.platform.b2b.process.approval.actions.B2BPermissionResultHelperImpl;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BPermissionService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author kaushik.ganguly
 * 
 */
public class FindApprovers extends AbstractSimpleB2BApproveOrderDecisionAction
{
	private static final Logger LOG = Logger.getLogger(FindApprovers.class);
	private DefaultB2BPermissionService b2bPermissionService;
	private B2BPermissionResultHelperImpl permissionResultHelper;

	@Override
	public AbstractSimpleDecisionAction.Transition executeAction(final B2BApprovalProcessModel approvalProcess)
			throws RetryLaterException
	{
		OrderModel order = null;
		try
		{
			order = approvalProcess.getOrder();

			final Collection openPermissionsForOrder = getPermissionResultHelper().filterResultByPermissionStatus(
					order.getPermissionResults(), PermissionStatus.OPEN);

			final Set permissionResults = b2bPermissionService.getApproversForOpenPermissions(order,
					(EnergizerB2BCustomerModel) order.getUser(), openPermissionsForOrder);

			if (CollectionUtils.isNotEmpty(permissionResults))
			{
				order.setPermissionResults(permissionResults);
				this.modelService.save(order);
				return AbstractSimpleDecisionAction.Transition.OK;
			}
			return AbstractSimpleDecisionAction.Transition.NOK;
		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage(), e);
			handleError(order, e);
		}
		return AbstractSimpleDecisionAction.Transition.NOK;
	}

	private void handleError(final OrderModel order, final Exception e)
	{
		if (order != null)
		{
			setOrderStatus(order, OrderStatus.B2B_PROCESSING_ERROR);
		}
		LOG.error(e.getMessage(), e);
	}

	public DefaultB2BPermissionService getB2bPermissionService()
	{
		return this.b2bPermissionService;
	}

	@Required
	public void setB2bPermissionService(final DefaultB2BPermissionService b2bPermissionService)
	{
		this.b2bPermissionService = b2bPermissionService;
	}

	public B2BPermissionResultHelperImpl getPermissionResultHelper()
	{
		return this.permissionResultHelper;
	}

	@Required
	public void setPermissionResultHelper(final B2BPermissionResultHelperImpl permissionResultHelper)
	{
		this.permissionResultHelper = permissionResultHelper;
	}



}
