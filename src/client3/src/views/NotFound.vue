<template>
  <div class="not-found">
    <h1>Page Not Found</h1>
    <p>The page you are looking for does not exist.</p>
    <router-link to="/">Return to home</router-link>
  </div>
</template>

<script lang="ts">
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.not-found {
  text-align: center;
  margin-top: 60px;
}
.not-found h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
}
.not-found p {
  margin-bottom: 2rem;
}
</style>
