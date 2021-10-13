
const path = require('path');
const webpack = require('webpack');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const uiConfig = require('./ui.config');

const ROOT = path.resolve(__dirname, '../src/main/webapp');
const JS_SRC = path.resolve(ROOT, 'js');
const SCSS_SRC = path.resolve(ROOT, 'scss');
const DEST_NAME = 'dist';
const DEST_PATH = `../src/main/webapp/${DEST_NAME}`;
const DEST = path.resolve(__dirname, DEST_PATH);

module.exports = {
  mode: 'production',
  entry: {
    style: path.join(SCSS_SRC, '/index.scss'),
    app: path.join(JS_SRC, '/index.jsx'),
  },
  output: {
    path: DEST,
    filename: '[name].[chunkhash].bundle.js',
    crossOriginLoading: 'use-credentials',
  },
  resolve: {
    modules: [path.resolve(ROOT, 'js'), path.resolve(ROOT, 'css'), 'node_modules'],
    extensions: ['.js', '.jsx'],
  },
  devtool: 'source-map',
  plugins: [
    new CleanWebpackPlugin([
      DEST,
    ], {
      // the default is the webpack configuration file directory, please specify the project root directory
      root: path.resolve(__dirname, '/'),
      verbose: true,
      dry: false,
    }),
    new CopyWebpackPlugin([{
      from: 'src/main/webapp/data',
      to: 'data',
    }]),
    new webpack.DefinePlugin({
      processENV: JSON.stringify({
        NODE_ENV: 'production',
      }),
      uiConfig: JSON.stringify(uiConfig),
    }),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, 'index.template.html'),
      filename: `${ROOT}/index.html`,
    }),
  ],
  optimization: {
    minimizer: [
      new TerserJSPlugin({}),
      new OptimizeCSSAssetsPlugin({}),
    ],
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
              sourceMap: false,
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
