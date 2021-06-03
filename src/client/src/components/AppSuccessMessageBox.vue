<template>
  <div
    id="successMessageDiv"
    class="alert alert-success"
    role="alert"
    v-show="message"
    v-cloak
  >
    {{ message }}
    <button
      type="button"
      class="close"
      v-on:click="$emit('close-box')"
      aria-label="Close"
    >
      <span aria-hidden="true">&times;</span>
    </button>
  </div>
</template>

<script>
export default {
  //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
  props: {
    message: {
      type: String,
      default: ""
    }
  }
};
</script>
