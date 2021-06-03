<template>
  <div
    id="errorMessageDiv"
    class="alert alert-danger"
    role="alert"
    v-if="message"
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
  props: {
    message: {
      type: String,
      default: ""
    }
  }
};
</script>
