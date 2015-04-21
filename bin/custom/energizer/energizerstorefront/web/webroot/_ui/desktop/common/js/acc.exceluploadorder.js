
$(document).ready(function(){
	
	$(".excelOrderSubmitButton").attr("disabled",true);
	$(".shippingPointRadioButton").change(function(){
		
		if($(".shippingPointRadioButton:checked").length>0)
			{
				$(".excelOrderSubmitButton").attr("disabled",false);
			}
		else
			{
				$(".excelOrderSubmitButton").attr("disabled",true);
			}		
	});
	
});
