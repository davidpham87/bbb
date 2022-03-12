(ns jetdotter.db
  (:require
   [babashka.fs :as fs]
   [datalevin.core :as d]))

;; Define an optional schema.
;; Note that pre-defined schema is optional, as Datalevin does schema-on-write.
;; However, attributes requiring special handling need to be defined in schema,
;; e.g. many cardinality, uniqueness constraint, reference type, and so on.
(def schema {:last-modified-time {:db/valueType :db.type/string}
             :filename           {:db/valueType :db.type/string}})

(def base-path "~/.jetdotter/")
(def db-path "~/.jetdotter/datalevin.db")

(defn make-db-folder! []
  (fs/create-dirs base-path)
  (d/create-conn db-path))

(defn conn []
  (let [db (if (fs/exists? db-path)
             db-path
             (do (make-db-folder!) db-path))]
    (d/get-conn db schema)))

(defn filename->entity-map [filename]
  {:last-modified-time (fs/last-modified-time filename)
   :filename (fs/canonicalize (fs/file filename))})

(defn save-files [filenames]
  (d/transact! conn (mapv filename->entity-map filenames)))

(defn last-modified-time
  ([conn filename]
   (->> (d/q '[:find ?t
               :in $ ?f
               ::where
               [?e :filename ?f]
               [?e :last-modified-time ?t]]
             conn
             filename)
        ffirst)))

(defn modified? [conn filename]
  (not= (fs/last-modified-time filename) (last-modified-time conn filename)))
