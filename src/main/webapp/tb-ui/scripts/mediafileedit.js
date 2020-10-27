var vm = new Vue({
    el: "#template",
    data: {
        mediaFileData: {
            directory: {
                "id" : directoryId
            }
        },
        myMediaFile: {},
        errorObj: {}
    },
    methods: {
        loadMediaFile: function () {
            axios
            .get(contextPath + '/tb-ui/authoring/rest/mediafile/' + mediaFileId)
            .then(response => {
                this.mediaFileData = response.data;
            })
        },
        handleFileUpload: function () {
            this.myMediaFile = this.$refs.myMediaFile.files[0];
        },
        saveMediaFile: function () {
            var uploadUrl = contextPath + '/tb-ui/authoring/rest/mediafiles';
            let fd = new FormData();
            fd.append('mediaFileData', new Blob([JSON.stringify(this.mediaFileData)], 
                {type: "application/json"}));
            if (this.myMediaFile) {
                fd.append('uploadFile', this.myMediaFile)
            }
            axios
            .post(uploadUrl, fd, 
                { headers: {'Content-Type': 'multipart/form-data'} })
            .then(response => {
                window.location.replace(mediaViewUrl + '&directoryId=' + directoryId);
            })
            .catch(error => {
                if (error.response.status == 401) {
                    window.location.replace($('#refreshURL').attr('value'));
                } else {
                    this.errorObj = error.response.data;
                }
            })
        },
        messageClear: function () {
            this.errorObj = null;
        }
    },
    mounted: function () {
        if (mediaFileId) {
            this.loadMediaFile();
        }
    }
});
