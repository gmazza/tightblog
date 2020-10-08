var vm = new Vue({
    el: '#template',
    data: {
        items: [],
        itemToEdit: {},
        errorObj: {},
        editModalTitle: '',
        deleteModalTitle: '',
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
            var categoryId = this.itemToEdit.id;

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
        showDeleteModal: function(item) {
            this.selectedCategoryId = item.id;
            // categoryName used in eval below
            var categoryName = item.name;
            this.deleteModalTitle = eval('`' + msg.confirmDeleteTmpl + '`')
            $('#deleteCategoryModal').modal('show');
        },
        showEditModal: function(item) {
            this.itemToEdit = {};
            this.itemToEdit.id = item.id;
            this.itemToEdit.name = item.name;
            // categoryName used in eval below
            var categoryName = item.name;
            this.editModalTitle = eval('`' + msg.editTitleTmpl + '`');
            $("#editCategoryModal").modal("show");
        },
        showAddModal: function() {
            this.itemToEdit = {};
            this.editModalTitle = msg.addTitle;
            $("#editCategoryModal").modal("show");
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
            if (error.response.status == 401) {
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
