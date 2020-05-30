var vm = new Vue({
  el: '#template',
  data: {
    urlRoot: contextPath + '/tb-ui/admin/rest/server/',
    metadata: { weblogList : [] },
    webloggerProps: { mainBlog: { id: '' } },
    successMessage: null,
    errorMessage: null
  },
  methods: {
    loadWebloggerProperties: function() {
      axios
      .get(this.urlRoot + 'webloggerproperties')
      .then(response => {
        this.webloggerProps = response.data;
        if (!this.webloggerProps.mainBlog) {
          this.webloggerProps.mainBlog = { id : '' };
        }
    })
    },
    loadMetadata: function() {
      axios
      .get(this.urlRoot + 'globalconfigmetadata')
      .then(response => {
          this.metadata = response.data;
      })
      .catch(error => this.commonErrorResponse(error, null));
    },
    updateProperties: function() {
      if (!this.webloggerProps.mainBlog.id) {
        this.webloggerProps.mainBlog = null;
      }
      axios
      .post(this.urlRoot + 'webloggerproperties', this.webloggerProps)
      .then(response => {
          this.errorObj = {};
          this.successMessage = response.data;
          this.loadWebloggerProperties();
          window.scrollTo(0, 0);
      })
      .catch(error => {
         if (error.response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
         } else if (error.response.status == 400) {
           this.errorObj = error.response.data;
           window.scrollTo(0, 0);
         }
      })
    },
    commonErrorResponse: function(error, errorMsg) {
      if (error.response.status == 408) {
         window.location.replace($('#refreshURL').attr('value'));
      } else {
         this.errorMessage = errorMsg ? errorMsg : error.response.data.error;
      }
    }
  },
  mounted: function() {
    this.loadMetadata();
    this.loadWebloggerProperties();
  }
})
