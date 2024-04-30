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
    } /*
    {
      path: '/app/entryEdit/:weblogId',
      name: 'entryEdit',
      component: () => import('../views/EntryEdit.vue'),
      props: (route) => ({
        weblogId: route.params.weblogId,
        entryId: route.query.entryId
      })
    }, */,
    {
      // can delete
      path: '/',
      name: 'home',
      component: HomeView
    }
  ]
})

export default router
