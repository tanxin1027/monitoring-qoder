import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/Dashboard.vue'),
        meta: { title: '数据大屏', icon: 'DataLine' }
      },
      {
        path: '/monitor/server',
        name: 'ServerMonitor',
        component: () => import('@/pages/monitor/Server.vue'),
        meta: { title: '服务器监控', icon: 'Monitor' }
      },
      {
        path: '/monitor/mysql',
        name: 'MysqlMonitor',
        component: () => import('@/pages/monitor/Mysql.vue'),
        meta: { title: 'MySQL监控', icon: 'Coin' }
      },
      {
        path: '/monitor/tomcat',
        name: 'TomcatMonitor',
        component: () => import('@/pages/monitor/Tomcat.vue'),
        meta: { title: 'Tomcat监控', icon: 'Box' }
      },
      {
        path: '/monitor/alert',
        name: 'Alert',
        component: () => import('@/pages/monitor/Alert.vue'),
        meta: { title: '告警管理', icon: 'Bell' }
      },
      {
        path: '/hospital/list',
        name: 'HospitalList',
        component: () => import('@/pages/hospital/List.vue'),
        meta: { title: '医院管理', icon: 'OfficeBuilding' }
      },
      {
        path: '/system/user',
        name: 'UserManage',
        component: () => import('@/pages/system/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: '/system/role',
        name: 'RoleManage',
        component: () => import('@/pages/system/Role.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: '/system/menu',
        name: 'MenuManage',
        component: () => import('@/pages/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.public) {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
