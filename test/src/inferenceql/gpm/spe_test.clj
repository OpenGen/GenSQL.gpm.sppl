(ns inferenceql.gpm.spe-test
  (:refer-clojure :exclude [slurp])
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [inferenceql.gpm.spe :as spe]
            [inferenceql.inference.gpm :as gpm]))

(defn spe?
  [x]
  (instance? inferenceql.gpm.spe.SPE x))

(def spe (spe/slurp (io/resource "model.json")))

(deftest slurp
  (is (spe? spe)))

(deftest condition
  (is (spe? (gpm/condition spe {})))
  (is (spe? (gpm/condition spe {:gender "female"}))))

(deftest logprob
  (is (= 1.0 (+ (Math/exp (gpm/logprob spe '(>= height 170)))
                (Math/exp (gpm/logprob spe '(< height 170)))))))
