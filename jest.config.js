module.exports = {
  collectCoverageFrom: [
    'src/main/webapp/js/**/*.{js,jsx}',
    '!src/main/webapp/js/**/*.test.{js,jsx}',
    '!src/main/webapp/js/**/*.foo.{js,jsx}',
    '!src/main/webapp/js/**/index.js',
    '!src/main/webapp/js/index.jsx',
    '!src/main/webapp/js/components/reports/**',
    '!src/main/webapp/js/components/reportDemo/**'
  ],
  coverageThreshold: {
    global: {
      statements: 25,
      branches: 10,
      functions: 15,
      lines: 25
    }
  },
  coverageReporters: ['json', 'lcov', 'text', 'html'],
  moduleDirectories: ['node_modules', 'src/main/webapp/js'],
  moduleNameMapper: {
    '.*\\.(css|less|styl|scss|sass)$':
      '<rootDir>/conf/test/jest-mocks/cssMocks.js',
    '.*\\.(jpg|jpeg|png|gif|eot|otf|webp|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
      '<rootDir>/conf/test/jest-mocks/imageMock.js',
    '.*\\.(svg)$':
      '<rootDir>/conf/test/jest-mocks/svgMock.js'
  },
  setupFilesAfterEnv: ['<rootDir>/conf/test/setup.js'],
  testRegex: 'src/main/webapp/js/.*\\.test\\.js$',
  automock: false
};
