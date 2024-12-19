<template>
  <div
    id="errorMessageDiv"
    class="alert alert-danger"
    role="alert"
    v-show="inErrorObj.errors && inErrorObj.errors.length > 0"
    v-cloak
  >
    <button type="button" class="close" v-on:click="$emit('close-box')" aria-label="Close">
      <span aria-hidden="true">&times;</span>
    </button>
    <ul class="list-unstyled">
      <li v-for="(item, index) in inErrorObj.errors" v-html="item" :key="`${index}`"></li>
    </ul>
  </div>
</template>

<script lang="ts">
import type { PropType } from 'vue'
import type { ErrorObj } from '@/types/interfaces'

export default {
  props: {
    inErrorObj: {
      type: Object as PropType<ErrorObj>,
      default: () => ({ errors: [] })
    }
  }
}
</script>
