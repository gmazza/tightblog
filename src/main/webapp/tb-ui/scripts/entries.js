var vm = new Vue({
    el: '#template',
    data: {
        lookupFields: {},
        searchParams: {
            categoryName: "",
            sortBy: "PUBLICATION_TIME",
            status: ""
        },
        entriesData: {
            entries: []
        },
        errorObj: {},
        selectedEntryId: null,
        deleteModalMsg: null,
        pageNum: 0,
        urlRoot: contextPath + '/tb-ui/authoring/rest/weblogentries/'
    },
    methods: {
        entryStatusClass: function(status) {
            if (status == 'DRAFT') {
                return 'draftEntryBox';
            } else if (status == 'PENDING') {
                return 'pendingEntryBox';
            } else if (status == 'SCHEDULED') {
                return 'scheduledEntryBox'; 
            } else {
                return null;
            }
        },
        showDeleteModal: function(entry) {
            this.selectedEntryId = entry.id;
            // title used in eval below
            var title = entry.title;
            this.deleteModalMsg = eval('`' + msg.confirmDeleteTmpl + '`')
            $('#deleteEntryModal').modal('show');
        },
        deleteWeblogEntry: function() {
            $('#deleteEntryModal').modal('hide');
            axios
            .delete(this.urlRoot + this.selectedEntryId)
            .then(response => {
                 this.loadEntries();
            })
            .catch(error => this.commonErrorResponse(error));
        },
        loadLookupFields: function() {
            axios
            .get(this.urlRoot + weblogId + '/searchfields'  )
            .then(response => {
                this.lookupFields = response.data;
            })
            .catch(error => this.commonErrorResponse(error));
        },
        dateToSeconds: function(dateStr, addOne) {
            if (dateStr) {
                return Math.floor( Date.parse(dateStr) / 1000 ) + (addOne ? 1440 * 60 - 1 : 0);
            } else {
                return null;
            }
        },
        loadEntries: function() {
            var queryParams = {...this.searchParams};

            queryParams.startDate = this.dateToSeconds(this.searchParams.startDateString, false);
            queryParams.endDate = this.dateToSeconds(this.searchParams.endDateString, true);

            if (queryParams.categoryName == "") {
                queryParams.categoryName = null;
            };
            if (queryParams.status == "") {
                queryParams.status = null;
            }
    
            axios
            .post(this.urlRoot + weblogId + '/page/' + this.pageNum, queryParams)
            .then(response => {
                this.entriesData = response.data;
            })
            .catch(error => this.commonErrorResponse(error));
        },
        previousPage: function() {
            this.pageNum--;
            this.loadEntries();
        },
        nextPage: function() {
            this.pageNum++;
            this.loadEntries();
        },
        updateStartDate: function(date) {
            this.searchParams.startDateString = date;
        },
        updateEndDate: function(date) {
            this.searchParams.endDateString = date;
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
        this.loadEntries();
    }
});
