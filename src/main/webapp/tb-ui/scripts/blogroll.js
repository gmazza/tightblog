$(function() {
    $('#editLinkModal').on('show.bs.modal', function(e) {
        var action = $(e.relatedTarget).data('action');

        var modal = $(this)
        var button = modal.find('button[id="saveButton"]');
        button.attr("data-action", action);
        var title = (action == 'edit') ? msg.editTitle : msg.addTitle;
        modal.find('#editLinkModalTitle').html(title);
    });
});

var vm = new Vue({
    el: '#template',
    data: {
        items: {},
        itemToEdit: {},
        successMessage: null,
        checkAll: false,
        errorObj: {}
    },
    computed: {
        orderedItems: function () {
          return _.orderBy(this.items, 'position')
        }
    },
    methods: {
        itemsSelected: function() {
            return $('input[name="idSelections"]:checked').size() > 0;
        },
        toggleCheckboxes: function(checked) {
            $('input[name="idSelections"]').each(function(){
                $(this).prop('checked', checked);
            });
        },
        loadItems: function() {
            axios
            .get(contextPath + '/tb-ui/authoring/rest/weblog/' + actionWeblogId + '/bookmarks')
            .then(response => {
                  this.items = response.data;
            })
            .catch(error => {
               self.commonErrorResponse(error, null)
            });
        },
        deleteLinks: function() {
            this.messageClear();
            $('#deleteLinksModal').modal('hide');
    
            var selectedLinkIds = [];
            $('input[name="idSelections"]:checked').each(function(){
                selectedLinkIds.push($(this).val());
            });
    
            axios
            .post(contextPath + '/tb-ui/authoring/rest/bookmarks/delete', selectedLinkIds)
            .then(response => {
                    this.successMessage = selectedLinkIds.length + ' link(s) deleted';
                    this.loadItems();
            });
        },
        editItem: function(item) {
            this.itemToEdit = JSON.parse(JSON.stringify(item));
        },
        addItem: function() {
            this.itemToEdit = {};
        },
        updateItem: function() {
            this.messageClear();
            if (this.itemToEdit.name && this.itemToEdit.url) {
                axios
                .put(contextPath + (this.itemToEdit.id ? '/tb-ui/authoring/rest/bookmark/' + this.itemToEdit.id
                    : '/tb-ui/authoring/rest/bookmarks?weblogId=' + actionWeblogId),
                    this.itemToEdit)
                .then(response => {
                     $("#editLinkModal").modal("hide");
                     this.itemToEdit = {};
                     this.loadItems();
                 })
                .catch(error => this.commonErrorResponse(error))
            }
        },
        messageClear: function() {
            this.errorObj = {};
        },
        inputClear: function() {
            this.messageClear();
            this.itemToEdit = {};
        },
        commonErrorResponse: function(error) {
            if (error.response.status == 408) {
                window.location.replace($('#refreshURL').attr('value'));
            } else {
                this.errorObj = error.response.data;
            }
        }
    },
    mounted: function() {
        this.loadItems();
    }
})
