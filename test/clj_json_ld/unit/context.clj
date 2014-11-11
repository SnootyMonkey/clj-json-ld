(ns clj-json-ld.unit.context
  "
  Test the context processing as defined here: http://www.w3.org/TR/json-ld-api/#context-processing-algorithm
  "
  (:require [midje.sweet :refer :all]
            [clj-json-ld.context :refer (update-with-local-context)]))


(def fcms-iri "http://falkland-cms.com/")
(def falklandsophile-iri "http://falklandsophile.com/")
(def snootymonkey-iri "http://snootymonkey.com")

(facts "about updating active context with local contexts"

  (facts "about invalid local contexts"

    (facts "as a scalar"
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} 1) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil 1]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} 1.1) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil 1.1]) => (throws clojure.lang.ExceptionInfo))

    (facts "as a keyword"
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} :foo) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil :foo]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} :foo) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil :foo]) => (throws clojure.lang.ExceptionInfo))

    (facts "as a sequential"
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [[]]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil []]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [()]) => (throws clojure.lang.ExceptionInfo)
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{} "" nil ()]) => (throws clojure.lang.ExceptionInfo)))

  (facts "about nil local contexts"

    (fact "result in a newly-initialized active context"
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} nil) => {}
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [nil]) => {}
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} [{:blat :bloo} nil]) => {}))

  (future-facts "about local contexts as remote strings")
        
  (facts "about @base in local contexts"

    (fact "@base of nil removes the @base"
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} {"@base" nil}) => {"@foo" :bar}
      (update-with-local-context {"@base" fcms-iri "@foo" :bar} {"@base2" nil}) => 
        {"@base" fcms-iri "@foo" :bar})

    (fact "@base of an absolute IRI makes the IRI @base"
      
      (update-with-local-context 
        {"@base" fcms-iri "@foo" :bar} 
        {"@base" falklandsophile-iri}) =>
        {"@base" falklandsophile-iri "@foo" :bar}
      
      (update-with-local-context
        {"@base" fcms-iri "@foo" :bar}
        {"@base2" falklandsophile-iri}) => 
        {"@base" fcms-iri "@foo" :bar}

      (update-with-local-context
        {"@base" fcms-iri "@foo" :bar}
        [{"@base" falklandsophile-iri} {}]) => 
        {"@base" falklandsophile-iri "@foo" :bar}

      (update-with-local-context
        {"@base" fcms-iri "@foo" :bar}
        [{"@base" falklandsophile-iri} {"@base" snootymonkey-iri}]) => 
        {"@base" snootymonkey-iri "@foo" :bar}

      (update-with-local-context
        {"@base" fcms-iri "@foo" :bar}
        [{"@base" falklandsophile-iri} {"@base" snootymonkey-iri} {} {"@base" fcms-iri}]) => 
        {"@base" fcms-iri "@foo" :bar}))

  (future-facts "about @vocab in local contexts")

  (future-facts "about @language in local contexts")

)