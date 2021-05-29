<template>
  <div>
    <nav id="nav">
      <a href="../../tb-ui/app/home">My Blogs</a>
      <router-link to="/admin/globalConfig">Global Configuration</router-link>
      <router-link to="/admin/userAdmin">User Administration</router-link>
      <router-link to="/admin/cachedData">Cached Data</router-link>
    </nav>
  </div>
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

#banner {
    margin: 0px;
    padding: 0px 0px 0px 0px;
}
.bannerStatusBox {
    width: 100%;
}
div.bannerStatusBox {
    height: 2em;
}
.bannerStatusBox a {
    font-weight: bold;
}
.bannerLeft {
    padding: 4px 15px 4px 10px;
}
.bannerRight {
    padding: 4px 10px 4px 15px;
    text-align: right;
}

</style>
