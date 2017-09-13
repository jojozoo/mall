<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	<meta name="format-detection" content="telephone=no">
	<meta name="author" content="SHOP++ Team">
	<meta name="copyright" content="SHOP++">
	<title>${message("member.deposit.recharge")}[#if showPowered] - Powered By SHOP++[/#if]</title>
	<link href="${base}/favicon.ico" rel="icon">
	<link href="${base}/resources/mobile/member/css/bootstrap.css" rel="stylesheet">
	<link href="${base}/resources/mobile/member/css/font-awesome.css" rel="stylesheet">
	<link href="${base}/resources/mobile/member/css/animate.css" rel="stylesheet">
	<link href="${base}/resources/mobile/member/css/common.css" rel="stylesheet">
	<link href="${base}/resources/mobile/member/css/profile.css" rel="stylesheet">
	<!--[if lt IE 9]>
		<script src="${base}/resources/mobile/member/js/html5shiv.js"></script>
		<script src="${base}/resources/mobile/member/js/respond.js"></script>
	<![endif]-->
	<script src="${base}/resources/mobile/member/js/jquery.js"></script>
	<script src="${base}/resources/mobile/member/js/bootstrap.js"></script>
	<script src="${base}/resources/mobile/member/js/velocity.js"></script>
	<script src="${base}/resources/mobile/member/js/velocity.ui.js"></script>
	<script src="${base}/resources/mobile/member/js/jquery.validate.js"></script>
	<script src="${base}/resources/mobile/member/js/underscore.js"></script>
	<script src="${base}/resources/mobile/member/js/common.js"></script>
	<script type="text/javascript">
		$().ready(function() {
			
			var $rechargeForm = $("#rechargeForm");
			var $giftMemberCode = $("#giftMemberCode");
			var $giftAmount = $("#giftAmount");
			var $submit = $("#submit");
			// 券
			$giftAmount.change(function() {
				var couponPrice = $("#giftAmount").val();
				var $element = $(this);
				if (/^\d+(\.\d{0,${setting.priceScale}})?$/.test($element.val())) {
					var max = ${coupon.balance} >= couponPrice ? couponPrice : ${coupon.balance};
					if (parseFloat($element.val()) > max) {
						$element.val(max);
					}
				} else {
					$element.val("0");
				}
			});
			
			// 表单验证
			$submit.click(function() {
				if ($giftMemberCode.val() == "") {
					$.alert("${message("shop.deposit.memberCodeNullOrMemberNull")}");
					return false;
				}
				$.ajax({
					url: "gift_do",
					type: "POST",
					data: $rechargeForm.serialize(),
					dataType: "json",
					beforeSend: function() {
						$submit.prop("disabled", true);
					},
					success: function(data) {
						 location.href = "log?type=1";
					},
					complete: function() {
						$submit.prop("disabled", false);
					}
				});
			});
		
		});
	</script>
</head>
<body class="profile">
	<header class="header-fixed">
		<a class="pull-left" href="${base}/member/index">
			<span class="glyphicon glyphicon-menu-left"></span>
		</a>
		${message("member.deposit.gift")}
	</header>
	<main>
		<div class="container-fluid">
			<form id="rechargeForm" action="gift_do" method="post">
				<div class="list-group list-group-flat">
					<div class="list-group-item">
						<div class="form-group">
							<label for="giftMemberCode">${message("member.deposit.giftMemberCode")}</label>
							<input id="giftMemberCode" name="giftMemberCode" class="form-control" type="text" maxlength="10" onpaste="return false;">
						</div>
					</div>
					<div class="list-group-item">
						<div class="form-group">
							<label for="giftAmount">${message("member.deposit.giftAmount")}</label>
							<input id="giftAmount" name="giftAmount" class="form-control" type="text" maxlength="16" onpaste="return false;">
						</div>
					</div>
					<div class="list-group-item small">${message("member.deposit.balance")}: ${currency(coupon.balance, true)}</div>
				</div>
				<div class="panel-footer text-center">
					<button class="btn btn-primary" id="submit" type="submit">${message("member.common.submit")}</button>
					<a class="btn btn-default" href="${base}/member/index">${message("member.common.back")}</a>
				</div>			
			</form>
		</div>
	</main>
</body>
</html>