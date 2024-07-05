import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      alias: ['/app/myBlogs', '/index.html'],
      name: 'myBlogs',
      meta: {
        userNeedsMFARegistration: true
      },
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/MyBlogs.vue')
    },
    {
      path: '/app/weblogConfig/:weblogId',
      name: 'weblogConfig',
      component: () => import('../views/WeblogConfig.vue'),
      // https://router.vuejs.org/guide/essentials/passing-props.html#boolean-mode
      props: true
    },
    {
      path: '/app/createWeblog',
      name: 'createWeblog',
      component: () => import('../views/WeblogConfig.vue')
    },
    {
      path: '/admin/globalConfig',
      name: 'globalConfig',
      component: () => import('../views/GlobalConfig.vue')
    },
    {
      path: '/logout',
      name: 'logout',
      redirect: () => {
        return import.meta.env.VITE_PUBLIC_PATH + '/app/logout'
      }
    },

    /*
    {
      path: '/app/entryEdit/:weblogId',
      name: 'entryEdit',
      component: () => import('../views/EntryEdit.vue'),
      props: (route) => ({
        weblogId: route.params.weblogId,
        entryId: route.query.entryId
      })
    }, */
    {
      // can delete
      path: '/home',
      name: 'home',
      component: HomeView
    }
  ]
})

export default router
