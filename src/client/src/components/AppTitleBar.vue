<template>
  <div>
    <div class="bannerStatusBox">
      <table
        v-if="sessionInfo != null"
        class="bannerStatusBox"
        cellpadding="0"
        cellspacing="0"
      >
        <tr>
          <td class="bannerLeft">
            <span>{{
              $t("navigationBar.productVersion", {
                version: startupConfig.tightblogVersion,
              })
            }}</span
            >:

            <span v-if="sessionInfo.authenticatedUser != null">
              {{
                $t("navigationBar.loggedInAs", {
                  screenName: sessionInfo.authenticatedUser.screenName,
                })
              }}

              <span v-if="sessionInfo.actionWeblog != null"
                ><b>{{ -$t("navigationBar.activeBlog") }}</b>
                <a :href="sessionInfo.actionWeblogURL">{{
                  sessionInfo.actionWeblog.handle
                }}</a>
              </span>
            </span>
          </td>

          <td class="bannerRight">
            <span v-if="sessionInfo.authenticatedUser != null">
              <span v-if="!mfaRegistration">
                <router-link
                  v-if="sessionInfo.userIsAdmin"
                  :to="{ name: 'globalConfig' }"
                  >{{ $t("navigationBar.globalAdmin") }}</router-link
                >
                |
                <router-link :to="{ name: 'myBlogs' }">{{
                  $t("navigationBar.blogList")
                }}</router-link>
                |
                <router-link :to="{ name: 'profile' }">{{
                  $t("navigationBar.viewProfile")
                }}</router-link>
                |
              </span>
              <a href="../../tb-ui/app/logout">{{
                $t("navigationBar.logout")
              }}</a>
            </span>

            <span v-else>
              <a href="../..">{{ $t("navigationBar.homePage") }}</a> |
              <a href="../../tb-ui/app/login-redirect">{{
                $t("navigationBar.login")
              }}</a>

              <span v-if="startupConfig.registrationPolicy != 'DISABLED'">
                |
                <router-link :to="{ name: 'register' }"
                  >{{ $t("navigationBar.register") }} new</router-link
                >
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
  props: {
    mfaRegistration: { type: Boolean, default: false },
  },
  data: function () {
    return {};
  },
  computed: {
    ...mapState("startupConfig", {
      startupConfig: (state) => state.startupConfig,
    }),
    ...mapState("sessionInfo", {
      sessionInfo: (state) => state.items,
    }),
  },
  methods: {
    ...mapActions({
      loadStartupConfig: "startupConfig/loadStartupConfig",
      loadSessionInfo: "sessionInfo/loadSessionInfo",
    }),
  },
  mounted: function () {
    this.loadStartupConfig();
    this.loadSessionInfo();
  },
  created: function () {
    Object.keys(this.$router.currentRoute.meta).forEach((prop, value) =>
      console.log(prop + " " + value)
    );
  },
};
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
