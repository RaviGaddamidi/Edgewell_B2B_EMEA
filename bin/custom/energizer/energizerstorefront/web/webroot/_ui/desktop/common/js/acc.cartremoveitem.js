var isContainerfull;
var spinnerLoader = '<div class="spinnerDiv"><img class="spinnerImg" src="/_ui/desktop/common/images/spinner.gif"/></div>';
ACC.cartremoveitem = {
            isOrderBlocked: false,
            showAndHideLoader: function(flag){    
                  //use flag value 'block' to show loader
                  //use flag value 'none' to hide loader
                  
                  $(".spinnerDiv").css("display",flag);
            },
            highlightQtyInputBox: function ()
            {     
                  $(".prdCode").each(function(){
                        if(ACC.cartremoveitem.checkProductID($(this).text().trim())){
                              $($(this).parent().find(".quantity .qty")).css("border","1px solid #ff0000");
                        }
                  });
            },
            checkProductID:function(prodId){
                  var isMatching = false;
                  $(".prod_det").each(function(){
                  //$(".productsNotAdded tbody td:first").each(function(){
                        if($(this).text().trim() == prodId ){
                              isMatching = true;
                              return false;
                        }
                  });
                  return isMatching;
            },
            bindAll: function ()
            {     
                  this.bindCartRemoveProduct();
            },
            bindCartData : function()
            {
                  ACC.cartremoveitem.getCartData();
            },
            
            bindCartRemoveProduct: function ()
            {
                  
                  $('.submitRemoveProduct').on("click", function ()
                  {   
                        ACC.cartremoveitem.showAndHideLoader("block");
                        var entryNum = $(this).attr('id').split("_")[1];
                        var $form = $('#updateCartForm' + entryNum);
                        var initialCartQuantity = $form.find('input[name=initialQuantity]');
                        var cartQuantity = $form.find('input[name=quantity]');
                        var productCode = $form.find('input[name=productCode]').val(); 

                        cartQuantity.val(0);
                        initialCartQuantity.val(0);

                        ACC.track.trackRemoveFromCart(productCode, initialCartQuantity, cartQuantity.val());

                        var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                        $.ajax({
                              url: $form.attr("action"),
                              data: $form.serialize(),
                              type: method,
                              success: function(data) 
                              {
                                  ACC.cartremoveitem.refreshCartData(data, entryNum, productCode, 0);
                              },
                              error: function(xht, textStatus, ex) 
                              {
                                    alert("Failed to remove quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });

                              });
                  //this function is doing the same as onBlur()         
            /*$('.qty').on("change", function ()
                  {     
                        alert(" Testing ");
                        var entryNum = $(this).attr('id').split("_")[1];                        
                        var $form = $('#updateCartForm' + entryNum);                      
                        var cartQuantity = $form.find('input[name=quantity]').val();                        
                        var productCode = $form.find('input[name=productCode]').val();                                                            
                                                            
                                                            
                        var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                       $.ajax({
                              url: $form.attr("action"),
                              data: $form.serialize(),
                              type: method,
                              success: function(data) 
                              {     
                                    ACC.cartremoveitem.getErrors(data, entryNum, productCode, cartQuantity);
                                    
                              },
                              error: function() 
                              {
                                    alert("Failed to remove quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });
                  });*/

                  $('.updateQuantityProduct').on("click", function (event)
                              { 
                        event.preventDefault();

                        var prodid  = $(this).attr('id').split("_"); 
                        var form    = $('#updateCartForm' + prodid[1]);
                        var productCode = form.find('input[name=productCode]').val(); 
                        var grid = $('#grid_' + prodid[1]);
                        grid.addClass("cboxGrid");

                        var strSubEntries = grid.data("sub-entries");
                        var arrSubEntries= strSubEntries.split(',');          
                        var firstVariantCode = arrSubEntries[0].split(':')[0];

                        var mapCodeQuantity = new Object();
                        
                        for (var i = 0; i < arrSubEntries.length; i++)
                        {
                              var arrValue = arrSubEntries[i].split(":");
                              mapCodeQuantity[arrValue[0]] = arrValue[1];
                        }

                        var method = "GET";
                        $.ajax({
                              url: ACC.config.contextPath + '/cart/getProductVariantMatrix',
                              data: {productCode: firstVariantCode},
                              type: method,
                              success: function(data) 
                              {
                                    grid.html(data);

                                    var $gridContainer = grid.find(".product-grid-container");      
                                    var numGrids = $gridContainer.length;

                                    for (var i = 0; i < numGrids; i++)
                                    {
                                          ACC.cartremoveitem.getProductQuantity($gridContainer.eq(i), mapCodeQuantity);
                                    }

                                    $.colorbox({
                                          html:      grid.clone(true).show(),
                                          scroll:    true,
                                          //width:     "80%",
                                          //height:    "80%",
                                          onCleanup: function() { 
                                                // remove the cloned grid
                                                grid.empty(); 

                                                strSubEntries = '';
                                                $.each(mapCodeQuantity, function(key, value) {
                                                      if (value != undefined)
                                                      {
                                                            strSubEntries = strSubEntries + key + ":"+ value+",";
                                                      }
                                                });

                                                grid.data('sub-entries', strSubEntries);
                                          }
                                    });         

                                    ACC.cartremoveitem.coreTableActions(prodid[1], mapCodeQuantity);
                              },
                              error: function(xht, textStatus, ex) 
                              {
                                    alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });         

                        //grid.show();          
                              });

                  $('.qty').on("keypress", function (e)
                              { 
                       
                        var $input = $(this);
                        if ((e.keyCode || e.which) == 13) // Enter was hit
                        {     
                              e.preventDefault();
                              $input.blur();
                        }
                        
                              });                  
                  $('.qty').on("blur", function ()
                    {       
                        var parentClass = $(this).parent().parent().attr("class");
                       /* if(parentClass == "quantity"){
                              ACC.cartremoveitem.showAndHideLoader("block"); 
                        }*/
                        var entryNum = $(this).parent().find('input[name=entryNumber]').val();                             
                        var $form = $('#updateCartForm' + entryNum);                      
                        var initialCartQuantity = $form.find('input[name=initialQuantity]').val();                        
                        var newCartQuantity = $form.find('input[name=quantity]').val();                        
                        var productCode = $form.find('input[name=productCode]').val();       
                        var moq =    $form.find('input[name=moq]').val();           
                        
                        if(initialCartQuantity != newCartQuantity)
                        {     
                              if(parentClass == "quantity"){
                                ACC.cartremoveitem.showAndHideLoader("block"); 
							  }
                              ACC.track.trackUpdateCart(productCode, initialCartQuantity, newCartQuantity);
                              var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                             
                              $.ajax({
                                    url: $form.attr("action"),
                                    data: $form.serialize(),
                                    type: method,
                                    success: function(data) 
                                    {   
                                           
                                          ACC.cartremoveitem.refreshCartData(data, entryNum, productCode, newCartQuantity);
                                          if(newCartQuantity % moq == 0)
                                          {                                         
                                          initialCartQuantity = newCartQuantity;
                                          $form.find('input[name=initialQuantity]').val(initialCartQuantity);
                                          }    
                                    },
                                    error: function(xht, textStatus, ex) 
                                    {
                                          
                                          alert("Failed to update quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                    }

                              });
                        }
                              });

            },

            getProductQuantity: function(gridContainer, mapData) 
            {
                  var skus          = jQuery.map(gridContainer.find("input[type='hidden'].sku"), function(o) {return o.value});
                  var quantities    = jQuery.map(gridContainer.find("input[type='textbox'].sku-quantity"), function(o) {return o});

                  var totalPrice = 0.0;
                  var totalQuantity = 0.0;

                  $.each(skus, function(index, skuId) 
                              { 
                        var quantity = mapData[skuId];
                        if (quantity != undefined)
                        {
                              quantities[index].value = quantity;
                              totalQuantity += parseFloat(quantity);

                              var indexPattern = "[0-9]+";
                              var currentIndex = parseInt(quantities[index].id.match(indexPattern));

                              var currentPrice = $("input[id='productPrice["+currentIndex+"]']").val();
                              totalPrice += parseFloat(currentPrice) * parseInt(quantity);
                        }
                              });

                  var subTotalValue = Currency.formatMoney(Number(totalPrice).toFixed(2), Currency.money_format[ACC.common.currentCurrency]);
                  var avgPriceValue = 0.0;
                  if (totalQuantity > 0)
                  {
                        avgPriceValue = Currency.formatMoney(Number(totalPrice/totalQuantity).toFixed(2), Currency.money_format[ACC.common.currentCurrency]);
                  }

                  gridContainer.parent().find('#quantity').html(totalQuantity);
                  gridContainer.parent().find("#avgPrice").html(avgPriceValue)
                  gridContainer.parent().find("#subtotal").html(subTotalValue);

                  var $inputQuantityValue = gridContainer.parent().find('#quantityValue');
                  var $inputAvgPriceValue = gridContainer.parent().find('#avgPriceValue');
                  var $inputSubtotalValue = gridContainer.parent().find('#subtotalValue');

                  $inputQuantityValue.val(totalQuantity);
                  $inputAvgPriceValue.val(Number(totalPrice/totalQuantity).toFixed(2));
                  $inputSubtotalValue.val(Number(totalPrice).toFixed(2));

            }, 

            coreTableActions: function(productCode, mapCodeQuantity)  
            {
                  var skuQuantityClass = '.sku-quantity';

                  var quantityBefore = 0;
                  var quantityAfter = 0;

                  var grid = $('#grid_' + productCode);

                  grid.on('click', skuQuantityClass, function(event) {
                        $(this).select();
                  });

                  grid.on('focusin', skuQuantityClass, function(event) {
                        quantityBefore = jQuery.trim(this.value);
                        if (quantityBefore == "") {
                              quantityBefore = 0;
                              this.value = 0;
                        }
                  });

                  grid.on('focusout', skuQuantityClass, function(event) {
                        var indexPattern           = "[0-9]+";
                        var currentIndex           = parseInt($(this).attr("id").match(indexPattern));
                        var $gridGroup             = $(this).parents('.orderForm_grid_group');
                        var $closestQuantityValue  = $gridGroup.find('#quantityValue');
                        var $closestAvgPriceValue  = $gridGroup.find('#avgPriceValue');
                        var $closestSubtotalValue  = $gridGroup.find('#subtotalValue');

                        var currentQuantityValue   = $closestQuantityValue.val();
                        var currentSubtotalValue   = $closestSubtotalValue.val();

                        var currentPrice = $("input[id='productPrice["+currentIndex+"]']").val();
                        var variantCode = $("input[id='cartEntries["+currentIndex+"].sku']").val();

                        quantityAfter = jQuery.trim(this.value);

                        if (isNaN(jQuery.trim(this.value))) {
                              this.value = 0;
                        }

                        if (quantityAfter == "") {
                              quantityAfter = 0;
                              this.value = 0;
                        }

                        if (quantityBefore == 0) {
                              $closestQuantityValue.val(parseInt(currentQuantityValue) + parseInt(quantityAfter));
                              $closestSubtotalValue.val(parseFloat(currentSubtotalValue) + parseFloat(currentPrice) * parseInt(quantityAfter));
                        } else {
                              $closestQuantityValue.val(parseInt(currentQuantityValue) + (parseInt(quantityAfter) - parseInt(quantityBefore)));
                              $closestSubtotalValue.val(parseFloat(currentSubtotalValue) + parseFloat(currentPrice) * (parseInt(quantityAfter) - parseInt(quantityBefore)));
                        }

                        if (parseInt($closestQuantityValue.val()) > 0) {
                              $closestAvgPriceValue.val(parseFloat($closestSubtotalValue.val()) / parseInt($closestQuantityValue.val()));
                        } else {
                              $closestAvgPriceValue.val(0);
                        }

                        $closestQuantityValue.parent().find('#quantity').html($closestQuantityValue.val());
                        $closestAvgPriceValue.parent().find('#avgPrice').html(ACC.productorderform.formatTotalsCurrency($closestAvgPriceValue.val()));
                        $closestSubtotalValue.parent().find('#subtotal').html(ACC.productorderform.formatTotalsCurrency($closestSubtotalValue.val()));

                        if (quantityBefore != quantityAfter)
                        {
                              var method = "POST";
                              $.ajax({
                                    url: ACC.config.contextPath + '/cart/update',
                                    data: {productCode: variantCode, quantity: quantityAfter, entryNumber: -1},
                                    type: method,
                                    success: function(data) 
                                    {
                                          ACC.cartremoveitem.refreshCartData(data, -1, productCode, null);
                                          mapCodeQuantity[variantCode] = quantityAfter;
                                    },
                                    error: function(xht, textStatus, ex) 
                                    {
                                          alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                    }

                              });
                        }

                  }); 

            },
            
            getErrors: function(cartData, entryNum, productCode, quantity)
            {                 
                  if (cartData.entries.length == 0)
                  {
                        location.reload();
                  }
                  else
                  {     
                        var errorsDiv = $('#businesRuleErrors');
                        
                        errorsDiv.html('');
                        $("#businesRuleErrors").show();           
                        var isContainerFullFlag = cartData.isContainerFull;                     
                        $('#isContainerFull').val(isContainerFullFlag);
                        
                        ACC.cartremoveitem.fillThis();
                        
                        var errorSize = cartData.businesRuleErrors.length;                      
                        var errors ="";                                             
                        if(errorSize > 0)
                {
                      $('#businesRuleErrors').fadeIn("fast");
                      for (var i = 0; i < errorSize; i++)
                      {
                            var error = cartData.businesRuleErrors[i];
                            
                            errors = errors + error + "<br/>";  
                            
                      }
                      errorsDiv.html(errors); 
                      errorsDiv.addClass("alert negative");
                      
                      $("html, body").animate({ scrollTop: 0 }, 50);
                      //errorsDiv.fadeOut(3000);
                }else{
                      businesRuleErrors
                      $('#businesRuleErrors').fadeOut(1000);
                      errorsDiv.removeClass("alert negative");
                }
                $('#validationErrors').fadeOut(5000);

                  }           
            },
                  

            refreshCartData: function(cartData, entryNum, productCode, quantity) 
            {                 
                  //alert("refreshCartData: "+cartData.entries.length);
                  $('#containerHeightLine').text(cartData.containerHeight);                  
                  // if cart is empty, we need to reload the whole page
                  if (cartData.entries.length == 0)
                  {
                        location.reload();
                  }
                  else
                  {                         
                        var form;   
                        var removeItem = false;
                        var totalProductWeightInPercent = cartData.totalProductWeightInPercent;
                        var totalProductVolumeInPercent = cartData.totalProductVolumeInPercent;
						var availableVolume = cartData.availableVolume;
                        var availableWeight = cartData.availableWeight;
                        ACC.cartremoveitem.isOrderBlocked =cartData.isOrderBlocked;
                                                
                        if (entryNum == -1) // grouped item
                        {   
                              var editLink = $('#QuantityProduct_' + productCode);
                              form = editLink.closest('form');

                              var quantity = 0;
                              var entryPrice = 0;
                              for (var i = 0; i < cartData.entries.length; i++)
                              {
                                    var entry = cartData.entries[i];
                                    if (entry.product.code == productCode)
                                    {                 
                                          quantity = entry.quantity;
                                          entryPrice = entry.totalPrice;
                                          break;
                                    }
                              }

                              if (quantity == 0)
                              {
                                    removeItem = true;
                                    form.parent().parent().remove();
                              }
                              else
                              {
                              
                                    form.find(".qty").html(quantity);
                                    form.parent().parent().find(".total").html(entryPrice.formattedValue);

                                    $('#weight_txt').val(totalProductWeightInPercent);
                                    $('#volume_txt').val(totalProductVolumeInPercent);   
                                    $('#availableVolume_txt').val(availableVolume);
                                    $('#availableWeight_txt').val(availableWeight);							  
                                    
                                    var isContainerFullFlag = cartData.isContainerFull;                                    
                                    $('#isContainerFull').val(isContainerFullFlag);                                           

                                    ACC.cartremoveitem.fillThis();

                              }

                        }
                        else //ungrouped item
                        {     
                              form = $('#updateCartForm' + entryNum);

                              if (quantity == 0)
                              {
                                    removeItem = true;
                                    form.parent().parent().remove();
                              }
                              else
                              {
                                    for (var i = 0; i < cartData.entries.length; i++)
                                    {
                                          var entry = cartData.entries[i];
                                          if (entry.entryNumber == entryNum)
                                          {                       
                                                form.find('input[name=quantity]').val(entry.quantity);
                                                form.parent().parent().find(".total").html(entry.totalPrice.formattedValue);

                                                $('#weight_txt').val(totalProductWeightInPercent);
                                                $('#volume_txt').val(totalProductVolumeInPercent);
                                                $('#availableVolume_txt').val(availableVolume);
                                                $('#availableWeight_txt').val(availableWeight);  												

                                                var isContainerFullFlag = cartData.isContainerFull;                                                     
                                                $('#isContainerFull').val(isContainerFullFlag);
                        
                                                ACC.cartremoveitem.fillThis();

                                          }
                                    }
                              }
                        }

                        // remove item, need to update other items' entry numbers
                        if (removeItem === true)
                        {     
                              $('.cartItem').each(function(index)
                                          {
                                    form = $(this).find('.quantity').children().first();
                                    var productCode = form.find('input[name=productCode]').val(); 

                                    for (var i = 0; i < cartData.entries.length; i++)
                                    {
                                          var entry = cartData.entries[i];
                                          if (entry.product.code == productCode)
                                          {                       
                                                form.find('input[name=entryNumber]').val(entry.entryNumber);
                                                break;
                                          }
                                    }
                                          });
                        }

                        // refresh mini cart    
                        ACC.minicart.refreshMiniCartCount();
                       
                        $('#orderTotals').next().remove();
                        $('#orderTotals').remove();
                        $("#ajaxCart").html($("#cartTotalsTemplate").tmpl({data: cartData}));      
                        
                        ACC.cartremoveitem.getErrors(cartData, entryNum, productCode, quantity);
                        if($(".form-actions .positive").length !== 0){
                              $(".form-actions .positive").click(); 
                        }                       
                  }
                  
                  $('#weight_txt').val(totalProductWeightInPercent);
                  $('#volume_txt').val(totalProductVolumeInPercent); 
                  $('#availableVolume_txt').val(availableVolume);
                  $('#availableWeight_txt').val(availableWeight);				  
                  
                  var isContainerFullFlag = cartData.isContainerFull;                                 
                  $('#isContainerFull').val(isContainerFullFlag);                                           

                  ACC.cartremoveitem.fillThis();
                  
                 ACC.cartremoveitem.showAndHideLoader("none");   
            
            },
            getCartData : function()
            {
            var contHeight = $("#volume_cont").height();
            var isContainerFull = $('#isContainerFull').val();
            var isOrderBlocked = $('#isOrderBlocked').val();
            var getVolTxt = $("#volume_txt").val();
            var getWeightTxt = $("#weight_txt").val();
            var weightCont = $("#weight_cont").height(); 
            var getavailableVolTxt = $("#availableVolume_txt").val();
            var getavailableWeightTxt = $("#availableWeight_txt").val();			
           // var percentageSign = $("#percentageSign").val();	
            var errorsDiv = $('#businesRuleErrors').show(); 
            var errorMsg = "";
            
            if(isOrderBlocked=='true'){
            	
            	$("#checkoutButton_top").attr("disabled", true);
                $("#checkoutButton_bottom").attr("disabled",true);
                errorMsg =errorMsg + " Dear Customer, You order has been blocked. Please contact Customer Care <br>"

          }
            
            if(isContainerFull == 'true')
            {
                  
                  if(getVolTxt < 100){
                        contHeight = (contHeight*getVolTxt)/100;
                        $("#volume_utilization").css('background-color', '#33cc33'); 
                        $("#utl_vol").text(getavailableVolTxt);
                        $("#volumePercentageSign").text("%");
                  }
                  else{
                        $("#volume_utilization").css('background-color', '#FF5757');       
                        $("#utl_vol").text("Volume Exceeded");
                        $("#volumePercentageSign").text("");
                  }                             
                  
                  if(getWeightTxt <100){
                        weightCont = (weightCont*getWeightTxt)/100;
                        $("#weight_utilization").css('background-color', '#33cc33');       
                        $("#utl_wt").text(getavailableWeightTxt);
                        $("#weightPercentageSign").text("%");
                  }
                  else{
                        $("#weight_utilization").css('background-color', '#FF5757');       
                        $("#utl_wt").text("Weight Exceeded");
                        $("#weightPercentageSign").text("");
                  }                             
                  
                        
            
                  $("#weight_utilization").css('height', weightCont);         
                  $("#volume_utilization").css('height', contHeight); 
            
                  $("#checkoutButton_top").attr("disabled", true);
                  $("#checkoutButton_bottom").attr("disabled",true);    
                  $("#continueButton_bottom").attr("disabled",true);    
                  
                  errorMsg = "Dear Customer, your order will not fit in one container. Please, adjust the cart and/or place multiple orders. <br>";
            }     
            
            

            
        if(errorMsg==""){
            errorsDiv.hide()
            errorsDiv.removeClass("alert negative");
        }else{
            errorsDiv.html(errorMsg); 
            errorsDiv.addClass("alert negative");
            $("html, body").animate({ scrollTop: 0 }, 50);
        }
            
            
            if(isContainerFull == 'false')
            { 
                   $("#volume_utilization").css('background-color', '#33cc33'); 
                   $("#weight_utilization").css('background-color', '#33cc33'); 
                  if(getVolTxt == 100)
                    {                     
                       $("#volume_utilization").css('height', contHeight);       
                       $("#volumePercentageSign").text("%");
                    }
                    if(getWeightTxt == 100)
                    {
                    $("#weight_utilization").css('height', contHeight);    
                    $("#weightPercentageSign").text("%");
                    }
					
					if(getVolTxt > 100){
                  	    contHeight = (contHeight*getVolTxt)/100;
                  	    $("#volume_utilization").css('height', contHeight);
                  	    $("#volume_utilization").css('background-color', '#FF5757');       
                        $("#utl_vol").text("Volume Exceeded");
                        $("#volumePercentageSign").text("");
                     }
                                               
                    if(getWeightTxt > 100){
                	    weightCont = (weightCont*getWeightTxt)/100;
                	    $("#weight_utilization").css('height', weightCont);
                  	    $("#weight_utilization").css('background-color', '#FF5757');       
                        $("#utl_wt").text("Weight Exceeded");
                        $("#weightPercentageSign").text("");
                  }

            }
                        
            },
            
            fillThis: function() {  
                  var contHeight = $("#volume_cont").height(); 
                  var volUtl= $("#volume_utilization").height();
                  var volCont = $("#volume_cont").height(); 
                  var weightUtl= $("#weight_utilization").height();
                  var weightCont = $("#weight_cont").height();                
                  var isContainerFull = $('#isContainerFull').val();
                  isContainerfull = isContainerFull;

                  if(null !=contHeight && null != volCont  ){

                        var getVolTxt = $("#volume_txt").val();
                        var getWeightTxt = $("#weight_txt").val();
						var getavailableVolTxt = $("#availableVolume_txt").val();
                        var getavailableWeightTxt = $("#availableWeight_txt").val();
                        
                        if(isContainerFull == 'true')
                        { 

                              if(getVolTxt < 100){
                                    contHeight = (contHeight*getVolTxt)/100;
                                    $("#volume_utilization").css('background-color', '#33cc33'); 
                                    $("#utl_vol").text(getavailableVolTxt);
                                    $("#volumePercentageSign").text("%");
                              }
                              else{
                                    $("#volume_utilization").css('background-color', '#FF5757');       
                                    $("#utl_vol").text("Volume Exceeded");
                                    $("#volumePercentageSign").text("");
                              }                             
                              if(getWeightTxt <100){
                                    weightCont = (weightCont*getWeightTxt)/100;
                                    $("#weight_utilization").css('background-color', '#33cc33'); 
                                    $("#utl_wt").text(getavailableWeightTxt);
                                    $("#weightPercentageSign").text("%");
                              }
                              else{
                                    $("#weight_utilization").css('background-color', '#FF5757');       
                                    $("#utl_wt").text("Weight Exceeded");
                                    $("#weightPercentageSign").text("");
                              }                             
                              
                                                      
                              $("#weight_utilization").css('height', weightCont);         
                              $("#volume_utilization").css('height', contHeight);                     
                         
                               //Disable checkout buttons
                              $("#checkoutButton_top").attr("disabled", true);
                              $("#checkoutButton_bottom").attr("disabled",true);   
                              $("#continueButton_bottom").attr("disabled",true);   
                        }
                        
                        if(isContainerFull == 'false')
                        {
                        if(ACC.cartremoveitem.isOrderBlocked != true)  {
                              $("#checkoutButton_top").attr("disabled", false);                      
                               $("#checkoutButton_bottom").attr("disabled",false);
                              $("#continueButton_bottom").attr("disabled",false);  
                        }
                       // $("#volume_utilization").css('background-color', '#33cc33'); 
                        // $("#weight_utilization").css('background-color', '#33cc33'); 
                         
                         //$("#utl_vol").text(getVolTxt);
                       // $("#utl_wt").text(getWeightTxt);
                        if(getVolTxt == 100)
                          {     
                             $("#volume_utilization").css('background-color', '#33cc33');						  
                             $("#volume_utilization").css('height', contHeight); 
                             $("#utl_vol").text(getavailableVolTxt);
                             $("#volumePercentageSign").text("%");
                          }
                          if(getWeightTxt == 100)
                          {
						       $("#weight_utilization").css('background-color', '#33cc33');
                               $("#weight_utilization").css('height', contHeight);    
                               $("#utl_wt").text(getavailableWeightTxt);
                               $("#weightPercentageSign").text("%");
                          }
						  
						   if(getVolTxt > 100)
						  {
                        	  $("#volume_utilization").css('background-color', '#FF5757');       
                              $("#utl_vol").text("Volume Exceeded");
                              $("#volumePercentageSign").text("");
                          }
                                                     
                        if(getWeightTxt > 100)
						{
                        	$("#weight_utilization").css('background-color', '#FF5757');       
                            $("#utl_wt").text("Weight Exceeded");
                            $("#weightPercentageSign").text("");
                        }
                          
                          ACC.common.$globalMessages.html('<div id="businesRuleErrors"></div>'      );
                                               
                        }
                        
                        if(getVolTxt <100){
                              
                              $("#volume_utilization").css('height', contHeight * getVolTxt / 100); var volUtlBar = document.getElementById("volume_utilization").style.height;
                              volUtlBar = volUtlBar.replace('px', '');
                              $("#volume_utilization").css('background-color', '#33cc33');
                              $("#utl_vol").text(getavailableVolTxt);
                              $("#volumePercentageSign").text("%");
							  if(volUtlBar > contHeight) { callBackIfExceeds(volUtl, volCont); } 
                        }
                        
                        if(getWeightTxt <100)
                        {           
                              $("#weight_utilization").css('height', contHeight * getWeightTxt / 100); var weightUtlBar = document.getElementById("weight_utilization").style.height;
                              weightUtlBar = weightUtlBar.replace('px', ''); 
                              $("#weight_utilization").css('background-color', '#33cc33');
							  $("#utl_wt").text(getavailableWeightTxt);
							  $("#weightPercentageSign").text("%");
							  if(weightUtlBar > contHeight) { callBackIfExceeds(weightUtl, weightCont); } 
                        
                        }
                        
                        if(isContainerFull == 'true')
                        {
                        $("#weight_utilization").css('height', weightCont);                          
                        $("#volume_utilization").css('height', contHeight); 
                        
                        }
                  }
            }
}


$(document).ready(function (){ 
            $("body").append(spinnerLoader);
      ACC.cartremoveitem.bindCartData();        
      ACC.cartremoveitem.bindAll();      
      if($("#productsNotAddedToCart").css('display') !== 'none'){
        ACC.cartremoveitem.highlightQtyInputBox();
      }
      
});
