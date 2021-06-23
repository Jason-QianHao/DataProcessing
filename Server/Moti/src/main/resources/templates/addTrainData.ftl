<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="UTF-8" />
<title>添加样本</title>

</head>

<body>
		<center>
		<h1>添加动作</h1>
		<form action="getData" style="font-size: 14px;" method="post"
			ENCTYPE="multipart/form-data">
			<table>
				<tr>
					<td>样本名称:</td>
					<td><input type="text" name="name" placeholder="格式name"></td>
				</tr>
				<tr>
					<td>上传文件:</td>
					<td><input type="file" name="file" multiple="multiple"></td>
				</tr>
				<tr> <td colspan="2"><input type="submit" value="提交"></td></tr>
				<#if error??>
						<div>
							<span id="message_LOGIN_TOO_MUCH" style="color: red">
								${error}</span>
						</div>
				</#if>
			</table>
		</form>
	</center>
</body>
</html>