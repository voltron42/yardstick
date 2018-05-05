(ns yardstick.handler
  (:require [clojure.string :as str])
  (:import (sun.reflect.generics.reflectiveObjects NotImplementedException)
           (clojure.lang ExceptionInfo)))

(defmulti handle-action (fn [action & _] action))

(defmethod handle-action :default [action & args]
  (throw (ExceptionInfo. "action not implemented" {:action action :args args})))

