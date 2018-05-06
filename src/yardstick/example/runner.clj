(ns yardstick.example.runner
  (:require [yardstick.core :as y]
            [clojure.pprint :as pp]
            [clojure.xml :as xml]
            [clojure.string :as str]
            [yardstick.example.hooks :as hooks]
            [yardstick.example.printer :as printer]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s])
  (:import (clojure.lang ExceptionInfo)))

(defn- build-exception [^Throwable err]
  {:tag (.getSimpleName (type err))
   :attrs {:message (.getMessage err)}
   :content [{:tag :stack-trace
              :content (mapv (fn [^StackTraceElement ste]
                               {:tag :stack-trace-element
                                :attrs {:class-name (.getClassName ste)
                                        :file-name (.getFileName ste)
                                        :line-number (.getLineNumber ste)
                                        :method-name (.getMethodName ste)}})
                             (.getStackTrace err))}]})

(defmulti xmlify-exception type)

(defmethod xmlify-exception ExceptionInfo [^ExceptionInfo err]
  (let [{{value ::s/value spec ::s/spec problems ::s/problems :as data} :error} (.getData err)
        _ (pp/pprint data)
        node {:tag :explain-data
              :attrs {:spec (str spec)
                      :value (pr-str value)}
              :content (map (fn [{:keys [path pred val via in]}]
                              {:tag :problem
                               :attrs {:path (str/join "," path)
                                       :pred (str pred)
                                       :val (pr-str val)
                                       :via (str/join "," via)
                                       :in (str/join "," in)}})
                            problems)}]
    (update-in (build-exception err) [:content] conj node)))

(defmethod xmlify-exception :default [^Throwable err]
  (build-exception err))

(defn -main [& _]
  (try
    (let [results (y/-run ["resources/examples"]
                          :hooks hooks/example-hooks
                          :printer printer/example-printer)
          output {:tag     :results
                  :content (mapv (fn [{:keys [event error] :as result}]
                                   (let [error (if (nil? error) [] [(xmlify-exception error)])]
                                     {:tag     event
                                      :attrs   (reduce-kv #(assoc %1 %2 (str/escape %3 {\" "&quot;" \' "&apos;"})) {} (dissoc result :event :error))
                                      :content error}))
                                 results)}]
      (spit "resources/examples/results.xml"
            (with-out-str
              (xml/emit output))))
    (catch ExceptionInfo e
      (println (.getMessage e))
      (pp/pprint (.getData e)))
    (catch Throwable t
      (println t)
      (.printStackTrace t)
      )))
