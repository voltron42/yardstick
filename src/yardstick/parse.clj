(ns yardstick.parse
  (:require [markdown.core :as md]
            [clojure.xml :as xml]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [endophile.core :as e]
            [endophile.hiccup :refer [to-hiccup]]
            [clojure.spec.alpha :as s]
            [yardstick.spec-model :as spec-model]
            [yardstick.handler :as h])
  (:import (java.io ByteArrayInputStream)))

(defn- spec-tags [tags]
  (let [tags (if (empty? tags) [] (str/split tags #","))]
    (set (mapv str/trim []))))

(defn- spec-step [step]
  (h/parse-spec (str/join (:step step))))

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