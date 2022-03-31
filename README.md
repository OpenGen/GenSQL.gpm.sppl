# inferenceql.gpm.spn
![tests](https://github.com/OpenIQL/inferenceql.gpm.spn/workflows/tests/badge.svg)
![linter](https://github.com/OpenIQL/inferenceql.gpm.spn/workflows/linter/badge.svg)

An implementation of the [generative probabilistic model](https://github.com/probcomp/inferenceql.inference) interface that delegates to a sum-product network.

## Compatibility

This library is not compatible with Python virtual environments; for isolation the use of Docker is recommended. 

## Prerequisites

Python and SPPL must be installed. To install the version of SPPL and its dependencies that this library has been tested with, run:

``` shell
pip install -r requirements.txt
pip install --no-deps "$(cat requirements-sppl.txt)"
```

## Usage

This library is intended for use with [inferenceql.query](https://github.com/probcomp/inferenceql.query). First, launch a REPL with inferenceql.query on the classpath:

``` shell
clj -Sdeps '{:deps {probcomp/inferenceql.query {:git/url "git@github.com:probcomp/inferenceql.query.git" :sha "…"}}}'
```

Next, initialize [libpython-clj](https://github.com/clj-python/libpython-clj):

``` clojure
(require '[libpython-clj2.python :as python])
(python/initialize!)
```

Make note of the Python executable being used in the output of `python/initialize!`. If you would like to use a different Python executable you can specify it explicitly:

``` clojure
(python/initialize! :python-executable "/path/to/python")
```

For more information, including documentation about how to initialize libpython on a system where multiple versions of Python are installed, refer to [libpython-clj's documentation](https://clj-python.github.io/libpython-clj/).

Next, define the model and the dataset to be queried, parsing columns as needed:

``` clojure
(require '[clojure.edn :as edn]
         '[inferenceql.gpm.spn :as spn]
         '[inferenceql.query.data :as data]
         '[inferenceql.query.main :as main]
         '[medley.core :as medley])

(def model (spn/slurp "path/to/model.json"))
(def schema (edn/read-string (slurp "path/to/schema.edn")))

(def data
  (into []
        (map (data/row-coercer schema))
        (main/slurp-csv "path/to/data.csv")))
```

Finally, launch the REPL:

``` clojure
(main/repl data {:model model})
```

When the `iql>` prompt appears enter your query:

``` sql
select * from generate age under model constrained by age > 48.0000001 and age < 48.0000002 limit 10
``` 
