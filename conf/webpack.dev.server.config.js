
const path = require('path');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const WriteFilePlugin = require('write-file-webpack-plugin');
const uiConfig = require('./ui.config');

const ROOT = path.resolve(__dirname, '../src/main/webapp');
const JS_SRC = path.resolve(ROOT, 'js');
const SCSS_SRC = path.resolve(ROOT, 'scss');
const DEST = path.resolve(__dirname, '../src/main/webapp/dist');

module.exports = {
  mode: 'development',
  entry: {
    style: path.join(SCSS_SRC, '/index.scss'),
    app: path.join(JS_SRC, '/index.jsx'),
  },
  devServer: {
    inline: true,
    port: 8088,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
    publicPath: 'https://localhost:8088/src/main/webapp/dist/',
    https: {
      /* cert: fs.readFileSync("path-to-cert-file.pem"),
            key: fs.readFileSync("path-to-key-file.pem"),
            cacert: fs.readFileSync("path-to-cacert-file.pem") */
    },
  },
  output: {
    path: DEST,
    filename: '[name].bundle.js',
    publicPath: 'https://localhost:8088/src/main/webapp/dist/',
    crossOriginLoading: 'use-credentials',
  },
  resolve: {
    modules: [path.resolve(ROOT, 'js'), path.resolve(ROOT, 'css'), 'node_modules'],
    extensions: ['.js', '.jsx'],
  },
  devtool: 'source-map',
  plugins: [
    new WriteFilePlugin({
      test: /\.html$/,
      useHashIndex: true,
    }),
    new CopyWebpackPlugin([
      { from: path.resolve(__dirname, 'index.template.dev.html'), to: `${ROOT}/index.html` },
    ]),
    new webpack.DefinePlugin({
      processENV: JSON.stringify({
        NODE_ENV: 'production',
      }),
      uiConfig: JSON.stringify(uiConfig),
    }),
  ],
  optimization: {
    splitChunks: {
      cacheGroups: {
        commons: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor',
          chunks: 'all',
        },
      },
    },
  },
  module: {
    rules: [
      {
        test: /\.js$|\.jsx$/,
        loader: 'babel-loader',
        include: JS_SRC,
        exclude: path.join(__dirname, 'node_modules'),
      },
      {
        test: /\.scss$/,
        use: [
          'style-loader',
          'css-loader',
          {
            loader: 'sass-loader',
            options: {
              sourceMap: true,
              includePaths: [
                path.resolve('./node_modules'),
              ],
              outputStyle: 'expanded',
            },
          },
        ],
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.less$/,
        use: ['style-loader', 'css-loader', 'less-loader'],
      },
      {
        test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        query: {
          mimetype: 'application/font-woff',
        },
      },
      {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        query: {
          mimetype: 'application/octet-stream',
        },
      },
      {
        test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file-loader',
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url-loader',
        query: {
          mimetype: 'image/svg+xml',
        },
      },
    ],
  },
};
