module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    sourceDir: 'src/main/frontend', 

    buildDir: 'src/main/resources/public/assets/js',

    clean: {
      options: {
        force: true
      },
      home: {
        src: [
          '<%= buildDir %>/*',
        ],
      }
    },
    apidoc: {
      'home-action': {
        src: "apidoc/src",
        dest: "apidoc/docs",
        template: "apidoc/template",
        options: {
          debug: false,
          includeFilters: [".*\\.js$"]
        }
      }
    },
    jsdoc: {
      home: {
        src: [
          '<%= sourceDir %>/actions/*.js',
          '<%= sourceDir %>/README.md'
        ],
        options: {
          destination: 'jsdoc/home'
        }
      }
    },
    eslint: {
      'home-build': {
        src: [
          '<%= sourceDir %>/**/*.js',
          '!<%= sourceDir %>/node_modules/**',
          '!<%= sourceDir %>/lib/**'
        ],
        options: {
          configFile: '.eslintrc.prod.json'
        }
      },
      'home-dev': {
        src: [
          '<%= sourceDir %>/**/*.js',
          '!<%= sourceDir %>/node_modules/**',
          '!<%= sourceDir %>/lib/**'
        ],
        options: {
          configFile: '.eslintrc.dev.json'
        }
      }
    },
    browserify: {
      options: {
        watch: true,
        keepAlive: false,
        browserifyOptions: {
          debug: false,
        },
        exclude: [
          'echarts'
        ],
        transform: [
          ["babelify"],
          ["envify"],
          ["browserify-shim"]
        ]
      },
      homeLive: {
        options: {
          watch: true,
          keepAlive: true,
          browserifyOptions: {
            debug: true,
          },
          plugin: [
            ["livereactload"]
          ],
          transform: [
            [{passthrough: 'warnings', configFile: '.eslintrc.home.dev.json'}, "eslintify"],
            ["babelify"],
            ["envify"],
            ["browserify-shim"]
          ],
          external: [
              // required from vendor/util.js
              // required from vendor/react.js
              'react',
              'react-dom',
              'react-router',
              'react-datetime',
              'react-intl',
              'react-intl/locale-data/de',
              'react-intl/locale-data/el',
              'react-intl/locale-data/en',
              'react-intl/locale-data/es',
              'react-grid-layout',
              'react-select',
              'react-bootstrap',
              'react-router-bootstrap',
              'react-router-redux',
              'react-bootstrap-daterangepicker',
              'react-scroll-up',
              'react-dropzone',
              'redux',
              'react-redux',
              'redux-thunk',
              'redux-logger',
              // globals
              'echarts',
            ]
        },
        files: {
          'target/classes/public/assets/js/home/bundle.js': [
            '<%= sourceDir %>/index.js'
          ]
        }
      },
      home: {
        files: {
          '<%= buildDir %>/home/bundle.js': [
            '<%= sourceDir %>/index.js'
          ]
        },
        options: {
          external: [
              // required from vendor/util.js
              // required from vendor/react.js
              'react',
              'react-dom',
              'react-router',
              'react-datetime',
              'react-intl',
              'react-intl/locale-data/de',
              'react-intl/locale-data/el',
              'react-intl/locale-data/en',
              'react-intl/locale-data/es',
              'react-grid-layout',
              'react-select',
              'react-bootstrap',
              'react-router-bootstrap',
              'react-router-redux',
              'react-bootstrap-daterangepicker',
              'react-scroll-up',
              'react-dropzone',
              'redux',
              'react-redux',
              'redux-thunk',
              'redux-logger',
              // globals
              'echarts',
            ],
        }
      },
      'vendor-util': {
        options: {
          alias: [
            'isomorphic-fetch:fetch',
            'jquery',
            'lodash:lodash',
            'es6-promise',
            'moment',
            'numeral',
            'clone',
            'keymirror',
            'sprintf',
            'history',
          ],
        },
        files: {
          '<%= buildDir %>/vendor/util.js': [],
        },
      },
      'vendor-react': {
        options: {
          require: [
            'react',
            'react-dom',
            'react-router',
            'react-datetime',
            'react-intl',
            'react-intl/locale-data/de',
            'react-intl/locale-data/el',
            'react-intl/locale-data/en',
            'react-intl/locale-data/es',
            'react-grid-layout',
            'react-select',
            'react-bootstrap',
            'react-router-bootstrap',
            'react-router-redux',
            'react-bootstrap-daterangepicker',
            'react-scroll-up',
            'react-dropzone',
            'redux',
            'react-redux',
            'redux-thunk',
            'redux-logger',
          ],
        },
        files: {
          '<%= buildDir %>/vendor/react.js': [],
        },
      },
      'vendor-jquery': {
        options: {
          require: [
            'jquery',
          ],
        },
        files: {
          '<%= buildDir %>/vendor/jquery.js': [],
        },
      },
    },
    uglify: {
      options: {
        banner: '/* <%= pkg.description %> version <%= pkg.version %> <%= grunt.template.today("yyyy-mm-dd") %> */\n',
        sourceMap: true
      },
      home: {
        files: {
          '<%= buildDir %>/home/bundle.min.js': [
            '<%= buildDir %>/home/bundle.js'
          ]
        }
      },
      'vendor-util': {
         files: {
          '<%= buildDir %>/vendor/util.min.js': [
            '<%= buildDir %>/vendor/util.js'
          ]
        }
      },
      'vendor-react': {
         files: {
          '<%= buildDir %>/vendor/react.min.js': [
            '<%= buildDir %>/vendor/react.js'
          ]
        }
      },
      'vendor-jquery': {
         files: {
          '<%= buildDir %>/vendor/jquery.min.js': [
            '<%= buildDir %>/vendor/jquery.js'
          ]
        }
      },
    },
    concat: {
      home: {
        src: [
          'node_modules/echarts/dist/echarts.min.js',
          '<%= buildDir %>/home/bundle.min.js'
        ],
        dest: '<%= buildDir %>/home/bundle.min.js',
      },
    },
    sync: {
      debug: {
        files: [{
          expand: true,
          cwd: '<%= buildDir %>/',
          src: ['**/*.js', '**/*.map'],
          dest: 'target/classes/public/assets/js/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: '<%= sourceDir %>/i18n/',
          src: ['*.json'],
          dest: 'target/classes/public/assets/js/home/i18n/',
          filter: 'isFile'
        }],
      },
      home: {
        files: [{
          expand: true,
          cwd: '<%= sourceDir %>/i18n/',
          src: ['*.json'],
          dest: '<%= buildDir %>/home/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-datetime/css/',
          src: ['*.css'],
          dest: 'src/main/resources/public/assets/lib/react-datetime/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'apidoc/docs/',
          src: ['**/*'],
          dest: 'src/main/resources/public/docs/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'jsdoc/home/',
          src: ['**/*'],
          dest: 'src/main/resources/public/docs/client/home/',
          filter: 'isFile'
        }]
      }
    },
    watch: {
      options: {
        interrupt: true
      },
      home: {
        files: [
          '<%= sourceDir %>/**/*.js'
        ],
        tasks: [
          'eslint:home-dev',
          'sync:home',
          'sync:debug',
        ],
      },
    }
  });

  // Events
  grunt.event.on('watch', function(action, filepath, target) {
    grunt.log.writeln(target + ': ' + filepath + ' has ' + action);
  });

  // Load the plugins
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-browserify');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-sync');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-apidoc');
  grunt.loadNpmTasks('grunt-jsdoc');
  grunt.loadNpmTasks('grunt-eslint');

  // Default task(s).
  grunt.registerTask('build', [
    'clean',
    'eslint:home-build',
    'browserify:home', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-jquery',
    'uglify', 'concat',
    'docs',
    'sync:home'
  ]);

  grunt.registerTask('develop', [
    'clean',
    'eslint:home-dev',
    'browserify:home', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-jquery',
    'uglify:vendor-jquery',
    'sync:home', 'sync:debug',
    'watch'
  ]);

  grunt.registerTask('develop-home-live', [
    'clean:home', 'clean:vendor',
    'sync:home', 'sync:debug',
    'browserify:homeLive'
  ]);

  grunt.registerTask('docs', ['apidoc:home-action', 'jsdoc:home']);

};
