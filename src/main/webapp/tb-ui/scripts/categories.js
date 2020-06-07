$(function() {
    $('#deleteCategoryModal').on('show.bs.modal', function(e) {
        // used by tmpl below
        var categoryName = $(e.relatedTarget).data('category-name');
        var modal = $(this)
        var tmpl = eval('`' + msg.confirmDeleteTmpl + '`')
        modal.find('#deleteCategoryModalTitle').html(tmpl);
    });

    $('#editCategoryModal').on('show.bs.modal', function(e) {
        $('#category-name').val('');

        var categoryId = $(e.relatedTarget).data('category-id');
        var action = $(e.relatedTarget).data('action');

        // .data('xxx') doesn't refresh w/changes by Vue
        // categoryName used by tmpl below for renames
        var categoryName = e.relatedTarget.dataset.categoryName;

        // populate edit modal with category-specific information
        var modal = $(this)
        var button = modal.find('button[id="saveButton"]');
        button.attr("data-category-id", categoryId);
        button.attr("data-action", action);
        var tmpl = eval('`' + (action == 'rename' ? msg.editTitleTmpl : msg.addTitle) + '`');
        modal.find('#editCategoryModalTitle').html(tmpl);
    });
});

var vm = new Vue({
    el: '#template',
    data: {
        items: [],
        itemToEdit: {},
        errorObj: {},
        showUpdateErrorMessage: false,
        selectedCategoryId: null,
        targetCategoryId: null,
    },
    computed: {
        orderedItems: function () {
            // using lodash
            return _.orderBy(this.items, 'position')
        },
        moveToCategories: function() {
            // adding extra var to trigger cache refresh
            var compareVal = this.selectedCategoryId;
            return this.items.filter(function(item) {
                return item.id != compareVal;
            })            
        }
    },
    methods: {
        updateItem: function(obj) {
            this.messageClear();
            // https://stackoverflow.com/a/18030442/1207540
            var categoryId = obj.target.getAttribute("data-category-id");
    
            this.messageClear();
            if (this.itemToEdit.name) {
                this.itemToEdit.name = this.itemToEdit.name.replace(/[,%"/]/g,'');
                if (this.itemToEdit.name) {
                    axios
                    .put(contextPath + (categoryId ? '/tb-ui/authoring/rest/category/' + categoryId
                        : '/tb-ui/authoring/rest/categories?weblogId=' + actionWeblogId),
                        this.itemToEdit)
                    .then(response => {
                         $('#editCategoryModal').modal('hide');
                         this.itemToEdit = {};
                         this.loadItems();
                    })
                    .catch(error => this.commonErrorResponse(error));
                }
            }
        },
        deleteItem: function() {
            this.messageClear();
            $('#deleteCategoryModal').modal('hide');
      
            axios
            .delete(contextPath + '/tb-ui/authoring/rest/category/' + this.selectedCategoryId + '?targetCategoryId=' + this.targetCategoryId)
            .then(response => {
                 this.targetCategoryId = null;
                 this.loadItems();
            })
            .catch(error => this.commonErrorResponse(error));
        },
        loadItems: function() {
            axios
            .get(contextPath + '/tb-ui/authoring/rest/categories?weblogId=' + actionWeblogId)
            .then(response => {
                this.items = response.data;
            })
            .catch(error => this.commonErrorResponse(error));
        },
        commonErrorResponse: function(error) {
            if (error.response.status == 408) {
               window.location.replace($('#refreshURL').attr('value'));
            } else if (error.response.status == 409) {
               this.showUpdateErrorMessage = true;
            } else {
               this.errorObj = response.data;
            }
        },
        messageClear: function() {
            this.showUpdateErrorMessage = false;
            this.errorObj = {};
        },
        inputClear: function() {
            this.messageClear();
            this.itemToEdit = {};
        }
    },
    mounted: function() {
        this.messageClear();
        this.loadItems();
    }
});
