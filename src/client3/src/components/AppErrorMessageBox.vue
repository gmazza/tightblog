<template>
  <div id="errorMessageDiv" class="alert alert-danger" role="alert" v-show="message" v-cloak>
    <div class="d-flex align-items-center">
      <span class="flex-grow-1">{{ message }}</span>
      <button
        type="button"
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
  props: {
    message: {
      type: String,
      default: ''
    }
  }
}
</script>
