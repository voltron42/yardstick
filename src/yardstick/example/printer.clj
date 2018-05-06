(ns yardstick.example.printer
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]
            [clojure.string :as str])
  (:import (clojure.lang ExceptionInfo)))

(defmulti ^:private print-event :event)

(defmethod print-event :default [_]
  ;TODO
  )

(defn example-printer [results-atom]
  (reify y/Printer
    (print-to [this event]
      (swap! results-atom conj event)
      (print-event event))))

(defmethod ^:private print-event :bad-file [{:keys [file ^ExceptionInfo error]}]
  (println "The following file could not be parsed due to the following reason:")
  (println (str "\t" file))
  (println (str "\t" (.getMessage error)))
  (pp/pprint (.getData error)))

(defmethod ^:private print-event :step-before-each-scenario [{:keys [spec scenario step ^Throwable error]}]
  (if (nil? error)
    (println (str "> " step " - SUCCESS!"))
    (do
      (println (str "> " step " - fails"))
      (println (str "\t" error))
      (pp/pprint (take 3 (.getStackTrace error)))
      (println "...")
      (println "")
      )))

(defmethod ^:private print-event :step [{:keys [spec scenario step error]}]
  (if (nil? error)
    (println (str "> " step " - SUCCESS!"))
    (do
      (println (str "> " step " - fails"))
      (println (str "\t" error))
      (pp/pprint (take 3 (.getStackTrace error)))
      (println "...")
      (println "")
      )))

(defmethod ^:private print-event :scenario-start [{:keys [spec scenario]}]
  (println (str "## " scenario)))

(defmethod ^:private print-event :before-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod ^:private print-event :after-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod ^:private print-event :scenario-end [{:keys [spec scenario]}]
  (println "")
  )

(defmethod ^:private print-event :spec-start [{:keys [spec]}]
  (println (str "# " spec)))

(defmethod ^:private print-event :before-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod ^:private print-event :after-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod ^:private print-event :spec-end [{:keys [spec]}]
  (println "")
  )

(defmethod ^:private print-event :suite-start [{}]
  (println "Beginning tests:"))

(defmethod ^:private print-event :before-suite [{:keys [error]}]
  ;TODO
  )

(defmethod ^:private print-event :after-suite [{:keys [error]}]
  ;TODO
  )

(defmethod ^:private print-event :suite-end [{}]
  (println "All tests completed."))
