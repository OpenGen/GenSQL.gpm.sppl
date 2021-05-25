(ns inferenceql.gpm.spn-test
  (:refer-clojure :exclude [slurp])
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [inferenceql.gpm.spn :as spn]
            [inferenceql.inference.gpm :as gpm]))

(defn spn?
  [x]
  (instance? inferenceql.gpm.spn.SPN x))

(def spn (spn/slurp (io/resource "model.json")))

(deftest slurp
  (is (spn? spn)))

(deftest condition
  (is (spn? (gpm/condition spn {})))
  (is (spn? (gpm/condition spn {:gender "female"}))))

(deftest logprob
  (is (= 1.0 (+ (Math/exp (gpm/logprob spn '(>= height 170)))
                (Math/exp (gpm/logprob spn '(< height 170)))))))
