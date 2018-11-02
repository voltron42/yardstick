(ns yardstick.files
  (:require [yardstick.parse :as p])
  (:import (java.io File)))

(defn- get-tests [file-list ^String test-file-or-folder]
  (let [file-obj (File. test-file-or-folder)]
    (if (.isDirectory file-obj)
      (reduce get-tests file-list (map #(.getAbsolutePath %) (.listFiles file-obj)))
      (conj file-list test-file-or-folder))))

(defn parse-spec-files-in-path [path print-to-fn]
  (let [files (filter #(.endsWith ^String % ".spec") (get-tests [] path))
        {:keys [specs bad-files]} (reduce
                                    (fn [out file]
                                      (try
                                        (update-in out [:specs] conj (p/parse-test-file (slurp file)))
                                        (catch Throwable t
                                          (update-in out [:bad-files] conj {:event :bad-file :file file :error t}))))
                                    {:specs []
                                     :bad-files []}
                                    files)]
    (doseq [bad-file bad-files]
      (print-to-fn bad-file))
    specs))