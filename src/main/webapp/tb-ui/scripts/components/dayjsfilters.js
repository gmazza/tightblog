Vue.filter('standard_datetime', function (isoDate) {
    if (!isoDate) return '';
    return dayjs(isoDate).format('DD MMM YYYY h:mm:ss A');
});
