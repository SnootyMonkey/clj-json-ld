(ns clj-json-ld.unit.context
  "
  Test the context processing as defined here: http://www.w3.org/TR/json-ld-api/#context-processing-algorithm
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.context :refer (update-with-local-context)]))

(def fcms-iri "http://falkland-cms.com/")
(def falklandsophile-iri "http://falklandsophile.com/")
(def snootymonkey-iri "http://snootymonkey.com")

(def active-context {"@base" fcms-iri "@foo" :bar})

(facts "about updating active context with local contexts"

  (facts "about invalid local contexts"

    (facts "as a scalar"
      (update-with-local-context active-context 1) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil 1]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context 1.1) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil 1.1]) => (throws clojure.lang.ExceptionInfo))

    (facts "as a keyword"
      (update-with-local-context active-context :foo) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil :foo]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context :foo) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil :foo]) => (throws clojure.lang.ExceptionInfo))

    (facts "as a sequential"
      (update-with-local-context active-context [[]]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil []]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [()]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context active-context [{} "" nil ()]) => (throws clojure.lang.ExceptionInfo)))

  (facts "about nil local contexts"

    (fact "result in a newly-initialized active context"
      (update-with-local-context active-context nil) => {}
      (update-with-local-context active-context [nil]) => {}
      (update-with-local-context active-context [{:blat :bloo} nil]) => {}))

  (future-facts "about local contexts as remote strings")
        
  (facts "about @base in local contexts"

    (fact "@base of nil removes the @base"
      (update-with-local-context active-context {"@base" nil}) => {"@foo" :bar}
      (update-with-local-context active-context {"@base2" nil}) => 
        active-context)

    (fact "@base of an absolute IRI makes the IRI @base"
      
      (update-with-local-context 
        active-context 
        {"@base" falklandsophile-iri}) =>
        {"@base" falklandsophile-iri "@foo" :bar}
      
      (update-with-local-context
        active-context
        {"@base2" falklandsophile-iri}) => 
        active-context

      (update-with-local-context
        active-context
        [{"@base" falklandsophile-iri} {}]) => 
        {"@base" falklandsophile-iri "@foo" :bar}

      (update-with-local-context
        active-context
        [{"@base" falklandsophile-iri} {"@base" snootymonkey-iri} {}]) => 
        {"@base" snootymonkey-iri "@foo" :bar}

      (update-with-local-context
        active-context
        [{"@base" falklandsophile-iri} {"@base" snootymonkey-iri} {} {"@base" nil} {"@base" fcms-iri}]) => 
        active-context))

  (future-facts "about @vocab in local contexts")

  (future-facts "about @language in local contexts"))