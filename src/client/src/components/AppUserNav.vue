<template>
    <div>
        <nav id="nav">
            <a href="../../tb-ui/app/home">My Blogs</a>
            <router-link :to="{ name: 'globalConfig' }">Test1</router-link>
            <router-link :to="{ name: 'userAdmin' }">Test2</router-link>
            <router-link :to="{ name: 'cachedData' }">Cached Data</router-link>
        </nav>
    </div>
</template>

<style scoped>
#nav {
  display: flex;
  justify-content: center;
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