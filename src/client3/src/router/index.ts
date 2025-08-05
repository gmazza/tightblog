import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

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
      path: '/admin/userAdmin',
      name: 'userAdmin',
      component: () => import('../views/UserAdmin.vue')
    },
    {
      path: '/admin/cachedData',
      name: 'cachedData',
      component: () => import('../views/CachedData.vue')
    },
    {
      path: '/app/logout',
      name: 'logout',
      beforeEnter: () => {
        window.location.href = import.meta.env.VITE_PUBLIC_PATH + '/app/logout'
      }
    } as unknown as RouteRecordRaw,
    {
      path: '/app/profile',
      name: 'profile',
      component: () => import('../views/UserEdit.vue')
    },
    {
      path: '/app/register',
      name: 'register',
      component: () => import('../views/UserEdit.vue')
    },
    {
      path: '/app/entryEdit/:weblogId',
      name: 'entryEdit',
      component: () => import('../views/EntryEdit.vue'),
      props: (route) => ({
        weblogId: route.params.weblogId,
        entryId: route.query.entryId
      })
    },
    {
      path: '/app/entries/:weblogId',
      name: 'entries',
      meta: {
        userNeedsMFARegistration: true
      },
      component: () => import('../views/BlogEntries.vue'),
      props: true
    },
    {
      path: '/app/comments/:weblogId',
      name: 'comments',
      component: () => import('../views/EntryComments.vue'),
      props: (route) => ({
        weblogId: route.params.weblogId,
        entryId: route.query.entryId || null
      })
    },
    {
      path: '/app/categories/:weblogId',
      name: 'categories',
      component: () => import('../views/CategoriesView.vue'),
      props: true
    },
    {
      path: '/app/tags/:weblogId',
      name: 'tags',
      component: () => import('../views/TagsView.vue'),
      props: true
    },
    {
      path: '/app/blogroll/:weblogId',
      name: 'blogroll',
      component: () => import('../views/BlogrollView.vue'),
      props: true
    },
    {
      path: '/app/mediaFiles/:weblogId',
      name: 'mediaFiles',
      component: () => import('../views/MediaFiles.vue'),
      props: true
    },
    {
      path: '/app/mediaFileEdit/:weblogId',
      name: 'mediaFileEdit',
      component: () => import('../views/MediaFileEdit.vue'),
      props: (route) => ({
        weblogId: route.params.weblogId,
        folderId: route.query.folderId,
        mediaFileId: route.query.mediaFileId || null
      })
    },
    {
      path: '/app/templates/:weblogId',
      name: 'templates',
      component: () => import('../views/BlogTemplates.vue'),
      props: true
    },
    {
      path: '/app/templateEdit/:weblogId',
      name: 'templateEdit',
      component: () => import('../views/TemplateEdit.vue'),
      props: (route) => {
        const props = {
          weblogId: route.params.weblogId,
          templateId: route.query.templateId || null,
          templateName: route.query.templateName || null
        }
        console.log('templateEdit props:', props)
        return props
      }
    }
  ]
})

export default router
