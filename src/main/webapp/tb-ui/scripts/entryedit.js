function split( val ) {
    return val.split( / \s*/ );
}

function extractLast( term ) {
    return split( term ).pop();
}

$(function() {
    $( "#accordion" ).accordion({});

    $( "#publishDateString" ).datepicker({
        showOn: "button",
        buttonImage: "../../../images/calendar.png",
        buttonImageOnly: true,
        changeMonth: true,
        changeYear: true
    });

    $('#deleteEntryModal').on('show.bs.modal', function(e) {
        //get data-id attribute of the clicked element
        var title = $(e.relatedTarget).attr('data-title');

        // populate delete modal with tag-specific information
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#confirmDeleteMsg').html(tmpl);
    });

    // tag autocomplete
    $( "#tags" )
    // don't navigate away from the field on tab when selecting an item
    .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB && $( this ).autocomplete( "instance" ).menu.active ) {
            event.preventDefault();
        }
    })
    .autocomplete({
        delay: 500,
        source: function(request, response) {
            $.getJSON(contextPath + "/tb-ui/authoring/rest/weblogentries/" + weblogId + "/tagdata",
            { prefix: extractLast( request.term ) },
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
            var terms = split( this.value );
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push( ui.item.value );
            // add placeholder to get the space at the end
            terms.push( "" );
            this.value = terms.join( " " );
            return false;
        }
    });
});

var vm = new Vue({
    el: '#template',
    data: {
        entry: {
            commentCountIncludingUnapproved: 0,
            category: {}
        },
        errorObj: {},
        entryId: entryIdParam,
        successMessage: null,
        commentCountMsg: null,
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
               if (error.response.status == 408) {
                 this.errorObj.errorMessage = eval('`' + msg.sessionTimeoutTmpl + '`');
                 window.scrollTo(0, 0);
               } else {
                 this.commonErrorResponse(error);
               }
            })
        },
        previewEntry: function() {
            window.open(this.entry.previewUrl);
        },
        deleteWeblogEntry: function() {
            $('#deleteWeblogEntryModal').modal('hide');

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
        formatDate: function(isoDate) {
            return dayjs(isoDate).format('DD MMM YYYY h:m:ss A');
        },
        messageClear: function() {
            this.successMessage = null;
            this.errorObj = {};
        },
        commonErrorResponse: function(error) {
            if (error.response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else {
               this.errorObj = error.response.data;
               window.scrollTo(0, 0);
            }
        }
    },
    mounted: function() {
        this.loadMetadata();
        this.loadRecentEntries();
        if (this.entryId) {
            this.getEntry();
        } else {
            this.loadMetadata();
        }
    }
});
