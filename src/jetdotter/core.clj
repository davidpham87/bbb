(ns jetdotter.core
  (:require
   [babashka.fs :as fs]
   [cheshire.core :as j]
   [clj-yaml.core :as yaml]
   [cli-matic.core  :as cli-matic]
   [clojure.edn]
   [clojure.java.io :refer (input-stream)]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [cognitect.transit :as t]
   [expound.alpha :as expound]
   [jetdotter.db :as db])
  (:import java.io.ByteArrayOutputStream)
  (:gen-class))

#_(alias 's 'clojure.spec.alpha)
(def data-format #{:json :yml :edn :yaml :transit})
(s/def ::data-format data-format)

(defn extension [filename]
  (keyword (last (str/split filename #"\."))))

(defn convert-extension [filename source target output-folder]
  (let [parent (fs/parent filename)
        filename-parts (str/split filename #"\.")
        new-ext (if  (= target :transit) :transit.json target)
        n-drop
        (if (and (= source :transit)
                 (= (last (butlast filename-parts)) "transit"))
          2 1)
        new-filename (fs/file-name (str
                                    (str/join "." (drop-last n-drop filename-parts ))
                                    "." (name new-ext)))]
    (str (or output-folder parent) (fs/file-separator) new-filename)))

(defn parse
  ([s type]
   (case type
     :transit (t/read (t/reader (input-stream (.getBytes s "UTF-8")) :json))
     :yml (yaml/parse-string s)
     :yaml (parse s :yml)
     :json (j/decode s keyword)
     :edn (clojure.edn/read-string s)
     identity)))

(defn generate
  ([data type] (generate data type true))
  ([data type pretty?]
   (case type
     :transit (let [baos (ByteArrayOutputStream. 4096)
                    writer (t/writer baos (if pretty? :json :json-verbose))
                    _ (t/write writer data)]
                (String. (.toByteArray baos)))
     :yml (yaml/generate-string data)
     :yaml (generate data :yml)
     :json (j/encode data {:pretty true})
     :edn (if pretty? (with-out-str (clojure.pprint/pprint data)) (str data))
     identity)))

(defn convert [s from to]
  (-> s (parse from) (generate to)))

(defn main [opts]
  (println "Jetdotter called with args: ")
  (println opts)
  (doseq [filename (get opts :_arguments)]
    (let [source-format (or (get-in opts [:from]) (extension filename))
          target-format (get-in opts [:to])
          output-filename (convert-extension
                           filename source-format target-format
                           (get-in opts [:output-folder]))]
      (println (format "Converting %s to %s" filename output-filename))
      (-> (slurp filename)
          (parse source-format)
          (generate target-format (get-in opts [:pretty]))
          (as-> $ (spit output-filename $))))))

(def cli-options
  {:command     "jetdotter"
   :description "Transform between JSON, EDN, YAML, and Transit"
   :version     "0.0.1"
   :opts        [{:as "Source Format. Only necessary for transit format."
                  :option "from"
                  :short "f"
                  :type :keyword
                  :default :edn}
                 {:as (format "Target Format. One of %s."
                              (str/join ", " (map name data-format)))
                  :option "to"
                  :short "t"
                  :type :keyword
                  :spec ::data-format
                  :default :yaml}
                 {:as "Pretty string output"
                  :option "pretty"
                  :short "p"
                  :type :with-flag
                  :default false}
                 {:as "Output Folder"
                  :option "output-folder"
                  :type :string}
                 {:as "DB path: Path for Datalevin database"
                  :option "db-path"
                  :type :string
                  :default "~/.local/jetdotter/jetdotter.db"}]
   :runs main})

(defn -main [& args] (cli-matic/run-cmd args cli-options))

(comment
  (parse (generate {:a 3} :transit) :transit)
  (parse (generate {:a 3} :json) :json)
  (parse (generate {:a 3} :yaml) :yaml)
  (parse (generate {:a 3} :edn) :edn)
  (def s "[\"^ \",\"~:a\",3,\"~:b\",[1,2,3,4],\"c\",\"~$a\",\"~:d/k\",[\"~#set\",[1,3,2]]]")
  (t/read (t/reader (input-stream (.getBytes s "UTF-8")) :json)))
