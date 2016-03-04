
function submitForm() {
    document.getElementById("form").submit();
}

function getPackingOptionChange(elem){
	
	var contHeight = elem.value;
	var packingOption = '${packingOptionList}';
	
	var packOptionList2 = ["1 SLIP SHEET AND 1 WOODEN BASE","2 SLIP SHEETS","2 WOODEN BASE"];	
	
		
	if(contHeight == '20FT'){
		$('#packingTypeForm option:contains("2 SLIP SHEETS")').remove();
		
	}
	else if(contHeight == '40FT'){
		/** $("#packingTypeForm").append('<option value="option6">option6</option>'); **/
		$("#packingTypeForm").empty();
		
		$.each(packOptionList2, function(val, text) {
			$("#packingTypeForm").append(
		        $('<option></option>').html(text)
		    );
		});
	}
}
	
	



