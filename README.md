# inferenceql.gpm.sppl
![tests](https://github.com/InferenceQL/inferenceql.gpm.spn/workflows/tests/badge.svg)
![linter](https://github.com/InferenceQL/inferenceql.gpm.spn/workflows/linter/badge.svg)

An implementation of the [generative probabilistic model](https://github.com/probcomp/inferenceql.inference) interface that delegates to a SPPL sum-product expression.

## Compatibility

This library is not compatible with Python virtual environments; for isolation the use of Docker is recommended. 

## Prerequisites

Nix must be installed with flake support. 

Run `nix develop` to get a shell with all the necessary dependencies installed.

If your editor / REPL has issues getting the necessary env vars for the correct versions/locations of stuff from the 
shell (likely with macOS GUI programs), start a remote REPL from the nix develop shell and connect to that REPL 
from your editor.

If you're using nREPL and deps.edn, you can add the following alias:

``` clojure
;; deps.edn
{
;; ...
:aliases {:nREPL
          {:extra-deps
            {nrepl/nrepl {:mvn/version "1.1.1"}}}}
}
```

Then start an nREPL server by running:

```shell
clj -A:test -M:nREPL -m nrepl.cmdline
```

(See https://nrepl.org/nrepl/usage/server.html for more ways to set up, like with Leiningen, or CIDER middleware for Emacs.)

Finally, connect to the running remote REPL with your editor's remote nREPL method.

### Old, non-nix prerequisite setup method

Python and [SPPL](https://github.com/probsys/sppl) must be installed. To install the version of SPPL and its dependencies that this library has been tested with, run:

``` shell
pip install -r requirements.txt
pip install --no-deps "$(cat requirements-sppl.txt)"
```

## Usage

First, initialize [libpython-clj](https://github.com/clj-python/libpython-clj):

``` clojure
(require '[libpython-clj2.python :as python])
(python/initialize!)
```

Make note of the Python executable being used in the output of `python/initialize!`. If you would like to use a different Python executable you can specify it explicitly:

``` clojure
(python/initialize! :python-executable "/path/to/python")
```

For more information, including documentation about how to initialize libpython-clj on a system where multiple versions of Python are installed, refer to [libpython-clj's documentation](https://clj-python.github.io/libpython-clj/).

Once libpython-clj has been initialized SPPL models that have been serialized to JSON can be read with `inferenceql.gpm.sppl/read-string`:

``` clojure
(require '[inferenceql.gpm.sppl :as sppl])

(def model (sppl/read-string (slurp "path/to/model.json")))
```
