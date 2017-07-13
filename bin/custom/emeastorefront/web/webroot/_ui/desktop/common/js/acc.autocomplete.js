ACC.autocomplete = {

	bindAll: function ()
	{
		this.bindSearchAutocomplete();
	},

	bindSearchAutocomplete: function ()
	{
		var $search = $("#search");
		var option  = $search.data("options");
		var cache   = {};

		if (option)
		{
            $search.autocomplete({
                minLength: option.minCharactersBeforeRequest,
                delay:     option.waitTimeBeforeRequest,
                appendTo:  ".siteSearch",
                source:    function(request, response) {

                    var term = request.term.toLowerCase();

                    if (term in cache) {
                        return response(cache[term]);
                    }

                    $.postJSON(option.autocompleteUrl, {term: request.term}, function(data) {
                        var autoSearchData = [];
                        if(data.suggestions != null){
                            $.each(data.suggestions, function (i, obj)
                            {
                                autoSearchData.push(
                                        {value: obj.term,
                                            url: ACC.config.contextPath + "/search?text=" + obj.term,
                                            type: "autoSuggestion"});
                            });
                        }
                        if(data.products != null){
                            $.each(data.products, function (i, obj)
                            {
                                                            
                                autoSearchData.push(
                                        {value: obj.name,
                                            code: obj.code,
                                            desc: obj.description,
                                            manufacturer: obj.manufacturer,
                                            url: ACC.config.contextPath + obj.url,
                                            price: (obj.price!=null && obj.price.formattedValue!=undefined)?obj.price.formattedValue:undefined,
                                            type: "productResult",
                                            image: (obj.images!=null && obj.images!=undefined)?obj.images[0].url:undefined});
                            });
                        }
                        cache[term] = autoSearchData;
                        return response(autoSearchData);
                    });
                },
                focus: function (event, ui)
                {
                    return false;
                },
                select: function (event, ui)
                {
                    window.location.href = ui.item.url;
                }
            }).data("autocomplete")._renderItem = function (ul, item)
            {
                if (item.type == "autoSuggestion")
                {
                    renderHtml = "<a href='?q=" + item.value + "' class='clearfix'>" + item.value + "</a>";
                    return $("<li class='suggestions'>")
                            .data("item.autocomplete", item)
                            .append(renderHtml)
                            .appendTo(ul);
                }
                if (item.type == "productResult")
                {
                    var renderHtml = "<a href='" + ACC.config.contextPath + item.url + "' class='product clearfix'>";
                    /* need to show the products with images */
                    if (item.image != null && item.image != undefined)
                    {
                    	
                        renderHtml += "<span class='thumb'><img src='" + item.image + "' /></span><span class='desc clearfix'>";
                    }
                    
                    if(item.value!=undefined && item.value!=null && item.price!=null && item.price!=undefined )
                    	{
                    	 renderHtml += "<span class='title'>" + item.value +
                         "</span><span class='price'>" + item.price + "</span></span>" +
                         "</a>";
                    	}
                    
                    if(item.value!=undefined && item.value!=null && (item.price==null || item.price==undefined ))
                	{
                	 renderHtml += "<span class='title'>" + item.value +
                     "</span><span class='price'> </span></span>" +
                     "</a>";
                	}
                   
                    return $("<li class='product'>").data("item.autocomplete", item).append(renderHtml).appendTo(ul);
                }
            };
		}
	}
};

$(document).ready(function ()
{
	ACC.autocomplete.bindAll();
});