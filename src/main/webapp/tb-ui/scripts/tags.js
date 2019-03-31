  $(function() {
    $('#deleteTagModal').on('show.bs.modal', function(e) {
        //get data-id attribute of the clicked element
        var tagName = $(e.relatedTarget).attr('current-tag');

        // populate delete modal with tag-specific information
        var modal = $(this)
        modal.find('button[id="deleteButton"]').data("tagName", tagName);
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#confirmDeleteMsg').html(tmpl);
    });

    $('#changeTagModal').on('show.bs.modal', function(e) {
        $('#newTag').val('');

        //get data-id attribute of the clicked element
        var tagName = $(e.relatedTarget).attr('current-tag');
        var action = $(e.relatedTarget).attr('action');

        // populate delete modal with tag-specific information
        var modal = $(this)
        var button = modal.find('button[id="changeButton"]');
        button.data("currentTag", tagName);
        button.data("action", action);
        var tmpl = eval('`' + (action == 'replace' ? msg.replaceTagTitleTmpl : msg.addTagTitleTmpl) + '`')
        modal.find('#changeTagModalTitle').html(tmpl);
    });

    $('#changeButton').on("click", function() {
        var currentTag = $(this).data('currentTag');
        var newTag = $('#newTag').val().trim();
        if (newTag) {
            if ($(this).data('action') == 'replace') {
                angular.element('#ngapp-div').scope().ctrl.replaceTag(currentTag, newTag);
            } else {
                angular.element('#ngapp-div').scope().ctrl.addTag(currentTag, newTag);
            }
            angular.element('#ngapp-div').scope().$apply();
            $('#changeTagModal').modal('hide');
        }
    });

});

tightblogApp.controller('PageController', ['$http',
    function PageController($http) {
    var self = this;
    this.tagData = {};
    this.errorObj = null;
    this.pageNum = 0;
    this.urlRoot = contextPath + '/tb-ui/authoring/rest/tags/';
    this.resultsMap = {};

    this.formatDate = function(inDate) {
        if (inDate) {
            return inDate[0] + '-' + ("00" + inDate[1]).slice(-2) + '-' + ("00" + inDate[2]).slice(-2);
        } else {
            return '';
        }
    }

    this.deleteTag = function(obj) {
        $('#deleteTagModal').modal('hide');

        // https://stackoverflow.com/a/18030442/1207540
        var tagname = obj.toElement.dataset.tagname;

        $http.delete(this.urlRoot + 'weblog/' + weblogId + '/tagname/' + tagname).then(
          function(response) {
             self.successMessage = '[' + tagname + '] deleted';
             $('#deleteButton').attr("data-tagname", '');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.addTag = function(currentTag, newTag) {
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/add/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.successMessage = 'Added [' + newTag + '] to ' + self.resultsMap.updated + ' entries having ['
                + currentTag + (self.resultsMap.unchanged > 0 ? '] (' + self.resultsMap.unchanged
                + ' already had [' + newTag + '])' : ']');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.replaceTag = function(currentTag, newTag) {
        $http.post(this.urlRoot + 'weblog/' + weblogId + '/replace/currenttag/' + currentTag + '/newtag/' + newTag).then(
          function(response) {
             self.resultsMap = response.data;
             self.successMessage = 'Replaced [' + currentTag + '] with [' + newTag + '] in ' + self.resultsMap.updated
                + ' entries' + (self.resultsMap.unchanged > 0 ? ', deleted [' + currentTag + '] from '
                + self.resultsMap.unchanged + ' entries already having [' + newTag + ']': '');
             self.loadTags();
          },
          self.commonErrorResponse
        )
    }

    this.loadTags = function() {
        $http.get(this.urlRoot + weblogId + '/page/' + this.pageNum).then(
        function(response) {
            self.tagData = response.data;
          },
          self.commonErrorResponse
        )
    };

    this.previousPage = function() {
        self.successMessage = '';
        this.pageNum--;
        this.loadTags();
    };

    this.nextPage = function() {
        self.successMessage = '';
        this.pageNum++;
        this.loadTags();
    };

    this.commonErrorResponse = function(response) {
        if (response.status == 408) {
           window.location.replace($('#refreshURL').attr('value'));
        } else if (response.status == 400) {
           self.successMessage = response.data;
        }
    }

    this.loadTags();

  }]);
