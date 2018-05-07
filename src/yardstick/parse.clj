(ns yardstick.parse
  (:require [clojure.xml :as xml]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [endophile.core :as e]
            [endophile.hiccup :refer [to-hiccup]]
            [clojure.spec.alpha :as s]
            [markdown.core :as md]
            [yardstick.spec-model :as spec-model])
  (:import (clojure.lang ExceptionInfo)
           (java.io ByteArrayInputStream)))

(defn- parse-spec
  ([line table]
    (let [[step & args] (parse-spec line)
          {:keys [head body]} table
          headers (->> head :row :headers (map :value) (map keyword))
          records (mapv #(into {} (mapv vector
                                        headers
                                        (map :value (:cells %))))
                        (:rows body))
          step (str step " %" (inc (count args)) "s")]
      (conj (vec (cons step args)) records)))
  ([line]
   (let [arg-count (atom 0)
         args (atom [])
         all-args (into [(str/replace
                           line
                           #"([\"'])(?:\\\1|.)*?\1"
                           (fn [match]
                             (swap! arg-count inc)
                             (swap! args conj (str/escape (first match) {\" "" \\ "\""}))
                             (str "%" @arg-count "s")))]
                        @args)]
     all-args)))

(defn- spec-tags [tags]
  (let [tags (if (empty? tags) [] (str/split tags #","))]
    (set (mapv str/trim tags))))

(defn- spec-step [{:keys [step table]}]
  (if (nil? table)
    (parse-spec (str/join step))
    (parse-spec (str/join step) table)))

(defmulti spec-steps first)

(defmethod spec-steps :simple [[_ steps]])

(defmethod spec-steps :with-tables [[_ {}]])

(defn- spec-scenario [{{header :header} :scenario-header {tags :tags} :tags {steps :step} :steps :or {tags ""}}]
  {:scenario header
   :tags (spec-tags tags)
   :steps (mapv spec-step steps)})

(defn- reduce-xml [xml-node]
  (let [{:keys [tag attrs content]} xml-node
        attrs (if (empty? attrs) [] [attrs])
        content (if (empty? content) [] (mapv #(if (string? %) % (reduce-xml %)) content))]
    (into [tag] (concat attrs content))))

(defn parse-test-file [test-file-contents]
  (let [escaped (reduce-kv str/replace test-file-contents {"\ntags: " "\n####" ">" "&gt;" "<" "&lt;"})
        html (md/md-to-html-string escaped)
        reduced (reduce-kv str/replace html {"&mdash;" "&#8212;" "&ndash;" "&#8211;"})
        parsed (xml/parse (ByteArrayInputStream. (.getBytes (str "<body>" reduced "</body>"))))
        hiccupped (rest (reduce-xml parsed))
        filtered (filter #(contains? #{:h1 :h2 :h4 :ul} (first %)) hiccupped)
        conformed (s/conform ::spec-model/spec filtered)]
    (when (= ::s/invalid conformed)
      (throw (ExceptionInfo. "File does not conform to test structure:" {:error (s/explain-data ::spec-model/spec filtered)})))
    (let [{{spec-header :header} :spec-header {tags :tags} :tags {for-each :step} :for-each :keys [scenarios] :or {tags "" for-each []}} conformed
          spec {:spec spec-header
                :tags (spec-tags tags)
                :for-each (mapv spec-step for-each)
                :scenarios (mapv spec-scenario scenarios)}]
      spec)))
