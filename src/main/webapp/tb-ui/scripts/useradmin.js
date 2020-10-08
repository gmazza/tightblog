var vm = new Vue({
    el: '#template',
    data: {
        urlRoot: contextPath + '/tb-ui/admin/rest/useradmin/',
        metadata: { weblogList : [] },
        pendingList: {},
        userList: {},
        userToEdit: null,
        userBeingEdited: null,
        userCredentials: null,
        userBlogList: {},
        successMessage: null,        
        errorObj: {}
    },
    methods: {
        loadMetadata: function() {
            axios
            .get(contextPath + '/tb-ui/register/rest/useradminmetadata')
            .then(response => {
                this.metadata = response.data;
            })
            .catch(error => this.commonErrorResponse(error, null));
        },
        getPendingRegistrations: function() {
            axios
            .get(this.urlRoot + 'registrationapproval')
            .then(response => {
                this.pendingList = response.data;
            });
        },
        loadUserList: function() {
            axios
            .get(this.urlRoot + 'userlist')
            .then(response => {
                this.userList = response.data;
                if (!this.userToEdit && Object.keys(this.userList).length > 0) {
                    for (first in this.userList) {
                        this.userToEdit = first;
                        break;
                    }
                    }
            })
            .catch(error => this.commonErrorResponse(error, null));
        },
        approveUser: function(userId) {
            this.processRegistration(userId, 'approve');
        },
        declineUser: function(userId) {
            this.processRegistration(userId, 'reject');
        },
        processRegistration: function(userId, command) {
            this.messageClear();
            axios
            .post(this.urlRoot + 'registrationapproval/' + userId + '/' + command)
            .then(response => {
                this.getPendingRegistrations();
                this.loadUserList();
            })
            .catch(error => this.commonErrorResponse(error));
        },
        loadUser: function() {
            this.messageClear();
            axios
            .get(this.urlRoot + 'user/' + this.userToEdit)
            .then(response => {
                 this.userBeingEdited = response.data.user;
                 this.userCredentials = response.data.credentials;
            });

            axios
            .get(this.urlRoot + 'user/' + this.userToEdit + '/weblogs')
            .then(response => {
                 this.userBlogList = response.data;
            })
        },
        updateUser: function() {
            this.messageClear();
            var userData = {};
            userData.user = this.userBeingEdited;
            userData.credentials = this.userCredentials;

            axios
            .put(this.urlRoot + 'user/' + this.userBeingEdited.id, userData)
            .then(response => {
                this.userBeingEdited = response.data.user;
                this.userCredentials  = response.data.credentials;
                this.loadUserList();
                this.getPendingRegistrations();
                this.successMessage = "User [" + this.userBeingEdited.screenName + "] updated."
            })
            .catch(error => this.commonErrorResponse(error))
        },
        cancelChanges: function() {
            this.messageClear();
            this.userBeingEdited = null;
            this.userCredentials = null;
        },
        formatDate: function(isoDate) {
            return dayjs(isoDate).format('DD MMM YYYY h:mm:ss A');
        },
        messageClear: function() {
            this.successMessage = null;
            this.errorObj = {};
        },
        commonErrorResponse: function(error) {
            if (error.response.status == 401) {
                window.location.replace($('#refreshURL').attr('value'));
            } else {
                this.errorObj = error.response.data;
            }
        }
    },
    mounted: function() {
        this.loadMetadata();
        this.getPendingRegistrations();
        this.loadUserList();
    }
})
