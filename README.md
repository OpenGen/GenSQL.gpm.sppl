# inferenceql.gpm.spn

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

For more information, including documentation about how to initialize libpython on a system where multiple versions of Python are installed, refer to [libpython-clj's documentation](https://clj-python.github.io/libpython-clj/).

Next, define the model and the dataset to be queried, parsing columns as needed:

``` clojure
(require '[inferenceql.gpm.spn :as spn]
         '[inferenceql.query.main :as main]
         '[medley.core :as medley])

(def model (spn/slurp "path/to/model.json"))

(def data
  (into []
        (map (fn [row]
               (-> row
                   (medley/update-existing :column1 parse-column1)
                   (medley/update-existing :column2 parse-column2)
                   ...)))
        (main/slurp-csv "path/to/csv")))
```

Finally, launch the REPL:

``` clojure
(main/repl data {:model model})
```

When the `iql>` prompt appears enter your query:

``` sql
select * from generate age under model constrained by age > 48.0000001 and age < 48.0000002 limit 10
``` 
