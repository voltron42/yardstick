(ns example.hooks
  (:require [yardstick.core1 :as y]
            [clojure.test :refer :all])
  (:import (org.junit Assert))
  (:gen-class))

(defn def-hooks []
  (y/def-hook :after-spec
              (fn [spec]
                (Assert/assertTrue "This is also an example exception" false)
                ))

  (y/def-hook :after-scenario
              (fn [spec scenario]
                (throw (Exception. "This is another example exception"))
                ))
  )
