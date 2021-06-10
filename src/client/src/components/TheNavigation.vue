<template>
  <div>
    <div class="bannerStatusBox">

    <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
            <td class="bannerLeft">

                <fmt:message key="product.name.version">
                    <fmt:param value="${tightblogVersion}" />:
                </fmt:message>

                <c:if test="${authenticatedUser != null}">
                    <fmt:message key="mainPage.loggedInAs" /> <c:out value="${authenticatedUser.screenName}"/>
                </c:if>

                <c:if test="${actionWeblog != null}">
                    - <fmt:message key="mainPage.currentWebsite" />
                    <b><a href='<c:out value="${actionWeblogURL}" />'>
                            <c:out value="${actionWeblog.handle}" />
                    </a></b>
                </c:if>

            </td>

            <td class="bannerRight">

                <c:if test="${authenticatedUser == null}">
                   <a href="<c:url value='/'/>"><fmt:message key="navigationBar.homePage" /></a> |
                </c:if>

                <c:if test="${userIsAdmin}">
                    <a href="<c:url value='/tb-ui2/index.html#/admin/globalConfig'/>"><fmt:message key="mainMenu.globalAdmin" /></a> |
                </c:if>

                <c:choose>
                    <c:when test="${authenticatedUser != null}">
                       <a href="<c:url value='/tb-ui/app/home'/>"><fmt:message key="mainMenu.title" /></a> |
                       <a href="<c:url value='/tb-ui/app/profile'/>"><fmt:message key="mainMenu.editProfile" /></a> |
                       <a href="<c:url value='/tb-ui/app/logout'/>"><fmt:message key="navigationBar.logout"/></a>
                    </c:when>

                    <c:otherwise>
                        <a href="<c:url value='/tb-ui/app/login-redirect'/>"><fmt:message key="navigationBar.login"/></a>

                        <c:if test="${registrationPolicy != 'DISABLED'}">
                            | <a href="<c:url value='/tb-ui/app/register'/>"><fmt:message key="navigationBar.register"/></a>
                        </c:if>
                    </c:otherwise>
                </c:choose>

            </td>
        </tr>
    </table>
  </div>

  <nav id="nav">
    <a href="../../tb-ui/app/home">My Blogs</a>
    <router-link to="/admin/globalConfig">Global Configuration</router-link>
    <router-link to="/admin/userAdmin">User Administration</router-link>
    <router-link to="/admin/cachedData">Cached Data</router-link>
  </nav>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data() {
  },
  computed: {
    ...mapState("globalConfig", {
      storeWebloggerProps: state => state.items,
      metadata: state => state.metadata
    })
  },
  methods: {
    ...mapActions({
      loadMetadata: "globalConfig/loadMetadata"
    }),
    loadGlobalMetadata: function() {
      this.loadMetadata().then(
        () => {},
        error => this.commonErrorResponse(error, null)
      );
    },
    commonErrorResponse: function(error, errorMsg) {
      if (errorMsg) {
        this.errorMessage = errorMsg;
      } else if (error && error.response && error.response.status == 401) {
        console.log("Redirecting...");
        window.location.href = "/tb-ui/app/login";
      } else if (error && error.response) {
        this.errorMessage = error.response.data.error;
      } else if (error) {
        this.errorMessage = error;
      } else {
        this.errorMessage = "System error.";
      }
    }
  },
  mounted: function() {
  }
}
</script>


<style scoped>
#nav {
  display: flex;
  justify-content: center;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
  padding: 0 10px;
}

#nav a.router-link-exact-active {
  color: blue;
}

#banner {
    margin: 0px;
    padding: 0px 0px 0px 0px;
}
.bannerStatusBox {
    width: 100%;
}
div.bannerStatusBox {
    height: 2em;
}
.bannerStatusBox a {
    font-weight: bold;
}
.bannerLeft {
    padding: 4px 15px 4px 10px;
}
.bannerRight {
    padding: 4px 10px 4px 15px;
    text-align: right;
}

</style>
