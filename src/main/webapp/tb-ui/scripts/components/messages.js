Vue.component('success-message-box', {
    //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    props: ['message'],
    template: `
        <div id="successMessageDiv" class="alert alert-success" role="alert" v-show="message" v-cloak>
            {{message}}
            <button type="button" class="close" v-on:click="$emit('close-box')" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
        </div>
        `
});

Vue.component('error-message-box', {
    //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    props: ['message'],
    template: `
        <div id="errorMessageDiv" class="alert alert-danger" role="alert" v-if="message" v-cloak>
            {{message}}
            <button type="button" class="close" v-on:click="$emit('close-box')" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
        </div>
        `
});

Vue.component('error-list-message-box', {
    //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    props: ['inErrorObj'],
    template: `
        <div id="errorMessageDiv" class="alert alert-danger" role="alert" v-show="inErrorObj.errors && inErrorObj.errors.length > 0" v-cloak>
            <button type="button" class="close" v-on:click="$emit('close-box')" aria-label="Close">
            <span aria-hidden="true">&times;</span>
            </button>
            <ul class="list-unstyled">
            <li v-for="item in inErrorObj.errors" v-html="item.message"></li>
            </ul>
        </div>
        `
});
