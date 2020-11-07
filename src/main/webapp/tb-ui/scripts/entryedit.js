Vue.component('tag-autocomplete', {
    props: ['tagsAsString'],
    template: `
        <input type="text" cssClass="entryEditTags"
            v-bind:value="tagsAsString"
            v-on:input="$emit('input', $event.target.value)"
            maxlength="255" style="width:60%">
    `,
    methods: {
        split: function(val) {
            return val.split( / \s*/ );
        },
        extractLast: function(term) {
            return this.split( term ).pop();
        }
    },
    mounted: function() {
        var self = this;
        $(this.$el).bind( "keydown", function( event ) {
            // don't navigate away from the field on tab when selecting an item
            if ( event.keyCode === $.ui.keyCode.TAB && $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
        })
        .autocomplete({
            delay: 500,
            source: function(request, response) {
                $.getJSON(contextPath + "/tb-ui/authoring/rest/weblogentries/" + weblogId + "/tagdata",
                { prefix: self.extractLast( request.term ) },
                function(data) {
                    response($.map(data.tagcounts, function (dataValue) {
                        return {
                            value: dataValue.name
                        };
                    }))
                })
            },
            focus: function() {
                // prevent value inserted on focus
                return false;
            },
            select: function( event, ui ) {
                var terms = self.split( this.value );
                // remove the current input
                terms.pop();
                // add the selected item
                terms.push( ui.item.value );
                // add placeholder to get the space at the end
                terms.push( "" );
                this.value = terms.join( " " );
                self.$emit('update-tags', this.value);
                return false;
            }
        });
    }
});

var vm = new Vue({
    el: '#template',
    data: {
        entry: {
            commentCountIncludingUnapproved: 0,
            category: {}
        },
        errorObj: {
            errors: []
        },
        entryId: entryIdParam,
        successMessage: null,
        commentCountMsg: null,
        deleteModalMsg: null,
        recentEntries: {
            PENDING: {},
            SCHEDULED: {},
            DRAFT: {},
            PUBLISHED: {}
        },
        metadata: {},
        urlRoot: contextPath + '/tb-ui/authoring/rest/weblogentries/'
    },
    methods: {
        getRecentEntries: function(entryType) {
            axios
            .get(this.urlRoot + weblogId + '/recententries/' + entryType)
            .then(response => {
                this.recentEntries[entryType] = response.data;
            })
        },
        loadMetadata: function() {
            axios
            .get(this.urlRoot + weblogId + '/entryeditmetadata')
            .then(response => {
                this.metadata = response.data;
                if (!this.entryId) {
                    // new entry init
                    this.entry.category.id = Object.keys(this.metadata.categories)[0];
                    this.entry.commentDays = "" + this.metadata.defaultCommentDays;
                    this.entry.editFormat = this.metadata.defaultEditFormat;
                }
            })
            .catch(error => commonErrorResponse(error))
        },
        getEntry: function() {
            axios
            .get(this.urlRoot + this.entryId)
            .then(response => {
                this.entry = response.data;
                var commentCount = this.entry.commentCountIncludingUnapproved;
                this.commentCountMsg = eval('`' + msg.commentCountTmpl + '`');
                this.entry.commentDays = "" + this.entry.commentDays;
            });
        },
        saveEntry: function(saveType) {
            this.messageClear();
            var urlStem = weblogId + '/entries';

            oldStatus = this.entry.status;
            this.entry.status = saveType;

            axios
            .post(this.urlRoot + urlStem, this.entry)
            .then(response => {
                 this.entryId = response.data.entryId;
                 this.successMessage = response.data.message;
                 this.errorObj = {};
                 this.loadRecentEntries();
                 this.getEntry();
                 window.scrollTo(0, 0);
            })
            .catch(error => {
                this.entry.status = oldStatus;
                if (error.response.status == 401) {
                    this.errorObj = {
                        errors: [ {message: eval('`' + msg.sessionTimeoutTmpl + '`')} ]
                    };
                    window.scrollTo(0, 0);
                } else {
                    this.commonErrorResponse(error);
                }
            })
        },
        previewEntry: function() {
            window.open(this.entry.previewUrl);
        },
        showDeleteModal: function(entry) {
            // title used in eval below
            var title = entry.title;
            this.deleteModalMsg = eval('`' + msg.confirmDeleteTmpl + '`')
            $('#deleteEntryModal').modal('show');
        },
        deleteWeblogEntry: function() {
            $('#deleteEntryModal').modal('hide');

            axios
            .delete(this.urlRoot + this.entryId)
            .then(response => {
                document.location.href=newEntryUrl;
            })
            .catch(error => this.commonErrorResponse(error));
        },
        loadRecentEntries: function() {
            this.getRecentEntries('DRAFT');
            this.getRecentEntries('PUBLISHED');
            this.getRecentEntries('SCHEDULED');
            this.getRecentEntries('PENDING');
        },
        updateTags: function(tagsString) {
            this.entry.tagsAsString = tagsString;
        },
        updatePublishDate: function(date) {
            this.entry.dateString = date;
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
               window.scrollTo(0, 0);
            }
        }
    },
    created: function() {
        // indicate requests via Ajax calls, so auth probs return 401s vs. login redirects
        axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
        this.loadMetadata();
        this.loadRecentEntries();
        if (this.entryId) {
            this.getEntry();
        } else {
            this.loadMetadata();
        }
    },
    mounted: function() {
        $( "#accordion" ).accordion({});
    }
});
