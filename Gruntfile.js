module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
      options: {
        force: true
      },
      utility: {
        src: [
          'src/main/resources/public/assets/js/build/utility/*.js',
          'src/main/resources/public/assets/js/build/utility/*.js.map',
          'src/main/resources/public/assets/js/build/utility/*.min.js',
          'src/main/resources/public/assets/js/build/utility/*.min.js.map',
          'src/main/resources/public/assets/js/build/utility/i18n/*.js'
        ]
      },
      home: {
        src: [
          'src/main/resources/public/assets/js/build/home/*.js',
          'src/main/resources/public/assets/js/build/home/*.js.map',
          'src/main/resources/public/assets/js/build/home/*.min.js',
          'src/main/resources/public/assets/js/build/home/*.min.js.map',
          'src/main/resources/public/assets/js/build/home/i18n/*.json'
        ],
      },
      'vendor': {
        src: [
          'src/main/resources/public/assets/js/build/vendor/*.js',
        ],
      },
    },
    apidoc: {
      'utility-api': {
        src: "apidoc/src/api",
        dest: "apidoc/docs/api",
        template: "apidoc/template",
        options: {
          debug: false,
          includeFilters: [".*\\.js$"]
        }
      },
      'utility-action': {
        src: "apidoc/src/action",
        dest: "apidoc/docs/action",
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
          'src/main/resources/public/assets/js/src/home/actions/*.js',
          'src/main/resources/public/assets/js/src/home/README.md'
        ],
        options: {
          destination: 'jsdoc/home'
        }
      }
    },
    eslint: {
      'utility-build': {
        src: [
          'src/main/resources/public/assets/js/src/utility/**/*.js',
          '!src/main/resources/public/assets/js/src/utility/i18n/**/*.js'
        ],
        options: {
          configFile: '.eslintrc.build.json'
        },
      },
      'home-build': {
        src: [
          'src/main/resources/public/assets/js/src/home/**/*.js',
          '!src/main/resources/public/assets/js/src/home/node_modules/**',
          '!src/main/resources/public/assets/js/src/home/lib/**'
        ],
        options: {
          configFile: '.eslintrc.build.json'
        }
      },
      'utility-dev': {
        src: [
          'src/main/resources/public/assets/js/src/utility/**/*.js',
          '!src/main/resources/public/assets/js/src/utility/i18n/**/*.js'
        ],
        options: {
          configFile: '.eslintrc.dev.json'
        },
      },
      'home-dev': {
        src: [
          'src/main/resources/public/assets/js/src/home/**/*.js',
          '!src/main/resources/public/assets/js/src/home/node_modules/**',
          '!src/main/resources/public/assets/js/src/home/lib/**'
        ],
        options: {
          configFile: '.eslintrc.home.dev.json'
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
          'target/classes/public/assets/js/build/home/bundle.js': [
            'src/main/resources/public/assets/js/src/home/index.js'
          ]
        }
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
            [{passthrough: 'warnings', configFile: '.eslintrc.dev.json'}, "eslintify"],
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
            // globals
            'echarts',
          ],
      },
        files: {
            'target/classes/public/assets/js/build/utility/bundle.js': [
            'src/main/resources/public/assets/js/src/utility/index.js'
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
            // globals
            'echarts',
          ],
        },
        files: {
          'src/main/resources/public/assets/js/build/utility/bundle.js': [
            'src/main/resources/public/assets/js/src/utility/index.js'
          ]
        }
      },
      home: {
        files: {
          'src/main/resources/public/assets/js/build/home/bundle.js': [
            'src/main/resources/public/assets/js/src/home/index.js'
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
          'src/main/resources/public/assets/js/build/vendor/util.js': [],
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
          'src/main/resources/public/assets/js/build/vendor/react.js': [],
        },
      },
      'vendor-leaflet': {
        options: {
          require: [
            'leaflet',
            'leaflet.heat',
            'leaflet-draw'
          ],
        },
        files: {
          'src/main/resources/public/assets/js/build/vendor/leaflet.js': [],
        },
      },
      'vendor-jquery': {
        options: {
          require: [
            'jquery',
          ],
        },
        files: {
          'src/main/resources/public/assets/js/build/vendor/jquery.js': [],
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
          'src/main/resources/public/assets/js/build/utility/bundle.min.js': [
            'src/main/resources/public/assets/js/build/utility/bundle.js'
          ]
        }
      },
      home: {
        files: {
          'src/main/resources/public/assets/js/build/home/bundle.min.js': [
            'src/main/resources/public/assets/js/build/home/bundle.js'
          ]
        }
      },
      'vendor-util': {
         files: {
          'src/main/resources/public/assets/js/build/vendor/util.min.js': [
            'src/main/resources/public/assets/js/build/vendor/util.js'
          ]
        }
      },
      'vendor-react': {
         files: {
          'src/main/resources/public/assets/js/build/vendor/react.min.js': [
            'src/main/resources/public/assets/js/build/vendor/react.js'
          ]
        }
      },
      'vendor-leaflet': {
         files: {
          'src/main/resources/public/assets/js/build/vendor/leaflet.min.js': [
            'src/main/resources/public/assets/js/build/vendor/leaflet.js'
          ]
        }
      },
      'vendor-jquery': {
         files: {
          'src/main/resources/public/assets/js/build/vendor/jquery.min.js': [
            'src/main/resources/public/assets/js/build/vendor/jquery.js'
          ]
        }
      },
    },
    concat: {
      utility: {
        src: [
          'node_modules/echarts/dist/echarts.min.js',
          'src/main/resources/public/assets/js/build/utility/bundle.min.js'
        ],
        dest: 'src/main/resources/public/assets/js/build/utility/bundle.min.js',
      },
      home: {
        src: [
          'node_modules/echarts/dist/echarts.min.js',
          'src/main/resources/public/assets/js/build/home/bundle.min.js'
        ],
        dest: 'src/main/resources/public/assets/js/build/home/bundle.min.js',
      },
    },
    sync: {
      debug: {
        files: [{
          expand: true,
          cwd: 'src/main/resources/public/assets/js/build/',
          src: ['**/*.js', '**/*.map'],
          dest: 'target/classes/public/assets/js/build/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'src/main/resources/public/assets/js/src/utility/i18n/',
          src: ['*.js'],
          dest: 'target/classes/public/assets/js/build/utility/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'src/main/resources/public/assets/js/src/home/i18n/',
          src: ['*.json'],
          dest: 'target/classes/public/assets/js/build/home/i18n/',
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
          cwd: 'src/main/resources/public/assets/js/src/utility/i18n/',
          src: ['*.js'],
          dest: 'src/main/resources/public/assets/js/build/utility/i18n/',
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
      },
      home: {
        files: [{
          expand: true,
          cwd: 'src/main/resources/public/assets/js/src/home/i18n/',
          src: ['*.json'],
          dest: 'src/main/resources/public/assets/js/build/home/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-datetime/css/',
          src: ['*.css'],
          dest: 'src/main/resources/public/assets/lib/react-datetime/',
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
      'utility-scripts': {
        files: [
          'src/main/resources/public/assets/js/src/utility/**/*.js',
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
      },
      home: {
        files: [
          'src/main/resources/public/assets/js/src/home/**/*.js'
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
    'eslint:utility-build', 'eslint:home-build',
    'browserify:home', 'browserify:utility', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'uglify', 'concat',
    'docs',
    'sync:home', 'sync:utility', 'sync:home'
  ]);

  grunt.registerTask('develop', [
    'clean',
    'eslint:utility-dev', 'eslint:home-dev',
    'browserify:home', 'browserify:utility', 'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'uglify:vendor-leaflet', 'uglify:vendor-jquery',
    'sync:home', 'sync:utility', 'sync:debug',
    'watch'
  ]);

  grunt.registerTask('develop-home-live', [
    'clean:home', 'clean:vendor',
    'sync:home', 'sync:debug',
    'browserify:homeLive'
  ]);

  grunt.registerTask('develop-utility-live', [
    'clean:utility', 'clean:vendor',
    'sync:utility', 'sync:debug',
    'browserify:vendor-util', 'browserify:vendor-react', 'browserify:vendor-leaflet', 'browserify:vendor-jquery',
    'browserify:utilityLive'
  ]);

  grunt.registerTask('docs', ['apidoc:utility-api', 'apidoc:utility-action', 'jsdoc:home']);

};
