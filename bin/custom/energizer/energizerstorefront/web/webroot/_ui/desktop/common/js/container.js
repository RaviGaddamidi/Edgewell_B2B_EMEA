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
      
      if($("#cnt_floorSpaceProducts #utl_vol").length !=0){
    	  try{
    	  renderFloorSpaceBlock();
    	  }catch(e){
    		  console.log("Error: "+e);
    	  }
      }      
});

function renderFloorSpaceBlock(){
	
	var mapData = $("#cnt_floorSpaceProducts #utl_vol").html();
	var floorSpaceMap = (mapData.substring(1,mapData.length-1)).split(",");
	//var usedBlock = floorSpaceMap.length;
	
      var totalBlock = parseInt($("#containerHeightLine").text())/2;
      var usedBlock = floorSpaceMap.length; //parseInt($($(".cnt_utlvolfill_cls")[0]).text())
      var remainingBlock = totalBlock - usedBlock;
      var totalWidth = parseInt($("#floorSpace_cont").css("width"));
      var totalHeight = parseInt($("#floorSpace_cont").css("height"));
      var individualBlockWidth = totalWidth/totalBlock;
      var leftPosition = 0;
      
      var separator = "<hr id='block_separator' style='border: 0 none; border-top: 2px dashed #322f32; background: none; height:0; position: absolute;z-index: 1; margin: 0;padding: 0;top: 50%;width: 100%; '/>";
      var parentBlockDiv = "<div id='blockDivMain'  style='width: 100%; height: 100%; position; relative;'><div id='blockDivParent' style='width: 100%; height: 100%; position; relative;'>"+separator+"</div></div>";
      var countDivParent = "<div id='blockCountParent' style='width: 100%; position: relative;'></div>"
      $("#floorSpace_cont").prepend(parentBlockDiv);
      
      var stackDiv = "<div id='stackMain' style='position: absolute; height: 150px; right: -70px; top: 0px;'><div class='stackName' style='height: 50%;'><span style='display: block; padding-top: 35px;'>2nd Stack</span></div><div class='stackName' style='height: 50%;'><span  style='display: block; padding-top: 35px;'>1st Stack</span></div></div>";
	  $("#blockDivMain").after(countDivParent);
      $("#blockDivMain").append(stackDiv);
      
      var isFloorSpaceFull = $("#floorSpaceFull").text();
     // alert("Check"+isFloorSpaceFull);
      for(var bCount=1; bCount<=totalBlock; bCount++){
            var blockBGColor = ""; 
            var blockDiv = "";
            var countDivChild = "<div class='blockCountChild' style='width:"+individualBlockWidth+"px; text-align: center; top: 2px;float: left;'>"+bCount+"</div>";
            if(isFloorSpaceFull == 'true'){
                  blockBGColor = "rgb(255, 87, 87)";
                  blockDiv = '<div style="height: 100%; border-bottom: 1px solid #000000; background-color: '+blockBGColor+'; width: '+individualBlockWidth+'px; border-left: 1px solid #000000; position: absolute; left:'+leftPosition+'px;"></div>';
            }else if(bCount<=usedBlock){            	
                  blockBGColor = "#33cc33";
                  var tempVar = bCount;                  
                  var innerBlockCount = parseInt(((floorSpaceMap[--tempVar]).split("="))[1]);
                  try{
                  blockDiv = '<div style="height: 100%; border-bottom: 1px solid #000000; background-color: transparent; width: '+individualBlockWidth+'px; border-left: 1px solid #000000; position: absolute; left:'+leftPosition+'px;">'+createInnerBlock(innerBlockCount,blockBGColor, totalHeight)+'</div>';
                  }catch(e){
                	  console.log("Err: "+e);
                  }
            }else{
                  blockBGColor = "#ffd799";                  
                  blockDiv = '<div style="height: 100%; border-bottom: 1px solid #000000; background-color: '+blockBGColor+'; width: '+individualBlockWidth+'px; border-left: 1px solid #000000; position: absolute; left:'+leftPosition+'px;"></div>';
            }
            $("#blockCountParent").append(countDivChild);
           /* var blockDiv = '<div style="height: '+individualBlockHeight+'px; border-top: 1px solid #000000; background-color: '+blockBGColor+'; width: 100%; position: absolute; bottom:'+bottomPosition+'px;"></div>';
            $("#floorSpace_cont").prepend(blockDiv);
            bottomPosition = bottomPosition + individualBlockHeight;
            */
            
            $("#blockDivParent").append(blockDiv);
            leftPosition = leftPosition + individualBlockWidth;      
       
      }
}

function createInnerBlock(noOfInnerBlock,bgColor, totalBlockHeight){
	var bottomPosition = 0;
	var individualInnerBlockHeight = totalBlockHeight / noOfInnerBlock;
	var blockItem = "";
	
	for(var innerBlockCount=1; innerBlockCount<=noOfInnerBlock; innerBlockCount++){
		var innerBlock = "<div class='innerDiv' style='width: 100%; height:50%;background-color:"+bgColor+";bottom:"+bottomPosition+"px; position: absolute;'></div>";
		blockItem += innerBlock;
		bottomPosition += individualInnerBlockHeight;
	}
	
	return blockItem;
}