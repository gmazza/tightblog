var vm = new Vue({
  el: "#template",
  data: {
    weblogTemplateData: {
      templates: [],
      templateRoleDescriptions: [],
      themes: []
    },
    allFilesSelected: false,
    newTemplateName: '',
    newTemplateRole: null,
    deleteDialogTitle: null,
    switchThemeTitle: null,
    targetThemeId: null,
    successMessage: null,
    errorObj: {}
  },
  computed: {
    switchToThemes: function () {
      return this.weblogTemplateData.themes.filter(function (theme) {
        return theme.id != currentTheme;
      })
    },
    targetThemeName: function() {
      // extra variable to activate value refreshing
      var themeIdToCompare = this.targetThemeId;
      var targetTheme = this.weblogTemplateData.themes.filter(function (theme) {
        return theme.id == themeIdToCompare;
      })
      return targetTheme[0].name;
    },
    templatesSelectedCount: function () {
      return this.weblogTemplateData.templates.filter(tmpl => tmpl.selected).length;
    }
  },
  methods: {
    loadTemplateData: function () {
      axios.get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/templates')
        .then(response => {
          this.weblogTemplateData = response.data;
          this.allFilesSelected = false;
          this.targetThemeId = this.switchToThemes[0].id;
        });
    },
    addTemplate: function () {
      this.messageClear();
      if (!this.newTemplateName) {
        return;
      }
      var newData = {
        "name": this.newTemplateName,
        "roleName": this.newTemplateRole,
        "template": ""
      };
      axios.post(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/templates', newData)
        .then(response => {
          this.successMessage = msg.success;
          this.loadTemplateData();
          this.resetAddTemplateData();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    showDeleteTemplatesModal: function () {
      this.messageClear();
      // vars used by eval
      var count = this.templatesSelectedCount;
      this.deleteDialogTitle = eval('`' + msg.confirmDeleteTemplate + '`')
      $('#deleteTemplatesModal').modal('show');
    },
    deleteTemplates: function () {
      $('#deleteTemplatesModal').modal('hide');

      var selectedItems = [];
      this.weblogTemplateData.templates.forEach(template => {
        if (template.selected) selectedItems.push(template.id);
      })

      axios.post(contextPath + '/tb-ui/authoring/rest/templates/delete', selectedItems)
        .then(response => {
          this.successMessage = msg.success;
          this.loadTemplateData();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    showSwitchThemeModal: function () {
      this.messageClear();
      // vars used by eval
      var targetThemeName = this.targetThemeName;
      this.switchThemeTitle = eval('`' + msg.switchThemeTitleTmpl + '`')
      $('#switchThemeModal').modal('show');
    },
    switchTheme: function () {
      this.messageClear();
      $('#switchThemeModal').modal('hide');

      axios.post(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/switchtheme/' + this.targetThemeId)
        .then(response => { window.location.replace(templatePageUrl); })
        .catch(error => this.commonErrorResponse(error));
    },
    toggleCheckboxes: function (checkAll) {
      this.weblogTemplateData.templates.filter(tmpl => !this.isBuiltIn(tmpl)).forEach(tmpl => {
        Vue.set(tmpl, 'selected', checkAll);
      })
    },
    isBuiltIn: function (template) {
      return template && template.derivation == 'Built-In';
    },
    resetAddTemplateData: function () {
      this.newTemplateName = '';
      this.newTemplateRole = 'CUSTOM_EXTERNAL';
    },
    commonErrorResponse: function (error) {
      if (error.response.status == 401) {
        window.location.replace($('#refreshURL').attr('value'));
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    }
  },
  created: function () {
    this.loadTemplateData();
    this.resetAddTemplateData();
  }
});
