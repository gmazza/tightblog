<template>
  <div>
    <nav id="nav">
      <router-link :to="{ name: 'entryEdit' }">New Entry</router-link>
      <router-link :to="{ name: 'entries' }">Entries</router-link>
      <!--router-link :to="{ name: 'mediaFiles' }">Media Files</router-link>
      <router-link :to="{ name: 'comments' }">Comments</router-link>
      <!--router-link :to="{ name: 'mediaFiles' }">Media Files</router-link>
      <router-link :to="{ name: 'categories' }">Categories</router-link>
      <router-link :to="{ name: 'tags' }">Tags</router-link>
      <router-link :to="{ name: 'blogroll' }">Blogroll</router-link-->
      <router-link :to="{ name: 'weblogConfig' }">Weblog Settings</router-link>
      <!--router-link :to="{ name: 'templates' }">Templates</router-link-->
    </nav>
  </div>
</template>
