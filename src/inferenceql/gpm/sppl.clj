(ns inferenceql.gpm.sppl
  (:refer-clojure :exclude [read-string slurp])
  (:require [inferenceql.inference.gpm :as gpm]
            [inferenceql.inference.gpm.proto :as proto]
            [libpython-clj2.python :as python]
            [libpython-clj2.require :as require]
            [medley.core :as medley]))

(require/require-python '[sppl.compilers.spe_to_dict :as spe_to_dict])
(require/require-python '[sppl.transforms :as transforms])
(require/require-python '[json :as json])

(def attrs
  '{>= "__ge__"
    > "__gt__"
    = "__lshift__"
    <= "__le__"
    < "__lt__"
    and "__and__"
    or "__or__"})

(defn ^:private tree->event
  ([node]
   (tree->event node {:operation? seq?
                      :operator first
                      :operands rest
                      :variable? symbol?}))
  ([node {:keys [operation? operator operands variable?] :as opts}]
   (cond (operation? node)
         (let [operator (operator node)
               attr (get attrs operator)
               [lhs rhs] (map #(tree->event % opts)
                              (operands node))]
           (if (= "__lshift__" attr)
             (python/call-attr lhs attr #{rhs})
             (python/call-attr lhs attr rhs)))

         (variable? node)
         (transforms/Identity (name node))

         :else
         node)))

(defn ^:private identity->str
  [identity]
  (str (python/get-attr identity "token")))

(defn ^:private dict->map
  [dict]
  (->> dict
       (python/as-jvm)
       (into {})
       (medley/map-keys identity->str)))

(defn ^:private map->dict
  [m]
  (->> m
       (medley/map-keys (comp transforms/Identity name))
       (python/->py-dict)))

(defrecord SPE [spe]
  proto/GPM
  (logpdf [this targets conditions]
    (let [{:keys [spe]} (cond-> this
                          (seq conditions) (gpm/condition conditions))
          targets (map->dict targets)]
      (python/call-attr spe "logpdf" targets)))

  (simulate [this targets conditions]
    (let [{:keys [spe]} (cond-> this
                          (seq conditions) (gpm/condition conditions))
          symbols (map (comp transforms/Identity name) targets)]
      (->> (python/call-attr spe "sample_subset" symbols 1)
           (first)
           (dict->map))))

  proto/LogProb
  (logprob [_ event]
    (let [event (tree->event event)]
      (python/call-attr spe "logprob" event)))

  proto/Condition
  (condition [this conditions]
    (if-not (seq conditions)
      this
      (-> spe
          (python/call-attr "constrain" (map->dict conditions))
          (->SPE))))

  proto/Constrain
  (constrain [_ event opts]
    (let [event (tree->event event opts)]
      (-> spe
          (python/call-attr "condition" event)
          (->SPE))))

  proto/Variables
  (variables [_]
    (->> (python/call-attr spe "get_symbols")
         (python/as-jvm)
         (map identity->str)))

  proto/MutualInfo
  (mutual-info [_ event-a event-b]
    (let [event-a (tree->event event-a)
          event-b (tree->event event-b)]
      (python/call-attr spe "mutual_information" event-a event-b))))

(def tag
  "Tag used when writing a SPE via `clojure.core/print-method`."
  'inferenceql.gpm.spe/SPE)

(defmethod print-method SPE
  [{:keys [spe]} writer]
  (let [json (-> spe
                 (spe_to_dict/spe_to_dict)
                 (json/dumps))]
    (.write writer "#")
    (.write writer (pr-str tag))
    (.write writer " ")
    (.write writer (pr-str json))))

(defn read-string
  "Reads an SPE value from input `String`."
  [json]
  (-> json
      (json/loads)
      (spe_to_dict/spe_from_dict)
      (->SPE)))

(def readers
  "Data readers for use with `clojure.edn/read` and `clojure.edn/read-string`."
  {tag read-string})

(defn slurp
  [f & opts]
  (-> (apply clojure.core/slurp f opts)
      (json/loads)
      (spe_to_dict/spe_from_dict)
      (->SPE)))
