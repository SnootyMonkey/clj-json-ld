(defproject clj-json-ld "0.1.0-SNAPSHOT"
  :description "JSON-LD for Clojure"
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
    [org.clojure/core.match "0.3.0-alpha4"] ; Erlang-esque pattern matching https://github.com/clojure/core.match
    [defun "0.2.0-RC"] ; Erlang-esque pattern matching for Clojure functions https://github.com/killme2008/defun
    [cheshire "5.4.0"] ; JSON de/encoding https://github.com/dakrone/cheshire
    [clojurewerkz/urly "2.0.0-alpha5"] ; URI and URL parsing library https://github.com/michaelklishin/urly
  ]

  :profiles {
    :dev {

      :dependencies [
        [midje "1.6.3"] ; Example-based testing https://github.com/marick/Midje
      ]
      
      :plugins [
        [codox "0.8.10"] ; Generate Clojure API docs https://github.com/weavejester/codox
        [lein-midje "3.1.3"] ; Example-based testing https://github.com/marick/lein-midje
        [lein-bikeshed "0.2.0"] ; Check for code smells https://github.com/dakrone/lein-bikeshed
        [lein-kibit "0.0.8"] ; Static code search for non-idiomatic code https://github.com/jonase/kibit
        [jonase/eastwood "0.2.1"] ; Clojure linter https://github.com/jonase/eastwood
        [lein-checkall "0.1.1"] ; Runs bikeshed, kibit and eastwood https://github.com/itang/lein-checkall
        [lein-ancient "0.5.5"] ; Check for outdated dependencies https://github.com/xsc/lein-ancient
        [lein-spell "0.1.0"] ; Catch spelling mistakes in docs and docstrings https://github.com/cldwalker/lein-spell
      ]

      :codox {
        :sources ["src/"]
        :include [clj-json-ld.core clj-json-ld.json-ld]
        :output-dir "doc/API"
        :src-dir-uri "http://github.com/SnootyMonkey/clj-json-ld/blob/master/"
        :src-linenum-anchor-prefix "L" ; for Github
        :defaults {:doc/format :markdown}
      }
    }
  }

  :aliases {
    "build" ["do" "clean," "deps," "compile"] ; clean and build code
    "midje" ["with-profile" "dev" "midje"] ; run all tests
    "spell!" ["spell" "-n"] ; check spelling in docs and docstrings
    "bikeshed!" ["bikeshed" "-v" "-m" "120"] ; code check with max line length warning of 120 characters
    "ancient" ["with-profile" "dev" "do" "ancient" ":allow-qualified," "ancient" ":plugins" ":allow-qualified"] ; check for out of date dependencies
  }

  ;; ----- Code check configuration -----

  :eastwood {
    ;; Enable some linters that are disabled by default
    :add-linters [:unused-namespaces :unused-private-vars]
    ;; More extensive lintering that will have a few false positives
    ;; :add-linters [:unused-namespaces :unused-private-vars :unused-locals :unused-fn-args]
    :exclude-namespaces [
      clj-json-ld.unit.context
      clj-json-ld.unit.iri
      clj-json-ld.unit.value
      clj-json-ld.spec.compaction
      clj-json-ld.spec.expansion
      clj-json-ld.spec.flattening
    ]
  }
)