(ns yardstick.core
  (:require [clojure.tools.cli :as cli]
            [yardstick.parse :as p]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :as pp]
            [clojure.data.json :as json])
  (:import (clojure.lang ExceptionInfo PersistentVector)
           (java.io File)))

(defn- get-step [action args]
  (apply format action (map json/write-str args)))

(defmulti do-step (fn [action & _] action))

(defmethod do-step :default [action & args]
  (throw (IllegalArgumentException.
           (str "Step Not Implemented: "
                (get-step action args)))))

(defprotocol Printer
  (print-to [this event]))

(def ^:private default-printer
  (reify Printer
    (print-to [_ event]
      (pp/pprint event))))

(defprotocol Hooks
  (before-spec [this spec])
  (after-spec [this spec])
  (before-scenario [this spec scenario])
  (after-scenario [this spec scenario])
  (before-step [this spec scenario step])
  (after-step [this spec scenario step])
  )

(def ^:private default-hooks
  (reify Hooks
    (before-spec [_ _] nil)
    (after-spec [_ _] nil)
    (before-scenario [_ _ _] nil)
    (after-scenario [_ _ _] nil)
    (before-step [_ _ _ _] nil)
    (after-step [_ _ _ _] nil)
    ))

(defn- get-tests [file-list ^String test-file-or-folder]
  (let [file-obj (File. test-file-or-folder)]
    (if (.isDirectory file-obj)
      (reduce get-tests file-list (map #(.getAbsolutePath %) (.listFiles file-obj)))
      (conj file-list test-file-or-folder))))

(defn- resolve-tags [include exclude]
  (fn [tags]
    (and
      (empty? (set/intersection exclude tags))
      (or
        (empty? include)
        (empty? tags)
        (not (empty? (set/intersection include tags)))))))

(defn run
  ([^PersistentVector paths & {:keys [^Hooks hooks ^Printer printer include-tags exclude-tags]
                               :or {hooks default-hooks
                                    printer default-printer
                                    include-tags #{}
                                    exclude-tags #{}}}]
   (let [tag-resolve (resolve-tags include-tags exclude-tags)
         files (filter #(.endsWith ^String % ".spec") (reduce get-tests [] paths))
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
       (print-to printer bad-file))
     (print-to printer {:event :suite-start})
     (doseq [{:keys [spec for-each scenarios]} (filter tag-resolve specs)]
       (try
         (print-to printer {:event :spec-start :spec spec})
         (before-spec hooks spec)
         (doseq [{:keys [scenario steps]} (filter tag-resolve scenarios)]
           (try
             (print-to printer {:event :scenario-start :spec spec :scenario scenario})
             (before-scenario hooks spec scenario)
             (doseq [step (concat for-each steps)]
               (let [event {:event :step :spec spec :scenario scenario :step (get-step (first step) (rest step))}]
                 (try
                   (before-step hooks spec scenario step)
                   (try
                     (apply do-step step)
                     (print-to printer event)
                     (catch Throwable t
                         (print-to printer (assoc event :error t))))
                   (catch Throwable t
                     (print-to printer {:error t :event :before-step :spec spec :scenario scenario :step step}))
                   (finally
                     (try
                       (after-step hooks spec scenario step)
                       (catch Throwable t
                         (print-to printer {:error t :event :after-step :spec spec :scenario scenario :step step})))))))
             (catch Throwable t
               (print-to printer {:error t :event :before-scenario :spec spec :scenario scenario}))
             (finally
               (try
                 (after-scenario hooks spec scenario)
                 (catch Throwable t
                   (let [event {:error t :event :after-scenario :spec spec :scenario scenario}]
                     (print-to printer event)))
                 (finally
                   (print-to printer {:event :scenario-end :spec spec :scenario scenario}))))))
         (catch Throwable t
           (let [event {:error t :event :before-spec :spec spec}]
             (print-to printer event)))
         (finally
           (try
             (after-spec hooks spec)
             (catch Throwable t
               (let [event {:error t :event :after-spec :spec spec}]
                 (print-to printer event)))
             (finally
               (print-to printer {:event :spec-end :spec spec}))))))
     nil)))
