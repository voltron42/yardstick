(ns yardstick.example.runner
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]
            [clojure.xml :as xml]
            [clojure.string :as str]
            [yardstick.example.hooks :as hooks]
            [yardstick.example.printer :as printer]
            [clojure.data.json :as json]
            [yardstick.example.steps :refer :all]
            [clojure.spec.alpha :as s]
            [yardstick.tools :as t])
  (:import (clojure.lang ExceptionInfo)))

(defn -main [& _]
  (try
    (let [results-atom (atom [])
          log-data (with-out-str
                     (y/run ["resources/examples"]
                            :hooks hooks/example-hooks
                            :printer (printer/example-printer results-atom)))
          results @results-atom
          filename "resources/examples/results.xml"]
      (t/print-results-as-xml results filename)
      (spit "resources/examples/runner.log" log-data)
      )
    (catch ExceptionInfo e
      (println (.getMessage e))
      (pp/pprint (.getData e)))
    (catch Throwable t
      (println t)
      (.printStackTrace t))))
