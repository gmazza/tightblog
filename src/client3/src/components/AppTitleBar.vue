<template>
  <div>
    <div class="bannerStatusBox">
      <table v-if="sessionInfo != null" class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
          <td class="bannerLeft">
            <span>{{
              $t('navigationBar.productVersion', {
                version: startupConfig.tightblogVersion
              })
            }}</span
            >:

            <span v-if="sessionInfo.authenticatedUser != null">
              {{
                $t('navigationBar.loggedInAs', {
                  screenName: sessionInfo.authenticatedUser.screenName
                })
              }}

              <span v-if="sessionInfo.actionWeblog != null"
                ><b>{{ -$t('navigationBar.activeBlog') }}</b>
                <a :href="sessionInfo.actionWeblogURL">{{ sessionInfo.actionWeblog.handle }}</a>
              </span>
            </span>
          </td>

          <td class="bannerRight">
            <span v-if="sessionInfo.authenticatedUser != null">
              <span v-if="!sessionInfo.userNeedsMFARegistration">
                <router-link v-if="sessionInfo.userIsAdmin" :to="{ name: 'globalConfig' }">{{
                  $t('navigationBar.globalAdmin')
                }}</router-link>
                |
                <router-link :to="{ name: 'myBlogs' }">{{
                  $t('navigationBar.blogList')
                }}</router-link>
                |
                <router-link :to="{ name: 'profile' }">{{
                  $t('navigationBar.viewProfile')
                }}</router-link>
                |
              </span>
              <router-link :to="{ name: 'logout' }">{{ $t('navigationBar.logout') }}</router-link>
            </span>

            <span v-else>
              <a href="../..">{{ $t('navigationBar.homePage') }}</a> |

              <!--router-link :to="{ name: 'loginRedirect' }">{{
                $t('navigationBar.login')
              }}</router-link-->

              <span v-if="startupConfig.registrationPolicy != 'DISABLED'">
                |
                <!--router-link :to="{ name: 'register' }">{{
                  $t('navigationBar.register')
                }}</router-link-->
              </span>
            </span>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useSessionInfoStore } from '../stores/sessionInfo'
import { useStartupConfigStore } from '../stores/startupConfig'
import { mapState, mapActions } from 'pinia'

export default defineComponent({
  props: {
    mfaRegistration: { type: Boolean, default: false }
  },
  data: function () {
    return {}
  },
  computed: {
    ...mapState(useSessionInfoStore, ['sessionInfo']),
    ...mapState(useStartupConfigStore, ['startupConfig'])
  },
  methods: {
    ...mapActions(useSessionInfoStore, ['loadSessionInfo']),
    ...mapActions(useStartupConfigStore, ['loadStartupConfig'])
  },
  mounted: function () {
    this.loadStartupConfig()
    this.loadSessionInfo()
  } /*
  created: function () {
    Object.keys(this.$router.currentRoute.meta).forEach((prop, value) =>
      console.log(prop + " " + value)
    );
  },*/
})
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
