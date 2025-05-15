<template>
  <div>
    <nav id="nav">
      <router-link :to="{ name: 'globalConfig' }">Global Configuration</router-link>
      <router-link :to="{ name: 'userAdmin' }">User Administration</router-link>
      <!--router-link :to="{ name: 'cachedData' }">Cached Data</router-link-->
    </nav>
  </div>
</template>
