(ns yardstick.tools
  (:require [clojure.tools.cli :as cli]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.xml :as xml])
  (:import (clojure.lang ExceptionInfo)))

(defmulti write-json-by-type type)

(defmethod write-json-by-type :default [value] value)

(defn write-json-custom [_ value]
  (write-json-by-type value))

(def ^:private cli-options
  [["-i" "--include INCLUDE" "Include tags"
    :default #{}
    :parse-fn #(set (str/split % #","))]
   ["-x" "--exclude EXCLUDE" "Exclude tags"
    :default #{}
    :parse-fn #(set (str/split % #","))]])

(defn parse-args [args]
  (let [{:keys [arguments summary errors] {:keys [include exclude] :as options} :options} (cli/parse-opts args cli-options)]
    (when-not (empty? errors)
      (throw (ExceptionInfo. "Parsing Errors:" {:errors errors})))
    (when (empty? arguments)
      (throw (ExceptionInfo. "Missing Test Path(s):" {:summary summary
                                                      :usage "[name] [options] file-paths"})))
    (assoc options :paths arguments :tags {:include include :exclude exclude})))

(defn- build-exception [^Throwable err]
  {:tag :error
   :attrs {:type (str (type err))
           :message (.getMessage err)}
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

(defn print-results-as-xml [results filename]
  (let [output {:tag     :results
                :content (mapv (fn [{:keys [event error] :as result}]
                                 (let [error (if (nil? error) [] [(xmlify-exception error)])]
                                   {:tag     event
                                    :attrs   (reduce-kv #(assoc %1 %2 (str/escape %3 {\" "&quot;" \' "&apos;"})) {} (dissoc result :event :error))
                                    :content error}))
                               results)}]
    (spit filename
          (with-out-str
            (xml/emit output)))))