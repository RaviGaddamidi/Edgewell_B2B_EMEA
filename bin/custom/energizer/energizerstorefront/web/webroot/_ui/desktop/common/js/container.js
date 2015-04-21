function callBackIfExceeds(vol, weight) {
	console.log("vol: " + vol +" weight:" + weight); }


$(document).ready(function(){

	var contHeight = $("#volume_cont").height(); 
	var volUtl= $("#volume_utilization").height();

	var volCont = $("#volume_cont").height(); 
	var weightUtl= $("#weight_utilization").height();
	var weightCont = $("#weight_cont").height(); 



	function fillThis(){

		var contHeight = $("#volume_cont").height(); var volUtl= $("#volume_utilization").height();
		var volCont = $("#volume_cont").height(); var weightUtl= $("#weight_utilization").height();
		var weightCont = $("#weight_cont").height(); 

		if(null !=contHeight && null != volCont  ){
			leftfill();
			rightfill();

			$("#volume_txt").keyup(function(){

				leftfill();

			});

			$("#weight_txt").keyup(function(){

				rightfill();

			});
		}

	}
	function leftfill()
	{

		var getVolTxt = $("#volume_txt").val();
		if(getVolTxt <100 ){
			$("#volume_utilization").css('height', contHeight * getVolTxt / 100); var volUtlBar = document.getElementById("volume_utilization").style.height;
			volUtlBar = volUtlBar.replace('px', ''); if(volUtlBar > contHeight) { callBackIfExceeds(volUtl, volCont); } 	
		}
	}

	function rightfill()
	{
		var getWeightTxt = $("#weight_txt").val(); 
		if(getWeightTxt <100){
			$("#weight_utilization").css('height', contHeight * getWeightTxt / 100); var weightUtlBar = document.getElementById("weight_utilization").style.height;
			weightUtlBar = weightUtlBar.replace('px', ''); if(weightUtlBar > contHeight) { callBackIfExceeds(weightUtl, weightCont); } 
		}

	}	


	fillThis();
});

