(ns yardstick.parse
  (:require [markdown.core :as md]
            [clojure.xml :as xml]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [endophile.core :as e]
            [endophile.hiccup :refer [to-hiccup]]
            [clojure.spec.alpha :as s]
            [yardstick.spec-model :as spec-model]))

(defn- parse-spec [line]
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
    all-args))

(defn- spec-tags [tags]
  (let [tags (if (empty? tags) [] (str/split tags #","))]
    (set (mapv str/trim tags))))

(defn- spec-step [step]
  (parse-spec (str/join (:step step))))

(defn- spec-scenario [{{header :header} :scenario-header {tags :tags} :tags {steps :step} :steps :or {tags ""}}]
  {:scenario header
   :tags (spec-tags tags)
   :steps (mapv spec-step steps)})

(defn parse-test-file [test-file-contents]
  (let [escaped (str/replace test-file-contents #"\ntags: " "\n####")
        parsed (e/mp escaped)
        hiccupped (to-hiccup parsed)
        filtered (filter #(contains? #{:h1 :h2 :h4 :ul} (first %)) hiccupped)
        conformed (s/conform ::spec-model/spec filtered)
        {{spec-header :header} :spec-header {tags :tags} :tags {for-each :step} :for-each :keys [scenarios] :or {tags "" for-each []}} conformed
        spec {:spec spec-header
              :tags (spec-tags tags)
              :for-each (mapv spec-step for-each)
              :scenarios (mapv spec-scenario scenarios)}]
    spec))