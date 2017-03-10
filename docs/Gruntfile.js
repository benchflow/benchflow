/*!
 * BenchFlow Docs' Gruntfile
 */

module.exports = function (grunt) {
  'use strict'

  // Force use of Unix newlines
  grunt.util.linefeed = '\n'

  RegExp.quote = function (string) {
    return string.replace(/[-\\^$*+?.()|[\]{}]/g, '\\$&')
  }

  var path = require('path')

  var configBridge = grunt.file.readJSON('./grunt/configBridge.json', { encoding: 'utf8' })

  // Project configuration.
  grunt.initConfig({

    // Task configuration.
    clean: {
      dist: 'dist',
    },

    exec: {
      htmlhint: {
        command: 'npm run htmlhint'
      },
      htmllint: {
        command: 'npm run htmllint'
      },
      jekyll: {
        command: 'npm run jekyll'
      },
      'jekyll-github': {
        command: 'npm run jekyll-github'
      }
    }//,

    // buildcontrol: {
    //   options: {
    //     dir: '_gh_pages',
    //     commit: true,
    //     push: true,
    //     message: 'Built %sourceName% from commit %sourceCommit% on branch %sourceBranch%'
    //   },
    //   pages: {
    //     options: {
    //       remote: 'git@github.com:twbs/derpstrap.git',
    //       branch: 'gh-pages'
    //     }
    //   }
    // },

    // compress: {
    //   main: {
    //     options: {
    //       archive: 'bootstrap-<%= pkg.version %>-dist.zip',
    //       mode: 'zip',
    //       level: 9,
    //       pretty: true
    //     },
    //     files: [
    //       {
    //         expand: true,
    //         cwd: 'dist/',
    //         src: ['**'],
    //         dest: 'bootstrap-<%= pkg.version %>-dist'
    //       }
    //     ]
    //   }
    // }

  })


  // These plugins provide necessary tasks.
  require('load-grunt-tasks')(grunt)
  require('time-grunt')(grunt)

  // Docs HTML validation task
  grunt.registerTask('validate-html', ['exec:jekyll', 'exec:htmllint', 'exec:htmlhint'])

  var runSubset = function (subset) {
    return !process.env.TWBS_TEST || process.env.TWBS_TEST === subset
  }
  var isUndefOrNonZero = function (val) {
    return val === undefined || val !== '0'
  }

  // Default task.
  // grunt.registerTask('default', ['clean:dist'])

  // Docs task.
  // grunt.registerTask('docs-css', ['exec:clean-css-docs', 'exec:postcss-docs'])
  // grunt.registerTask('lint-docs-css', ['exec:scss-lint-docs'])
  // grunt.registerTask('docs-js', ['exec:uglify-docs'])
  // grunt.registerTask('docs', ['lint-docs-css', 'docs-css', 'docs-js', 'clean:docs', 'copy:docs'])
  // grunt.registerTask('docs-github', ['exec:jekyll-github'])

  // grunt.registerTask('prep-release', ['dist', 'docs', 'docs-github', 'compress'])

  // Publish to GitHub
  // grunt.registerTask('publish', ['buildcontrol:pages'])
}