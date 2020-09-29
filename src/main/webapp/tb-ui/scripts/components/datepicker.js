Vue.component('date-picker', {
    //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    props: ['datevalue'],
    template: `
        <input type="text" size="12" 
            v-bind:value="datevalue" 
            v-on:input="$emit('input', $event.target.value)"
        readonly/>
        `,
    mounted: function() {
        var self = this;
        $(this.$el).datepicker({
            showOn: "button",
            buttonImage: "../../../images/calendar.png",
            buttonImageOnly: true,
            changeMonth: true,
            changeYear: true,
            onSelect: function(date) {
                self.$emit('update-date', date);
            }
        });
    },
    beforeDestroy: function() {
        $(this.$el).datepicker('hide').datepicker('destroy');
    }
});
