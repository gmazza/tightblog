<template>
  <img src="@/assets/help.png" border="0" alt="icon" :title="helpText" />
</template>

<script>
export default {
  props: {
    helpText: {
      type: String,
      default: "",
    },
  },
};
</script>
