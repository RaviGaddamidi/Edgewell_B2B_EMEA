ACC.approval = {

	submitApprovalDecision: function (desictionCode)
	{
		$('#approverSelectedDecision').attr("value", desictionCode);
		
		var comments = $('#comments').val();
		if(comments == 0){
		comments = "";
		
	}
	if(comments){
		
		$("#approvalDecisionForm").submit();
		return true;
	}else{
		
		var html = "<div class='alert negative'>Please Select Proper Comment</div>";
		$('#globalMessages').html(html);
		
		return false;
	}
		
	},

	bindToApproverDecisionButton: function ()
	{
		$('.approverDecisionButton').click(function()
		{
			ACC.approval.submitApprovalDecision($(this).data("decision"));

		});
	}
};

$(document).ready(function ()
{
	ACC.approval.bindToApproverDecisionButton();
});
