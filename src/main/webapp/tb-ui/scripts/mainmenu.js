var vm = new Vue({
  el: "#template",
  data: {
    roles: [],
    confirmResignationText: null,
    roleIdToResign: null,
    successMessage: null,
    errorObj: {}
  },
  methods: {
    toggleEmails: function(role) {
      axios.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + role.id + '/emails/' + role.emailComments)
      .catch(error => self.commonErrorResponse(error));
    },
    getRoleText: function(weblogRole) {
      if (weblogRole == 'POST') {
        return 'PUBLISHER';
      } else if (weblogRole == 'EDIT_DRAFT') {
        return 'CONTRIBUTOR';
      }; // else 'OWNER'
      return weblogRole;
    },
    getUnapprovedCommentsString: function(unapprovedCommentCount) {
      return eval('`' + msg.unapprovedCommentsTmpl + '`')
    },
    showResignWeblog: function(role) {
      this.roleIdToResign = role.id;
      var weblogName = role.weblog.name;
      this.confirmResignationText = eval('`' + msg.confirmResignationTmpl + '`');
      $('#resignWeblogModal').modal('show');
    },
    resignWeblog: function() {
      $('#resignWeblogModal').modal('hide');
      axios.post(contextPath + '/tb-ui/authoring/rest/weblogrole/' + this.roleIdToResign + '/detach')
      .then(response => {
        this.successMessage = 'to fill in...';
        this.listBlogs();
      })
      .catch(error => this.commonErrorResponse(error))
    },
    listBlogs: function() {
      axios.get(contextPath + '/tb-ui/authoring/rest/loggedinuser/weblogs')
      .then(response => {
        this.roles = response.data;
      });
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
    this.listBlogs();
  }
});
