let httpsConfig = true;
try {
  // if local-certs.js exists, use that for https config
  httpsConfig = require("./local-certs.js");
} catch (ex) {
  // if local-certs.js doesn't exist, don't do anything
  console.warn(
    "Warning: By not providing a specific certification, webpack will send a self-signed certification when using https. This may cause unexpected issues in certain browsers."
  );
  console.warn(ex);
}

module.exports = {
  publicPath: "/tb-ui2/",

  chainWebpack: config => {
    config.plugins.delete("prefetch");
  },

  devServer: {
    //    port: 8181,
    //    host: 'localhost.politicopro.com',
    https: httpsConfig,
    // https://stackoverflow.com/q/62944640/1207540
    headers: {
      "Access-Control-Allow-Origin" : "*",
      "Access-Control-Allow-Headers": "Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE",
    },
    // https://github.com/chimurai/http-proxy-middleware#tldr
    proxy: {
      "/tb-ui": {
        target: "https://localhost:8443/"
      }
    }
  },

  pluginOptions: {
    i18n: {
      locale: "en",
      fallbackLocale: "en",
      localeDir: "locales",
      enableInSFC: false
    }
  }
};
