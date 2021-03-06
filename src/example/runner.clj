(ns example.runner
  (:require [yardstick.core1 :as y]
            [clojure.test :refer :all]
            [clojure.pprint :as pp]
            [example.steps :refer :all]
            [example.hooks :refer :all]
            [example.printer :as p]
            [yardstick.tools :as t]
            [clojure.data.json :as json])
  (:import (clojure.lang ExceptionInfo)))

(deftest test-example-runner
  (let [results-atom (atom [])]
    (try
      (y/def-consumer
        (fn [event]
          (swap! results-atom conj event)
          (p/print-event event)))
      (def-hooks)
      (let [_ (y/run ["resources"])
            results @results-atom
            filename "resources/results.xml"]
        (t/print-results-as-xml results filename)
        (spit "resources/results.json"
              (json/write-str results :value-fn t/jsonify-exception)))
      (catch ExceptionInfo e
        (println (.getMessage e))
        (pp/pprint (.getData e)))
      (catch Throwable t
        (println t)
        (.printStackTrace t)))))
