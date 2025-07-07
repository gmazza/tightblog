<template>
  <div id="successMessageDiv" class="alert alert-success" role="alert" v-show="message" v-cloak>
    <div class="d-flex align-items-center">
      <span class="flex-grow-1">{{ message }}</span>
      <button
        type="button"
        v-show="message"
        class="btn-close ms-auto"
        v-on:click="$emit('close-box')"
        aria-label="Close"
      >
        <span aria-hidden="true"></span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
  props: {
    message: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.d-flex {
  display: flex; /* Remove !important */
}
</style>
