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

<h1><fmt:message key="${pageTitleKey}"/></h1>

<c:if test="${status.error}">
    <h2><fmt:message key="installer.startupProblemMessage"/></h2>

    <h3><fmt:message key="installer.whatHappened"/></h3>
    <p><fmt:message key="${status.descriptionKey}"/></p>
    <ul>
        <c:forEach var="message" items="${messages}">
            <li><c:out value="${message}"/></li>
        </c:forEach>
    </ul>

    <c:if test="${rootCauseStackTrace != null && rootCauseStackTrace != ''}">
        <h3><fmt:message key="installer.whyDidThatHappen"/></h3>
        <p><fmt:message key="installer.heresTheStackTrace"/></p>
        <pre>
            [<c:out value="${rootCauseStackTrace}"/>]
        </pre>
    </c:if>
</c:if>

<br/>
<br/>
