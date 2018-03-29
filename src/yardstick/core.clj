(ns yardstick.core
  (require [clojure.tools.cli :as cli]
           [gutenberg.markdown :as md])
  (:import (clojure.lang ExceptionInfo)
           (java.io File)))

(defprotocol Handler
  (before-suite [this])
  (after-suite [this])
  (before-spec [this])
  (after-spec [this])
  (before-scenario [this])
  (after-scenario [this]))

(def ^:private default-handler
  (reify Handler
    (before-suite [_] nil)
    (after-suite [_] nil)
    (before-spec [_] nil)
    (after-spec [_] nil)
    (before-scenario [_] nil)
    (after-scenario [_] nil)))

(def ^:private cli-options [])

(defn- parse-args [args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-options)]
    (when-not (empty? errors)
      (doseq [error errors]
        (println error))
      (System/exit 1))
    (println summary)
    (assoc options :paths arguments)))

(defn- get-tests [file-list ^String test-file-or-folder]
  (let [file-obj (File. test-file-or-folder)]
    (if (.isDirectory file-obj)
      (reduce get-tests file-list (.list file-obj))
      (conj file-list test-file-or-folder))))

(defn -run
  ([] (-run []))
  ([cli-args] (-run cli-args default-handler))
  ([cli-args ^Handler handler]
   (let [{:keys [paths]} (parse-args cli-args)]
     (try
       (before-suite handler)
       (doseq [test-file (reduce get-tests [] paths)
               markdown (md/parse (slurp test-file))
               ]
         (try
           (before-spec handler)
           (catch Throwable t
             )
           (finally
             (after-spec handler)
             ))
         )
       (catch Throwable t
         )
       (finally
         (after-suite handler))))))