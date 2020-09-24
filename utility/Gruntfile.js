module.exports = function (grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    sourceDir: 'src/main/frontend', 

    buildDir: 'src/main/resources/public/assets/js',

    clean: {
      options: {
        force: true
      },
      utility: {
        src: [
          '<%= buildDir %>/*',
        ]
      },
    },
    apidoc: {
      'utility-action': {
        src: "apidoc/src",
        dest: "apidoc/docs",
        template: "apidoc/template",
        options: {
          debug: false,
          includeFilters: [".*\\.js$"]
        }
      }
    },
    eslint: {
      'utility-build': {
        src: [
          '<%= sourceDir %>/**/*.js',
          '!<%= sourceDir %>/i18n/**/*.js'
        ],
        options: {
          configFile: '.eslintrc.prod.json'
        },
      },
      'utility-dev': {
        src: [
          '<%= sourceDir %>/**/*.js',
          '!<%= sourceDir %>/i18n/**/*.js'
        ],
        options: {
          configFile: '.eslintrc.dev.json'
        },
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
      utilityLive: {
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
            [{ passthrough: 'warnings', configFile: '.eslintrc.dev.json' }, "eslintify"],
            ["babelify"],
            ["envify"],
            ["browserify-shim"]
          ],
          // Exclude from being bundled into main app
          // The following will be resolved globally (shim) or via earlier vendor includes
          external: [
            // required from vendor/util.js
            'fetch',
            'lodash',
            'es6-promise',
            'moment',
            'numeral',
            'clone',
            'keymirror',
            'sprintf',
            'history',
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
            // required from vendor/jquery.js
            'jquery',
            // required from vendor/leaflet.js
            'leaflet',
            'leaflet.heat',
            'leaflet-draw',
            'chroma-js',
            // globals
            'echarts',
          ],
        },
        files: {
          'target/classes/public/assets/js/utility/bundle.js': [
            '<%= sourceDir %>/index.js'
          ]
        }
      },
      utility: {
        options: {
          // Exclude from being bundled into main app
          // The following will be resolved globally (shim) or via earlier vendor includes
          external: [
            // required from vendor/util.js
            'fetch',
            'lodash',
            'es6-promise',
            'moment',
            'numeral',
            'clone',
            'keymirror',
            'sprintf',
            'history',
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
            // required from vendor/jquery.js
            'jquery',
            // required from vendor/leaflet.js
            'leaflet',
            'leaflet.heat',
            'leaflet-draw',
            'chroma-js',
            // globals
            'echarts',
          ],
        },
        files: {
          '<%= buildDir %>/utility/bundle.js': [
            '<%= sourceDir %>/index.js'
          ]
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
      'vendor-leaflet': {
        options: {
          require: [
            'leaflet',
            'leaflet.heat',
            'leaflet-draw',
            'chroma-js',
          ],
        },
        files: {
          '<%= buildDir %>/vendor/leaflet.js': [],
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
      utility: {
        files: {
          '<%= buildDir %>/utility/bundle.min.js': [
            '<%= buildDir %>/utility/bundle.js'
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
      'vendor-leaflet': {
        files: {
          '<%= buildDir %>/vendor/leaflet.min.js': [
            '<%= buildDir %>/vendor/leaflet.js'
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
      utility: {
        src: [
          'node_modules/echarts/dist/echarts.min.js',
          '<%= buildDir %>/utility/bundle.min.js'
        ],
        dest: '<%= buildDir %>/utility/bundle.min.js',
      }
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
          src: ['*.js'],
          dest: 'target/classes/public/assets/js/utility/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'src/main/resources/public/assets/css/utility/',
          src: ['*.css'],
          dest: 'target/classes/public/assets/css/utility/',
          filter: 'isFile'
        }],
      },
      utility: {
        files: [{
          expand: true,
          cwd: 'node_modules/jquery/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/jquery/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/leaflet/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/leaflet/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/leaflet-draw/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/leaflet-draw/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/echarts/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/echarts/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-select/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/react-select/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-bootstrap-daterangepicker/css/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/react-bootstrap-daterangepicker/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-grid-layout/css/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/react-grid-layout/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: '<%= sourceDir %>/i18n/',
          src: ['*.js'],
          dest: '<%= buildDir %>/utility/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'apidoc/docs/',
          src: ['**/*'],
          dest: 'src/main/resources/public/docs/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/font-awesome/',
          src: ['css/*', 'fonts/*'],
          dest: 'src/main/resources/public/assets/lib/font-awesome/'
        },
        {
          expand: true,
          cwd: 'node_modules/rc-switch/assets/',
          src: ['*'],
          dest: 'src/main/resources/public/assets/lib/rc-switch/'
        }]
      }
    },
    watch: {
      options: {
        interrupt: true
      },
      'utility-scripts': {
        files: [
          '<%= sourceDir %>/**/*.js',
        ],
        tasks: [
          'eslint:utility-dev',
          'sync:utility',
          'sync:debug',
        ],
      },
      'utility-stylesheets': {
        files: [
          'src/main/resources/public/assets/css/*.css',
          'src/main/resources/public/assets/css/utility/*.css',
        ],
        tasks: [
          'sync:utility',
          'sync:debug',
        ],
      }
    }
  });

  // Events
  grunt.event.on('watch', function (action, filepath, target) {
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
  grunt.loadNpmTasks('grunt-eslint');

  // Default task(s).
  grunt.registerTask('build', [
    'clean',
    'eslint:utility-build',
    'browserify:utility', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'uglify', 'concat',
    'docs',
    'sync:utility'
  ]);

  grunt.registerTask('develop', [
    'clean',
    'eslint:utility-dev',
    'browserify:utility', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'uglify:vendor-leaflet', 'uglify:vendor-jquery',
    'sync:utility', 'sync:debug',
    'watch'
  ]);

  grunt.registerTask('develop-utility-live', [
    'clean',
    'sync:utility', 'sync:debug',
    'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'browserify:utilityLive'
  ]);

  grunt.registerTask('docs', ['apidoc:utility-action']);

};
