(ns yardstick.core
  (:require [yardstick.tags :as t]
            [yardstick.files :as f]
            [clojure.pprint :as pp])
  (:import (clojure.lang Keyword IFn))
  (:gen-class))

(def ^:private default-hooks {:before-spec {}
                              :after-spec {}
                              :before-scenario {}
                              :after-scenario {}
                              :before-suite {}
                              :after-suite {}})

(def ^:private valid-hooks (set (keys default-hooks)))

(defprotocol Yardstick
  (-def-hook [this ^Keyword hook ^String tags ^IFn func])
  (-def-step [this ^String step-str ^IFn step-fn])
  (-run-tests [this]))

(defn -before-suite [^Yardstick y ^IFn func]
  (-def-hook y :before-suite "" func))

(defn -after-suite [^Yardstick y ^IFn func]
  (-def-hook y :before-suite "" func))

(defn -)

(defn- all-suite-hooks [hooks suite-state])

(defn- before-suite [hooks suite-state])

(defn- after-suite [hooks suite-state])

(defn -build-yardstick [config]
  (let [hooks (atom default-hooks)
        steps (atom {})
        suite-state (atom {})
        run-tag-fn (t/make-tag-validator (t/parse-tag-set (:tags config)))
        all-specs (f/parse-spec-files-in-path (:specsDir config) pp/pprint)
        run-specs (filter #(run-tag-fn (:tags %)) all-specs)]
    (reify Yardstick
      (-def-hook [_ ^Keyword hook ^String tags func]
        (when-not (valid-hooks hook)
          (throw (IllegalArgumentException. (str hook " is invalid, must be one of " valid-hooks))))
        (let [tag-set (t/parse-tag-set tags)]
          (swap! hooks update-in [hook] assoc tag-set {:tag-fn (t/make-tag-validator tag-set)
                                                       :hook-fn func})))
      (-def-step [_ ^String step-str ^IFn step-fn]
        (when (contains? @steps step-str)
          (throw (IllegalArgumentException. (str "Step already defined: " step-str))))
        (swap! steps assoc step-str step-fn))
      (-run-tests [_]
        ; TODO
        ))))