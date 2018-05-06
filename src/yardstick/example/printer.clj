(ns yardstick.example.printer
  (:require [yardstick.core :as y]))

(defmulti print-event :event)

(def example-printer
  (reify y/Printer
    (print-to [this event]
      (print-event event))))

(defmethod print-event :bad-file [{:keys [file error]}]
  ;TODO
  )

(defmethod print-event :step-before-each-scenario [{:keys [spec scenario step error]}]
  ;TODO
  )

(defmethod print-event :step [{:keys [spec scenario step error]}]
  ;TODO
  )

(defmethod print-event :before-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod print-event :after-scenario [{:keys [spec scenario error]}]
  ;TODO
  )

(defmethod print-event :before-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod print-event :after-spec [{:keys [spec error]}]
  ;TODO
  )

(defmethod print-event :before-suite [{:keys [error]}]
  ;TODO
  )

(defmethod print-event :after-suite [{:keys [error]}]
  ;TODO
  )
