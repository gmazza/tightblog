var vm = new Vue({
    el: '#template',
    data: {
        lookupFields: {},
        searchParams: {
            status: ""
        },
        commentData: {
            comments: []
        },
        entryTitleMsg: null,
        nowShowingMsg: null,
        errorObj: {},
        selectedCommentId: null,
        pageNum: 0,
        urlRoot: contextPath + '/tb-ui/authoring/rest/comments/'
    },
    methods: {
        loadLookupFields: function() {
            axios
            .get(this.urlRoot + 'searchfields')
            .then(response => {
                this.lookupFields = response.data;
            })
            .catch(error => this.commonErrorResponse(error));
        },
        loadComments: function() {
            var urlToUse = this.urlRoot + weblogId + '/page/' + this.pageNum;
            if (entryId) {
                urlToUse += "?entryId=" + entryId
                entryTitleMsg = ' ';
            }
    
            var queryParams = {...this.searchParams};
            queryParams.startDate = this.dateToSeconds(this.searchParams.startDateString, false);
            queryParams.endDate = this.dateToSeconds(this.searchParams.endDateString, true);
    
            if (queryParams.status == "") {
                queryParams.status = null;
            };
    
            axios
            .post(urlToUse, queryParams)
            .then(response => {
                this.commentData = response.data;
                if (entryId) {
                    var entryTitle = this.commentData.entryTitle;
                    this.entryTitleMsg = eval('`' + entryTitleTmpl + '`');
                }
                var count = this.commentData.comments.length;

                this.nowShowingMsg = eval('`' + nowShowingTmpl + '`');
            })
            .catch(error => this.commonErrorResponse(error));
        },
        updateStartDate: function(date) {
            this.searchParams.startDateString = date;
        },
        updateEndDate: function(date) {
            this.searchParams.endDateString = date;
        },
        previousPage: function() {
            this.pageNum--;
            this.loadComments();
        },
        nextPage: function() {
            this.pageNum++;
            this.loadComments();
        },
        editComment: function(comment) {
            this.messageClear();
            comment.editable = true;
            comment.originalContent = comment.content;
        },
        editCommentCancel: function(comment) {
            this.messageClear();
            comment.editable = false;
            comment.content = comment.originalContent;
        },
        saveComment: function(comment) {
            this.messageClear();
            if (!comment.editable) {
                return;
            }
            comment.editable = false;

            axios
            .put(this.urlRoot + comment.id + '/content' , comment.content,
             { headers: {'Content-Type': 'application/json'} })
            .catch(error => this.commonErrorResponse(error));
        },
        approveComment: function(comment) {
            this.messageClear();
            axios
            .post(this.urlRoot + comment.id + '/approve')
            .then(response => {
                comment.status = 'APPROVED';
            })
            .catch(error => this.commonErrorResponse(error))
        },
        hideComment: function(comment) {
            this.messageClear();
            axios
            .post(this.urlRoot + comment.id + '/hide')
            .then(response => {
                comment.status = 'DISAPPROVED';
            })
            .catch(error => this.commonErrorResponse(error));
        },
        deleteComment: function(comment) {
            this.messageClear();
            axios
            .delete(this.urlRoot + comment.id)
            .then(response => {
                this.loadComments();
            })
            .catch(error => this.commonErrorResponse(error));
        },
        getCommentHeader: function(comment) {
            var name = comment.name;
            var email = comment.email;
            var remoteHost = comment.remoteHost;
            return eval('`' + commentHeaderTmpl + '`');
        },
        commentStatusClass: function(status) {
            if (status == 'SPAM') {
                return 'spamcomment';
            } else if (status == 'PENDING' || status == 'DISAPPROVED') {
                return 'pendingcomment';
            } else {
                return null;
            }
        },
        dateToSeconds: function(dateStr, addOne) {
            if (dateStr) {
                return Math.floor( Date.parse(dateStr) / 1000 ) + (addOne ? 1440 * 60 - 1 : 0);
            } else {
                return null;
            }
        },    
        messageClear: function() {
            this.errorObj = {};
        },
        commonErrorResponse: function(error) {
            if (error.response.status == 401) {
               window.location.replace($('#refreshURL').attr('value'));
            } else {
               this.errorMsg = error.response.data;
            }
        }
    },
    mounted: function() {
        this.loadLookupFields();
        this.loadComments();
    }
});
