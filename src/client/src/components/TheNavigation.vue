<template>
    <div>
    <div class="bannerStatusBox">
    <table v-if="sessionInfo != null" class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
            <td class="bannerLeft">
                <span>{{ $t("product.nameVersion", { version: startupConfig.tightblogVersion }) }}</span>:
                
                <span v-if="sessionInfo.authenticatedUser != null">
                  {{ $t("navigationBar.loggedInAs", { screenName: sessionInfo.authenticatedUser.screenName }) }}
                
                  <span v-if="sessionInfo.actionWeblog != null"><b>{{ - $t("navigationBar.activeBlog")}}</b>
                    <a :href="sessionInfo.actionWeblogURL">{{ sessionInfo.actionWeblog.handle }}</a>
                  </span>
                </span>
            </td>

            <td class="bannerRight">

                <span v-if="sessionInfo.authenticatedUser != null">
                    <router-link v-if="sessionInfo.userIsAdmin" :to="{ name: 'globalConfig' }">{{ $t("navigationBar.globalAdmin") }}</router-link> |
                    <a href="../../tb-ui/app/home">{{ $t("navigationBar.blogList") }}</a> |
                    <router-link :to="{ name: 'profile' }">{{ $t("navigationBar.viewProfile") }}</router-link> |
                    <a href="../../tb-ui/app/logout">{{ $t("navigationBar.logout") }}</a>
                </span>

                <span v-else>
                    <a href="../..">{{ $t("navigationBar.homePage") }}</a> |
                    <a href="../../tb-ui/app/login-redirect">{{ $t("navigationBar.login") }}</a>

                    <span v-if="startupConfig.registrationPolicy != 'DISABLED'">
                      | <a href="../../tb-ui/app/register">{{ $t("navigationBar.register") }}</a>
                    </span>

                    <span v-if="startupConfig.registrationPolicy != 'DISABLED'">
                      | <router-link to="{ name: 'register' }">{{ $t("navigationBar.register") }} new</router-link>
                    </span>
                </span>

            </td>
        </tr>
    </table>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from "vuex";

export default {
  data: function () {
    return {
    }
  },
  computed: {
    ...mapState("startupConfig", {
      startupConfig: state => state.startupConfig
    }),
    ...mapState("sessionInfo", {
      sessionInfo: state => state.items
    })
  },
  methods: {
    ...mapActions({
      loadStartupConfig: "startupConfig/loadStartupConfig",
      loadSessionInfo: "sessionInfo/loadSessionInfo"
    })
  },
  mounted: function() {
    this.loadStartupConfig();
    this.loadSessionInfo();
  }
}
</script>


<style scoped>
#banner {
    margin: 0px;
    padding: 0px 0px 0px 0px;
}
.bannerStatusBox {
    width: 100%;
    font-weight: 500;
}
div.bannerStatusBox {
    height: 2em;
}
.bannerStatusBox a {
    font-weight: bold;
}
.bannerLeft {
    padding: 4px 15px 4px 10px;
    text-align: start;
}
.bannerRight {
    padding: 4px 10px 4px 15px;
    text-align: right;
}

</style>
