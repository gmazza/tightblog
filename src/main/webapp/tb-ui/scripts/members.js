var vm = new Vue({
  el: "#template",
  data: {
    roles: [],
    potentialMembers: {},
    userToAdd: null,
    userToAddRole: null,
    successMessage: null,
    errorObj: {}
  },
  methods: {
    updateRoles: function () {
      this.messageClear();
      axios.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/memberupdate', this.roles)
        .then(response => {
          this.successMessage = response.data;
          this.loadMembers();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    addUserToWeblog: function () {
      this.messageClear();
      if (!this.userToAdd || !this.userToAddRole) {
        return;
      }
      axios.post(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/user/' + this.userToAdd +
        '/role/' + this.userToAddRole + '/attach')
        .then(response => {
          this.successMessage = response.data;
          this.userToAdd = '';
          this.userToAddRole = '';
          this.loadMembers();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    loadPotentialMembers: function () {
      this.userToAdd = null;
      axios.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/potentialmembers')
        .then(response => {
          this.potentialMembers = response.data;
          if (Object.keys(this.potentialMembers).length > 0) {
            for (first in this.potentialMembers) {
              this.userToAdd = first;
              break;
            }
          }
        });
    },
    loadMembers: function () {
      axios.get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/members')
        .then(response => {
          this.roles = response.data;
        });
      this.loadPotentialMembers();
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
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
    this.loadMembers();
  }
});
