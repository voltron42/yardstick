(ns yardstick.example.runner
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]
            [clojure.xml :as xml])
  (:import (clojure.lang ExceptionInfo)))

(defmulti print-event :event)

(def ^:private example-printer
  (reify y/Printer
    (print [_ event]
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

(def ^:private example-handler
  (reify y/Handler
    (before-suite [_]
      ;TODO
      )
    (after-suite [_]
      ;TODO
      )
    (before-spec [_]
      ;TODO
      )
    (after-spec [_]
      ;TODO
      )
    (before-scenario [_]
      ;TODO
      )
    (after-scenario [_]
      ;TODO
      )))

(defmethod y/handle-action "On the customer page" [_]
  ;TODO
  )

(defmethod y/handle-action "Search for customer %1s" [_ customer-name]
  ;TODO
  )

(defmethod y/handle-action "The customer %1s is listed" [_ customer-name]
  ;TODO
  )

(defmethod y/handle-action "Find and Open product page for %1s" [_ product]
  ;TODO
  )

(defmethod y/handle-action "Verify product %1s as %2s" [_ property value]
  ;TODO
  )

(defmethod y/handle-action "Delete product %1s" [_ product]
  ;TODO
  )

(defn- xmlify-exception [err]
  {:tag (str (type err))
   :attrs {:message (.getMessage err)}
   :content (map (fn [^StackTraceElement ste]
                   {:tag :stack-trace-element
                    :attrs {:class-name (.getClassName ste)
                            :file-name (.getFileName ste)
                            :line-number (.getLineNumber ste)
                            :method-name (.getMethodName ste)}})
                 (.getStackTrace err))})

(defn -main [& args]
  (try
    (let [{:keys [bad-files results]}
          (y/-run "resources/examples"
                  :handler example-handler
                  :printer example-printer)
          output {:tag :results
                  :content (map #(fn [{:keys [event error] :as result}]
                                   (let [error (if (empty? error) [] [(xmlify-exception error)])]
                                     {:tag event
                                      :attrs (dissoc result :event :error)
                                      :content error}))
                                results)}]
      (spit "resources/examples/results.xml"
            (with-out-str (xml/emit output))))
    (catch ExceptionInfo e
      (println (.getMessage e))
      (pp/pprint (.getData e)))))
