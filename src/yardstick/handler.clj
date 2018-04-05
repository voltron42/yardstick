(ns yardstick.handler
  (:require [clojure.string :as str])
  (:import (sun.reflect.generics.reflectiveObjects NotImplementedException)
           (clojure.lang ExceptionInfo)))

(defn parse-spec [line]
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

(defmulti handle-action (fn [action & _] action))

(defmethod handle-action :default [action & args]
  (throw (ExceptionInfo. "action not implemented" {:action action :args args})))

