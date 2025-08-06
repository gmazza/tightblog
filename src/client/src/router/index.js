import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

// make sure paths defined in WebSecurityConfiguration.java
// to limit access to them based on auth status and auth user roles
const routes = [
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
     path: '/logout',
     name: "logout",
     beforeEnter() {location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/logout'}
  },
  {
     path: '/login-redirect',
     name: "loginRedirect",
     beforeEnter() {location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/login-redirect'}
  }
];

const router = new VueRouter({
  mode: "history",
//  base: import.meta.env.VITE_PUBLIC_PATH,
  base: import.meta.env.BASE_URL,
  routes,
});

export default router;
