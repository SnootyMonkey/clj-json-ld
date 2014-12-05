--=  clj-json-ld  =--
=====================

[![MPL License](http://img.shields.io/badge/license-MPL-green.svg?style=flat)](https://www.mozilla.org/MPL/2.0/)
[![Build Status](http://img.shields.io/travis/SnootyMonkey/clj-json-ld.png?style=flat)](https://travis-ci.org/SnootyMonkey/clj-json-ld)

> "Data is messy and disconnected. JSON-LD organizes and connects it, creating a better Web."

clj-json-ld is the Clojure library for [JSON-LD](http://json-ld.org/) (JavaScript Object Notation for Linking Data).

* [Introduction](#introduction)
  * [Benefits of Linked Data](#benefits-of-linked-data)
  * [Benefits of JSON-LD](#benefits-of-json-ld)
  * [JSON-LD 101](#json-ld-101)
  * [Capabilities of clj-json-ld](#capabilities-of-clj-json-ld)
* [Installation](#installation)
* [Usage](#usage)
* [Testing](#testing)
* [Development and Contributing](#development-and-contributing)
  * [Branches](#branches)
* [Acknowledgements](#acknowledgements)
* [License](#license)

## Introduction

clj-json-ld implements the [JSON LD 1.0 Processing Algorithms and API](http://www.w3.org/TR/json-ld-api/)
as a [conforming JSON-LD Processor](http://www.w3.org/TR/json-ld-api/#conformance).


### Benefits of Linked Data

[Linked Data](http://www.w3.org/DesignIssues/LinkedData.html) is a way to create a network of standards-based machine interpretable data across different documents, APIs and Web sites. It allows an application to start at one piece of Linked Data, and follow embedded links to other pieces of Linked Data that come from different sites and APIs across the Web.


### Benefits of JSON-LD

JSON-LD is a lightweight syntax to serialize Linked Data in [JSON](http://www.ietf.org/rfc/rfc4627.txt). Its design allows existing JSON to be interpreted as Linked Data with minimal changes.

JSON-LD is a way to use Linked Data in API programming environments, to build interoperable APIs, and to store Linked Data in JSON-based database engines (most NoSQL databases). Since JSON-LD is 100% compatible with JSON, the large number of JSON parsers, libraries and databases available today can be reused. 

JSON-LD provides:

* a universal identifier mechanism for JSON objects,
* a way to disambiguate keys shared among different JSON documents,
* a mechanism in which a value in a JSON object may refer to a JSON object on a different site on the Web,
* the ability to annotate strings in JSON objects with their language,
* a way to associate data types with string values such as dates and times,
* and a facility to express one or more directed graphs, such as a social network, or a taxonomy, in a single document.

The JSON-LD syntax is designed to not disturb already deployed systems running on JSON, and provides a smooth upgrade path from JSON to JSON-LD. Since the shape of JSON data varies wildly, JSON-LD features mechanisms to reshape documents into a deterministic structure which simplifies their processing.

JSON-LD is designed to be usable directly as JSON, with no knowledge of [RDF](http://www.w3.org/TR/2014/PR-rdf11-concepts-20140109/). It is an alternative syntax for the same underlying data model as RDF, so it is also designed to be transformable to/from RDF, if desired, for use with other Linked Data technologies like [SPARQL](http://www.w3.org/TR/rdf-sparql-query/).

Developers who require any of the facilities listed above or need to serialize/deserialize an RDF Graph or RDF Dataset in a JSON-based syntax will find JSON-LD of interest.


### JSON-LD 101

Let's take a look at some very simple JSON about a book that you might get back from an API:

```json
{
  "name": "Myth of Sisyphus",
  "author": "Albert Camus",
  "location": "http://amazon.com/Myth-Sisyphus-Albert-Camus/dp/7500133340/",
  "image": "http://ecx.images-amazon.com/images/I/61hJVrZgBBL.jpg"
}
```

A different API might provide this JSON about the very same book:

```json
{
  "author": "Albert Camus",
  "title": "Myth of Sisyphus",
  "image": "myth.png",
  "location": "3rd Floor, Manning Bldg.",
  "lang": "en-US"
}
```

As a human, it's easy to deduce what this is all about. We have the title of the book, the author, and different locations and images of the book.

As a computer algorithm however, this is all quite vague. Is `name` the same as `title`? Is `location` a place or a URL? Is `image` the name of a file? A full URL? A relative URL? A base-64 encoded image? What's `lang` mean?

The same JSON documents converted to JSON-LD documents remove much of this ambiguity:

```json
{
  "http://www.schema.org/name": "Myth of Sisyphus",
  "http://www.schema.org/author": "Albert Camus",
  "http://www.schema.org/url": {"@id": "http://amazon.com/Myth-Sisyphus-Albert-Camus/dp/7500133340/"}, ← The '@id' keyword means 'This value is an identifier that is an IRI (URL)'
  "http://www.schema.org/image": {"@id": "http://ecx.images-amazon.com/images/I/61hJVrZgBBL.jpg"} ← The '@id' keyword means 'This value is an identifier that is an IRI (URL)'
}
```

and:

```json
{
  "http://www.schema.org/author": "Albert Camus",
  "http://www.schema.org/name": "Myth of Sisyphus",
  "http://www.schema.org/image": "myth.png",
  "http://www.schema.org/contentLocation": "3rd Floor, Manning Library",
  "http://www.schema.org/inLanguage": "en-US"
}
```

Our ambiguity is cleared up nicely by using JSON-LD documents. The `@id` term tells us that `image` is a URL in one case, but not in the other. Using a common schema, [schema.org's book schema](http://www.schema.org/Book) in this case, has brought together `name` and `title` as referring to the same thing, but the `location` values as referring to different things, a virtual location in one case and a physical location in the other.

But who wants their JSON documents to look like this? A computer algorithm that's dealing with JSON documents from different sources does, but you don't want to read and write your JSON documents like this all the time. With JSON-LD we can move this metadata out of the JSON document and into a context.

The contexts for the above JSON-LD documents look like this:

```json
{
  "@context": {
    "name": "http://schema.org/name",  ← This means that 'name' is shorthand for 'http://schema.org/name' 
    "author": "http://www.schema.org/author",  ← This means that 'author' is shorthand for 'http://schema.org/author' 
    "location": {
      "@id": "http://schema.org/url",  ← This means that 'location' is shorthand for 'http://schema.org/url' 
      "@type": "@id"  ← This means that a string value associated with 'location' should be interpreted as an identifier that is an IRI (a URL)
    },
    "image": {
      "@id": "http://schema.org/image",  ← This means that 'image' is shorthand for 'http://schema.org/image' 
      "@type": "@id"  ← This means that a string value associated with 'image' should be interpreted as an identifier that is an IRI (a URL)
    }
  }
}
```

and:

```json
{
  "@context": {
    "author": "http://www.schema.org/author",  ← This means that 'author' is shorthand for 'http://schema.org/author' 
    "title": "http://www.schema.org/name",  ← This means that 'title' is shorthand for 'http://schema.org/name' 
    "image": "http://schema.org/image", ← This means that 'image' is shorthand for 'http://schema.org/image', and is not an IRI (a URL)
    "location": "http://www.schema.org/contentLocation",  ← This means that 'location' is shorthand for 'http://schema.org/contentLocation', and is not an IRI (a URL)
    "lang": "http://www.schema.org/inLanguage"  ← This means that 'lang' is shorthand for 'http://schema.org/inLanguage' 
  }
}
```

If that context is then embedded in its own section of the JSON-LD document, or placed in an accessible location online, say at `http://the-site.org/contexts/book.jsonld`, then the original simpler JSON can be used, but with the semantic ambiguity removed. This is achieved with the addition of a `@context` property to make it a JSON-LD document.

The context can be included by reference:

```json
{
  "@context": "http://the-site.org/contexts/book.jsonld",
  "name": "Myth of Sisyphus",
  "author": "Albert Camus",
  "location": "http://amazon.com/Myth-Sisyphus-Albert-Camus/dp/7500133340/",
  "image": "http://ecx.images-amazon.com/images/I/61hJVrZgBBL.jpg"
}
```

Or the context can be directly embedded:

```json
{
  "@context": {
    "author": "http://www.schema.org/author", 
    "title": "http://www.schema.org/name", 
    "image": "http://schema.org/image",
    "location": "http://www.schema.org/contentLocation",
    "lang": "http://www.schema.org/inLanguage" 
  },
  "author": "Albert Camus",
  "title": "Myth of Sisyphus",
  "image": "myth.png",
  "location": "3rd Floor, Manning Bldg.",
  "lang": "en-US"
}
```

Or the context can be left out completely and provided by an HTTP Link Header:

```
HTTP/1.1 200 OK
Content-Type: application/json
Link: <http://the-site.org/contexts/book.jsonld>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"

{
  "name": "Myth of Sisyphus",
  "author": "Albert Camus",
  "location": "http://amazon.com/Myth-Sisyphus-Albert-Camus/dp/7500133340/",
  "image": "http://ecx.images-amazon.com/images/I/61hJVrZgBBL.jpg"
}
```

Or maybe the JSON provider is not interested in providing JSON-LD in any form. In that case, we can create the context for the JSON data ourselves and provide it directly to the JSON-LD processor.


### Capabilities of clj-json-ld

A JSON-LD processor, like clj-json-ld, helps you transform your JSON-LD documents in between different valid JSON-LD formats, some more explicit for semantic reasoning by algorithms, and others more readable and compact for humans and for transmission.

clj-json-ld can perform the [expansion](http://www.w3.org/TR/json-ld/#expanded-document-form), [compaction](http://www.w3.org/TR/json-ld/#compacted-document-form), and [flattening](http://www.w3.org/TR/json-ld/#flattened-document-form) operations defined in the [expansion](http://www.w3.org/TR/json-ld-api/#expansion-algorithm), [compaction](http://www.w3.org/TR/json-ld-api/#compaction-algorithm) and [flattening](http://www.w3.org/TR/json-ld-api/#flattening-algorithm) sections of the [processing specification](http://www.w3.org/TR/json-ld-api/).

:memo: Show the document expanded, compacted and flattened.


## Installation

:bomb: This library is currently a WIP stub and is not yet usable. Stay tuned!


## Usage

At the Clojure REPL:

```clojure
(require '[clj-json-ld.core :as json-ld])
```

In your Clojure namespace:

```clojure
(ns your-app.core
  (:require [clj-json-ld.core :as :json-ld]))  
```

:warning: The `flatten` function specified in [JSON-LD 1.0 Processing Algorithms and API](http://www.w3.org/TR/json-ld-api/) conflicts with Clojure core's [flatten](https://clojuredocs.org/clojure.core/flatten) function, so unless you use `:as` in your require, or exclude Clojure's `flatten` from your namespace with `(:refer-clojure :exclude [flatten])`, you'll see a warning about the replacement:

```
WARNING: flatten already refers to: #'clojure.core/flatten in namespace: user, being replaced by: #'clj-json-ld.core/flatten
```

More detailed usage instructions are in the API documentation.


## Testing

The [JSON-LD Test Suite](http://json-ld.org/test-suite/) is used to verify [JSON-LD Processor conformance](http://json-ld.org/test-suite/reports/). clj-json-ld uses the JSON-LD Test Suite, as well as some additional tests.

To run the tests you need to clone the [JSON-LD tests GitHub repository](https://github.com/json-ld/tests) as a peer directory to this repository:

```console
git clone https://github.com/json-ld/tests json-ld-tests
```

Then run the tests from this repository with:

```console
lein midje
```

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


## Acknowledgements

Thank you to the creators of the [JSON-LD 1.0 W3C Recommendation](http://www.w3.org/TR/json-ld/) for their hard work in creating the specification and the comprehensive test suite. Thank you to the authors and editors for the very clear spec writing.

Portions of the [Benefits of Linked Data](#benefits-of-linked-data) and [Benefits of JSON-LD](#benefits-of-json-ld) sections of this README document are lifted with slight modifications from the [JSON-LD 1.0 W3C Recommendation](http://www.w3.org/TR/json-ld/).

Thank you to [Gregg Kellog](https://github.com/gkellogg), author of the [json-ld Ruby processor](https://github.com/ruby-rdf/json-ld/), and [Dave Longley](https://github.com/dlongley), author of the [pyld Python processor](https://github.com/digitalbazaar/pyld), for providing prior implementations that were useful to study, particularly in the area of testing.


## License

clj-json-ld is distributed under the [Mozilla Public License v2.0](http://www.mozilla.org/MPL/2.0/).

Copyright © 2014 [Snooty Monkey, LLC](http://snootymonkey.com/)