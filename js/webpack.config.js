angulate2ScalaJsBundler = require("angulate2-scalajs-bundler");
module.exports = require('./scalajs.webpack.config');
module.exports.context = __dirname;
angulate2ScalaJsBundler.apply("quizleague-js", module);