(ns inferenceql.gpm.sppl-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [inferenceql.gpm.sppl :as sppl]
            [inferenceql.inference.gpm :as gpm]))

(defn spe?
  [x]
  (instance? inferenceql.gpm.sppl.SPE x))

(def spe (sppl/json->SPE (slurp (io/resource "model.json"))))

(deftest json->spe
  (is (spe? spe)))

(deftest condition
  (is (spe? (gpm/condition spe {})))
  (is (spe? (gpm/condition spe {:gender "female"}))))

(deftest logprob
  (is (= 1.0 (+ (Math/exp (gpm/logprob spe '(>= height 170)))
                (Math/exp (gpm/logprob spe '(< height 170)))))))

(deftest round-trip
  (let [rt-spe (edn/read-string {:readers sppl/readers}
                                (pr-str spe))]
    (is (spe? rt-spe))
    (is (= spe rt-spe))))
