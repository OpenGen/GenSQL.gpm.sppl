(ns inferenceql.gpm.spn-test
  (:refer-clojure :exclude [slurp])
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [inferenceql.gpm.spn :as spn]))

(deftest slurp
  (is (instance? inferenceql.gpm.spn.SPN (spn/slurp (io/resource "model.json")))))
