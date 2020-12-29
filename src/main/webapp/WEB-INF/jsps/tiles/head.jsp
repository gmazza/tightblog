<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
<link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tbeditorui.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/colors.css'/>" />

<script src="https://cdn.jsdelivr.net/npm/vue@2.6.12/dist/vue.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/axios@0.21.0/dist/axios.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.min.js"></script>

<sec:csrfMetaTags />
