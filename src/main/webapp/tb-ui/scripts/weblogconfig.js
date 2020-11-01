var vm = new Vue({
    el: '#template',
    data: {
        weblog: {
            "theme": "rolling",
            "locale": "en",
            "timeZone": "America/New_York",
            "editFormat": "HTML",
            "allowComments": "NONE",
            "spamPolicy": "NO_EMAIL",
            "visible": true,
            "entriesPerPage": 12,
            "defaultCommentDays": -1
        },
        metadata: {
            sharedThemeMap: []
        },
        deleteDialogTitle: null,
        deleteDialogInstruction: null,
        deleteHandle: "",
        successMessage: null,
        errorObj: {}
    },
    methods: {
        loadMetadata: function () {
            axios.get(contextPath + '/tb-ui/authoring/rest/weblogconfig/metadata')
                .then(response => {
                    this.metadata = response.data;
                })
                .catch(error => this.commonErrorResponse(error));
        },
        loadWeblog: function () {
            axios.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId)
                .then(response => {
                    this.weblog = response.data;
                    // used in eval below
                    var weblogHandle = this.weblog.handle;
                    this.deleteDialogTitle = eval('`' + msg.deleteDialogTitleTmpl + '`');
                    this.deleteDialogInstruction = eval('`' + msg.deleteDialogInstructionTmpl + '`');
                }
                )
        },
        updateWeblog: function () {
            this.messageClear();
            var urlToUse = contextPath + (weblogId ? '/tb-ui/authoring/rest/weblog/' + weblogId
                : '/tb-ui/authoring/rest/weblogs');

            axios.post(urlToUse, this.weblog)
                .then(response => {
                    this.weblog = response.data;
                    if (!weblogId) {
                        window.location.replace(homeUrl);
                    } else {
                        this.successMessage = msg.successMessage;
                    }
                    window.scrollTo(0, 0);
                })
                .catch(error => {
                    if (error.response.status == 400) {
                        this.errorObj = error.response.data;
                        window.scrollTo(0, 0);
                    } else {
                        this.commonErrorResponse(error);
                    }
                })
        },
        deleteWeblog: function () {
            $('#deleteWeblogModal').modal('hide');

            if (weblogId && this.weblog.handle.toUpperCase() === this.deleteHandle.toUpperCase().trim()) {           
                axios.delete(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId)
                .then(response => {
                    window.location.replace(homeUrl);
                })
                .catch(error => this.commonErrorResponse(error));
            }
        },
        cancelChanges: function () {
            this.messageClear();
            if (weblogId) {
                this.loadWeblog();
                window.scrollTo(0, 0);
            } else {
                window.location.replace(homeUrl);
            }
        },
        commonErrorResponse: function (error) {
            if (error.response.status == 401) {
                window.location.replace($('#refreshURL').attr('value'));
            } else {
                this.errorObj = error.response.data;
                window.scrollTo(0, 0);
            }
        },
        messageClear: function () {
            this.errorObj = {};
            this.successMessage = null;
        }
    },
    mounted: function () {
        this.messageClear();
        this.loadMetadata();
        if (weblogId) {
            this.loadWeblog();
        }
    }
})
