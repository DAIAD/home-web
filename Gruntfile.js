

module.exports = function (grunt) {
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
					'src/main/resources/public/assets/js/build/utility/*.min.js.map'
				]
            },
					home: {
                src: [
					'src/main/resources/public/assets/js/build/home/*.js',
					'src/main/resources/public/assets/js/build/home/*.js.map',
					'src/main/resources/public/assets/js/build/home/*.min.js',
					'src/main/resources/public/assets/js/build/home/*.min.js.map'
								],
            }
        },
        apidoc: {
			utility: {
				src: "apidoc/src",
				dest: "apidoc/docs",
				template: "apidoc/template",
				options: {
					debug: false,
					includeFilters: [ ".*\\.js$" ]
				}
			}
		},
        jshint: {
            options: {
              ignores: [],
              //unused: true,
              eqnull: true,
              additionalSuffixes: ['.js'],
                      reporter: require('jshint-stylish'),
                      esnext: true

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
					debug: false
				},
				exclude: [
				  'echarts'
				],
				transform: [
					[
						"babelify"
					], [
						"envify"
					], [
						"browserify-shim"
					]
				]
			},
			utility: {
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
			}
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
						}

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
				}]
			},
			utility: {
				files: [{
					expand: true,
					cwd: 'node_modules/jquery/dist/',
					src: ['**/*'],
					dest: 'src/main/resources/public/assets/lib/jquery/',
					filter: 'isFile'
				},{
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
				}]
			},
			home: {
						files: [{
							expand: true,
							cwd: 'src/main/resources/public/assets/js/src/home/i18n/',
							src: ['*.js'],
							dest: 'src/main/resources/public/assets/js/build/home/i18n/',
							filter: 'isFile'
						}]
					}
		},
		watch: {
			options: {
				interrupt: true
			},
			utility: {
				files: [
					'src/main/resources/public/assets/js/src/**/*.js'
				],
				tasks: ['jshint', 'sync']
			}
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
    grunt.registerTask('build', ['clean', 'jshint', 'browserify', 'uglify', 'concat', 'docs', 'sync:utility']);

	grunt.registerTask('develop', ['clean', 'jshint', 'browserify', 'sync:home', 'sync:utility', 'sync:debug', 'watch']);

	grunt.registerTask('docs', ['apidoc:utility']);

};
