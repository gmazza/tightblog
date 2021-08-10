<template>
  <div>
    <nav id="nav">
      <router-link :to="{ name: 'weblogConfig' }">Weblog Settings</router-link>
      <router-link :to="{ name: 'blogroll' }">Blogroll</router-link>
      <router-link :to="{ name: 'categories' }">Categories</router-link>
      <router-link :to="{ name: 'tags' }">Tags</router-link>
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
