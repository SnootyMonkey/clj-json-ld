(defproject clj-json-ld "0.1.0-SNAPSHOT"
  :description "JSON-LD for Clojure/ClojureScript"
  :url "https://github.com/SnootyMonkey/clj-json-ld"
  :license {
    :name "Mozilla Public License v2.0"
    :url "http://www.mozilla.org/MPL/2.0/"
  }
  :support {
    :name "Sean Johnson"
    :email "sean@snootymonkey.com"
  }

  :min-lein-version "2.5.0" ; highest version supported by Travis-CI as of 10/28/2014

  :dependencies [
    [org.clojure/clojure "1.6.0"] ; Lisp on the JVM http://clojure.org/documentation
    [cheshire "5.3.1"] ; JSON de/encoding https://github.com/dakrone/cheshire
  ]

  :profiles {
    :dev {

      :dependencies [
        [midje "1.6.3"] ; Example-based testing https://github.com/marick/Midje
      ]
      
      :plugins [
        [codox "0.8.10"] ; Generate Clojure API docs https://github.com/weavejester/codox
        [lein-midje "3.1.3"] ; Example-based testing https://github.com/marick/lein-midje
        [lein-bikeshed "0.1.8"] ; Check for code smells https://github.com/dakrone/lein-bikeshed
        [lein-kibit "0.0.8"] ; Static code search for non-idiomatic code https://github.com/jonase/kibit
        [jonase/eastwood "0.1.4"] ; Clojure linter https://github.com/jonase/eastwood
        [lein-checkall "0.1.1"] ; Runs bikeshed, kibit and eastwood https://github.com/itang/lein-checkall
        [lein-ancient "0.5.5"] ; Check for outdated dependencies https://github.com/xsc/lein-ancient
        [lein-spell "0.1.0"] ; Catch spelling mistakes in docs and docstrings https://github.com/cldwalker/lein-spell
      ]

      :codox {
        :sources ["src/"]
        :output-dir "doc/API"
        :src-dir-uri "http://github.com/SnootyMonkey/clj-json-ld/blob/master/"
        :src-linenum-anchor-prefix "L" ; for Github
      }
    }
  }

  :aliases {
    "build" ["do" "clean," "deps," "compile"] ; clean and build code
    "midje" ["with-profile" "dev" "midje"] ; run all tests
    "test!" ["with-profile" "dev" "do" "build," "midje"] ; build and run all tests
    "spell!" ["spell" "-n"] ; check spelling in docs and docstrings
    "ancient" ["with-profile" "dev" "do" "ancient" ":allow-qualified," "ancient" ":plugins" ":allow-qualified"] ; check for out of date dependencies
  }
)