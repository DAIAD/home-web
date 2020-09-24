module.exports = function (grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    sourceDir: 'apidoc/src',

    buildDir: 'apidoc/docs',

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
      'api': {
        src: "<%= sourceDir %>",
        dest: "<%= buildDir %>",
        template: "apidoc/template",
        options: {
          debug: false,
          includeFilters: [".*\\.js$"]
        }
      }
    },
    sync: {
      'api': {
        files: [{
          expand: true,
          cwd: 'apidoc/docs/',
          src: ['**/*'],
          dest: 'src/main/resources/public/docs/',
          filter: 'isFile'
        }]
      }
    },
    watch: {
      options: {
        interrupt: true
      },
      'api': {
        files: [
          '<%= sourceDir %>/**/*',
        ],
        tasks: [
          'apidoc:api',
        ]
      }
    }
  });

  // Events
  grunt.event.on('watch', function (action, filepath, target) {
    grunt.log.writeln(target + ': ' + filepath + ' has ' + action);
  });

  // Load the plugins
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-apidoc');
  grunt.loadNpmTasks('grunt-sync');

  // Default task
  grunt.registerTask('build', [
    'clean',
    'apidoc:api',
    'sync:api'
  ]);

  grunt.registerTask('develop', [
    'clean',
    'apidoc:api',
    'watch'
  ]);

};
