import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    alias: "/app/myBlogs",
    name: "myBlogs",
    component: () =>
      import(/* webpackChunkName: "myblogs" */ "../views/MyBlogs"),
  },
  {
    path: "/app/profile",
    name: "profile",
    component: () =>
      import(/* webpackChunkName: "useredit" */ "../views/UserEdit"),
  },
  {
    path: "/app/register",
    name: "register",
    component: () =>
      import(/* webpackChunkName: "useredit" */ "../views/UserEdit"),
  },
  {
    path: "/app/createWeblog",
    name: "createWeblog",
    component: () =>
      import(/* webpackChunkName: "weblogconfig" */ "../views/WeblogConfig"),
  },
  {
    path: "/app/blogroll/:weblogId",
    name: "blogroll",
    component: () =>
      import(/* webpackChunkName: "blogroll" */ "../views/Blogroll"),
    props: true,
  },
  {
    path: "/app/categories/:weblogId",
    name: "categories",
    component: () =>
      import(/* webpackChunkName: "categories" */ "../views/Categories"),
    props: true,
  },
  {
    path: "/authoring/weblogConfig/:weblogId",
    name: "weblogConfig",
    component: () =>
      import(/* webpackChunkName: "weblogconfig" */ "../views/WeblogConfig"),
    // https://router.vuejs.org/guide/essentials/passing-props.html#boolean-mode
    props: true,
  },
  {
    path: "/admin/globalConfig",
    name: "globalConfig",
    component: () =>
      import(/* webpackChunkName: "globalConfig" */ "../views/GlobalConfig"),
  },
  {
    path: "/admin/userAdmin",
    name: "userAdmin",
    component: () =>
      import(/* webpackChunkName: "userAdmin" */ "../views/UserAdmin"),
  },
  {
    path: "/admin/cachedData",
    name: "cachedData",
    component: () =>
      import(/* webpackChunkName: "cachedData" */ "../views/CachedData"),
  },
];

const router = new VueRouter({
  routes,
});

export default router;
