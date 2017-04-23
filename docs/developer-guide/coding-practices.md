---
layout: docs
title: Coding Practices
description: BenchFlow Coding Practices
group: developer-guide
---

## Style guidelines

For each language we follow a *style guideline* and use an *automatic checker* configured as part of the build process and also integrated into the IDEs. We also use an *automatic formatter* that will help us to conform to the style guidelines and is also integrated into the build process and into the IDEs.

An important question is in which phase of the build process should these tools run. Is the code for matters for matters for matter for matter formatting tools should run before compilation (provided they don't break your code). The the decision to when run the checkers is a little bit more complicated. In terms of the maven development lifecycle phases, these static
analysis tools are typically bound to the phase `validate` (which
happens before `compile`) or the phase `verify` (which happens after
`package`). We believe neither of these choices are good. Using the `validate`
phase is bad because the 1st static check should be performed by
the compiler and if we use the `verify` phase we have to wait for the
entire packaging phase to happen and that takes quite some time. So we
decided to bind all the static analysis tools to the `test` phase.

Wherever possible we use maven configuration inheritance to share the configuration of
these and other plug-ins among multiple projects. So the plug-ins of general use are set up in the maven configuration file located in the root of the repository. Each project then refers to it adding to its configuration file

```
<parent>
  <groupId>cloud.benchflow</groupId>
  <artifactId>benchflow</artifactId>
  <version>0.1.0</version>
</parent>
```

This is enough to get the plug-ins working. You can also override configuration parameters if necessary.

### Scala
We are using mostly the [Scala Style Guide](http://docs.scala-lang.org/style/). For us it's most important aspects and the ones that are mandatory are those automatically checked by the checker software described below. So in practice our Scala style guidelines are reflected in the scalastyle configuration file that we crafted.

The current file contain parts from the automatically generated standard scalastyle
configuration file and parts from the [configuration file used by codacy](https://github.com/codacy/codacy-scalastyle/blob/master/src/main/resources/docs/scalastyle_config.xml)
We then made on top of it the following changes:

- Currently there is no standard header in the files (which typically
  would contain information regarding license) so that check is turned off.
- The check on naming standards for fields apparently doesn't
  distinguish real fields (on classes or traits) from constants (on
  objects) so this check is removed.
- The complaints about imports using wildcards are also turned off because we
  think there are valid uses for it. For example when importing classes
  whose purpose is to do implicit conversions or for example when
  importing type members of a class that you are already importing (for
  example in code dealing with actors). I think this is used all over
  the scala standard library anyway.
- Are turned off complaints regarding grouped imports because that
  conflicts in some cases with having wildcard imports. Basically you
  cannot turn that on and the last option off.
- Also turned off are complaints about imports that are not in the beginning of
  the file. We think it is important to allow that because it can bring
  benefits in terms of performance and in terms of clarity or better
  understanding of the code for example when you are importing a mutable
  collection or when you have more than one top level class (or trait or
  object) in the same file (or even if they are inside the scope of
  another class) which are effectively different worlds which access
  different sets of imports. This kind of code can be seen when dealing
  with actors for example.

#### Checker

Verification of the style guidelines is done using [scalastyle](http://www.scalastyle.org/). Refer to the website for integration with various tools. The build process should also be integrated with it.

- Maven:
  The scalastyle maven plug-in was added to the main maven configuration file so all you need to do is to set it as your parent project as described above.
  Run it with

  ```
  mvn scalastyle:check
  ```

  or as part of

  ```
  mvn test`
  ```

- Intellij:
  - Enable scalastyle in Intellij by selecting Settings->Editor->Inspections, then searching for Scala style inspections.
  - copy the `scalastyle-config.xml` from the root of the repo to `.idea/scalastyle_config.xml` (`_` is important)
  - then you should see violations when editing the code

#### Formatter

To help format your code according to the guidelines you probably want to use a tool. For example:

- [scalariform](https://github.com/scala-ide/scalariform).
  
  Run it with the `+doubleIndentClassDeclaration` flag to conform to the guideline
  
  - Vim:
      - install the command line version of scalariform (on Mac: `brew install scalariform`)
      - install the [vim-autoformat plug-in](https://github.com/Chiel92/vim-autoformat)
      - add that to your .vimrc:

      ```
      let g:formatdef_scalariform = '"scalariform +doubleIndentClassDeclaration --stdin"'
      let g:formatters_scala = ['scalariform']
      ```

      - and optionally a mapping (for example `noremap ,f :Autoformat<CR>`)
  - IntelliJ:
      - install the [plugin](https://plugins.jetbrains.com/plugin/7480-scalariform)
      - set the settings as above (Preferences -> Scalariform)
      - When editing a Scala file, you will find "Format with Scalariform" option in the Code menu.
  - Maven:
  
      It is also integrated in the main maven configuration file. Run it with

      ```
      mvn scalariform:format
      ```
    
      or as part of
    
      ```
      mvn process-sources
      ```
    
      which runs before the compiler

### Java
We are following the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

#### Checker

To check it we are using [checkstyle](http://checkstyle.sourceforge.net/).

- Maven
 
  The checkstyle maven plug-in was added to the main maven configuration file so all you need to do is to set it as your parent project as described above.
  Run it with

  ```
  mvn checkstyle:check
  ```

  or as part of

  ```
  mvn test`
  ```

- Eclipse ...
- IntelliJ ...

#### Formatter

- Maven
  
  The maven plug-in responsible for formatting (<https://github.com/coveo/fmt-maven-plugin>) basically calls the software Google users to format their Java code (<https://github.com/google/google-java-format>)
which they say doesn't accept configuration on purpose to make sure nobody deviates from the standard.

  If we decide we want to have a standard that is based on the Google standard but with something different we have to use a formatter that accepts configuration. For example:
[formatter-maven-plugin](http://code.revelc.net/formatter-maven-plugin/formatter-maven-plugin/index.html)

  Run it with

  ```
  mvn com.coveo:fmt-maven-plugin:format
  ```

  or it will run automatically before the compiler

- Eclipse
  
  You can configure the built-in formatter with this configuration file:
  <https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml>

- IntelliJ
 
  You can configure the built-in formatter with this configuration file:
  <https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml>
