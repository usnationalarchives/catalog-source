// Generated on 2014-03-18 using generator-angular 0.7.1
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  grunt.loadNpmTasks('grunt-svninfo');
  grunt.loadNpmTasks('grunt-text-replace');

  var modRewrite = require('connect-modrewrite');
  // Define the configuration for all the tasks
  grunt.initConfig({
      replace: {
        revision: {
          overwrite: true,
          src: ['<%= yeoman.dist %>/scripts/*config.js'],
          replacements: [
            {
              from: 'revNumberPH',                   // string replacement
              to: '<%= svninfo.rev %>'
            }
          ]
        },
        run: {
          overwrite: true,
          src: ['<%= yeoman.app %>/scripts/*config.js'],
          replacements: [
            {
              from: /('REVISION': ').*'/,                   // string replacement
              to: '$1<%= svninfo.rev %>\''
            }
          ]
        }
      },
      svninfo: {
        options: {
          cwd: '<%= yeoman.app %>/../'
        }
      },
      compress: {
        dist: {
          options: {
            archive: './dist.zip',
            mode: 'zip'
          },
          files: [
            {src: './dist/**'}
          ]
        }
      },
      // Project settings
      yeoman: {
        // configurable paths
        app: require('./bower.json').appPath || 'app',
        dist: 'dist'
      },

      // Watches files for changes and runs tasks based on the changed files
      watch: {
        js: {
          files: ['<%= yeoman.app %>/scripts/**/*.js'],
          tasks: ['newer:jshint:all'],
          options: {
            livereload: true
          }
        },
        jsTest: {
          files: ['test/spec/{,*/}*.js'],
          tasks: ['newer:jshint:test', 'karma']
        },
        sass: {
          files: ['<%= yeoman.app %>/styles/**/*.scss'], //['<%= yeoman.app %>/styles/{,*//*}*.{scss,sass}'],
          tasks: ['sass', 'autoprefixer']
        },
        gruntfile: {
          files: ['Gruntfile.js']
        },
        livereload: {
          options: {
            livereload: '<%= connect.options.livereload %>'
          },
          files: [
            '<%= yeoman.app %>/{,*/}*.html',
            '<%= yeoman.app %>/views/**/*.html',
            '.tmp/styles/{,*/}*.css',
            '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
          ]
        }
      },

      // The actual grunt server settings
      connect: {
        options: {
          port: 9000,
          // Change this to '0.0.0.0' to access the server from outside.
          hostname: '0.0.0.0',
          livereload: 35729
        },
        livereload: {
          options: {
            open: true,
            base: [
              '.tmp',
              '<%= yeoman.app %>'
            ],
            middleware: function (connect, options) {
              var middlewares = [];

              middlewares.push(modRewrite([
                '^/login /index.html [L]',
                '^/mobileFacets /index.html [L]',
                '^/resetpassword /index.html [L]',
                '^/thanks /index.html [L]',
                '^/registration /index.html [L]',
                '^/activation /index.html [L]',
                '^/results /index.html [L]',
                '^/advancedsearch /index.html [L]',
                '^/accounts/.* /index.html [L]',
                '^/search /index.html [L]',
                '^/moderatorWorkbench /index.html [L]',
                '^/administratorWorkbench /index.html [L]',
                '^/homePageManager /index.html [L]',
                '^/id/.* /index.html [L]',
                '^/statistics /index.html  [L]',
                '^/contentNotFound /index.html [L]',
                '^/print /index.html [L]',
                '^/InteractiveDocumentation /index.html [L]',
                '^/emailverify /index.html [L]',
                '^/arcsearch/.* /index.html [L]'
              ])); //Matches everything that does not contain a '.' (period)
              options.base.forEach(function (base) {
                middlewares.push(connect.static(base));
              });
              return middlewares;
            }
          }
        },
        test: {
          options: {
            port: 9001,
            base: [
              '.tmp',
              'test',
              '<%= yeoman.app %>'
            ]
          }
        },
        dist: {
          options: {
            base: '<%= yeoman.dist %>'
          }
        }
      },

      // Make sure code styles are up to par and there are no obvious mistakes
      jshint: {
        options: {
          jshintrc: '.jshintrc',
          reporter: require('jshint-stylish')
        },
        all: [
          'Gruntfile.js',
          '<%= yeoman.app %>/scripts/*.js',
          '<%= yeoman.app %>/scripts/controllers/*.js',
          '<%= yeoman.app %>/scripts/directives/*.js',
          '<%= yeoman.app %>/scripts/Services/*.js'
        ],
        test: {
          options: {
            jshintrc: 'test/.jshintrc'
          },
          src: ['test/spec/{,*/}*.js']
        }
      },

      // Empties folders to start fresh
      clean: {
        dist: {
          files: [
            {
              dot: true,
              src: [
                '.tmp',
                '<%= yeoman.dist %>/*',
                '!<%= yeoman.dist %>/.git*'
              ]
            }
          ]
        },
        server: '.tmp'
      },

      // Add vendor prefixed styles
      autoprefixer: {
        options: {
          browsers: ['last 4 versions']
        },
        dist: {
          files: [
            {
              expand: true,
              cwd: '.tmp/styles/',
              src: '{,*/}*.css',
              dest: '.tmp/styles/'
            }
          ]
        }
      },

      // Automatically inject Bower components into the app
      'bower-install': {
        app: {
          html: '<%= yeoman.app %>/index.html',
          ignorePath: '<%= yeoman.app %>/'
        }
      },


      // Compiles Sass to CSS and generates necessary files if requested
      sass: {
        dist: {
          files: [
            {
              expand: true,
              cwd: '<%= yeoman.app %>/styles',
              src: ['{main,ie8}.scss'],
              dest: '<%= yeoman.app %>/styles',
              ext: '.css'
            }
          ]
        }
      },

      // Renames files for browser caching purposes
      rev: {
        dist: {
          files: {
            src: [
              '<%= yeoman.dist %>/scripts/{,*/}*.js',
              '<%= yeoman.dist %>/styles/{,*/}*.css',
              /*'<%= yeoman.dist %>/images/{,*//*}*.{png,jpg,jpeg,webp,svg}', //gif,*/
              '<%= yeoman.dist %>/styles/fonts/*'
            ]
          }
        }
      },

      // Reads HTML for usemin blocks to enable smart builds that automatically
      // concat, minify and revision files. Creates configurations in memory so
      // additional tasks can operate on them
      useminPrepare: {
        html: '<%= yeoman.app %>/index.html',
        options: {
          dest: '<%= yeoman.dist %>'
        }
      },

      // Performs rewrites based on rev and the useminPrepare configuration
      usemin: {
        html: ['<%= yeoman.dist %>/{,*/}*.html'],
        css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
        options: {
          dirs: ['<%= yeoman.dist %>', '<%= yeoman.dist %>/images']
        }
      },

      // The following *-min tasks produce minified files in the dist folder
      imagemin: {
        dist: {
          files: [
            {
              expand: true,
              cwd: '<%= yeoman.app %>/images',
              src: '{,*/}*.{png,jpg,jpeg,gif}',
              dest: '<%= yeoman.dist %>/images'
            }
          ]
        }
      },
      svgmin: {
        dist: {
          files: [
            {
              expand: true,
              cwd: '<%= yeoman.app %>/images',
              src: '{,*/}*.svg',
              dest: '<%= yeoman.dist %>/images'
            }
          ]
        }
      },
      htmlmin: {
        dist: {
          options: {
            collapseWhitespace: true,
            collapseBooleanAttributes: true,
            removeCommentsFromCDATA: true,
            removeOptionalTags: true
          },
          files: [
            {
              expand: true,
              cwd: '<%= yeoman.dist %>',
              src: ['*.html', 'views/**/*.html'],
              dest: '<%= yeoman.dist %>'
            }
          ]
        }
      },

      // Allow the use of non-minsafe AngularJS files. Automatically makes it
      // minsafe compatible so Uglify does not destroy the ng references
      ngmin: {
        dist: {
          files: [
            {
              expand: true,
              cwd: '.tmp/concat/scripts',
              src: '*.js',
              dest: '.tmp/concat/scripts'
            }
          ]
        }
      },

      // Replace Google CDN references
      cdnify: {
        dist: {
          html: ['<%= yeoman.dist %>/*.html']
        }
      },

      // Copies remaining files to places other tasks can use
      copy: {
        dist: {
          files: [
            {
              expand: true,
              dot: true,
              cwd: '<%= yeoman.app %>',
              dest: '<%= yeoman.dist %>',
              src: [
                '*.{ico,png,txt}',
//                '.htaccess',
                '*.html',
                'views/**/*.html',
                'images/**/*',
                'fonts/*',
                'scripts/assets/**/*',
                'scripts/config.js',
                'styles/ios.css',
                'OpaInteractiveDocumentation/**/*',
                'apiLocation.js'
              ]
            },
            {
              expand: true,
              cwd: '.tmp/images',
              dest: '<%= yeoman.dist %>/images',
              src: ['generated/*']
            },
            {
              expand: true,
              cwd: '<%= yeoman.app %>/lib/kendo-ui-core/styles',
              dest: '<%= yeoman.dist %>/styles',
              src: [
                'textures/*',
                'BlueOpal/*'
              ]
            },
            {
              expand: true,
              dot: true,
              cwd: '<%= yeoman.app %>/lib/components-font-awesome',
              dest: '<%= yeoman.dist %>',
              src: [
                'fonts/*'
              ]
            },
            {
              expand: true,
              dot: true,
              cwd: '<%= yeoman.app %>/lib/sass-bootstrap',
              dest: '<%= yeoman.dist %>',
              src: [
                'fonts/*'
              ]
            }
          ]
        },
        styles: {
          expand: true,
          cwd: '<%= yeoman.app %>/styles',
          dest: '.tmp/styles/',
          src: '{,*/}*.css'
        }
      },

      // Run some tasks in parallel to speed up the build process
      concurrent: {
        server: [
          'sass'
        ],
        test: [
          'sass'
        ],
        dist: [
          'sass'/*,
          /*'imagemin',
          'svgmin'*/
        ]
      },

      // By default, your `index.html`'s <!-- Usemin block --> will take care of
      // minification. These next options are pre-configured if you do not wish
      // to use the Usemin blocks.
      // cssmin: {
      //   dist: {
      //     files: {
      //       '<%= yeoman.dist %>/styles/main.css': [
      //         '.tmp/styles/{,*/}*.css',
      //         '<%= yeoman.app %>/styles/{,*/}*.css'
      //       ]
      //     }
      //   }
      // },
      // uglify: {
      //   dist: {
      //     files: {
      //       '<%= yeoman.dist %>/scripts/scripts.js': [
      //         '<%= yeoman.dist %>/scripts/scripts.js'
      //       ]
      //     }
      //   }
      // },
      // concat: {
      //   dist: {}
      // },

      // Test settings
      karma: {
        unit: {
          configFile: 'karma.conf.js',
          singleRun: true
        }
      }
    }
  )
  ;

  grunt.registerTask('install', 'install the backend and frontend dependencies', function () {
    var exec = require('child_process').exec;
    var cb = this.async();
    exec('bower install', {cwd: ''}, function (err, stdout, stderr) {
      console.log(stdout);
      cb();
    });
  });

  grunt.registerTask('serve', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      /*'bower-install',*/
      'svninfo',
      'replace:run',
      'install',
      'concurrent:server',
      'autoprefixer',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('server', function () {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve']);
  });

  grunt.registerTask('test', [
    'clean:server',
    'concurrent:test',
    'autoprefixer',
    'connect:test',
    'karma'
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'svninfo',
    /*'bower-install',*/
    'install',
    'useminPrepare',
    'concurrent:dist',
    'autoprefixer',
    'concat',
    'ngmin',
    'copy:dist',
    'cdnify',
    'cssmin',
    'uglify',
    'rev',
    'usemin',
    'replace:revision'
    /*'compress:dist'*/
    /*,'htmlmin'*/
  ]);

  grunt.registerTask('default', [
    'newer:jshint',
    'test',
    'build'
  ]);
}
;
