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
      import("../views/MyBlogs.vue"),
  },
  {
    path: "/app/scanCode",
    name: "register",
    meta: {
      userNeedsMFARegistration: true,
    },
    component: () =>
      import("../views/ScanCode.vue"),
  },
  {
    path: "/app/createWeblog",
    name: "createWeblog",
    component: () =>
      import("../views/WeblogConfig.vue"),
  },
  {
    path: "/app/blogroll/:weblogId",
    name: "blogroll",
    component: () =>
      import("../views/Blogroll.vue"),
    props: true,
  },
  {
    path: "/app/categories/:weblogId",
    name: "categories",
    component: () =>
      import("../views/Categories.vue"),
    props: true,
  },
  {
    path: "/app/entries/:weblogId",
    name: "entries",
    meta: {
      userNeedsMFARegistration: true,
    },
    component: () =>
      import("../views/Entries.vue"),
    props: true,
  },
  {
    path: "/app/entryEdit/:weblogId",
    name: "entryEdit",
    component: () =>
      import("../views/EntryEdit.vue"),
    props: (route) => ({
      weblogId: route.params.weblogId,
      entryId: route.query.entryId,
    }),
  },
  {
    path: "/app/comments/:weblogId",
    name: "comments",
    component: () =>
      import("../views/Comments.vue"),
    props: (route) => ({
      weblogId: route.params.weblogId,
      entryId: route.query.entryId,
    }),
  },
  {
    path: "/app/tags/:weblogId",
    name: "tags",
    component: () => import("../views/Tags.vue"),
    props: true,
  },
  {
    path: "/app/templates/:weblogId",
    name: "templates",
    component: () =>
      import("../views/Templates.vue"),
    props: true,
  },
  {
    path: "/app/mediaFiles/:weblogId",
    name: "mediaFiles",
    component: () =>
      import("../views/MediaFiles.vue"),
    props: true,
  },
  {
     path: '/logout',
     name: "logout",
     beforeEnter() {location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/logout'}
  },
  {
     path: '/login-redirect',
     name: "loginRedirect",
     beforeEnter() {location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login-redirect'}
  },
  {
    path: "/app/mediaFileEdit/:weblogId",
    name: "mediaFileEdit",
    component: () =>
      import("../views/MediaFileEdit.vue"),
    props: true,
  },
  {
    path: "/app/templateEdit/:weblogId",
    name: "templateEdit",
    component: () =>
      import("../views/TemplateEdit.vue"),
    props: true,
  },
  {
    path: "/app/weblogConfig/:weblogId",
    name: "weblogConfig",
    component: () =>
      import("../views/WeblogConfig.vue"),
    // https://router.vuejs.org/guide/essentials/passing-props.html#boolean-mode
    props: true,
  },
  {
    path: "/admin/globalConfig",
    name: "globalConfig",
    component: () =>
      import("../views/GlobalConfig.vue"),
  },
  {
    path: "/admin/userAdmin",
    name: "userAdmin",
    component: () =>
      import("../views/UserAdmin.vue"),
  },
  {
    path: "/admin/cachedData",
    name: "cachedData",
    component: () =>
      import("../views/CachedData.vue"),
  },
  {
    path: "*",
    name: "notFound",
    component: () =>
      import("../views/NotFound.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
//  base: import.meta.env.VITE_PUBLIC_PATH,
  base: import.meta.env.BASE_URL,
  routes,
});

export default router;
