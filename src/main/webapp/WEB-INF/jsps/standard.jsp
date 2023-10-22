<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico">
        <title>TightBlog: <fmt:message key="${pageTitleKey}"/></title>

        <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
        <link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">

        <link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tbeditorui.css'/>" />
        <link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/colors.css'/>" />

        <script src="https://cdn.jsdelivr.net/npm/vue@2.6.12/dist/vue.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/axios@0.21.0/dist/axios.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.min.js"></script>

        <sec:csrfMetaTags />
    </head>
    <body>
        <div id="banner">
            <c:if test="${hasBanner}">
                <jsp:include page="bannerStatus.jsp" />
            </c:if>
        </div>

        <div id="content">
            <div id="nosidebar_maincontent_wrap">
                <div id="maincontent">
                    <jsp:include page="${pageToUse}"/>
                </div>
            </div>
        </div>

        <div id="footer">
            <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/js/bootstrap.min.js"></script>
        </div>
    </body>
</html>
