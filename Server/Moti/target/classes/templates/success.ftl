<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="UTF-8" />
<title>添加样本</title>
</head>

<body>
	<h2 style="color: red""> Don't close this window!</h2>
	QT and Server transport succssfully!<br/>
	Please wait for the files coming from server!
	<form action="reTransWeights">
		<input type="submit" value="重新获取文件"></td></tr>
	</form>
	<script src="js/jquery-1.7.2.min.js" type="text/javascript"></script>
	<script type="text/javascript">
        var url = "/transWeights";
        var fileName = "testAjaxDownload.txt";
        var form = $("<form></form>").attr("action", url).attr("method", "post");
        form.append($("<input></input>").attr("type", "hidden").attr("name", "fileName").attr("value", fileName));
        form.appendTo('body').submit().remove();
	</script>
</body>
</html>