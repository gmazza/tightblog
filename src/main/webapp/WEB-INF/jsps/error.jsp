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

<%-- https://stackoverflow.com/a/28353662, attributes available in DefaultErrorAttributes.java --%>

<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
<link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tbeditorui.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/colors.css'/>" />

<script src="https://cdn.jsdelivr.net/npm/vue@2.6.12/dist/vue.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/axios@0.21.0/dist/axios.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.min.js"></script>

<div style="padding: 15px 25px 25px 25px">
    <h2 class="errorTitle"><fmt:message key="error.title.500" /></h2>

    <table width="80%" border="1px" style="border-collapse: collapse;">
        <tr>
            <td width="20%">Timestamp</td>
            <td><c:out value="${timestamp}" /></td>
        </tr>
        <tr>
            <td width="20%">Status</td>
            <td><c:out value="${status}" /></td>
        </tr>
        <tr>
            <td width="20%">Error</td>
            <td><c:out value="${error}" /></td>
        </tr>
        <tr>
            <td width="20%">Exception</td>
            <td><c:out value="${exception}" /></td>
        </tr>
        <tr>
            <td width="20%">Message</td>
            <td><c:out value="${message}" /></td>
        </tr>
        <tr>
            <td width="20%">Path</td>
            <td><c:out value="${path}" /></td>
        </tr>
    </table>
</div>

<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/js/bootstrap.min.js"></script>
