var vm = new Vue({
  el: "#template",
  data: {
    mediaDirectories: [],
    mediaFiles: [],
    allFilesSelected: false,
    newFolderName: null,
    currentFolderId: null,
    targetFolderId: null,
    successMessage: null,
    modalMessage: null,
    errorObj: {},
  },
  computed: {
    moveToFolders: function () {
      // adding extra var to trigger cache refresh
      var compareVal = this.currentFolderId;
      return this.mediaDirectories.filter(function (item) {
        return item.id != compareVal;
      })
    },
    filesSelectedCount: function () {
      return this.mediaFiles.filter(file => file.selected).length;
    }
  },
  methods: {
    loadMediaDirectories: function () {
      axios
        .get(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories')
        .then(response => {
          this.mediaDirectories = response.data;
          if (this.mediaDirectories && this.mediaDirectories.length > 0) {
            if (!this.currentFolderId) {
              this.currentFolderId = directoryId ? directoryId : this.mediaDirectories[0].id;
            }
            this.loadMediaFiles();
          }
        })
    },
    loadMediaFiles: function () {
      axios.
        get(contextPath + '/tb-ui/authoring/rest/mediadirectories/' + this.currentFolderId + '/files')
        .then(response => {
          this.mediaFiles = response.data;
          this.allFilesSelected = false;
        })
    },
    getFolderName: function (folderId) {
      for (i = 0; this.mediaDirectories && i < this.mediaDirectories.length; i++) {
        if (this.mediaDirectories[i].id == folderId) {
          return this.mediaDirectories[i].name;
        }
      }
      return null;
    },
    copyToClipboard: function (mediaFile) {
      const textarea = document.createElement('textarea');
      document.body.appendChild(textarea);

      if (mediaFile.imageFile === true) {
        anchorTag = (mediaFile.anchor ? '<a href="' + mediaFile.anchor + '">' : '') +
          '<img src="' + mediaFile.permalink + '"' +
          ' alt="' + (mediaFile.altText ? mediaFile.altText : mediaFile.name) + '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : '') +
          '>' +
          (mediaFile.anchor ? '</a>' : '');
      } else {
        anchorTag = '<a href="' + mediaFile.permalink + '"' +
          (mediaFile.titleText ? ' title="' + mediaFile.titleText + '"' : '') +
          '>' + (mediaFile.altText ? mediaFile.altText : mediaFile.name) + '</a>';
      }

      textarea.value = anchorTag;
      textarea.select();
      document.execCommand('copy');
      textarea.remove();
    },
    toggleCheckboxes: function(checkAll) {
      this.mediaFiles.forEach(file => {
        // Vue.set to force model refreshing
        Vue.set(file, 'selected', checkAll);
      })
    },
    addFolder: function () {
      if (!this.newFolderName) {
        return;
      }
      this.messageClear();
      var newFolder = {
        name: this.newFolderName
      };
      axios
        .put(contextPath + '/tb-ui/authoring/rest/weblog/' + weblogId + '/mediadirectories', newFolder)
        .then(response => {
          this.currentFolderId = response.data;
          this.newFolderName = '';
          this.loadMediaDirectories();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    showDeleteFolderModal: function () {
      this.messageClear();
      // vars used by eval
      var folderName = this.getFolderName(this.currentFolderId);
      var count = this.mediaFiles.length;
      this.modalMessage = eval('`' + msg.confirmDeleteFolderTmpl + '`')
      $('#deleteFolderModal').modal('show');
    },
    deleteFolder: function () {
      this.messageClear();
      $('#deleteFolderModal').modal('hide');
      axios
        .delete(contextPath + '/tb-ui/authoring/rest/mediadirectory/' + this.currentFolderId)
        .then(response => {
          this.successMessage = response.data;
          this.currentFolderId = null;
          this.loadMediaDirectories();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    showDeleteFilesModal: function () {
      this.messageClear();
      // vars used by eval
      var count = this.filesSelectedCount;
      this.modalMessage = eval('`' + msg.confirmDeleteFilesTmpl + '`')
      $('#deleteFilesModal').modal('show');
    },
    deleteFiles: function () {
      this.messageClear();
      $('#deleteFilesModal').modal('hide');

      var filesToDelete = [];
      this.mediaFiles.forEach(mediaFile => {
        if (mediaFile.selected) filesToDelete.push(mediaFile.id);
      })

      axios.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId,
        filesToDelete)
        .then(response => {
          this.successMessage = response.data;
          this.loadMediaFiles();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    showMoveFilesModal: function () {
      this.messageClear();
      var self = this;
      var targetFolder = this.mediaDirectories.filter(function (item) {
        return item.id == self.targetFolderId;
      });
      if (targetFolder.length != 1) {
        return;
      }
      // vars used by eval
      var count = this.filesSelectedCount;
      var targetFolderName = targetFolder[0].name;
      this.modalMessage = eval('`' + msg.confirmMoveFilesTmpl + '`')
      $('#moveFilesModal').modal('show');
    },
    moveFiles: function () {
      this.messageClear();
      $('#moveFilesModal').modal('hide');

      var filesToMove = [];
      this.mediaFiles.forEach(mediaFile => {
        if (mediaFile.selected) filesToMove.push(mediaFile.id);
      })

      axios.post(contextPath + '/tb-ui/authoring/rest/mediafiles/weblog/' + weblogId +
        "/todirectory/" + this.targetFolderId, filesToMove)
        .then(response => {
          this.successMessage = response.data;
          this.loadMediaFiles();
        })
        .catch(error => this.commonErrorResponse(error));
    },
    commonErrorResponse: function (error) {
      if (error.response.status == 401) {
        window.location.replace($('#refreshURL').attr('value'));  // return;
      } else {
        this.errorObj = error.response.data;
      }
    },
    messageClear: function () {
      this.successMessage = null;
      this.errorObj = {};
    }
  },
  mounted: function () {
    this.loadMediaDirectories();
  }
});
