var vm = new Vue({
    el: '#template',
    data: {
        tagData: { tags: {} },
        editModalTitle: '',
        editModalAction: null,
        editModalCurrentTag: null,
        newTagName: null,
        pageNum: 0,
        urlRoot: contextPath + '/tb-ui/authoring/rest/tags/',
        resultsMap: {},
        successMessage: '',
        errorObj: {}
    },
    computed: {
        tagsSelected: function () {
            return this.tagData.tags.filter(tag => tag.selected).length > 0;
        }
    },
    methods: {
        toggleCheckboxes: function (checkAll) {
            this.tagData.tags.forEach(tag => {
                // Vue.set to force model refreshing
                Vue.set(tag, 'selected', checkAll);
            });
        },
        deleteTags: function () {
            this.messageClear();
            $('#deleteTagsModal').modal('hide');

            var selectedTagNames = [];
            this.tagData.tags.forEach(tag => {
                if (tag.selected) selectedTagNames.push(tag.name);
            });

            axios
                .post(contextPath + '/tb-ui/authoring/rest/tags/weblog/' + weblogId + '/delete', selectedTagNames)
                .then(response => {
                    this.successMessage = selectedTagNames.length + ' tag(s) deleted';
                    this.loadTags();
                });
        },
        showReplaceModal: function (oldTag) {
            this.messageClear();
            // tagName needed for eval below
            var tagName = oldTag.name;
            this.editModalTitle = eval('`' + msg.replaceTagTitleTmpl + '`');
            this.editModalAction = 'replace';
            this.editModalCurrentTag = tagName;
            $('#changeTagModal').modal('show');
        },
        showAddModal: function (currentTag) {
            this.messageClear();
            // tagName needed for eval below
            var tagName = currentTag.name;
            this.editModalTitle = eval('`' + msg.addTagTitleTmpl + '`');
            this.editModalAction = 'add';
            this.editModalCurrentTag = tagName;
            $('#changeTagModal').modal('show');
        },
        tagUpdate: function () {
            this.messageClear();
            if (this.editModalAction == 'replace') {
                this.replaceTag(this.editModalCurrentTag, this.newTagName);
            } else if (this.editModalAction == 'add') {
                this.addTag(this.editModalCurrentTag, this.newTagName);
            }
            $('#changeTagModal').modal('hide');
            this.inputClear();
        },
        addTag: function (currentTag, newTag) {
            this.messageClear();
            axios
                .post(this.urlRoot + 'weblog/' + weblogId + '/add/currenttag/' + currentTag + '/newtag/' + newTag)
                .then(response => {
                    this.resultsMap = response.data;
                    this.successMessage = 'Added [' + newTag + '] to ' + this.resultsMap.updated + ' entries having ['
                        + currentTag + (this.resultsMap.unchanged > 0 ? '] (' + this.resultsMap.unchanged
                            + ' already had [' + newTag + '])' : ']');
                    this.loadTags();
                })
                .catch(error => this.commonErrorResponse(error));
        },
        replaceTag: function (currentTag, newTag) {
            this.messageClear();
            axios
                .post(this.urlRoot + 'weblog/' + weblogId + '/replace/currenttag/' + currentTag + '/newtag/' + newTag)
                .then(response => {
                    this.resultsMap = response.data;
                    this.successMessage = 'Replaced [' + currentTag + '] with [' + newTag + '] in ' + this.resultsMap.updated
                        + ' entries' + (this.resultsMap.unchanged > 0 ? ', deleted [' + currentTag + '] from '
                            + this.resultsMap.unchanged + ' entries already having [' + newTag + ']' : '');
                    this.loadTags();
                })
                .catch(error => this.commonErrorResponse(error));
        },
        loadTags: function () {
            axios
                .get(this.urlRoot + weblogId + '/page/' + this.pageNum)
                .then(response => {
                    this.tagData = response.data;
                })
                .catch(error => this.commonErrorResponse(error));
        },
        previousPage: function () {
            this.messageClear();
            this.pageNum--;
            this.loadTags();
        },
        nextPage: function () {
            this.messageClear();
            this.pageNum++;
            this.loadTags();
        },
        commonErrorResponse: function (error) {
            if (response.status == 401) {
                window.location.replace($('#refreshURL').attr('value'));
            } else {
                this.errorObj = error.response.data;
            }
        },
        messageClear: function () {
            this.successMessage = '';
            this.errorObj = {};
        },
        inputClear: function () {
            this.newTagName = '';
        }
    },
    mounted: function () {
        this.loadTags();
    }
});
