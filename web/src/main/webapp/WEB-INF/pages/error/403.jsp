<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../common/taglibs.jsp" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <%@ include file="../common/meta.jsp" %>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>缺少权限</title>
  <%@ include file="../common/bootstrap-resources.jsp" %>
  <%@ include file="../common/shopping-resources.jsp" %>
</head>
<body>
<jsp:include page="../main/header.jsp"/>
<!--breadcrumb start-->
<div class="breadcrumb-wrapper">
  <div class="container">
    <h1>缺少权限</h1>
  </div>
</div>
<!--end breadcrumb-->
<div class="space-60"></div>
<div class="container">
  <div class="error-404">
    <h1><i class="fa fa-exclamation-triangle"></i> 缺少权限</h1>
    <p>你没有对应的权限访问此页面,你可以点击 <a href="/">这里</a> 回到主页!</p>
  </div>
</div>
<div class="space-60"></div>

<%@include file="../main/subscribe.jsp"%>
<%@include file="../main/footer.jsp"%>
</body>