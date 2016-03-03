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
      renderFloorSpaceBlock();
});

function renderFloorSpaceBlock(){
      var totalBlock = parseInt($("#containerHeightLine").text())/2;
      var usedBlock = parseInt($($(".cnt_utlvolfill_cls")[0]).text())
      var remainingBlock = totalBlock - usedBlock;
      var totalHeight = parseInt($("#floorSpace_cont").css("height"));
      var individualBlockHeight = totalHeight/totalBlock;
      var bottomPosition = 0;
      var isFloorSpaceFull = $("#floorSpaceFull").text();
      
      for(var bCount=1; bCount<=totalBlock; bCount++){
            var blockBGColor = ""; 
            if(isFloorSpaceFull == 'true'){
                  blockBGColor = "#ff0000"
            }else if(bCount<=usedBlock){
                  blockBGColor = "#33cc33";
            }else{
                  blockBGColor = "#ffd799";
            }
            var blockDiv = '<div style="height: '+individualBlockHeight+'px; border-top: 1px solid #000000; background-color: '+blockBGColor+'; width: 100%; position: absolute; bottom:'+bottomPosition+'px;"></div>';
            $("#floorSpace_cont").prepend(blockDiv);
            bottomPosition = bottomPosition + individualBlockHeight;
      }
}
