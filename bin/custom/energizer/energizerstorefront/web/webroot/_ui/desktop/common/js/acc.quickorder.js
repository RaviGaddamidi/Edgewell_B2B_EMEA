ACC.quickorder = {
		
	bindValidation:function()
	{
		
		$("input.orderEntryQuantity").change(function(){
				qty=$(this).val();
			try
			{
				if(qty.match('\\d\\.{1,}')!=null)
					{
						$(this).parent().children("span").text(enterWholeNumber).removeClass("display_none");
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
						return;
					}
				qty=parseInt(qty);
				if(qty<=0)
					{
						$(this).parent().children("span").text(enterPositiveNumber).removeClass("display_none");
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
						return;
					}
				else
					{
						$(this).parent().children("span").text("").addClass("display_none");
						$(this).removeClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
					}
				if(ACC.quickorder.isValidUOM(qty,parseInt($(this).next("input").val())))
					{
						$(this).parent().children("span").text("").addClass("display_none");
						$(this).removeClass("red_border_input");
						ACC.quickorder.updateOrderedQty($(this).val(),$(this).prev().val())
					}
				else
					{
						$(this).parent().children("span").text(enterMOQMultiples+" - "+$(this).next("input").val()).removeClass("display_none");
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
					}
			}
			catch(err)
			{
				$(this).parent().children("span").text(inputValidNumber).removeClass("display_none");
				$(this).addClass("red_border_input");
				ACC.quickorder.enableOrDisableCheckoutButton();
			}
			
			
		});
	},

	isValidUOM:function(val,uom)
	{
		if(val==uom || val%uom==0)
			{
				return true;
			}
		else
			{
				return false;
			}
		
	},
	enableOrDisableCheckoutButton:function()
	{
		if($(".quickorder_errormsg:visible").length==0)
			{
			$(".quickOrderSubmitButton").attr("disabled",false);
			}
		else
			{
			$(".quickOrderSubmitButton").attr("disabled",true);
			}
		
	},
	
	updateOrderedQty:function(qty,productCode)
	{
		$.ajax({
			url:'/my-account/quickorder/qtyAjaxUpdate',
			data:{'qty':qty,'productCode':productCode},
			
			success:function()
			{
				ACC.quickorder.enableOrDisableCheckoutButton();
			}
		})
		
	}
};


$(document).ready(function(){
	
	if($("body.page-quickorderpage"))
		{
		 ACC.quickorder.bindValidation();
		}
	
	
});
