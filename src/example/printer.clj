(ns example.printer
  (:require [clojure.pprint :as pp])
  (:import (clojure.lang ExceptionInfo))
  (:gen-class))

(defmulti print-event :event)

(defmethod print-event :default [_]
  ;TODO
  )

(defmulti print-error type)

(defmethod print-error :default [^Throwable t]
  (println (str "\t" (.getMessage t))))

(defmethod print-error ExceptionInfo [^ExceptionInfo t]
  (println (str "\t" (.getMessage t)))
  (pp/pprint (.getData t)))

(defmethod print-event :bad-file [{:keys [file ^ExceptionInfo error]}]
  (println "The following file could not be parsed due to the following reason:")
  (println (str "\t" file))
  (print-error error))

(defmethod print-event :step-before-each-scenario [{:keys [spec scenario step ^Throwable error]}]
  (if (nil? error)
    (println (str "> " step " - SUCCESS!"))
    (do
      (println (str "> " step " - fails"))
      (println (str "\t" error))
      (pp/pprint (take 3 (.getStackTrace error)))
      (println "...")
      (println "")
      )))

(defmethod print-event :step [{:keys [spec scenario step error]}]
  (if (nil? error)
    (println (str "> " step " - SUCCESS!"))
    (do
      (println (str "> " step " - fails"))
      (println (str "\t" error))
      (pp/pprint (take 3 (.getStackTrace error)))
      (println "...")
      (println "")
      )))

(defmethod print-event :step-start [{:keys [spec scenario step]}]
  (println (str "# " step)))

(defmethod print-event :before-step [{:keys [spec scenario step error]}]
  ;TODO
  )

(defmethod print-event :after-step [{:keys [spec scenario step error]}]
  ;TODO
  )

(defmethod print-event :step-end [{:keys [spec scenario step]}]
  (println "")
  )

(defmethod print-event :scenario-start [{:keys [spec scenario]}]
  (println (str "## " scenario)))

(defmethod print-event :before-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod print-event :after-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod print-event :scenario-end [{:keys [spec scenario]}]
  (println "")
  )

(defmethod print-event :spec-start [{:keys [spec]}]
  (println (str "# " spec)))

(defmethod print-event :before-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod print-event :after-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod print-event :spec-end [{:keys [spec]}]
  (println "")
  )

(defmethod print-event :suite-start [{}]
  (println "Beginning tests:"))

(defmethod print-event :before-suite [{:keys [error]}]
  ;TODO
  )

(defmethod print-event :after-suite [{:keys [error]}]
  ;TODO
  )

(defmethod print-event :suite-end [{}]
  (println "All tests completed."))
