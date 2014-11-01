-= clj-json-ld =- [![Build Status](https://travis-ci.org/SnootyMonkey/clj-json-ld.png?branch=master)](https://travis-ci.org/SnootyMonkey/clj-json-ld)
=================

The Clojure library for [JSON-LD](http://json-ld.org/) (JavaScript Object Notation for Linking Data).

"Data is messy and disconnected. JSON-LD organizes and connects it, creating a better Web."

## Introduction

clj-json-ld implements the [JSON LD 1.0 Processing Algorithms and API](http://www.w3.org/TR/json-ld-api/). Whoop-de-doo! Why should you care?


### Benefits of Linked Data

[Linked Data](http://www.w3.org/DesignIssues/LinkedData.html) is a way to create a network of standards-based machine interpretable data across different documents and Web sites. It allows an application to start at one piece of Linked Data, and follow embedded links to other pieces of Linked Data that are hosted on different sites across the Web.


### Benefits of JSON-LD

JSON-LD is a lightweight syntax to serialize Linked Data in [JSON](http://www.ietf.org/rfc/rfc4627.txt). Its design allows existing JSON to be interpreted as Linked Data with minimal changes. JSON-LD is primarily intended to be a way to use Linked Data in Web-based programming environments, to build interoperable Web services, and to store Linked Data in JSON-based storage engines. Since JSON-LD is 100% compatible with JSON, the large number of JSON parsers and libraries available today can be reused. 

JSON-LD provides:

* a universal identifer mechanism for JSON objects,
* a way to disambiguate keys shared among different JSON documents,
* a mechanism in which a value in a JSON object may refer to a JSON object on a different site on the Web,
* the ability to annotate strings in JSON objects with their language,
* a way to associate datatypes with values such as dates and times,
* and a facility to express one or more directed graphs, such as a social network, in a single document.

The JSON-LD syntax is designed to not disturb already deployed systems running on JSON, but provide a smooth upgrade path from JSON to JSON-LD. Since the shape of such data varies wildly, JSON-LD features mechanisms to reshape documents into a deterministic structure which simplifies their processing.

JSON-LD is designed to be usable directly as JSON, with no knowledge of [RDF](http://www.w3.org/TR/2014/PR-rdf11-concepts-20140109/). It is also designed to be usable as RDF, if desired, for use with other Linked Data technologies like SPARQL.

Developers who require any of the facilities listed above or need to serialize an RDF Graph or RDF Dataset in a JSON-based syntax will find JSON-LD of interest.


## Installation

TBD.

## Usage

:bomb: This library is currently a WIP stub and is not yet usable. Stay tuned!

At the REPL:

```clojure
(require '[clj-json-ld.core :as json-ld])
```

In your namespace:

```clojure
(ns your-app.core
  (:require [clj-json-ld.core :as :json-ld]))  
```

:warning: The `flatten` function specified in [JSON-LD 1.0 Processing Algorithms and API](http://www.w3.org/TR/json-ld-api/) conflicts with Clojure core's [flatten](https://clojuredocs.org/clojure.core/flatten) function, so unless you use `:as` in your require you'll see a warning about the replacement:

```
WARNING: flatten already refers to: #'clojure.core/flatten in namespace: user, being replaced by: #'clj-json-ld.core/flatten
```

More detailed usage instructions are in the API documentation.

## Testing

The [JSON-LD Test Suite](http://json-ld.org/test-suite/) is used to verify [JSON-LD Processor conformance](http://json-ld.org/test-suite/reports/). clj-json-ld uses the JSON-LD Test Suite, as well as some additional tests.

## Development and Contributing

If you'd like to enhance clj-json-ld, please fork it [on GitHub](https://github.com/SnootyMonkey/clj-json-ld). If you'd like to contribute back your enhancements (awesome!), please submit your pull requests to the `dev` branch. I promise to look at every pull request and incorporate it, or at least provide feedback on why if I won't.

* Do your best to conform to the coding style that's here... I like it.
* Use 2 soft spaces for indentation.
* Don't leave trailing spaces after lines.
* Don't leave trailing new lines at the end of files.
* Write comments.
* Write tests.
* Don't submit über pull requests, keep your changes atomic.
* Have fun!

### Branches

There are 2 long lived branches in the repository:

[master](https://github.com/SnootyMonkey/clj-json-ld/tree/master) - mainline, picked up by [continual integration on Travis-CI](https://travis-ci.org/SnootyMonkey/clj-json-ld), named releases are tagged with: vX.X.X

[dev](https://github.com/SnootyMonkey/clj-json-ld/tree/dev) - development mainline, picked up by [continual integration on Travis-CI](https://travis-ci.org/SnootyMonkey/clj-json-ld)

Additional short lived feature branches will come and go.

## License

clj-json-ld is distributed under the [Mozilla Public License v2.0](http://www.mozilla.org/MPL/2.0/).

Copyright © 2014 [Snooty Monkey, LLC](http://snootymonkey.com/)