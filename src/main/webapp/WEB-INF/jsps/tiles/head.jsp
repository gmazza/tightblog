<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
<link href="https://cdnjs.cloudflare.com/ajax/libs/mdbootstrap/4.7.6/css/mdb.min.css" rel="stylesheet">

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tbeditorui.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/theme/colors.css'/>" />

<sec:csrfMetaTags />
