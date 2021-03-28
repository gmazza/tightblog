import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home
  },
  {
    path: "/admin/globalConfig",
    name: "globalConfig",
    component: () =>
      import(/* webpackChunkName: "globalConfig" */ "../views/GlobalConfig")
  },
  {
    path: "/admin/userAdmin",
    name: "userAdmin",
    component: () =>
      import(/* webpackChunkName: "userAdmin" */ "../views/UserAdmin")
  },
  {
    path: "/admin/cachedData",
    name: "cachedData",
    component: () =>
      import(/* webpackChunkName: "cachedData" */ "../views/CachedData")
  }
];

const router = new VueRouter({
  routes
});

export default router;
