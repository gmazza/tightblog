import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/app/profile",
    name: "profile",
    component: () =>
      import(/* webpackChunkName: "useredit" */ "../views/UserEdit")
  },
  {
    path: "/app/register",
    name: "register",
    component: () =>
      import(/* webpackChunkName: "useredit" */ "../views/UserEdit")
  },
  {
    path: "/",
    alias: "/admin/globalConfig",
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
