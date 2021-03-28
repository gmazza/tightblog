<template>
  <nav id="nav">
    <a href="../../tb-ui/tb-ui/app/home">My Blogs</a>
    <a href="../../tb-ui/app/admin/globalConfig">Global Configuration</a>
    <router-link to="/admin/globalConfig">Global Configuration</router-link>
    <router-link to="/admin/userAdmin">User Administration</router-link>
    <router-link to="/admin/cachedData">Cached Data</router-link>
  </nav>
</template>

<style scoped>
#nav {
  display: flex;
  justify-content: center;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
  padding: 0 10px;
}

#nav a.router-link-exact-active {
  color: blue;
}
</style>
