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
        deleteWeblogConfirmation: null,
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
                    this.deleteWeblogConfirmation = eval('`' + msg.deleteWeblogTmpl + '`');
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
                    this.successMessage = msg.successMessage;
                    if (!weblogId) {
                        window.location.replace(homeUrl);
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

            axios.delete(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId)
                .then(response => {
                    window.location.replace(homeUrl);
                })
                .catch(error => this.commonErrorResponse(error));
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
