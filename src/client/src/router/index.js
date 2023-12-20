import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

// make sure paths defined in WebSecurityConfiguration.java
// to limit access to them based on auth status and auth user roles
const routes = [
  {
    path: "/",
    alias: ["/app/myBlogs", "/index.html"],
    name: "myBlogs",
    meta: {
      userNeedsMFARegistration: true,
    },
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
    path: "/app/scanCode",
    name: "register",
    meta: {
      userNeedsMFARegistration: true,
    },
    component: () =>
      import(/* webpackChunkName: "scanCode" */ "../views/ScanCode"),
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
    path: "/app/entries/:weblogId",
    name: "entries",
    meta: {
      userNeedsMFARegistration: true,
    },
    component: () =>
      import(/* webpackChunkName: "entries" */ "../views/Entries"),
    props: true,
  },
  {
    path: "/app/entryEdit/:weblogId",
    name: "entryEdit",
    component: () =>
      import(/* webpackChunkName: "entryEdit" */ "../views/EntryEdit"),
    props: (route) => ({
      weblogId: route.params.weblogId,
      entryId: route.query.entryId,
    }),
  },
  {
    path: "/app/comments/:weblogId",
    name: "comments",
    component: () =>
      import(/* webpackChunkName: "comments" */ "../views/Comments"),
    props: (route) => ({
      weblogId: route.params.weblogId,
      entryId: route.query.entryId,
    }),
  },
  {
    path: "/app/tags/:weblogId",
    name: "tags",
    component: () => import(/* webpackChunkName: "tags" */ "../views/Tags"),
    props: true,
  },
  {
    path: "/app/templates/:weblogId",
    name: "templates",
    component: () =>
      import(/* webpackChunkName: "templates" */ "../views/Templates"),
    props: true,
  },
  {
    path: "/app/mediaFiles/:weblogId",
    name: "mediaFiles",
    component: () =>
      import(/* webpackChunkName: "mediaFiles" */ "../views/MediaFiles"),
    props: true,
  },
  {
    path: "/app/mediaFileEdit/:weblogId",
    name: "mediaFileEdit",
    component: () =>
      import(/* webpackChunkName: "mediaFileEdit" */ "../views/MediaFileEdit"),
    props: true,
  },
  {
    path: "/app/templateEdit/:weblogId",
    name: "templateEdit",
    component: () =>
      import(/* webpackChunkName: "templateEdit" */ "../views/TemplateEdit"),
    props: true,
  },
  {
    path: "/app/weblogConfig/:weblogId",
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
  {
    path: "*",
    name: "notFound",
    component: () =>
      import(/* webpackChunkName: "notFound" */ "../views/NotFound"),
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.VUE_APP_PUBLIC_PATH,
  routes,
});

export default router;
