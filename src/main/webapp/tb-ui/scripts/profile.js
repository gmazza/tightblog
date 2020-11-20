var vm = new Vue({
    el: "#template",
    data: {
        metadata: {},
        userBeingEdited: {},
        userCredentials: {},
        errorObj: {},
        hideButtons: false,
        showSuccessMessage: false
    },
    methods: {
        loadMetadata: function () {
            axios.get(contextPath + '/tb-ui/register/rest/useradminmetadata')
                .then(response => {
                    this.metadata = response.data;
                })
                .catch(error => this.commonErrorResponse(error))
        },
        loadUser: function () {
            axios.get(contextPath + '/tb-ui/authoring/rest/userprofile/' + userId)
                .then(response => {
                    this.userBeingEdited = response.data;
                    this.userCredentials = {};
                })
        },
        updateUser: function () {
            this.messageClear();
            var userData = {};
            userData.user = this.userBeingEdited;
            userData.credentials = this.userCredentials;
            var urlToUse = contextPath + (userId ? '/tb-ui/authoring/rest/userprofile/' + userId
                : '/tb-ui/register/rest/registeruser');

            axios.post(urlToUse, userData)
                .then(response => {
                    this.userBeingEdited = response.data.user;
                    if (!userId) {
                        this.hideButtons = true;
                        userId = this.userBeingEdited.id;
                    }
                    this.userCredentials = {};
                    this.showSuccessMessage = true;
                })
                .catch(error => this.commonErrorResponse(error));
        },
        cancelChanges: function () {
            this.messageClear();
            this.userBeingEdited = null;
            this.credentials = {};
        },
        messageClear: function () {
            this.errorObj = {};
            this.showSuccessMessage = false;
        },
        commonErrorResponse: function (error) {
            if (error.response.status == 401) {
                window.location.replace($('#refreshURL').attr('value'));
            } else {
                this.errorObj = error.response.data;
            }
        }
    },
    created: function () {
        this.loadMetadata();
        if (userId) {
            this.loadUser();
        }
    }
});
