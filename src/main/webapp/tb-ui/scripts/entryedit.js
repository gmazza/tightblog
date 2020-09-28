function split( val ) {
    return val.split( / \s*/ );
}

function extractLast( term ) {
    return split( term ).pop();
}

$(function() {
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

Vue.component('date-picker', {
 //   https://vuejs.org/v2/guide/components.html#Using-v-model-on-Components
    template: `
        <input type="text" size="12" 
            v-bind:value="datevalue" 
            v-on:input="$emit('input', $event.target.value)"
        readonly/>
        `,
    props: ['datevalue'],
    mounted: function() {
        var self = this;
        $(this.$el).datepicker({
            showOn: "button",
            buttonImage: "../../../images/calendar.png",
            buttonImageOnly: true,
            changeMonth: true,
            changeYear: true,
            onSelect: function(date) {
                self.$emit('update-date', date);
            }
        });
    },
    beforeDestroy: function() {
        $(this.$el).datepicker('hide').datepicker('destroy');
    }
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
        formatDate: function(isoDate) {
            return dayjs(isoDate).format('DD MMM YYYY h:mm:ss A');
        },
        updatePublishDate: function(date) {
            this.entry.dateString = date;
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
        $( "#accordion" ).accordion({});
        this.loadMetadata();
        this.loadRecentEntries();
        if (this.entryId) {
            this.getEntry();
        } else {
            this.loadMetadata();
        }
    }
});
