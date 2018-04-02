(ns leiningen.cucumber
  (:require [clojure.java.io :refer :all]
            [leiningen.core.eval :refer [eval-in-project]]
            [leiningen.core.project :as project])
  (:import [cucumber.runtime RuntimeOptions]))

(defn- configure-feature-paths [runtime-options feature-paths]
  (when (.. runtime-options (getFeaturePaths) (isEmpty))
    (.. runtime-options (getFeaturePaths) (addAll feature-paths))))

(defn- configure-glue-paths [runtime-options glue-paths feature-paths]
  (when (.. runtime-options (getGlue) (isEmpty))
    (if (empty? glue-paths)
      (.. runtime-options (getGlue) (addAll (into [] (map #(str (file % "step_definitions/")) feature-paths))))
      (.. runtime-options (getGlue) (addAll glue-paths)))))

(defn create-formatter-name [formatter]
  (if (map? formatter)
    (str (name (:type formatter)) ":" (:path formatter))
    (name formatter)))

(defn add-formatter [args {:keys [formatter]}]
  (if
    (and
     formatter
     (not
      (some #{"-p" "--plugin"} (map #(.. %1 (trim) (toLowerCase)) args))))
    (concat ["-p" (create-formatter-name formatter)] args)
    args))

(defn create-partial-runtime-options [{:keys [cucumber-feature-paths target-path cucumber-glue-paths cucumber] :or {cucumber-feature-paths ["features"]} :as opts} args]
  (let [runtime-options (RuntimeOptions. (vec (add-formatter args cucumber)))]
    (configure-feature-paths runtime-options (or (:feature-paths cucumber) cucumber-feature-paths))
    (configure-glue-paths runtime-options (or (:glue-paths cucumber) cucumber-glue-paths) (.getFeaturePaths runtime-options))
    runtime-options))

(defn cucumber
  "Runs Cucumber features in test/features with glue in test/features/step_definitions"
  [project & args]
  (binding [leiningen.core.main/*exit-process?* true]
    (let [runtime (gensym "runtime")
          runtime-options (create-partial-runtime-options project args)
          glue-paths (vec (.getGlue runtime-options))
          feature-paths (vec (.getFeaturePaths runtime-options))
          target-path (:target-path project)
          cucumber-opts (:cucumber project)
          project (project/merge-profiles project [:test])]
      (eval-in-project
       (-> project
           (update-in [:dependencies] conj
                      ['com.siili/lein-cucumber "1.0.7"]
                      ['info.cukes/cucumber-clojure "1.2.4"])
           (update-in [:source-paths] (partial apply conj) glue-paths))
       `(do
          (let [~runtime (leiningen.cucumber.util/run-cucumber! ~feature-paths ~glue-paths ~target-path ~(vec (add-formatter args cucumber-opts)))]
            (leiningen.core.main/exit (.exitStatus ~runtime))))
       '(require 'leiningen.cucumber.util 'leiningen.core.main)))))
