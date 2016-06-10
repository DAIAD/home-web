module.exports = function(grunt) {

  //require('time-grunt')(grunt);

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
          'src/main/resources/public/assets/js/build/home/i18n/*.js'
        ],
      },
      'vendor': {
        src: [
          'src/main/resources/public/assets/js/build/vendor/*.js',
        ],
      },
    },
    apidoc: {
      utility: {
        src: "apidoc/src",
        dest: "apidoc/docs",
        template: "apidoc/template",
        options: {
          debug: false,
          includeFilters: [".*\\.js$"]
        }
      }
    },
    jshint: {
      options: {
        ignores: [
          // The following are ignored due to problems with ES6 syntax
          'src/main/resources/public/assets/js/src/utility/service/query.js', // ES6 spread operator
          'src/main/resources/public/assets/js/src/utility/components/reports-measurements/pane.js', // ES6 spread operator
          'src/main/resources/public/assets/js/src/utility/components/reports-measurements/unit-view.js', // ES6 spread operator
        ],
        //unused: true,
        eqnull: true,
        additionalSuffixes: ['.js'],
        reporter: require('jshint-stylish'),
        esnext: true,
        //force: true
      },
      utility: [
        'src/main/resources/public/assets/js/src/utility/**/*.js',
        '!src/main/resources/public/assets/js/src/utility/i18n/**/*.js'
      ],
      home: [
        'src/main/resources/public/assets/js/src/home/**/*.js',
        '!src/main/resources/public/assets/js/src/home/i18n/**/*.js'
      ]
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
            'react-addons-pure-render-mixin',
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
            'react-bootstrap-datetimepicker',
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
            'leaflet-choropleth',
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
            'src/main/resources/public/assets/js/src/home/main.js'
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
          'src/main/resources/public/assets/js/build/vendor/util.js': [],
        },
      },
     'vendor-react': {
        options: {
          require: [
            'react',
            'react-dom',
            'react-addons-pure-render-mixin',
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
            'react-bootstrap-datetimepicker',
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
            'leaflet-draw',
            'leaflet-choropleth',
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
    exorcise: {
      utility: {
        options: {
          strict: false
        },
        files: {
          'src/main/resources/public/assets/js/build/utility/bundle.js.map': [
            'src/main/resources/public/assets/js/build/utility.js'
          ]
        }
      },
      home: {
        options: {
          strict: false
        },
        files: {
          'src/main/resources/public/assets/js/build/home/bundle.js.map': [
            'src/main/resources/public/assets/js/build/home.js'
          ]
        }
      }
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
          src: ['*.js'],
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
          cwd: 'node_modules/bootstrap/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/bootstrap/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/bootstrap-select/dist/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/bootstrap-select/',
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
          cwd: 'node_modules/react-bootstrap-datetimepicker/css/',
          src: ['**/*'],
          dest: 'src/main/resources/public/assets/lib/react-bootstrap-datetimepicker/',
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
          dest: 'src/main/resources/public/docs/api/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/font-awesome/',
          src: ['css/*', 'fonts/*'],
          dest: 'src/main/resources/public/assets/lib/font-awesome/'
        }]
      },
      home: {
        files: [{
          expand: true,
          cwd: 'src/main/resources/public/assets/js/src/home/i18n/',
          src: ['*.js'],
          dest: 'src/main/resources/public/assets/js/build/home/i18n/',
          filter: 'isFile'
        }, {
          expand: true,
          cwd: 'node_modules/react-datetime/css/',
          src: ['*.css'],
          dest: 'src/main/resources/public/assets/lib/react-datetime/',
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
          'jshint:utility',
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
          'jshint:home',
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
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-browserify');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-sync');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-apidoc');
  grunt.loadNpmTasks('grunt-jsxhint');

  // Default task(s).
  grunt.registerTask('build', [
    'clean', 
    'jshint', 
    'browserify', 'uglify', 'concat', 
    'docs', 
    'sync:home', 'sync:utility', 'sync:home'
  ]);

  grunt.registerTask('develop', [
    'clean', 
    'jshint', 
    'browserify', 'uglify:vendor-leaflet', 'uglify:vendor-jquery', 
    'sync:home', 'sync:utility', 'sync:debug', 
    'watch'
  ]);

  grunt.registerTask('docs', ['apidoc:utility']);

};
