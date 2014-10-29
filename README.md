-= clj-json-ld =- [![Build Status](https://travis-ci.org/SnootyMonkey/clj-json-ld.png?branch=master)](https://travis-ci.org/SnootyMonkey/clj-json-ld)
=================

The Clojure library for [JSON-LD](http://json-ld.org/) (JavaScript Object Notation for Linking Data).

"Data is messy and disconnected. JSON-LD organizes and connects it, creating a better Web."

## Benefits

TBD.

### For Producers of JSON Data

TBD.

### For Consumers of JSON-LD Data

TBD.

## Installation

TBD.

## Usage

Note: This library is currently a WIP stub and is not yet usable.

At the REPL:

```clojure
(require '[clj-json-ld.core :as json-ld])
```

In your namespace:

```clojure
(ns your-app.core
  (:require [clj-json-ld.core :as :json-ld]))  
```

Note: The `flatten` function specified in [JSON-LD 1.0 Processing Algorithms and API](http://www.w3.org/TR/json-ld-api/) conflicts with Clojure core's [flatten](https://clojuredocs.org/clojure.core/flatten) function, so unless you use `:as` you'll see a warning about the replacement:

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