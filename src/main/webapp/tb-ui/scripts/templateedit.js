var vm = new Vue({
    el: "#template",
    data: {
        templateData: {
            role: null,
        },
        lastSavedName: null,
        showSuccessMessage: false,
        errorObj: {},
        templateLoaded: false
    },
    methods: {
        launchPage: function() {
            window.open(weblogUrl + 'page/' + this.lastSavedName, '_blank');
        },
        loadTemplate: function() {
            var urlStem;
            if (templateId) {
                urlStem = '/tb-ui/authoring/rest/template/' + templateId;
            } else {
                urlStem = '/tb-ui/authoring/rest/weblog/' + weblogId + '/templatename/' + templateName + '/';
            }
            axios.get(contextPath + urlStem)
            .then(response => {
                this.templateData = response.data;
                this.lastSavedName = this.templateData.name;
                this.templateLoaded = true;
            });
        },
        saveTemplate: function() {
            this.messageClear();
            var urlStem = '/tb-ui/authoring/rest/weblog/' + weblogId + '/templates';
  
            axios.post(contextPath + urlStem, this.templateData)
            .then(response => {
                templateId = response.data;
                this.loadTemplate();
                this.showSuccessMessage = true;
            })
            .catch(error => this.commonErrorResponse(error))
        },
        messageClear: function () {
            this.showSuccessMessage = false;
            this.errorObj = {};
        },
        commonErrorResponse: function (error) {
            if (error.response.status == 401) {
              window.location.replace($('#refreshURL').attr('value'));
            } else {
              this.errorObj = error.response.data;
            }
        },
    },
    created: function() {
        this.loadTemplate();
    }
});
